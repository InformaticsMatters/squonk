package org.squonk.execution.runners;

import java.io.File;

public interface ServiceRunner {

    /** Get the working dir
     *
     * @return
     */
    File getHostWorkDir();

    /**
     * Cleans up the running container. This method blocks
     * until the action is complete. After this call the running container
     * will have been removed from the container eco-system and can no-longer
     * be used.
     */
    void cleanup();
}
