package org.squonk.util;

import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.io.JsonHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by timbo on 09/05/2016.
 */
public class MoleculeObjectUtils {

    public static Stream<MoleculeObject> deduplicate(
            Stream<MoleculeObject> mols,
            String canonicalSmilesField,
            List<String> keepFirstFields,
            List<String> keepLastFields,
            List<String> appendFields) {

        if (canonicalSmilesField == null) {
            throw new NullPointerException("Must specify canonicalValueName");
        }


        Map<Object, MoleculeObject> results = new LinkedHashMap<>();

        mols.forEachOrdered((mo) -> {
            String smiles = mo.getValue(canonicalSmilesField, String.class);
            if (smiles != null) {
                MoleculeObject existing = results.get(smiles);
                if (existing != null) {
                    for (Map.Entry<String,Object> e :  mo.getValues().entrySet()) {
                        String k = e.getKey();
                        Object v = e.getValue();
                        if (k.equals(canonicalSmilesField)) {
                            continue;
                        } else if (appendFields.contains(k)) {
                            List l = (List)existing.getValue(k);
                            if (l == null) {
                                l = new ArrayList();
                                existing.putValue(k, l);
                            }
                            l.add(v);
                        }
                        if (keepLastFields.contains(k)) {
                            existing.putValue(k, v);
                        }
                        if (keepFirstFields.contains(k)) {
                            existing.getValues().putIfAbsent(k, v);
                        }
                    }
                } else {
                    MoleculeObject first = new MoleculeObject(smiles, "smiles");
                    for (Map.Entry<String,Object> e :  mo.getValues().entrySet()) {
                        String k = e.getKey();
                        if (k.equals(canonicalSmilesField)) {
                            continue;
                        } else if (appendFields.contains(k)) {
                            List l = new ArrayList();
                            l.add(e.getValue());
                            first.putValue(k, l);
                        } else if (keepFirstFields.contains(k) || keepLastFields.contains(k)) {
                            first.putValue(k, e.getValue());
                        }
                    }
                    results.put(smiles, first);
                }
            }
        });

        return results.values().stream();
    }

    /** Read teh dataset from these sources
     *
     * @param dataIn Input stream for the Dataset's data
     * @param metaIn Input stream for the Dataset's metadata
     * @return
     * @throws IOException
     */
    public static Dataset<MoleculeObject> readDataset(InputStream dataIn, InputStream metaIn) throws IOException {
        DatasetMetadata meta = JsonHandler.getInstance().objectFromJson(metaIn, DatasetMetadata.class);
        return new Dataset(MoleculeObject.class, IOUtils.getGunzippedInputStream(dataIn), meta);
    }

    public static Dataset<MoleculeObject> readDatasetFromFiles(File dataFile, File metaFile) throws IOException {
        if (!dataFile.exists()) {
            throw new IOException("File " + dataFile.getPath() + " does not exist");
        }
        if (!metaFile.exists()) {
            throw new IOException("File " + metaFile.getPath() + " does not exist");
        }
        InputStream dataIn = new FileInputStream(dataFile);
        if (dataFile.getName().endsWith(".gz")) {
            dataIn = new GZIPInputStream(dataIn);
        }
        InputStream metaIn = new FileInputStream(metaFile);
        return readDataset(dataIn, metaIn);
    }

    public static Dataset<MoleculeObject> readDatasetFromFiles(String dataFile, String metaFile) throws IOException {
        return readDatasetFromFiles(new File(dataFile), new File(metaFile));
    }

    /** Convenience method to read data using the standard file name conventions. .data.gz is appended for the Dataset's
     * data file and .meta is appended for the DatasetMetadata
     *
     * @param basePath The full file path except for the .data.gz and .meta extensions
     * @return
     * @throws IOException
     */
    public static Dataset<MoleculeObject> readDatasetFromFiles(String basePath) throws IOException {
        return  readDatasetFromFiles(new File(basePath + ".data.gz"), new File(basePath+".meta"));
    }


