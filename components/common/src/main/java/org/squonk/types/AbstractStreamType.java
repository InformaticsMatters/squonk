/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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

package org.squonk.types;

import org.squonk.io.SquonkDataSource;
import org.squonk.io.FileDataSource;
import org.squonk.io.InputStreamDataSource;
import org.squonk.util.IOUtils;

import javax.activation.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/** Wrapper around data from a file to allow strong typing and type conversion
 *
 *
 * @author timbo
 */
public abstract class AbstractStreamType implements StreamType {

    private final SquonkDataSource[] dataSources;
    private final String mediaType;

    public AbstractStreamType(InputStream inputStream, String mediaType, Boolean gzipped) {
        this.dataSources = new SquonkDataSource[] {new InputStreamDataSource(SquonkDataSource.ROLE_DEFAULT, null, mediaType, inputStream, gzipped)};
        this.mediaType = mediaType;
    }

    public AbstractStreamType(File file, String mediaType, Boolean gzipped) {
        this.dataSources = new SquonkDataSource[] {new FileDataSource(SquonkDataSource.ROLE_DEFAULT, mediaType, file, gzipped)};
        this.mediaType = mediaType;
    }

    public AbstractStreamType(InputStream[] inputStreams, String mediaType, String[] roles, String[] names, String[] streamMediaTypes, Boolean[] gzipped) {
        assert inputStreams.length == names.length;
        assert inputStreams.length == streamMediaTypes.length;
        assert inputStreams.length == gzipped.length;
        this.mediaType = mediaType;
        dataSources = new SquonkDataSource[inputStreams.length];
        for (int i=0; i<inputStreams.length; i++) {
            dataSources[i] = new InputStreamDataSource(roles[i], names[i], streamMediaTypes[i], inputStreams[i], gzipped[i]);
        }
    }

    public AbstractStreamType(File[] files, String mediaType, String[] roles, String[] streamMediaTypes, Boolean[] gzipped) {
        assert files.length == roles.length;
        assert files.length == streamMediaTypes.length;
        assert files.length == gzipped.length;

        this.mediaType = mediaType;
        dataSources = new SquonkDataSource[files.length];
        for (int i=0; i<dataSources.length; i++) {
            dataSources[i] = new FileDataSource(roles[i], streamMediaTypes[i], files[i], gzipped[i]);
        }
    }

    public AbstractStreamType(SquonkDataSource dataSource, String mediaType) {
        this.dataSources = new SquonkDataSource[] {dataSource};
        this.mediaType = mediaType;
    }

    public AbstractStreamType(SquonkDataSource[] dataSources, String mediaType) {
        this.dataSources = dataSources;
        this.mediaType = mediaType;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public String[] getStreamMediaTypes() {
        String[] types =  new String[dataSources.length];
        for (int i=0; i<types.length; i++) {
            types[i] = dataSources[i].getContentType();
        }
        return types;
    }

    /** Get all the data as it was generated, which might or might not be gzipped.
     *
     * @return
     */
    @Override
    public InputStream[] getInputStreams() throws IOException {
        InputStream[] inputStreams = new InputStream[dataSources.length];
        for (int i=0; i<inputStreams.length; i++) {
            inputStreams[i] = dataSources[i].getInputStream();
        }
        return inputStreams;
    }

    @Override
    public SquonkDataSource[] getDataSources() throws IOException {
        return dataSources;
    }

    @Override
    public String[] getStreamNames() {
        String[] names = new String[dataSources.length];
        for (int i=0; i<names.length; i++) {
            names[i] = dataSources[i].getName();
        }
        return names;
    }


    /** Get the data as it was generated, which might or might not be gzipped.
     * This assumes the type has only a single stream which may not be the case for some types.
     *
     * @return
     */
    public InputStream getInputStream() throws IOException {
        if (dataSources != null && dataSources.length > 0) {
            return getInputStreams()[0];
        } else {
            return null;
        }
    }

    /** Get the Gzipped compressed data for this object.
     *
     * @return
     * @throws IOException
     */
    public InputStream getGzippedInputStream() throws IOException {
        if (dataSources == null) {
            return null;
        }
        InputStream is = getInputStream();
        return is == null ? null : IOUtils.getGzippedInputStream(is);
    }

    /** Get the data in uncompressed format
     *
     * @return
     * @throws IOException
     */
    public InputStream getGunzipedInputStream() throws IOException {
        if (dataSources == null) {
            return null;
        }
        return IOUtils.getGunzippedInputStream(getInputStream());
    }

    /** Get the bytes of the uncompressed data.
     *
     *
     * @return
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        if (dataSources == null) {
            return null;
        }
        InputStream is = getGunzipedInputStream();
        if (is == null) {
            return null;
        }
        byte[] bytes = IOUtils.convertStreamToBytes(is);
        return bytes;
    }

    /** Replace the source InputStream with a ByteArrayInputStream for the bytes so that the data can be read repeatedly.
     * WARNING if the data is very large this will result in OutOfMemory errors so only call this method if you are sure
     * that the data is modest in size.
     *
     * @throws IOException
     */
    public void materialize() throws IOException {
        if (dataSources == null) {
            return;
        }
        synchronized (dataSources) {
            for (SquonkDataSource ds: dataSources) {
                ds.materialize();
            }
        }
    }

}
