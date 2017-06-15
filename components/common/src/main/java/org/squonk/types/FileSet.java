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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/** Represents contents of one or more files that can be saved as a JSON object and then written to files.
 * The content is stored internally as gzipped bytes, so do not use this for very large files (a few MB should
 * be fine).
 * When writing to file a parent directory is specified and you can choose whether to gunzip the contents. If not
 * gunzipped the specified filename has .gz appended to it.
 *
 *
 * Created by timbo on 15/06/17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FileSet implements Serializable {

    private final List<FileObject> files = new ArrayList<>();

    public FileSet(@JsonProperty("files") List<FileObject> files) {
        if (files != null && !files.isEmpty()) {
            this.files.addAll(files);
        }
    }

    public FileSet() {

    }

    public List<FileObject> getFiles() {
        return files;
    }

    /** Convenience creator for using a byte array. Note, it is more efficient to use the InputStream version.
     *
     * @param id
     * @param filename
     * @param content
     * @return
     * @throws IOException
     */
    public FileObject addFile(String id, String filename, byte[] content) throws IOException{
        return addFile(id, filename, new ByteArrayInputStream(content));
    }

    /** Add a new FileObject with. The contents are gzipped if not already gzipped.
     * When adding multiple files you are responsible for ensuring that the ids and file names are unique
     *
     * @param id An ID for the file
     * @param filename The filename used to write the file. Do not add a .gz suffix.
     * @param content What will become the contents of the file.
     * @return The created FileObject which will already have been added to the files List
     * @throws IOException
     */
    public FileObject addFile(String id, String filename, InputStream content) throws IOException{
        FileObject fo = new FileObject(id, filename, IOUtils.convertStreamToBytes(IOUtils.getGzippedInputStream(content)));
        files.add(fo);
        return fo;
    }

    /** Convenience method for creating an FileObject from a File.
     *
     * @param id FileObject ID
     * @param dir Parent dir containing the file
     * @param filename Name of the file
     * @param optional If true does not throw an error if the file does not exist, but no FileObject is added and
     *                 returns null
     * @return
     * @throws IOException
     */
    public FileObject readFile(String id, java.io.File dir, String filename, boolean optional) throws IOException {
        java.io.File f = new java.io.File(dir, filename);
        if (!f.exists()) {
            if (optional) {
                return null;
            } else {
                throw new IOException("File not found: " + filename);
            }
        }
        return addFile(id, filename, new FileInputStream(f));
    }

    public static class FileObject {

        private final String id;
        private final String filename;
        private final byte[] content;

        private FileObject(
                @JsonProperty("id") String id,
                @JsonProperty("filename") String filename,
                @JsonProperty("content") byte[] content) {
            this.id = id;
            this.filename = filename;
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public String getFilename() {
            return filename;
        }

        public byte[] getContent() {
            return content;
        }

        /** Write this FileObject to the specified directory. If you specify not to gunzip the contents then the filename
         * used has .gz appended to it.
         *
         * @param dir The parent directory
         * @param gunzip Whether to gunzip the contents
         * @throws IOException
         */
        public void write(java.io.File dir, boolean gunzip) throws IOException {
            java.io.File f = new java.io.File(dir, gunzip ? filename : filename + ".gz" );
            OutputStream out = new FileOutputStream(f);
            InputStream in = new ByteArrayInputStream(content);
            IOUtils.transfer(gunzip ? IOUtils.getGunzippedInputStream(in) : in, out, 4048);
            out.flush();
            out.close();
        }
    }
}
