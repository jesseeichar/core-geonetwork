package jeeves.server;

/**
 * A tag interface used to identify all spring components that need to be
 * be created and initialized after jeeves.  
 * 
 * This is only required if:
 * 
 * <ul>
 *      <li>The bean needs to perform tasks in @PostConstruction or in the constructor</li>
 *      <li>The bean is not initialized at a later point</li>
 *      <li>The bean depends on one of the objects that Jeeves injects into the application context</li>
 * </ul>
 * 
 * @author Jesse
 */
public interface PostJeevesInitialization {

}
