package org.squonk.execution.variable.impl;


import org.squonk.api.VariableHandler;
import org.squonk.util.IOUtils;

import java.io.*;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * Created by timbo on 13/03/16.
 */
public class FilesystemWriteContext extends AbstractFilesystemContext implements VariableHandler.WriteContext {


    private static final Logger LOG = Logger.getLogger(FilesystemWriteContext.class.getName());

    public FilesystemWriteContext(File dir, String baseName) {
        super(dir, baseName);
    }

    @Override
    public void writeTextValue(String value, String key) throws IOException {
        LOG.info("Writing text to file using key " + key);
        File f = generateFile(key);
        try (FileWriter out = new FileWriter(f)) {
            out.append(value);
        }
    }

    @Override
    public void writeStreamValue(InputStream value, String key) throws Exception {
        LOG.info("Writing stream to file using key " + key);
        File f = generateFile(key);
        boolean gzip = key != null && key.toLowerCase().endsWith(".gz");
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
