package files.api.localservice;

import java.util.Properties;

public interface FilesConfig {

    Properties getPersistenceProperties();

    String getRootFolder();

}
