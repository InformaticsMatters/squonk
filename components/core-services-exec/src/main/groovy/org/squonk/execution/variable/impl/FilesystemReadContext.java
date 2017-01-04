package org.squonk.execution.variable.impl;

import org.squonk.api.VariableHandler;
import org.squonk.client.VariableClient;
import org.squonk.util.IOUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by timbo on 13/03/16.
 */
public class FilesystemReadContext extends AbstractFilesystemContext implements VariableHandler.ReadContext {


    public FilesystemReadContext(File dir, String baseName) {
        super(dir, baseName);
    }

    @Override
    public String readTextValue(String key) throws IOException {
        File f = generateFile(key);
        if (f.exists()) {
            StringBuilder b = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                reader.lines().forEach(l -> b.append(l));
            }
            return b.toString();
        } else {
            return null;
        }
    }

    @Override
    public String readSingleTextValue(String key) throws Exception {
        return readTextValue(key);
    }

    @Override
    public InputStream readStreamValue(String key) throws Exception {
        File f = generateFile(key);
        boolean gzip = key.toLowerCase().endsWith(".gz");
        if (f.exists()) {
            return gzip ? new GZIPInputStream(new FileInputStream(f)) : new FileInputStream(f);
        } else {
            return null;
        }
    }

    @Override
    public InputStream readSingleStreamValue(String key) throws Exception {
        return readStreamValue(key);
    }
}