    /** Write the dataset.
     *
     * @param dataset The dataset to write
     * @param data OutputStream for the data
     * @param meta OutputStream for the metadata
     * @param regenerateMetadata whether to regenerate the value class mappings for the metadata if the metadata already exists
     * @throws IOException
     */
    public static void writeDataset(Dataset<MoleculeObject> dataset, OutputStream data, OutputStream meta, boolean regenerateMetadata) throws IOException {
        DatasetMetadata md;
        try {
            if (regenerateMetadata || dataset.getMetadata() == null) {
                Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
                try (Stream s = generator.getAsStream()) {
                    JsonHandler.getInstance().marshalStreamToJsonArray(s, data);
                }
                md = generator.getDatasetMetadata();
            } else {
                try (Stream s = dataset.getStream()) {
                    JsonHandler.getInstance().marshalStreamToJsonArray(s, data);
                }
                md = dataset.getMetadata();
            }
            JsonHandler.getInstance().objectToOutputStream(md, meta);
        } finally {
            data.close();
            meta.close();
        }
    }

    /** Write to files containing the data and metadata.
     * Convenience method that uses the @{link #writeDataset} method to do the writing, but generates the OutputStreams as
     * is needed. The file are checked if they already exist to avoid being overwritten (IOException is thrown) and if the file
     * name of the data file ends in .gz then the results will be gzipped.
     *
     * @param dataset
     * @param dataFile
     * @param metaFile
     * @param regenerateMetadata
     * @throws IOException
     */
    public static void writeDatasetToFiles(Dataset<MoleculeObject> dataset, File dataFile, File metaFile, boolean regenerateMetadata) throws IOException {
        if (dataFile.exists()) {
            throw new IOException("File " + dataFile.getPath() + " already exists");
        }
        if (!dataFile.createNewFile()) {
            throw new IOException("File " + dataFile.getPath() + " cannot be created");
        }
        if (metaFile.exists()) {
            throw new IOException("File " + metaFile.getPath() + " already exists");
        }
        if (!metaFile.createNewFile()) {
            throw new IOException("File " + metaFile.getPath() + " cannot be created");
        }
        OutputStream dataOut = new FileOutputStream(dataFile);
        if (dataFile.getName().endsWith(".gz")) {
            dataOut = new GZIPOutputStream(dataOut);
        }
        OutputStream metaOut = new FileOutputStream(metaFile);
        writeDataset(dataset, dataOut, metaOut, regenerateMetadata);
    }

    /** Write to files containing the data and metadata.
     * Convenience method that uses the @{link #writeDatasetToFiles(Dataset, File, File, boolean)} method but allows the
     * file names to be specified as Strings.
     *
     * @param dataset
     * @param dataFilePath
     * @param metaFilePath
     * @param regenerateMetadata
     * @throws IOException
     */
    public static void writeDatasetToFiles(Dataset<MoleculeObject> dataset, String dataFilePath, String metaFilePath, boolean regenerateMetadata) throws IOException {
        writeDatasetToFiles(dataset, new File(dataFilePath), new File(metaFilePath), regenerateMetadata);
    }

    /** Convenience method to write data using the standard file name conventions. .data.gz is appended for the Dataset's
     * data file and .meta is appended for the DatasetMetadata
     *
     * @param basePath The full file path except for the .data.gz and .meta extensions
     * @return
     * @throws IOException
     */
    public static void writeDatasetToFiles(Dataset<MoleculeObject> dataset, String basePath, boolean regenerateMetadata) throws IOException {
        writeDatasetToFiles(dataset, new File(basePath+".data.gz"), new File(basePath+".meta"), regenerateMetadata);
    }

    /** Convenience method for processing a dataset of molecules.
     * Read from files specified by the input stem, processes the stream of molecules with the consumer and writes the
     * results to files specified by the output stem (generating the metadata)
     *
     * @param input The base path to use to read the data from files. See @{link #readDatasetFromFiles(String)}
     * @param output The base path to use to write the data to files. See @{link #writeDatasetToFiles(Dataset, String, boolean)}
     * @param consumer Consumer to use to process the stream of MoleculeObjects with the @{link java.util.stream.Stream.peek()} method
     * @throws IOException
     */
    public static void processDataset(String input, String output, Consumer consumer) throws IOException {
        Dataset dataset = readDatasetFromFiles(input);
        Stream<MoleculeObject> stream = dataset.getStream().peek(consumer);
        dataset.replaceStream(stream);
        writeDatasetToFiles(dataset, output, true);
    }

    public static void processDatasetStream(String input, String output, Function<Stream<MoleculeObject>, Stream<MoleculeObject>> function) throws IOException {
        Dataset dataset = readDatasetFromFiles(input);
        Stream<MoleculeObject> stream = function.apply(dataset.getStream());
        dataset.replaceStream(stream);
        writeDatasetToFiles(dataset, output, true);
    }

}
