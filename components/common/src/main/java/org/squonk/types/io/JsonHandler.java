/*
 * Copyright (c) 2019 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.types.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.util.IOUtils;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;

/**
 * @author timbo
 */
public class JsonHandler {

    private static final Logger LOG = Logger.getLogger(JsonHandler.class.getName());

    public static final String ATTR_DATASET_METADATA = "DatasetMetadata";
    public static final String ATTR_VALUE_MAPPINGS = "ValueMappings";

    private final ObjectMapper mapper;

    private static final JsonHandler instance = new JsonHandler();

    public JsonHandler() {

        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        // TODO - better way to register custom deserializers ?
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer());
        module.addSerializer(MoleculeObject.class, new MoleculeObjectJsonSerializer());
        module.addDeserializer(BasicObject.class, new BasicObjectJsonDeserializer());
        module.addSerializer(BasicObject.class, new BasicObjectJsonSerializer());
        module.addDeserializer(Color.class, new ColorJsonDeserializer());
        module.addSerializer(Color.class, new ColorJsonSerializer());
        mapper.registerModule(module);
    }

    public static JsonHandler getInstance() {
        return instance;
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    public String objectToJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public byte[] objectToBytes(Object o) throws JsonProcessingException {
        return mapper.writeValueAsBytes(o);
    }

    public void objectToFile(Object o, File f) throws JsonProcessingException, IOException {
        mapper.writeValue(f, o);
    }

    public void objectToOutputStream(Object o, OutputStream out) throws JsonProcessingException, IOException {
        mapper.writeValue(out, o);
    }

    public <T> T objectFromJson(InputStream is, Class<T> type) throws IOException {
        return mapper.readValue(is, type);
    }


    public <T> T objectFromJson(String s, Class<T> type) throws IOException {
        return mapper.readValue(s, type);
    }

    public <T> T objectFromJson(InputStream is, TypeReference<T> type) throws IOException {
        return mapper.readValue(is, type);
    }

    public <T> T objectFromJson(String s, TypeReference<T> type) throws IOException {
        return mapper.readValue(s, type);
    }

    public <T> Iterator<T> iteratorFromJson(String s, Class<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        return reader.readValues(s);
    }

    public <T> Iterator<T> iteratorFromJson(String s, TypeReference<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        return reader.readValues(s);
    }

    public <T> Iterator<T> iteratorFromJson(InputStream is, Class<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        return reader.readValues(is);
    }

    public <T> Iterator<T> iteratorFromJson(InputStream is, TypeReference<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        return reader.readValues(is);
    }

    public <T> Stream<T> streamFromJson(String json, Class type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        Iterator<T> iter = reader.readValues(json);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, true);
    }

    public <T> Stream<T> streamFromJson(String json, TypeReference<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        Iterator<T> iter = reader.readValues(json);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, true);
    }

    public <T> Stream<T> streamFromJson(InputStream is, Class<T> type, boolean autoClose) throws IOException {
        return streamFromJson(is, type, Collections.emptyMap(), autoClose);
    }

    public <T> Stream<T> streamFromJson(InputStream is, TypeReference<T> type, boolean autoClose) throws IOException {
        return streamFromJson(is, type, Collections.emptyMap(), autoClose);
    }

    /**
     * Generate a Stream of objects of the specified type. The input stream must
     * contain JSON containing objects of just that type.
     *
     * @param <T>
     * @param is
     * @param type
     * @param mappings
     * @param autoClose
     * @return
     * @throws IOException
     */
    public <T> Stream<T> streamFromJson(final InputStream is, final Class<T> type, Map<String, Class> mappings, final boolean autoClose) throws IOException {
        return streamFromJson(is, mapper.constructType(type), mappings, autoClose);
    }

    public <T> Stream<T> streamFromJson(final InputStream is, final TypeReference<T> type, Map<String, Class> mappings, final boolean autoClose) throws IOException {
        return streamFromJson(is, mapper.constructType(type.getType()), mappings, autoClose);
    }

    public <T> Stream<T> streamFromJson(final InputStream is, final JavaType type, Map<String, Class> mappings, final boolean autoClose) throws IOException {

        ObjectReader reader = mapper.readerFor(type);
        if (mappings != null && !mappings.isEmpty()) {
            reader = reader.withAttribute(ATTR_VALUE_MAPPINGS, mappings);
        }
        Iterator<T> iter = reader.readValues(is);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        Stream<T> stream = StreamSupport.stream(spliterator, true);
        if (autoClose) {
            return stream.onClose(() -> IOUtils.close(is));
        } else {
            return stream;
        }
    }

    public <T> InputStream marshalStreamToJsonArray(Stream<T> stream, boolean gzip) throws IOException {
        final PipedInputStream pis = new PipedInputStream();
        final OutputStream pout = new PipedOutputStream(pis);
        final OutputStream out = (gzip ? new GZIPOutputStream(pout, true) : pout);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> c = new Callable() {
            public Boolean call() throws Exception {
                marshalStreamToJsonArray(stream, out);
                return true;
            }
        };
        Future<Boolean> future = executor.submit(c);
        executor.shutdown();
        return pis;
    }

    /** Holder class that allows InputStream and Future to be returned. In the case of an error writing to the InputStream
     * the Future.get() method will throw an ExecutionException with the thrown exception. Note that this will only happen
     * after the InputStream is read so some rollback may be required
     *
     */
    public class MarshalData {
        private final InputStream inputStream;
        private final Future<Boolean> future;

        MarshalData(InputStream inputStream, Future<Boolean> future) {
            this.inputStream = inputStream;
            this.future = future;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public Future<Boolean> getFuture() {
            return future;
        }
    }

    public <T> MarshalData marshalData(Stream<T> stream, boolean gzip) throws IOException {
        final PipedInputStream pis = new PipedInputStream();
        final OutputStream pout = new PipedOutputStream(pis);
        final OutputStream out = (gzip ? new GZIPOutputStream(pout, true) : pout);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> c = new Callable() {
            public Boolean call() throws Exception {
                marshalStreamToJsonArray(stream, out);
                return true;
            }
        };
        Future<Boolean> future = executor.submit(c);
        executor.shutdown();

        return new MarshalData(pis, future);
    }

    public <T> void marshalStreamToJsonArray(Stream<T> stream, OutputStream out) throws IOException {

        ObjectWriter ow = mapper.writer();
        try (SequenceWriter sw = ow.writeValuesAsArray(out)) {

            stream.forEachOrdered((i) -> {
                //LOG.info("Writing to json: "  + i);
                try {
                    sw.write(i);
                    sw.flush();
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write object: " + i, ex);
                }
            });

        } finally {
            out.close();
            stream.close();
        }
    }

    /**
     * Use the metadata to deserialize the JSON in the InputStream to a Dataset
     * of the right type.
     *
     * @param <T>      The type of objects in the Dataset
     * @param metadata The metadata describing the Dataset
     * @param json     The JSON
     * @return
     * @throws IOException
     */
    public <T extends BasicObject> Dataset<T> unmarshalDataset(DatasetMetadata<T> metadata, InputStream json) throws IOException {
        ObjectReader reader = mapper.readerFor(metadata.getType()).with(ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_DATASET_METADATA, metadata));
        MappingIterator iter = reader.readValues(json);
        return new Dataset<>(metadata.getType(), iter, metadata);
    }

    public <T extends BasicObject> Dataset<T> unmarshalDataset(DatasetMetadata<T> metadata, String json) throws IOException {
        ObjectReader reader = mapper.readerFor(metadata.getType()).with(ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_DATASET_METADATA, metadata));
        MappingIterator iter = reader.readValues(json);
        return new Dataset<>(metadata.getType(), iter, metadata);
    }

    public static String getJsonSchemaAsString(Class clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        JsonSchema schema = schemaGen.generateSchema(clazz);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }

}
