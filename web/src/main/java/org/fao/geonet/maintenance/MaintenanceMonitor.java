package org.fao.geonet.maintenance;


/**
 * The feedback mechanism for a {@link MaintenanceTask}.
 * @author Jesse
 */
public interface MaintenanceMonitor extends MaintenanceReporting {
    /**
     * A {@link MaintenanceTask} has to call this
     * regularly to see if it is allowed to be running or
     * if it must wait.
     * 
     * This method will block until task is allowed to work.
     * @throws InterruptedException thrown if 
     */
    public void obtainPermissionToWork() throws InterruptedException;
}
