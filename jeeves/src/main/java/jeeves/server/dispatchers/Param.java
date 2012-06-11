package jeeves.server.dispatchers;

/**
 * Represents one of the parameters being passed to a service's init method 
 *
 * @author jeichar
 */
public class Param {
    String name;
    String value;
    public Param(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
}
