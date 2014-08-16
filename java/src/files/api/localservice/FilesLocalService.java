package files.api.localservice;

import files.api.domain.CatalogItem;
import files.api.domain.Reference;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
@ApplicationScoped
@Transactional(puName = FilesConstants.PU_NAME)
public class FilesLocalService {

    private static final Logger logger = Logger.getLogger(FilesLocalService.class.getName());
    @Inject
    private FilesConfig config;

    public String initTempFolder() {
        try {
            String sessionId = UUID.randomUUID().toString();
            File directory = new File(config.getRootFolder() + "/temp" + "/" + sessionId);
            if (!directory.exists()) {
                createDir(directory);
            }
            return sessionId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initTempFolderFromTag(String sourceTag) {
        try {
            File directory = new File(config.getRootFolder() + "/temp" + "/" + sourceTag);
            if (!directory.exists()) {
                createDir(directory);
            }
            removeFiles(directory);
            File sourceDir = new File(config.getRootFolder() + "/private/" + sourceTag);
            copyFiles(sourceDir, directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTempFile(String tempFolderName, String name, InputStream inputStream) {
        try {
            File directory = new File(config.getRootFolder() + "/temp" + "/" + tempFolderName);
            File file = new File(directory, name);
            writeFile(file, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTempFile(String tempFolderName, String name) {
        File file = new File(config.getRootFolder() + "/temp/" + tempFolderName + "/" + name);
        if (!file.delete()) {
            logger.log(Level.WARNING, String.format("No se pudo eliminar el archivo %s", file.getAbsolutePath()));
        }
    }

    public void commitPrivateFiles(String tempFolderName, String tag) {
        try {
            File sourceDir = new File(config.getRootFolder() + "/temp" + "/" + tempFolderName);
            File destDir = new File(config.getRootFolder() + "/private" + "/" + tag);
            if (!sourceDir.exists()) {
                throw new IOException("Directorio temporario desconocido; " + tempFolderName);
            }
            if (!destDir.exists()) {
                createDir(destDir);
            }
            copyFiles(sourceDir, destDir);
            removeFiles(sourceDir);
            if (!sourceDir.delete()) {
                logger.log(Level.WARNING, String.format("No se pudo eliminar el directorio %s", sourceDir.getAbsolutePath()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<PrivateItem> listPrivateItemByReferenceTag(String tag) {
        File directory = new File(config.getRootFolder() + "/private" + "/" + tag);
        List<PrivateItem> list = new ArrayList<PrivateItem>();
        for (File file : directory.listFiles()) {
            PrivateItem privateItem = new PrivateItem();
            privateItem.setName(file.getName());
            list.add(privateItem);
        }
        return list;
    }

    public String createFile(EntityManager entityManager, String name, String properties, InputStream inputStream) {
        try {
            String realPath = config.getRootFolder() + "/shared";
            File dir = new File(realPath);
            if (!dir.exists()) {
                createDir(dir);
            }
            CatalogItem item = findCatalogItemByName(entityManager, name);
            boolean insert = item == null;
            if (insert) {
                item = new CatalogItem();
                item.setName(name);
                item.setProperties(properties);
                entityManager.persist(item);
            }
            File file = resolveFile(entityManager, item);
            if (!insert && file.exists()) {
                throw new RuntimeException("El archivo ya está catalogado");
            }
            writeFile(file, inputStream);
            return item.getId().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Response retrieveContent(EntityManager entityManager, String name) {
        CatalogItem archivo = findCatalogItemByName(entityManager, name);
        final File file = resolveFile(entityManager, archivo);
        if (!file.exists()) {
            throw new RuntimeException("Catálogo inconsistente. No se pudo encontrar el contenido");
        }
        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                InputStream inputStream = new FileInputStream(file);
                try {
                    transfer(inputStream, outputStream);
                    outputStream.flush();
                } finally {
                    inputStream.close();
                }

            }
        };
        return Response.ok(streamingOutput).build();
    }

    public Response retrievePrivateContent(String tag, String name) {
        final File file = new File(config.getRootFolder() + "/private" + "/" + tag + "/" + name);
        if (!file.exists()) {
            throw new RuntimeException("No se pudo encontrar el contenido privado");
        }
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                InputStream inputStream = new FileInputStream(file);
                try {
                    transfer(inputStream, outputStream);
                    outputStream.flush();
                } finally {
                    inputStream.close();
                }

            }
        };
        return Response.ok(streamingOutput).build();
    }

    public void updateReferences(EntityManager entityManager, UpdateReferencesData data) {
        TypedQuery<Reference> query = entityManager.createQuery("select o from Reference o where o.tag = :referenceTag", Reference.class);
        query.setParameter("referenceTag", data.getReferenceTag());
        for (Reference reference : query.getResultList()) {
            entityManager.remove(reference);
        }
        if (data.getCatalogItemIdList() != null) {
            for (Long id : data.getCatalogItemIdList()) {
                CatalogItem catalogItem = entityManager.find(CatalogItem.class, id);
                Reference reference = new Reference();
                reference.setCatalogItem(catalogItem);
                reference.setTag(data.getReferenceTag());
                entityManager.persist(reference);
            }
        }
    }

    public void deleteFile(EntityManager entityManager, String name) {
        CatalogItem item = findCatalogItemByName(entityManager, name);
        if (item == null) {
            return;
        }
        TypedQuery<Long> query = entityManager.createQuery("select count(o) from Reference o where o.catalogItem.id = :catalogItemId", Long.class);
        query.setParameter("catalogItemId", item.getId());
        if (!query.getSingleResult().equals(0l)) {
            throw new RuntimeException("El archivo está en uso");
        }
        entityManager.remove(item);
        String realPath = config.getRootFolder();
        File dir = new File(realPath);
        File file = new File(dir, item.getId() + "." + name);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Could not delete " + name);
        }
    }

    public List<CatalogItem> listCatalogItemByPattern(EntityManager entityManager, String pattern) {
        TypedQuery<CatalogItem> query = entityManager.createQuery("select o from CatalogItem o where o.name like :pattern", CatalogItem.class);
        query.setParameter("pattern", pattern + "%");
        List<CatalogItem> resultList = query.getResultList();
        return resultList;
    }

    public List<CatalogItem> listCatalogItemByReferenceTag(EntityManager entityManager, String referenceTag) {
        TypedQuery<CatalogItem> query = entityManager.createQuery("select o.catalogItem from Reference o where o.tag = :referenceTag order by o.catalogItem.name", CatalogItem.class);
        query.setParameter("referenceTag", referenceTag);
        List<CatalogItem> resultList = query.getResultList();
        return resultList;
    }

    public CatalogItem findCatalogItemByName(EntityManager entityManager, String nombre) {
        TypedQuery<CatalogItem> query = entityManager.createQuery("select o from CatalogItem o where o.name = :name", CatalogItem.class);
        query.setParameter("name", nombre);
        return query.getResultList().isEmpty() ? null : query.getResultList().get(0);
    }

    private File resolveFile(EntityManager entityManager, CatalogItem catalogItem) {
        String realPath = config.getRootFolder() + "/shared";
        File dir = new File(realPath);
        String name = catalogItem.getId().toString() + "." + catalogItem.getName();
        return new File(dir, name);
    }


    private void removeFiles(File dir) {
        for (File file : dir.listFiles()) {
            if (!file.delete()) {
                logger.log(Level.WARNING, String.format("No se pudo eliminar el archivo %s", file.getAbsolutePath()));
            }
        }
    }

    private void copyFiles(File sourceDir, File destDir) throws IOException {
        for (File file : sourceDir.listFiles()) {
            File destfile = new File(destDir, file.getName());
            copyFile(file, destfile);
        }
    }

    private void copyFile(File file, File destfile) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        try {
            OutputStream outputStream = new FileOutputStream(destfile);
            try {
                transfer(inputStream, outputStream);
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } finally {
            inputStream.close();
        }
    }

    private void writeFile(File file, InputStream inputStream) throws IOException {
        OutputStream outputStream = new FileOutputStream(file);
        try {
            transfer(inputStream, outputStream);
            outputStream.flush();
        } finally {
            outputStream.close();
        }
    }

    private synchronized void createDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear " + dir.getAbsolutePath());
        }
    }

    private void transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int r = inputStream.read(buffer, 0, buffer.length);
        while (r > -1) {
            outputStream.write(buffer, 0, r);
            r = inputStream.read(buffer, 0, buffer.length);
        }
    }

}
