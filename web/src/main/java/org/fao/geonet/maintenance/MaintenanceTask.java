package org.fao.geonet.maintenance;

import javax.annotation.Nonnull;

import jeeves.server.context.ServiceContext;

/**
 * A task that analyzes the catalog and looks for potential problems with the system.
 * 
 * @author Jesse
 */
public interface MaintenanceTask {
    /**
     * Perform one small short unit of work for this task. From the Point of view of the Maintenance framework a task is never done and the
     * CPU time will be shared between all tasks and each task will continuously be called. So if the task finishes it should restart. If it
     * is determined that no work should be done (IE the task only needs to be ran occasionally, then it can just return immediately).
     * 
     * The class can be thread safe because a task will always be ran on a single thread.
     * 
     * As soon as an issue is found it should be reported to reporting.
     * 
     * Tasks should never perform writes. Only reads. To help enforce this the database transaction will be closed and aborted after calling
     * this method.
     * 
     * @param state the return value from last call. First call it will be 0.
     * @param serviceContext a service context for accessing resources needed to perform task.
     * @param reporting the object needed to report issues
     * 
     * @return id of next state
     */
    public int performTaskStep(int state, ServiceContext serviceContext, @Nonnull MaintenanceReporting reporting) throws Exception;

}
