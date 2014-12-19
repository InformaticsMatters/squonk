package com.im.lac.portal.webapp;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import com.im.lac.portal.service.DatasetRow;
import com.im.lac.portal.service.ListDatasetRowFilter;
import com.im.lac.portal.service.PrototypeService;
import com.im.lac.portal.service.PrototypeServiceMock;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.request.resource.DynamicImageResource;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class DynamicStructureImageResource extends DynamicImageResource {

    private static final Rectangle RECTANGLE = new Rectangle(200, 130);
    @Inject
    private PrototypeService service;

    public DynamicStructureImageResource() {
        CdiContainer.get().getNonContextualManager().postConstruct(this);
    }

    @Override
    protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {
        // this disables some unwanted default caching
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        String datasetIdAsString = attributes.getParameters().get("datasetIdAsString").toString();
        String rowIdAsString = attributes.getParameters().get("rowIdAsString").toString();
        try {
            return renderStructure(datasetIdAsString, rowIdAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String loadStructureData(String datasetIdAsString, String rowIdAsString) {
        String structureData = null;
        Long datasetId = Long.valueOf(datasetIdAsString);
        Long rowId = Long.valueOf(rowIdAsString);
        DatasetRow datasetRow = service.findDatasetRowById(datasetId, rowId);
        if (datasetRow!= null) {
            structureData = (String) datasetRow.getProperty(PrototypeServiceMock.STRUCTURE_FIELD_NAME);
        }
        return structureData;
    }

    protected Rectangle getRectangle() {
        return RECTANGLE;
    }

    protected Molecule getMolecule(String datasetIdAsString, String rowIdAsString) throws Exception {
        String structureAsString = loadStructureData(datasetIdAsString, rowIdAsString);
        Molecule molecule = MolImporter.importMol(structureAsString);
        molecule.dearomatize();
        return molecule;
    }

    private byte[] renderStructure(String datasetIdAsString, String rowIdAsString) throws Exception {
        MolPrinter molPrinter = new MolPrinter();
        molPrinter.setMol(getMolecule(datasetIdAsString, rowIdAsString));

        BufferedImage image = new BufferedImage(getRectangle().width, getRectangle().height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0, 0, getRectangle().width, getRectangle().height);
        double scale = molPrinter.maxScale(getRectangle());
        molPrinter.setScale(scale);
        molPrinter.paint(graphics2D, getRectangle());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}

