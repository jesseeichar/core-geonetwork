//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jeeves.constants.Jeeves;

import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

//=============================================================================

/** This class is a container for the user-profiles.xml file
  */

public class ProfileManager implements ApplicationContextAware
{
	public static final String GUEST = "Guest";
	public static final String ADMIN = "Administrator";

	private Map<String, Profile> htProfiles;

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public boolean exists(String profile)
	{
		return htProfiles.containsKey(profile);
	}

	/**
	 * Return the case corrected version of the supplied 
	 * profile name, or empty string if none match.
	 * 
	 * @param profile The profile name to be corrected.
	 * @return The correct profile name
	 */
	public String getCorrectCase(String profile)
	{
		Set<String> keys = htProfiles.keySet();
		for (String key : keys) {
			if (key.equalsIgnoreCase(profile)) {
				return key;
			}
		}
		
		return "";
	}

	public Profile getProfile(String profileName) {
	    return htProfiles.get(profileName);
	}
	//--------------------------------------------------------------------------

	public Element getProfilesElement(String profile)
	{
		Set<String> set = getProfilesSet(profile);

		//--- build proper result

		Element elResult = new Element(Jeeves.Elem.PROFILES);

		for (String p : set) {
			if (p.equals(GUEST))
				continue;

			elResult.addContent(new Element(p));
		}

		return elResult;
	}

	//--------------------------------------------------------------------------

	public Set<String> getProfilesSet(String profile)
	{
		HashSet<String>   hs = new HashSet<String>();
		ArrayList<String> al = new ArrayList<String>();

		al.add(profile);

		while(!al.isEmpty())
		{
			profile = (String) al.get(0);
			al.remove(0);

			hs.add(profile);

			Profile elProfile = htProfiles.get(profile);

			al.addAll(elProfile.getParentProfileNames());
		}
		return hs;
	}

	//--------------------------------------------------------------------------
	/** Returns all services accessible by the given profile
	  */

	public Element getAccessibleServices(String profile)
	{
		//--- build proper result

		Element elRes = new Element(Jeeves.Elem.SERVICES);

		for (String service : htProfiles.get(profile).getAllAccess(this)) {
			elRes.addContent(new Element(Jeeves.Elem.SERVICE)
					.setAttribute(new Attribute(Jeeves.Attr.NAME, service)));
		}

		return elRes;
	}

    //--------------------------------------------------------------------------
	/** Returns true if the service is accessible from the given profile, resolving
	  * any inheritance
	  */

	public boolean hasAccessTo(String profileName, String service)
	{
	    Profile profile = htProfiles.get(profileName);
	    return profile.getAllAccess(this).contains(service);
	}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        htProfiles = applicationContext.getBeansOfType(Profile.class);
    }
}

//=============================================================================


