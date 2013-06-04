package org.fao.geonet.maintenance;

import java.util.Map;

import jeeves.server.context.ServiceContext;


public class CatalogMaintenanceTask implements Runnable {

    private final MaintenanceMonitor _monitor;
    private final Map<String, MaintenanceTask> _tasks;
    private ServiceContext _serviceContext;

    public CatalogMaintenanceTask(ServiceContext context, MaintenanceMonitor monitor, Map<String, MaintenanceTask> tasks) {
        this._monitor = monitor;
        this._tasks = tasks;
        this._serviceContext = context;
    }

    @Override
    public void run() {
            while (!Thread.interrupted()) {
                for (Map.Entry<String, MaintenanceTask> task : _tasks.entrySet()) {
                    try {
                        _monitor.obtainPermissionToWork();
                        CatalogMaintenance.LOGGER.debug("Starting maintenance task: "+task.getKey());
                        task.getValue().performTaskStep(0, _serviceContext, _monitor);
                    } catch (InterruptedException e) {
                        // signal to shutdown
                    } catch (Exception e) {
                        CatalogMaintenance.LOGGER.error("Error while executing task: "+task.getKey());
                    }
                }
            }
    }


}
