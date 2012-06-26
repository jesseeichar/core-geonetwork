package org.fao.geonet.kernel.search;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.apache.lucene.search.Query;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.function.QueryBoostFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * A QueryBoostFactory for backwards compatibility so that it is possible to
 * migrate configuration from old configuration to new configuration. This will
 * be phased out.
 * 
 * @author jeichar
 */
public class BackwardsCompatibleQueryBoostFactory implements QueryBoostFactory {
	Class<?> boostQueryClass;
	Class<?>[] types;
	Object[] params;

	@Override
	public Query createBoost(Query query) throws Exception {
		try {
			Log.debug(Geonet.SEARCH_ENGINE, "Create boosting query:"
					+ boostQueryClass);

			Class<?>[] clTypesArrayAll = new Class[types.length + 1];
			clTypesArrayAll[0] = Class
					.forName("org.apache.lucene.search.Query");

			System.arraycopy(types, 0, clTypesArrayAll, 1,
					types.length);
			Object[] inParamsArrayAll = new Object[params.length + 1];
			inParamsArrayAll[0] = query;
			System.arraycopy(params, 0, inParamsArrayAll, 1,
					params.length);
			try {
				if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
					Log.debug(
							Geonet.SEARCH_ENGINE,
							"Creating boost query with parameters:"
									+ Arrays.toString(inParamsArrayAll));
				Constructor<?> c = boostQueryClass.getConstructor(clTypesArrayAll);
				return (Query) c.newInstance(inParamsArrayAll);
			} catch (Exception e) {
				Log.warning(Geonet.SEARCH_ENGINE,
						" Failed to create boosting query: " + e.getMessage()
								+ ". Check Lucene configuration");
				e.printStackTrace();
			}
		} catch (Exception e1) {
			Log.warning(
					Geonet.SEARCH_ENGINE,
					" Error on boosting query initialization: "
							+ e1.getMessage() + ". Check Lucene configuration");
		}
		return null;
	}
	
	@Required
	public void setBoostQueryClass(String boostQueryClass) throws ClassNotFoundException {
		this.boostQueryClass = Class.forName(boostQueryClass);

	}

	/**
	 * Set the type and value of the constructor parameters. Key is type value
	 * is value
	 */
	@Required
	public void setParams(Map<String, String> params) throws Exception {
		this.types = new Class[params.size()];
		this.params = new Object[params.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String paramType = entry.getKey();
			String value = entry.getValue();

			if ("double".equals(paramType)) {
				this.types[i] = double.class;
			} else if ("int".equals(paramType)) {
				this.types[i] = int.class;
			} else {
				this.types[i] = Class.forName(paramType);
			}

			if ("org.apache.lucene.util.Version".equals(paramType)) {
				this.params[i] = org.apache.lucene.util.Version.LUCENE_30;
			} else if ("java.io.File".equals(paramType) && value != null) {
				File f = new File(value);
				if (!f.exists() && ServiceContext.get() != null) { // try relative to appPath
					f = new File(ServiceContext.get().getAppPath() + value);
					if(!f.exists()) {
						f = new File(ServiceContext.get().getServletContext().getRealPath(value));
					}
				}
				if (f != null && f.exists()) {
					this.params[i] = f;
				}
			} else if ("double".equals(paramType) && value != null) {
				this.params[i] = Double.parseDouble(value);
			} else if ("int".equals(paramType) && value != null) {
				this.params[i] = Integer.parseInt(value);
			} else if (value != null) {
				this.params[i] = value;
			} else {
				// No value. eg. Version
			}

			i++;
		}
	}

}
