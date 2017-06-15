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

package org.squonk.execution.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.DockerCmdExecFactory
import com.github.dockerjava.api.command.ExecCreateCmdResponse
import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.command.ExecStartResultCallback
import com.github.dockerjava.netty.NettyDockerCmdExecFactory
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * Created by timbo on 03/07/16.
 */
class DockerRunnerSpec extends Specification {

    void "clean workdir"() {

        setup:
        DockerRunner runner = new DockerRunner("busybox", null, "/source")
        runner.init()

        when:
        runner.cleanup()

        then:
        !runner.getHostWorkDir().exists()
    }

    void "simple execute"() {

        setup:
        DockerRunner runner = new DockerRunner("busybox", null, "/source")
        runner.init()

        when:
        runner.writeInput("run.sh", "touch /source/IWasHere\n")
        runner.execute("/bin/sh", "/source/run.sh")

        then:
        new File(runner.getHostWorkDir(), 'IWasHere').exists()

        cleanup:
        runner.cleanup();
    }

//    void "start and stop"() {
//
//        setup:
//        DockerRunner runner = new DockerRunner("busybox", TMP_DIR, "/source")
//        runner.init()
//        Thread.start {
//            println "starting container"
//            runner.execute("sleep", "10")
//            println "execution complete"
//        }
//        sleep(1500)
//
//        when:
//
//        println "stopping container"
//        runner.stop()
//        println "container stopped"
//
//        then:
//        !runner.isRunning()
//
//        cleanup:
//        runner.cleanup();
//    }

    void "basic bridge network"() {

        setup:
        def config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withApiVersion("1.23")
                .build();
        def dockerClient = DockerClientBuilder.getInstance(config).build();
        CreateContainerResponse container = dockerClient.createContainerCmd("busybox")
                .withCmd("sleep", "5")
                .withNetworkMode("bridge")
                .exec()
        dockerClient.startContainerCmd(container.getId()).exec()

        when:
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
        def networksMap = inspectContainerResponse.getNetworkSettings().getNetworks()

        then:
        networksMap.get("bridge") != null
        networksMap.size() == 1

        cleanup:
        dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
    }

//    void "with bridge network"() {
//
//        setup:
//        DockerRunner runner = new DockerRunner("busybox", TMP_DIR, "/source")
//            .withNetwork("bridge")
//        runner.init()
//
//
//        Thread.start {
//            println "starting container"
//            runner.execute("sleep", "10")
//            println "execution complete"
//        }
//        println "started container"
//        sleep(1000)
//        println "inspecting"
//        InspectContainerResponse inspectContainerResponse = runner.inspectContainer();
//        println "inspected"
//
//        when:
//        def networksMap = inspectContainerResponse.getNetworkSettings().getNetworks()
//
//        then:
//        runner.isRunning()
//        networksMap.getServiceDescriptors("bridge") != null
//        networksMap.size() == 1
//
//        cleanup:
//        runner.cleanup();
//
//    }


    @IgnoreIf({ !os.linux })
    void "simple input via stdin"() {

        setup:
        def config = DefaultDockerClientConfig.createDefaultConfigBuilder() .build();

        NettyDockerCmdExecFactory dockerCmdExecFactory = new NettyDockerCmdExecFactory()
                //.withReadTimeout(1000)
                .withConnectTimeout(1000)
                //.withMaxTotalConnections(100)
                //.withMaxPerRouteConnections(10)

        DockerClient dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();

        CreateContainerResponse container = dockerClient.createContainerCmd("busybox")
                .withCmd("sleep", "99")
                .exec()
        println "Created container $container"
        InputStream stdin = new ByteArrayInputStream("STDIN\n".getBytes("UTF-8"));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();

        when:
        dockerClient.startContainerCmd(container.getId()).exec()

        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(container.getId())
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withCmd("cat")
                .exec();
        println "response $execCreateCmdResponse"

        boolean completed = dockerClient.execStartCmd(execCreateCmdResponse.getId())
                .withDetach(false)
                .withTty(true)
                .withStdIn(stdin)
                .exec(new ExecStartResultCallback(stdout, System.err))
                .awaitCompletion(5, TimeUnit.SECONDS);

        def result = stdout.toString("UTF-8")
        //println result


        then:
        completed
        result == "STDIN\n"

    }

}
