package files.api.localservice;

import toolkit.services.PU;
import toolkit.services.Transactional;
import toolkit.services.TransactionalInterceptor;

import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;

/**
 * @author simetrias
 */
@Interceptor
@Transactional(puName = FilesConstants.PU_NAME)
public class FilesTransactionalInterceptor extends TransactionalInterceptor {

    @Inject
    @PU(puName = FilesConstants.PU_NAME)
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}

