package files.api.localservice;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.Properties;

@Alternative
@ApplicationScoped
public class FilesIDEAMySQLConfig implements FilesConfig {

    private static final String FILES_FOLDER = "files";

    @Override
    public Properties getPersistenceProperties() {
        Properties prop = new Properties();
        prop.setProperty("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/files");
        prop.setProperty("javax.persistence.jdbc.password", "");
        prop.setProperty("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
        prop.setProperty("javax.persistence.jdbc.user", "root");
        prop.setProperty("eclipselink.ddl-generation", "create-tables");
        prop.setProperty("eclipselink.logging.level", "FINE");
        return prop;
    }

    @Override
    public String getRootFolder() {
        return FILES_FOLDER;
    }

}
