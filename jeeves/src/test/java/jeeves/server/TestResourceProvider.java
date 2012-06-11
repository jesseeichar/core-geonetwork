package jeeves.server;

import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.geotools.data.DataStore;

import jeeves.server.resources.ResourceListener;
import jeeves.server.resources.ResourceProvider;
import jeeves.server.resources.Stats;

public class TestResourceProvider implements ResourceProvider {
    public ResourceProvider spy = mock(ResourceProvider.class);
    
    public String getName() {
        return spy.getName();
    }
    public Map<String, String> getProps() {
        return spy.getProps();
    }
    public Stats getStats() throws SQLException {
        return spy.getStats();
    }
    public DataStore getDataStore() {
        return spy.getDataStore();
    }
    @PreDestroy
    public void end() {
        spy.end();
    }
    public Object open() throws Exception {
        return spy.open();
    }
    public void close(Object resource) throws Exception {
        spy.close(resource);
    }
    public void abort(Object resource) throws Exception {
        spy.abort(resource);
    }
    public void addListener(ResourceListener l) {
        spy.addListener(l);
    }
    public void removeListener(ResourceListener l) {
        spy.removeListener(l);
    }
    
    
    
}
