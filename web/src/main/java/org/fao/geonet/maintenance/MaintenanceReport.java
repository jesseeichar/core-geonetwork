package org.fao.geonet.maintenance;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A report of an issue with the system.
 * 
 * @author Jesse
 */
public class MaintenanceReport {
    /**
     * An enumeration of the allowed types of the report.
     * 
     * @author Jesse
     */
    public enum Category {
        /**
         * Represents an error in the metadata. Perhaps xsd validation problem or broken link, incorrect translation or missing parent
         */
        METADATA_ERROR,
        /**
         * Indicates the metadata fails some validation check
         */
        METADATA_VALIDATION,
        /**
         * Indicates an inconsistency with the metadata. For example the layer name listed in a coupled resource does not exist in the
         * server any longer
         */
        METADATA_INCONSISTENCY,
        /**
         * A catch all for other metadata issues
         */
        METADATA_OTHER,
        /**
         * A problem with configuration of the system
         */
        CONFIG,
        /**
         * A problem in the settings
         */
        SETTING,
        /**
         * A problem with the harvester
         */
        HARVESTER,
        /**
         * An issue with the security of the system or user, group, profile management
         */
        SECURITY,
        /**
         * Catch all for any other type of problem
         */
        OTHER
    }

    /**
     * A rating of the importance/severity of the issue. Likely used for sorting and filter in the UI.
     * 
     * @author Jesse
     */
    public enum Severity {
        CRITICAL, IMPORTANT, NON_CRITICAL, TRIVIAL
    }

    private static final int[] EMPTY_INTS = new int[0];

    private static final URL[] EMPTY_URLS = new URL[0];

    private final @Nonnull
    Category _category;
    private final @Nonnull
    Severity _severity;
    private final Map<String, MaintenanceIssueDescription> _description = new HashMap<String, MaintenanceIssueDescription>();
    private @Nullable
    URL[] _fixLink;
    private @Nullable
    int[] _groupIds;
    private @Nullable
    int[] _userIds;
    private @Nonnull String _taskClass;
    private @Nonnull String _issueId;

    /**
     * Constructor
     * 
     * @param task the task that created this issue
     * @param issueId an id that identifies this issue.  This is used later ensure that the same issue is only written to the 
     *                  database once.  The issue only has to be unique with in the task.  A final id will combine the issueId and the taskClassName
     * @param category a category used by UI for organizing issues
     * @param severity the importance/severity of the issue
     * @param description a description of the issue. Other translations can be added by addDescription
     */
    public MaintenanceReport(@Nonnull MaintenanceTask task, @Nonnull String issueId, @Nonnull Category category, @Nonnull Severity severity, @Nonnull MaintenanceIssueDescription description) {
        this._taskClass = task.getClass().getName();
        this._issueId = issueId;
        this._category = category;
        this._severity = severity;
        this._description.put(description._lang, description);
    }

    /**
     * Get the category
     */
    public Category getCategory() {
        return _category;
    }

    /**
     * Get the importance/severity of the issue
     */
    public Severity getSeverity() {
        return _severity;
    }

    /**
     * Add some extra translations about the issue
     * 
     * @param description description of translation
     */
    public void addDescription(@Nonnull MaintenanceIssueDescription description) {
        this._description.put(description._lang, description);
    }

    /**
     * Get all the translation of the issue description
     * 
     * @return
     */
    public @Nonnull
    Map<String, MaintenanceIssueDescription> getDescription() {
        return Collections.unmodifiableMap(_description);
    }

    /**
     * Optionally set some groupIds of groups who are affected by this issue.
     * 
     * @param groupIds ids of groups affected by this issue
     */
    public void setGroupIds(int... groupIds) {
        this._groupIds = groupIds;
    }

    /**
     * Return the ids of the groups potentially affected by this issue.
     */
    public @Nonnull
    int[] getGroupIds() {
        if (_groupIds == null) {
            return EMPTY_INTS;
        }
        return _groupIds.clone();
    }

    /**
     * Optionally set some userIds of users who are affected by this issue.
     * 
     * @param userIds ids of users affected by this issue
     */
    public void setUserIds(int... userIds) {
        this._userIds = userIds;
    }

    /**
     * Return the ids of the users potentially affected by this issue.
     */
    public @Nonnull
    int[] getUserIds() {
        if (_userIds == null) {
            return EMPTY_INTS;
        }
        return _userIds.clone();
    }

    /**
     * Optional. A link that can potentially fix the issue
     * 
     * @param fixLink the url of the link
     */
    public void setFixLinks(URL... fixLink) {
        this._fixLink = fixLink;
    }

    /**
     * Return the fix link
     */
    public @Nonnull
    URL[] getFixLink() {
        if (this._fixLink == null) {
            return EMPTY_URLS;
        }
        return _fixLink.clone();
    }

    /**
     * Get the name of the class who created this report.
     */
    public @Nonnull String getTaskClass() {
        return _taskClass;
    }
    
    /**
     * Get an identifier for this issue
     */
    public @Nonnull String getId() {
        return _taskClass+"|||"+_issueId;
    }
}
