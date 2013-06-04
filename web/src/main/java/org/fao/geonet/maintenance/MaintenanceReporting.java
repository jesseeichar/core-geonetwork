package org.fao.geonet.maintenance;

import jeeves.interfaces.Logger;

public interface MaintenanceReporting {

    /**
     * Report an issue with the system that should be
     * reported back to the administrator.
     *
     * @param report the report
     * @throws InterruptedException signals that maintanence thread should shutdown. 
     */
    public void report(MaintenanceReport report) throws InterruptedException;
    
    /**
     * Obtain a logger for the maintenance module.
     * 
     * @return a logger for the maintenance module.
     */
    public Logger logger();

}
