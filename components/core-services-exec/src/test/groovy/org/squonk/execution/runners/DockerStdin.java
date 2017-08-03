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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class DockerStdin {

    public static void main(String[] args) throws Exception {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder() .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        CreateContainerResponse container = dockerClient.createContainerCmd("busybox")
                .withCmd("sleep", "99")
                .exec();
        System.out.println("Created container " + container.getId());
        InputStream stdin = new ByteArrayInputStream("STDIN\n".getBytes("UTF-8"));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();

        dockerClient.startContainerCmd(container.getId()).exec();

        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(container.getId())
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withCmd("cat")
                .exec();

        boolean completed = dockerClient.execStartCmd(execCreateCmdResponse.getId())
                .withDetach(false)
                .withTty(true)
                .withStdIn(stdin)
                .exec(new ExecStartResultCallback(stdout, System.err))
                .awaitCompletion(5, TimeUnit.SECONDS);

        System.out.println("Completed = " + completed);
        System.out.println("output = " + stdout.toString("UTF-8"));
    }
}
