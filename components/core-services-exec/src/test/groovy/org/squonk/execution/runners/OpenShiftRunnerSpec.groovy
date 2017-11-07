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

package org.squonk.execution.runners

import spock.lang.Specification

/**
 * OpenShiftRunner unit tests.
 *
 * Created by Alan Christie on 07/11/17.
 */
class OpenShiftRunnerSpec extends Specification {

    def hostBaseWorkDir = null

    void "construction"() {

        when:
        OpenShiftRunner runner = new OpenShiftRunner("busybox", hostBaseWorkDir, "/source", "123")

        then:
        runner.currentStatus == AbstractRunner.RUNNER_CREATED;
        runner.getLocalWorkDir() == "/source"

        cleanup:
        runner.cleanup();

    }

    void "make workdir"() {

        setup:
        OpenShiftRunner runner = new OpenShiftRunner("busybox", hostBaseWorkDir, "/source", "123")

        when:
        runner.init()

        then:
        runner.getHostWorkDir().exists()

        cleanup:
        runner.cleanup();

    }

    void "clean workdir"() {

        setup:
        ContainerRunner runner = new OpenShiftRunner("busybox", hostBaseWorkDir, "/source", "123")
        runner.init()

        when:
        runner.cleanup()

        then:
        !runner.getHostWorkDir().exists()

    }

    void "execute before init"() {

        setup:
        ContainerRunner runner = new OpenShiftRunner("busybox", hostBaseWorkDir, "/source", "123")

        when:
        runner.execute("/bin/sh", "/source/run.sh")

        then:
        IllegalStateException e = thrown()
        e.message.contains("execute() with bad isRunning state")
        runner.currentStatus == AbstractRunner.RUNNER_CREATED

        cleanup:
        runner.cleanup();

    }

    void "simple execute outside openshift"() {

        setup:
        ContainerRunner runner = new OpenShiftRunner("busybox", hostBaseWorkDir, "/source", "123")
        runner.init()

        when:
        boolean minishift = minishiftIsRunning()

        then: 'Minishift must not be running for this test'
        !minishift

        when:
        runner.writeInput("run.sh", "touch /source/IWasHere\n")
        runner.execute("/bin/sh", "/source/run.sh")

        then:
        IllegalStateException e = thrown()
        e.message.startsWith("Exception creating ")
        runner.currentStatus == AbstractRunner.RUNNER_FINISHED
        !runner.getHostWorkDir().exists()

        cleanup:
        runner.cleanup();

    }

    boolean minishiftIsRunning() {

        def proc = 'minishift status'.execute()
        proc.waitForOrKill(500)
        def matcher = (proc.text =~ /(?m)Running/)
        return matcher.count > 0

    }

}
