package org.squonk.execution.variable.impl;


import org.squonk.api.VariableHandler;
import org.squonk.util.IOUtils;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by timbo on 13/03/16.
 */
public class FilesystemWriteContext extends AbstractFilesystemContext implements VariableHandler.WriteContext {


    public FilesystemWriteContext(File dir, String baseName) {
        super(dir, baseName);
    }

    @Override
    public void writeTextValue(String value, String key) throws IOException {
        File f = generateFile(key);
        try (FileWriter out = new FileWriter(f)) {
            out.append(value);
        }
    }

    @Override
    public void writeStreamValue(InputStream value, String key) throws Exception {
        File f = generateFile(key);
        boolean gzip = key.toLowerCase().endsWith(".gz");
        try (OutputStream out = gzip ? new GZIPOutputStream( new FileOutputStream(f)): new FileOutputStream(f)) {
            IOUtils.transfer(value, out, 4096);
        }
    }

    @Override
    public void deleteVariable() throws Exception {
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith(baseName)) {
                f.delete();
            }
        }
    }

}
