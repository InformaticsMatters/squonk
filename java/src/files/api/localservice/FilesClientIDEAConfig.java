package files.api.localservice;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

/**
 * @author simetrias
 */
@ApplicationScoped
@Alternative
public class FilesClientIDEAConfig implements FilesClientConfig {

    @Override
    public String getServiceUriBase() {
        return "http://localhost:8085/ws/files";
    }

}
