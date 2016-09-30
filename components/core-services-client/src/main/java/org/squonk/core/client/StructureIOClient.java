package org.squonk.core.client;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.squonk.dataset.Dataset;
import org.squonk.io.DepictionParameters;
import org.squonk.io.QueryParams;
import org.squonk.types.MoleculeObject;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.squonk.io.DepictionParameters.OutputFormat;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import static org.squonk.io.DepictionParameters.OutputFormat.*;

/**
 * Created by timbo on 01/09/16.
 */
@Default
@ApplicationScoped
public abstract class StructureIOClient extends AbstractHttpClient implements Serializable {
    private static final Logger LOG = Logger.getLogger(StructureIOClient.class.getName());

    private static final String SVG_TEXT1_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<svg\n" +
            "        xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
            "        xmlns=\"http://www.w3.org/2000/svg\"\n" +
            "\t\twidth=\"__width__mm\" height=\"__height__mm\"\n" +
            "        version=\"1.0\"\n" +
            "        viewBox='0 0 300.0 __box_height__' >" +
            "<text x=\"5\" y=\"15\" fill=\"grey\">__text1__</text></svg>";

    private static final String SVG_TEXT2_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<svg\n" +
            "        xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
            "        xmlns=\"http://www.w3.org/2000/svg\"\n" +
            "\t\twidth=\"__width__mm\" height=\"__height__mm\"\n" +
            "        version=\"1.0\"\n" +
            "        viewBox='0 0 300.0 __box_height__' >" +
            "<text x=\"5\" y=\"15\" fill=\"grey\">__text1__</text>" +
            "<text x=\"5\" y=\"35\" fill=\"grey\">__text2__</text>" +
            "</svg>";


    public static final StructureIOClient CDK = new CDK();

    public static StructureIOClient[] clients = new StructureIOClient[]{CDK};

    protected void debugConnections(String method, URI uri) { }

    public byte[] renderImage(String structure, String structureFormat, OutputFormat imgFormat, DepictionParameters depict) {
        throw new UnsupportedOperationException("NYI");
    }

    protected byte[] doRenderImage(String mol, String molFormat, OutputFormat imgFormat, DepictionParameters depict) {

        if (imgFormat == null) {
            return handleErrorImage("Image format not specified", depict);
        }
        if (mol == null || mol.isEmpty()) {
            return handleErrorImage("No structure specified", depict);
        }

        if (imgFormat == svg) {
            String svg = renderSVG(mol, molFormat, depict);
            return svg.getBytes(StandardCharsets.UTF_8);
        } else if (molFormat.startsWith("smiles")) {
            return createImageUsingGet(mol, molFormat, imgFormat, depict);
        } else {
            return createImageUsingPost(mol, molFormat, imgFormat, depict);
        }
    }

    public String renderSVG(String mol, String molFormat, DepictionParameters depict) {
        throw new UnsupportedOperationException("NYI");
    }

    public String renderSVGError(DepictionParameters depict, String message) {
        return handleErrorSVG(depict, message);
    }

    protected String doRenderSVG(String mol, String molFormat, DepictionParameters depict) {

        if (molFormat == null) {
            return handleErrorSVG(depict, "Format not specified");
        }
        if (mol == null || mol.isEmpty()) {
            return handleErrorSVG(depict, "No structure specified");
        }

        if (molFormat.startsWith("smiles")) {
            return createSVGUsingGet(mol, molFormat, depict);
        } else {
            return createSVGUsingPost(mol, molFormat, depict);
        }
    }

    public InputStream molConvert(Dataset<MoleculeObject> mols, String toFormat, boolean gzip) throws IOException {
        throw new UnsupportedOperationException("NYI");
    }

