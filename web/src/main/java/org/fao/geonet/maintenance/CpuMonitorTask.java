package org.fao.geonet.maintenance;

import static org.fao.geonet.maintenance.CatalogMaintenance.LOGGER;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;

public class CpuMonitorTask implements Runnable, MaintenanceMonitor {
    private static final String GROUP_ID_PARAM_NAME = "groupId";
    private static final Object USER_ID_PARAM_NAME = "userId";
    private static final String FIXLINK_ID_PARAM_NAME = "fixLinkId";

    private static final int LOG_INTERVAL = 30000;
    private static final String REPORTS_TABLE = "MaintenanceReports";
    private static final String REPORTS_LOCAL_TABLE = "MaintenanceReportLocalization";
    private static final String REPORTS_PARAMS_TABLE = "MaintenanceParams";
    private BlockingQueue<MaintenanceReport> _reports = new ArrayBlockingQueue<MaintenanceReport>(20);
    private final AtomicBoolean _canRun = new AtomicBoolean(false);
    private long _timeSinceLogging;
    private int _checkInterval;

    private Map<Long, ThreadTime> _threadTimeMap = new HashMap<Long, ThreadTime>();
    private ThreadMXBean _threadBean = ManagementFactory.getThreadMXBean();
    private OperatingSystemMXBean _opBean = ManagementFactory.getOperatingSystemMXBean();
    private double _maximumCpuUsage;
    private ResourceManager _resourceManager;
    private SerialFactory _serialFactory = new SerialFactory();
    private volatile boolean _stopped = true;

    public CpuMonitorTask(ResourceManager resourceManager, int _timeSinceLogging, int _checkInterval, double _maximumCpuUsage) {
        super();
        this._resourceManager = resourceManager;
        this._timeSinceLogging = _timeSinceLogging;
        this._checkInterval = _checkInterval;
        this._maximumCpuUsage = _maximumCpuUsage;
    }

    @Override
    public void run() {
        while (!_stopped && !Thread.interrupted()) {

            Set<Long> mappedIds;
            mappedIds = new HashSet<Long>(_threadTimeMap.keySet());

            long[] allThreadIds = _threadBean.getAllThreadIds();
            removeDeadThreads(mappedIds, allThreadIds);

            mapNewThreads(allThreadIds);

            Collection<ThreadTime> values;

            values = new HashSet<ThreadTime>(_threadTimeMap.values());

            for (ThreadTime threadTime : values) {
                threadTime.setCurrent(_threadBean.getThreadCpuTime(threadTime.getId()));
            }

            double avarageUsagePerCPU = getAverageUsagePerCPU();
            if (avarageUsagePerCPU > this._maximumCpuUsage) {
                _canRun.set(false);
            } else {
                if (_reports.isEmpty()) {
                    synchronized (this) {
                        _canRun.set(true);
                        notifyAll();
                    }
                } else {
                    handleNextReport();
                }
            }

            if (LOGGER.isDebugEnabled() && System.currentTimeMillis() - _timeSinceLogging > LOG_INTERVAL) {
                LOGGER.debug("Maintenance can run: " + _canRun + "\n CPU usage: " + avarageUsagePerCPU);
                _timeSinceLogging = System.currentTimeMillis();
            }

            try {
                Thread.sleep(_checkInterval);
            } catch (InterruptedException e) {
                return;
            }

            for (ThreadTime threadTime : values) {
                threadTime.setLast(threadTime.getCurrent());
            }
        }
    }

