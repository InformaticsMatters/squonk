package org.squonk.cpsign

import org.squonk.util.IOUtils

/**
 * Created by timbo on 21/10/2016.
 */
class CPSignConfig {

    static final File workDir = new File(IOUtils.getConfiguration("CPSIGN_MODEL_DIR", null))
    static final File license = new File(IOUtils.getConfiguration("CPSIGN_LICENSE_URL", null))
}
