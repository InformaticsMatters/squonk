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
public abstract class AbstractStreamType {

    private InputStream inputStream;


    public AbstractStreamType(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /** Get the data as it was generated, which might or might not be gzipped
     *
     * @return
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /** Get the Gzipped compressed data for this object.
     *
     * @return
     * @throws IOException
     */
    public InputStream getGzippedInputStream() throws IOException {
        if (inputStream == null) {
            return null;
        }
        return inputStream == null ? null : IOUtils.getGzippedInputStream(inputStream);
    }

    /** Get the data in uncompressed format
     *
     * @return
     * @throws IOException
     */
    public InputStream getGunzipedInputStream() throws IOException {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof GZIPInputStream) {
            return inputStream;
        } else {
            return inputStream == null ? null : IOUtils.getGunzippedInputStream(inputStream);
        }
    }

    /** Get the bytes of the uncompressed data.
     *
     *
     * @return
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        if (inputStream == null) {
            return null;
        }
        byte[] bytes = IOUtils.convertStreamToBytes(getGunzipedInputStream());
        return bytes;
    }

    /** Replace the source InputStream with a ByteArrayInputStream for the bytes so that the data can be read repeatedly.
     * WARNING if the data is very large this will result in OutOfMemory errors so only call this method if you are sure
     * that the data is modest in size.
     *
     * @throws IOException
     */
    public void materialize() throws IOException {
        if (inputStream == null || inputStream instanceof ByteArrayInputStream) {
            return;
        }
        byte[] bytes = IOUtils.convertStreamToBytes(inputStream);
        inputStream = new ByteArrayInputStream(bytes);
    }

}