    private void handleNextReport() {
        MaintenanceReport report = _reports.poll();
        if (report != null) {
            Dbms dbms = null;
            try {
                dbms = (Dbms) _resourceManager.openDirect(Geonet.Res.MAIN_DB);
                String id = report.getId();
                String taskClass = report.getTaskClass();
                String insertIntoReportSQL = "INSERT INTO " + REPORTS_TABLE + " (id, taskClass, category, severity) VALUES (?,?,?)";
                String insertIntoLocalSQL = "INSERT INTO " + REPORTS_LOCAL_TABLE
                        + " (reportid, landid, name, description) VALUES (?,?,?,?,?)";
                String insertIntoParamsSQL = "INSERT INTO " + REPORTS_PARAMS_TABLE + " (reportid, id, name, value) VALUES (?,?,?,?)";

                dbms.execute(insertIntoReportSQL, id, report.getCategory().ordinal(), report.getSeverity().ordinal());
                for (MaintenanceIssueDescription desc : report.getDescription().values()) {
                    dbms.execute(insertIntoLocalSQL, id, taskClass, desc._lang, desc._name, desc._description);
                }

                for (int groupId : report.getGroupIds()) {
                    int groupIdParam = _serialFactory.getSerial(dbms, REPORTS_PARAMS_TABLE);
                    dbms.execute(insertIntoParamsSQL, id, groupIdParam, GROUP_ID_PARAM_NAME, groupId);
                }
                for (int userId : report.getUserIds()) {
                    int userIdParam = _serialFactory.getSerial(dbms, REPORTS_PARAMS_TABLE);
                    dbms.execute(insertIntoParamsSQL, id, userIdParam, USER_ID_PARAM_NAME, userId);
                }
                for (URL fixLink : report.getFixLink()) {
                    int fixLinkIdParam = _serialFactory.getSerial(dbms, REPORTS_PARAMS_TABLE);
                    dbms.execute(insertIntoParamsSQL, id, fixLinkIdParam, FIXLINK_ID_PARAM_NAME, fixLink);
                }
                dbms.commit();
            } catch (Exception e) {
                if (dbms != null) {
                    dbms.abort();
                }
                LOGGER.error("There was an error while attempting to use dbms: " + dbms, e);
            } finally {
                try {
                    _resourceManager.close(Geonet.Res.MAIN_DB, dbms);
                } catch (Exception e) {
                    _canRun.set(false);
                    _reports.add(report);
                    LOGGER.error("There was an error while attempting to close the dbms: ", e);
                }
            }
        }
    }

    private double getTotalUsage() {
        Collection<ThreadTime> values;
        values = new HashSet<ThreadTime>(_threadTimeMap.values());

        double usage = 0D;
        for (ThreadTime threadTime : values) {
            usage += ((double) (threadTime.getCurrent() - threadTime.getLast())) / ((double) _checkInterval * 10000);
        }
        return usage;
    }

    private double getAverageUsagePerCPU() {
        return getTotalUsage() / _opBean.getAvailableProcessors();
    }

    private void mapNewThreads(long[] allThreadIds) {
        for (long id : allThreadIds) {
            synchronized (_threadTimeMap) {
                if (!_threadTimeMap.containsKey(id))
                    _threadTimeMap.put(id, new ThreadTime(id));
            }
        }
    }

    private void removeDeadThreads(Set<Long> mappedIds, long[] allThreadIds) {
        outer: for (long id1 : mappedIds) {
            for (long id2 : allThreadIds) {
                if (id1 == id2)
                    continue outer;
            }
            synchronized (_threadTimeMap) {
                _threadTimeMap.remove(id1);
            }
        }
    }

    @Override
    public void obtainPermissionToWork() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException("A trigger has been sent indicating we must shutdown");
        }
        synchronized (this) {
            while (!_canRun.get() && !_stopped) {
                wait();
            }
        }
    }

    @Override
    public void report(MaintenanceReport report) throws InterruptedException {
        _reports.put(report);
    }

    @Override
    public Logger logger() {
        return CatalogMaintenance.LOGGER;
    }

    private static class ThreadTime {

        private long id;
        private long last;
        private long current;

        public ThreadTime(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public long getLast() {
            return last;
        }

        public void setLast(long last) {
            this.last = last;
        }

        public long getCurrent() {
            return current;
        }

        public void setCurrent(long current) {
            this.current = current;
        }
    }

    public void stop() {
        synchronized (this) {
            _reports.clear();
            this._stopped = true;
            notifyAll();
        }
    }
}
