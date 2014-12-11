package org.fao.geonet.wro4j;

/**
 * Enumerates the supported @import (option) options in less files.
 *
 * @author Jesse on 12/11/2014.
 */
public enum LessImportOption {
    /**
     * Indicate that the file can be repeatedly imported.
     */
    MULTIPLE,
    /**
     * Indicates that the resource should be only imported a single time.
     */
    ONCE
}
