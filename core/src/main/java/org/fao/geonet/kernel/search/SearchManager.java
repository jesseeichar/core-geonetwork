//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search;

import com.google.common.annotations.VisibleForTesting;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.xml.Configuration;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.annotation.PreDestroy;

/**
 * Indexes metadata using Lucene.
 */
public class SearchManager {
	public static final String INDEXING_ERROR_FIELD = "_indexingError";

    private static final String SEARCH_STYLESHEETS_DIR_PATH = "xml/search";

	private static final Configuration FILTER_1_0_0 = new org.geotools.filter.v1_0.OGCConfiguration();
    private static final Configuration FILTER_1_1_0 = new org.geotools.filter.v1_1.OGCConfiguration();
    private static final Configuration FILTER_2_0_0 = new org.geotools.filter.v2_0.FESConfiguration();
    private Path _stylesheetsDir;

    @Autowired
    private ApplicationContext _applicationContext;
    @Autowired
    private SettingInfo _settingInfo;
    @Autowired
    private SchemaManager _schemaManager;
    @Autowired
    private GeonetworkDataDirectory _geonetworkDataDirectory;
    private Path htmlCacheDir;

    public SettingInfo getSettingInfo() {
        return _settingInfo;
    }
    @VisibleForTesting
    public void initNonStaticData(boolean logAsynch, boolean logSpatialObject, String luceneTermsToExclude,
                                     int maxWritesInTransaction) throws Exception {
        _stylesheetsDir = _geonetworkDataDirectory.resolveWebResource(SEARCH_STYLESHEETS_DIR_PATH);

        if (_stylesheetsDir == null || !Files.isDirectory(_stylesheetsDir)) {
            throw new Exception("directory " + _stylesheetsDir + " not found");
        }
        Path htmlCacheDirTest = _geonetworkDataDirectory.getHtmlCacheDir();
        Files.createDirectories(htmlCacheDirTest);
        this.htmlCacheDir = htmlCacheDirTest.toAbsolutePath();

        initZ3950();
    }

    @PreDestroy
    public void end() throws Exception {
        endZ3950();
    }

    Element transform(String styleSheetName, Element xml) throws Exception {
        try {
            Path styleSheetPath = _stylesheetsDir.resolve(styleSheetName).toAbsolutePath();
            return Xml.transform(xml, styleSheetPath);
        } catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE, "Search stylesheet contains errors : " + e.getMessage());
            throw e;
        }
    }

	/**
     * Initializes the Z3950 client searcher.
	 */
	private void initZ3950() {}

	/**
	 * deinitializes the Z3950 client searcher.
	 */
	private void endZ3950() {}

    /**
	 * Indexes a metadata record.
     *
	 * @param schemaDir
	 * @param metadata
	 * @param id
	 * @param moreFields
     * @param forceRefreshReaders if true then block all searches until they can obtain a up-to-date reader
	 * @throws Exception
	 */
	public void index(Path schemaDir, Element metadata, String id, List<Element> moreFields,
                      MetadataType metadataType, String root, boolean forceRefreshReaders)
            throws Exception {
        throw new UnsupportedOperationException("To implement");
	}

    /**
     *  deletes a document.
     *
     * @param fld
     * @param txt
     * @throws Exception
     */
	public void delete(String fld, String txt) throws Exception {
        throw new UnsupportedOperationException("To implement");
	}
	
    /**
     *  deletes a list of documents.
     *
     * @param fld
     * @param txts
     * @throws Exception
     */
    public void delete(String fld, List<String> txts) throws Exception {
        throw new UnsupportedOperationException("To implement");
    }


    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public Set<Integer> getDocsWithXLinks() throws Exception {
        throw new UnsupportedOperationException("To implement");
	}

    public ISODate getDocChangeDate(String mdId) throws Exception {
        throw new UnsupportedOperationException("To implement");
    }

    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public Map<String,String> getDocsChangeDate() throws Exception {
        throw new UnsupportedOperationException("To implement");
    }

	/**
	 * Browses the index and returns all values for the Lucene field.
	 *
	 * @param fld	The Lucene field name
	 * @return	The list of values for the field
	 * @throws Exception
	 */
    public Vector<String> getTerms(String fld) throws Exception {
        throw new UnsupportedOperationException("To implement");
    }

	/**
	 * Browses the index for the specified Lucene field and return the list of terms found containing the search value
     * with their frequency.
	 *
	 * @param fieldName	The Lucene field name
	 * @param searchValue	The value to search for. Could be "".
	 * @param maxNumberOfTerms	Max number of term's values to look in the index. For large catalogue
	 * this value should be increased in order to get better results. If this
	 * value is too high, then looking for terms could take more times. The use
	 * of good analyzer should allow to reduce the number of useless values like
	 * (a, the, ...).
	 * @param threshold	Minimum frequency for a term to be returned.
	 * @return	An unsorted and unordered list of terms with their frequency.
	 * @throws Exception
	 */
	public Collection<Object> getTermsFequency(String fieldName, String searchValue, int maxNumberOfTerms,
	                                            int threshold, ServiceContext context) throws Exception {
        throw new UnsupportedOperationException("To implement");
    }


    /**
     * Creates an index in directory luceneDir if not already there.
     *
     * @param rebuild
     * @throws Exception
     */
	private void setupIndex(boolean rebuild) throws Exception {
        throw new UnsupportedOperationException("To implement");
	}

    /**
	 *  Rebuilds the Lucene index. If xlink or from selection parameters
     *  are defined, reindex a subset of record. Otherwise reindex all records.
	 *
	 *  @param context
	 *  @param xlinks   Search all docs with XLinks, clear the XLinks cache and index all records found.
     *  @param reset
     *  @param fromSelection    Reindex all records from selection.
	 *
     * @return
     * @throws Exception
     */
	public boolean rebuildIndex(ServiceContext context,
                                boolean xlinks,
                                boolean reset,
                                boolean fromSelection) throws Exception {
        throw new UnsupportedOperationException("To implement");
	}

    public Path getHtmlCacheDir() {
        return this.htmlCacheDir;
    }
}
