package com.im.lac.portal.service;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordReader;
import chemaxon.struc.MProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PrototypeServiceMock implements PrototypeService {

    private static final Logger logger = LoggerFactory.getLogger(PrototypeServiceMock.class.getName());

    // TODO decide how to best handle this
    public static final String STRUCTURE_FIELD_NAME = "structure_as_text";

    @Inject
    private DatabaseMock databaseMock;

    @Override
    public DatasetDescriptor createDataset(DatamartSearch datamartSearch) {
        DatasetMock datasetMock = new DatasetMock();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("structure", "Structure");
        properties.put("p1", "Property 1");
        properties.put("p2", "Property 2");
        for (long i = 1; i <= 100; i++) {
            datasetMock.addDatasetRow(i, properties);
        }

        return databaseMock.persistDatasetMock(datasetMock);
    }

    @Override
    public DatasetDescriptor createDataset(DatasetInputStreamFormat format, InputStream inputStream, Map<String, Class> fieldConfig) {
        // import from SDF and add to database mock.
        try {
            DatasetMock datasetMock = parseSdf(format, inputStream, fieldConfig);
            return databaseMock.persistDatasetMock(datasetMock);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read file", ex);
        }
    }

    /** Parse file using ChemAxon Marvin and generate a DataSetMock
     * Note: this is a temporary method to allow data to be created for testing. 
     * It will be replaced by something more solid.
     * 
     * @param format currently ignored as format recognition left to Marvin
     * @param inputStream The data in a format that Marvin can handle
     * @param fieldConfig A Map of field names and destination classes. Must not be
     * null. Empty Map means no conversions and everything will be a String. The data is 
     * obtained as String and converted to the corresponding class using the #convert()
     * method (note: this is very primitive).
     * @return The generated DataSetMock with one DataSetRow per record in the input file.
     */
    protected DatasetMock parseSdf(DatasetInputStreamFormat format, InputStream inputStream, Map<String, Class> fieldConfig) throws Exception {
        MRecordReader recordReader = null;
        DatasetMock datasetMock = new DatasetMock();
        try {
            recordReader = MFileFormatUtil.createRecordReader(inputStream, null, null, null);
            long count = 0;
            logger.info("Parsing file");

            while (true) {
                count++;
                logger.debug("Reading record");
                MRecord rec = recordReader.nextRecord();
                if (rec == null) {
                    break;
                } else {
                    Map<String, Object> properties = new HashMap<String, Object>();
                    properties.put(STRUCTURE_FIELD_NAME, rec.getString());
                    String[] fields = rec.getPropertyContainer().getKeys();
                    List<MProp> values = rec.getPropertyContainer().getPropList();
                    for (int x = 0; x < fields.length; x++) {
                        String prop = fields[x];
                        String strVal = MPropHandler.convertToString(values.get(x), null);
                        Object objVal = convert(strVal, fieldConfig.get(prop));
                        logger.trace("Generated value for field " + prop + " of " + objVal + " type " + objVal.getClass().getName());
                        properties.put(prop, objVal);
                    }
                    datasetMock.addDatasetRow(count, properties);
                }
            }
        } finally {
            if (recordReader != null) {
                try {
                    recordReader.close();
                } catch (IOException ioe) {
                    logger.warn("Failed to close MRecordReader", ioe);
                }
            }
        }
        logger.info("File processed " + datasetMock.getDatasetRowList().size() + " records handled");
        return datasetMock;
    }
    
    private Object convert(String value, Class cls) {
        if (cls == null || cls == String.class) {
            return value;
        } else if (cls == Integer.class) {
            return new Integer(value);
        } else if (cls == Float.class) {
            return new Float(value);
        } else {
            throw new IllegalArgumentException("Unsupported conversion: " + cls.getName());
        }
    }

    @Override
    public List<Long> listDatasetRowId(DatasetDescriptor datasetDescriptor) {

        return null;
    }

    @Override
    public List<DatasetDescriptor> listDatasetDescriptor() {
        return null;
    }

    @Override
    public List<PropertyDefinition> listPropertyDefinition(ListPropertyDefinitionFilter filter) {
        return null;
    }

    @Override
    public List<DatasetRow> listDatasetRow(ListDatasetRowFilter filter) {
        return null;
    }

}
