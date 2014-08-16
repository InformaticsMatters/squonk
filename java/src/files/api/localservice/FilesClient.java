package files.api.localservice;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import files.api.domain.CatalogItem;
import toolkit.services.AbstractServiceClient;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * @author simetrias
 */
public class FilesClient extends AbstractServiceClient {

    @Inject
    private FilesClientConfig config;

    @Override
    protected String getServiceBaseUri() {
        return config.getServiceUriBase();
    }

    public String createFile(String name, String properties, InputStream inputStream) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("name", name);
        queryParams.add("properties", properties);
        WebResource.Builder builder = newResourceBuilder("/createFile", queryParams);
        return builder.post(String.class, inputStream);
    }

    public InputStream retrieveContent(String name) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("name", name);
        WebResource.Builder builder = newResourceBuilder("/retrieveContent", queryParams);
        return builder.get(InputStream.class);
    }

    public InputStream retrievePrivateContent(String tag, String name) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("tag", tag);
        queryParams.add("name", name);
        WebResource.Builder builder = newResourceBuilder("/retrievePrivateContent", queryParams);
        return builder.get(InputStream.class);
    }

    public List<CatalogItem> listCatalogItemByPattern(String pattern) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("pattern", pattern);
        WebResource.Builder builder = newResourceBuilder("/listCatalogItemByPattern", queryParams);
        GenericType<List<CatalogItem>> genericType = new GenericType<List<CatalogItem>>() {
        };
        return builder.get(genericType);
    }

    public List<CatalogItem> listCatalogItemByReferenceTag(String referenceTag) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("referenceTag", referenceTag);
        WebResource.Builder builder = newResourceBuilder("/listCatalogItemByReferenceTag", queryParams);
        GenericType<List<CatalogItem>> genericType = new GenericType<List<CatalogItem>>() {
        };
        return builder.get(genericType);
    }

    public void deleteFile(String name) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("name", name);
        WebResource.Builder builder = newResourceBuilder("/deleteFile", queryParams);
        builder.post();
    }

    public String initTempFolder() {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        WebResource.Builder builder = newResourceBuilder("/initTempFolder", queryParams);
        return builder.post(String.class);
    }

    public void createTempFile(String tempFolderName, String name, InputStream inputStream) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("tempFolderName", tempFolderName);
        queryParams.add("name", name);
        WebResource.Builder builder = newResourceBuilder("/createTempFile", queryParams);
        builder.post(inputStream);
    }

    public void deleteTempFile(String tempFolderName, String name) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("tempFolderName", tempFolderName);
        queryParams.add("name", name);
        WebResource.Builder builder = newResourceBuilder("/deleteTempFile", queryParams);
        builder.post();
    }

    public void commitPrivateFiles(String tempFolderName, String tag) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("tempFolderName", tempFolderName);
        queryParams.add("tag", tag);
        WebResource.Builder builder = newResourceBuilder("/commitPrivateFiles", queryParams);
        builder.post();
    }

    public List<PrivateItem> listPrivateItemByReferenceTag(String tag) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("tag", tag);
        WebResource.Builder builder = newResourceBuilder("/listPrivateItemByReferenceTag", queryParams);
        GenericType<List<PrivateItem>> genericType = new GenericType<List<PrivateItem>>() {
        };
        return builder.get(genericType);
    }

    public CatalogItem findCatalogItemByName(String name) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("name", name);
        WebResource.Builder builder = newResourceBuilder("/findCatalogItemByName", queryParams);
        try {
            return builder.get(CatalogItem.class);
        } catch (UniformInterfaceException ex) {
            if (ex.getResponse().getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                return null;
            } else {
                throw ex;
            }
        }
    }

    public void updateReferences(UpdateReferencesData data) {
        WebResource.Builder builder = newResourceBuilder("/updateReferences");
        builder.post(data);
    }

}
