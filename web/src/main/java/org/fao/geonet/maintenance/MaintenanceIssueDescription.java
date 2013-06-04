package org.fao.geonet.maintenance;

/**
 * A single translation description of the issue
 * 
 * @author Jesse
 */
public class MaintenanceIssueDescription {
    public final String _lang;
    public final String _name;
    public final String _description;

    /**
     * Constructor
     * 
     * @param lang 2 letter language code of description
     * @param name name
     * @param description description of issue
     */
    public MaintenanceIssueDescription(String lang, String name, String description) {
        this._lang = lang;
        this._name = name;
        this._description = description;
    }
}