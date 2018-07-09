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

import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/** Wrapper around data from a file to allow strong typing and type conversion
 *
 *
 * @author timbo
 */
public abstract class AbstractStreamType implements StreamType {

    private final InputStream[] inputStreams;
    private final String[] names;


    public AbstractStreamType(InputStream inputStream) {
        this.inputStreams = new InputStream[] {inputStream};
        this.names = new String[] {"data"};
    }

    public AbstractStreamType(InputStream[] inputStreams, String[] names) {
        this.inputStreams = inputStreams;
        this.names = names;
    }

    /** Get all the data as it was generated, which might or might not be gzipped.
     *
     * @return
     */
    public InputStream[] getInputStreams() {
        return inputStreams;
    }

    public String[] getStreamNames() {
        return names;
    }


    /** Get the data as it was generated, which might or might not be gzipped.
     * This assumes the type has only a single stream which may not be the case for some types.
     *
     * @return
     */
    public InputStream getInputStream() {
        if (inputStreams != null && inputStreams.length > 0) {
            return inputStreams[0];
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
        if (inputStreams == null) {
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
        if (inputStreams == null) {
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
        if (inputStreams == null) {
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
        if (inputStreams == null) {
            return;
        }
        synchronized (inputStreams) {
            InputStream[] results = new InputStream[inputStreams.length];
            for (int i = 0; i < inputStreams.length; i++) {
                InputStream is = inputStreams[i];
                if (is == null) {
                    results[i] = null;
                } else if (is instanceof ByteArrayInputStream) {
                    results[i] = is;
                } else {
                    byte[] bytes = IOUtils.convertStreamToBytes(is);
                    results[i] = new ByteArrayInputStream(bytes);
                }
            }
            for (int i = 0; i < inputStreams.length; i++) {
                inputStreams[i] = results[i];
            }
        }

    }

}
