package jeeves.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Profile {
    private List<String> parentProfilesNames = Collections.emptyList();
    private Set<String> access = Collections.emptySet();
    
    /** Not a bean property */
    private volatile Set<String> allAccess;
    
    public void setAccess(Collection<String> access) {
        this.access = new HashSet<String>(access);
    }
    public Collection<String> getAccess() {
        return access;
    }
    public void setExtends(String extend) {
        parentProfilesNames = Arrays.asList(extend.split(","));
    }
    public List<String> getParentProfileNames() {
        return parentProfilesNames;
    }
    public Set<String> getAllAccess(ProfileManager profileManager) {
        if(allAccess == null) {
            synchronized (this) {
                if(allAccess == null) {
                    allAccess = new HashSet<String>();
                    allAccess.addAll(access);
                    for (String parent : parentProfilesNames) {
                        Set<String> parentAccess = profileManager.getProfile(parent).getAllAccess(profileManager);
                        allAccess.addAll(parentAccess);
                    }
                }
            }
        }
        return allAccess;
    }
}
