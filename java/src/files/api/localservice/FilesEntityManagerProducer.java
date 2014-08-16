package files.api.localservice;

import toolkit.services.PU;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

/**
 * @author simetrias
 */
public class FilesEntityManagerProducer {

    private static EntityManagerFactory emf;
    @Inject
    private FilesConfig config;

    private void checkEMF() {
        if (emf == null) {
            Properties properties = config.getPersistenceProperties();
            if (properties == null) {
                emf = Persistence.createEntityManagerFactory(FilesConstants.PU_NAME);
            } else {
                emf = Persistence.createEntityManagerFactory(FilesConstants.PU_NAME, properties);
            }
        }
    }

    @Produces
    @PU(puName = FilesConstants.PU_NAME)
    @RequestScoped
    EntityManager createEntityManager() {
        checkEMF();
        return emf.createEntityManager();
    }

    void close(@Disposes @PU(puName = FilesConstants.PU_NAME) EntityManager entityManager) {
        entityManager.close();
    }
}
