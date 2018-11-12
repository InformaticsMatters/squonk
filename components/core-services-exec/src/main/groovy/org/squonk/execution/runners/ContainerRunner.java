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
public interface ContainerRunner extends ServiceRunner {

    File getHostWorkDir();

    String getLocalWorkDir();

    long writeInput(String fileName, String content, boolean executable)
            throws IOException;

    InputStream readOutput(String filename)
            throws IOException;

    long writeInput(String filename, InputStream content)
            throws IOException;

    long writeInput(String filename, String content)
            throws IOException;

    /**
     * Given a configuration String this method returns that string plus any
     * extra configuration required by a Nextflow process.
     * <p/>
     * If the runner has extra configuration items required for Nexdtfloe
     * (typically something like the PVC mount name) then this material is
     * appended to the supplied string and returned.
     * <p/>
     * The string mignt not be modified.
     *
     * @param originalConfig The original Nextflow configuration.
     * @return The originalConfig with extra Nextflow configuration appended.
     *         The string may be returned unmodified. If the originalConfig
     *         string is null, null is returned.
     */
    String addExtraNextflowConfig(String originalConfig);

    /**
     * Execute a command in the container. This method can only be called once
     * for each containerRunner instance. Prior to calling this method
     * users must have called the object's init() method.
     * <p/>
     * The method runs to completion, returning the container execution
     * state (exit code).
     *
     * @param cmd The command
     * @return Container execution result. Non-zero on failure.
     *
     * @see #init()
     */
    int execute(String... cmd);

    /**
     * A pre-execute initialisation stage. Runners must expect this call prior
     * to execute(). Here they can do any pre-execution actions that are
     * required.
     */
    void init()
        throws IOException;

    /**
     * Returns the container's log content.
     *
     * @return The container log
     */
    String getLog();

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
