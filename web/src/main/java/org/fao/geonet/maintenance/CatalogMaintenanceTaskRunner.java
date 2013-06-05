package org.fao.geonet.maintenance;

import java.util.HashMap;
import java.util.Map;

import jeeves.server.context.ServiceContext;

public class CatalogMaintenanceTaskRunner implements Runnable {

    private static final long LOG_DELAY = 30000;
    private final MaintenanceMonitor _monitor;
    private final Map<String, MaintenanceTask> _tasks;
    private ServiceContext _serviceContext;
    private boolean _stopped = false;;

    public CatalogMaintenanceTaskRunner(ServiceContext context, MaintenanceMonitor monitor, Map<String, MaintenanceTask> tasks) {
        this._monitor = monitor;
        this._tasks = tasks;
        this._serviceContext = context;
    }

    @Override
    public void run() {
        Map<Class<?>, Long> tasksMentionedInLog = new HashMap<Class<?>, Long>();
        while (!_stopped && !Thread.interrupted()) {
            for (Map.Entry<String, MaintenanceTask> task : _tasks.entrySet()) {
                try {
                    _monitor.obtainPermissionToWork();
                    Long lastUpdated = tasksMentionedInLog.get(task.getValue().getClass());
                    if (lastUpdated == null) {
                        lastUpdated = -1L;
                    }
                    if (CatalogMaintenance.LOGGER.isDebugEnabled() && System.currentTimeMillis() - lastUpdated.longValue() > LOG_DELAY) {
                        CatalogMaintenance.LOGGER.debug("Running maintenance task: " + task.getKey());
                        tasksMentionedInLog.put(task.getValue().getClass(), System.currentTimeMillis());
                    }
                    task.getValue().performTaskStep(0, _serviceContext, _monitor);
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // signal to shutdown
                    return;
                } catch (Exception e) {
                    CatalogMaintenance.LOGGER.error("Error while executing task: " + task.getKey());
                } finally {
                    try {
                        _serviceContext.getResourceManager().abort();
                    } catch (Exception e) {
                        // no matter
                        CatalogMaintenance.LOGGER.info("error rolling back the resource manager"+e.getMessage());
                    }
                }
            }
        }
    }

    public void stop() {
        _stopped  = true;
    }
}
