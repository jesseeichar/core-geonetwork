package jeeves.config.springutil;

import java.io.File;
import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ReloadableBean implements ApplicationContextAware {

    private File configFile;
    private ApplicationContext applicationContext;
    public ReloadableBean(File configFile) {
        this.configFile = configFile;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void reload() throws Exception {
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(new String[]{configFile.getAbsolutePath()}, applicationContext);
        ReloadableBean newBean = context.getBean(getClass());
        
        for (Field field : getClass().getDeclaredFields()) {
            if(field.getName() == "configFile" || field.getName() == "applicationContext") {
                continue;
            }
            
            field.setAccessible(true);
            field.set(this, field.get(newBean));
        }
    }

}
