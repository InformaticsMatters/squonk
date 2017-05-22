package org.squonk.execution.variable.impl;

import java.io.File;

/**
 * Created by timbo on 01/01/17.
 */
public abstract class AbstractFilesystemContext {

    protected final File dir;
    protected final String baseName;

    protected AbstractFilesystemContext(File dir, String baseName) {
        this.dir = dir;
        this.baseName = baseName;
    }

    protected File generateFile(String key) {
        if (key == null) {
            return new File(dir, baseName);
        } else {
            return new File(dir, baseName + "." + key);
        }
    }

}
