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

package org.squonk.execution.runners;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * All container runner classes must implement the ContainerRunner interface
 * or throw an UnsupportedExc exception.
 */
public interface ContainerRunner {

    File getHostWorkDir();

    long writeInput(String fileName, String content, boolean executable)
            throws IOException;

    InputStream readOutput(String filename)
            throws IOException;

    long writeInput(String filename, InputStream content)
            throws IOException;

    long writeInput(String filename, String content)
            throws IOException;

    /**
     * Execute a command in the container. This method can only be called once
     * for each containerRunner instance.
     * <p/>
     * The method runs to completion, returning the container execution
     * state (exit code).
     *
     * @param cmd The command
     * @return Container execution result. Non-zero on failure.
     */
    int execute(String... cmd);

    /**
     * Returns the container's log content.
     *
     * @return The container log
     */
    String getLog();

    /**
     * Cleans up the running container. This method blocks
     * until the action is complete. After this call the running container
     * will have been removed from the container eco-system and can no-longer
     * be used.
     */
    void cleanup();

    Properties getFileAsProperties(String file)
            throws IOException;

    // ------------------------------------------------------------------------
    // The following legacy methods, are currently only
    // supported by the DockerRunner

    /**
     * Adds a Volume to the (Docker) container.
     *
     * @param mountAs The path
     * @return The mounted Volume
     *
     * @see #addBind(String, Volume, AccessMode)
     */
    @Deprecated
    Volume addVolume(String mountAs);

    /**
     * Represents a host path being bind mounted as a Volume in a Docker
     * container. The Bind can be in read only or read write access mode.
     *
     * @param hostDir The path
     * @param volume The Volume
     * @param mode The access mode
     * @return The mounted Volume
     *
     * @see #addVolume(String)
     */
    @Deprecated
    Bind addBind(String hostDir, Volume volume, AccessMode mode);

}
