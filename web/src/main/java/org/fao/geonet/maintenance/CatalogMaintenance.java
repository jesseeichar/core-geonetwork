package org.fao.geonet.maintenance;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.interfaces.Logger;
import jeeves.server.PostJeevesInitialization;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for running tasks that will scan the catalog looking for problems in the data or even configuration.
 * 
 * The maintenance thread will run waiting for times of little CPU usage and when it is determined that there is only a light load on the
 * system the maintenance tasks will be ran.
 * 
 * @author Jesse
 */
@Component
public class CatalogMaintenance implements ApplicationContextAware, PostJeevesInitialization {
    private static final class MaintenanceThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            ThreadGroup group = new ThreadGroup("Catalog Maintenance");
            Thread thread = new Thread(group, r, r.getClass().getSimpleName());
            thread.setDaemon(true);
            return thread;
        }
    }

    public static final Logger LOGGER = Log.createLogger(Geonet.GEONETWORK + ".maintenance");

    private final ThreadFactory _threadFactory = new MaintenanceThreadFactory();

    private ExecutorService _executor = Executors.newFixedThreadPool(2, _threadFactory);

    private ApplicationContext _springAppContext;

    private double _maximumCpuUsage = 100;

    private int _checkInterval = 500;

    private int _timeSinceLogging = 30000;

    private CpuMonitorTask _cpuMonitorTask;

    private CatalogMaintenanceTaskRunner _taskRunner;

    /**
     * Set the executor for executing tasks. It will have 2 tasks submitted. The maintenance thread and a watchdog thread.
     * 
     * Default value is an fixed sized executor pool with 2 threads and both threads named after the runnable class and in the 'Catalog
     * Maintenance' threadgroup.
     */
    public void setExecutorService(ExecutorService executor) {
        this._executor = executor;
    }

    @PostConstruct
    public void start() throws BeansException, Exception {
        ResourceManager manager = _springAppContext.getBean(ResourceManager.class);
        _cpuMonitorTask = new CpuMonitorTask(manager, _timeSinceLogging, _checkInterval, _maximumCpuUsage);
        _executor.execute(_cpuMonitorTask);

        Map<String, MaintenanceTask> tasks = _springAppContext.getBeansOfType(MaintenanceTask.class);
        ServiceContext context = ServiceContext.create((JeevesApplicationContext) _springAppContext);
        _taskRunner = new CatalogMaintenanceTaskRunner(context, _cpuMonitorTask, tasks);
        _executor.execute(_taskRunner);
    }

    @PreDestroy
    public void end() {
        _cpuMonitorTask.stop();
        _taskRunner.stop();
        _cpuMonitorTask = null;
        _taskRunner = null;
        _executor.shutdownNow();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this._springAppContext = applicationContext;
    }

    public void setMaximumCpuUsage(double maximumCpuUsage) {
        this._maximumCpuUsage = maximumCpuUsage;
    }

    public void setCheckInterval(int checkInterval) {
        this._checkInterval = checkInterval;
    }

    public void setTimeSinceLogging(int timeSinceLogging) {
        this._timeSinceLogging = timeSinceLogging;
    }
}