    protected InputStream doMolConvert(Dataset<MoleculeObject> mols, String toFormat, boolean gzip) throws IOException {

        if ("sdf".equals(toFormat)) {
            URIBuilder b = createURIBuilder(getMolConvertBase() + "/convert_to_sdf");
            b.addParameter("Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON);
            b.addParameter("Content-Encoding", "gzip");
            b.addParameter("Accept", CommonMimeTypes.MIME_TYPE_MDL_SDF);
            if (gzip) {
                b.addParameter("Accept-Encoding", "gzip");
            }

            InputStream is = executePostAsInputStream(b, new InputStreamEntity(mols.getInputStream(true)));
            return is;

        }
        throw new IllegalArgumentException("Format " + toFormat + " not supported");
    }

    private byte[] createImageUsingGet(String mol, String molFormat, OutputFormat imgFormat, DepictionParameters depict) {
        URIBuilder b = createURIBuilder(getMolDepictBase());
        QueryParams queryParams = depict.asQueryParams();
        queryParams.add("mol", mol);
        queryParams.add("molFormat", molFormat);
        queryParams.add("format", imgFormat.toString());
        queryParams.consume((k, v) -> b.addParameter(k, v));
        try (InputStream is = executeGetAsInputStream(b)) {
            return IOUtils.convertStreamToBytes(is);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to generate image", e);
            return handleErrorImage("Failed to generate image", depict);
        }
    }

    private String createSVGUsingPost(String mol, String molFormat, DepictionParameters depict) {
        URIBuilder b = createURIBuilder(getMolDepictBase());
        QueryParams queryParams = depict.asQueryParams();
        queryParams.add("molFormat", molFormat);
        queryParams.add("format", "svg");
        queryParams.consume((k, v) -> b.addParameter(k, v));
        try (InputStream is = executePostAsInputStream(b, new StringEntity(mol))) {
            return IOUtils.convertStreamToString(is);
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to generate svg", e);
            return handleErrorSVG(depict, "Failed to generate svg", e.getClass().getName());
        }
    }

    private String createSVGUsingGet(String mol, String molFormat, DepictionParameters depict) {
        URIBuilder b = createURIBuilder(getMolDepictBase());
        QueryParams queryParams = depict.asQueryParams();
        queryParams.add("mol", mol);
        queryParams.add("molFormat", molFormat);
        queryParams.add("format", "svg");
        queryParams.consume((k, v) -> b.addParameter(k, v));
        try (InputStream is = executeGetAsInputStream(b)) {
            return IOUtils.convertStreamToString(is);
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to generate image", e);
            return handleErrorSVG(depict, "Failed to generate image", e.getClass().getName());
        }
    }

    private byte[] createImageUsingPost(String mol, String molFormat, OutputFormat imgFormat, DepictionParameters depict) {
        URIBuilder b = createURIBuilder(getMolDepictBase());
        QueryParams queryParams = depict.asQueryParams();
        queryParams.add("molFormat", molFormat);
        queryParams.add("format", imgFormat.toString());
        queryParams.consume((k, v) -> b.addParameter(k, v));
        try (InputStream is = executePostAsInputStream(b, new StringEntity(mol))) {
            return IOUtils.convertStreamToBytes(is);
        } catch (IOException e) {
            return handleErrorImage("Failed to generate image", depict);
        }
    }


    protected URIBuilder createURIBuilder(String base) {
        return new URIBuilder().setPath(base);
    }


    protected abstract String getBase();

    protected String getMolDepictBase() {
        return getBase() + "moldepict";
    }

    protected String getMolConvertBase() {
        return getBase() + "rest/v1/converters";
    }

    protected byte[] handleErrorImage(String message, DepictionParameters depict) {
        LOG.warning(message);
        return new byte[0];
    }

    protected String handleErrorSVG(DepictionParameters depict, String message) {
        float viewHeight = 300 * depict.getHeight() / depict.getWidth();
        String results = SVG_TEXT1_TEMPLATE
                .replace("__width__", depict.getWidth().toString())
                .replace("__height__", depict.getHeight().toString())
                .replace("__text1__", message)
                .replace("__box_height__", ""+viewHeight);

        return results;
    }

    protected String handleErrorSVG(DepictionParameters depict, String message1, String message2) {
        float viewHeight = 300 * depict.getHeight() / depict.getWidth();
        String results = SVG_TEXT2_TEMPLATE
                .replace("__width__", depict.getWidth().toString())
                .replace("__height__", depict.getHeight().toString())
                .replace("__text1__", message1)
                .replace("__text2__", message2)
                .replace("__box_height__", ""+viewHeight);

        return results;
    }


    public static class CDK extends StructureIOClient {

        @Override
        protected String getBase() {
            return "http://chemservices:8080/chem-services-cdk-basic/";
        }

        @Override
        public String renderSVG(String mol, String molFormat, DepictionParameters depict) {
            return doRenderSVG(mol, molFormat, depict);
        }

        @Override
        public byte[] renderImage(String mol, String molFormat, OutputFormat imgFormat, DepictionParameters depict) {
            return doRenderImage(mol, molFormat, imgFormat, depict);
        }

        @Override
        public InputStream molConvert(Dataset<MoleculeObject> mols, String toFormat, boolean gzip) throws IOException {
            return doMolConvert(mols, toFormat, gzip);
        }
    }

}



