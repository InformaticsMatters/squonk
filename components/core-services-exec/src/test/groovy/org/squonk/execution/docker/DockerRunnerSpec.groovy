package org.squonk.execution.docker

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import spock.lang.Specification

/**
 * Created by timbo on 03/07/16.
 */
class DockerRunnerSpec extends Specification {

    static String TMP_DIR = System.getProperty("java.io.tmpdir")

    void "clean workdir"() {

        setup:
        DockerRunner runner = new DockerRunner("busybox", TMP_DIR, "/source")
        runner.init()

        when:
        runner.cleanup()

        then:
        !runner.getHostWorkDir().exists()
    }

    void "simple execute"() {

        setup:
        DockerRunner runner = new DockerRunner("busybox", TMP_DIR, "/source")
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
//        networksMap.get("bridge") != null
//        networksMap.size() == 1
//
//        cleanup:
//        runner.cleanup();
//
//    }

}
