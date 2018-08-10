/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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
package org.squonk.execution

import org.squonk.core.DockerServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.io.IODescriptor
import org.squonk.jobdef.JobStatus
import org.squonk.reader.SDFReader
import org.squonk.types.MoleculeObject
import org.squonk.types.SDFile
import org.squonk.types.io.JsonHandler
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

class JobManagerSpec extends Specification {

    static String USER = 'nobody'

    private DockerServiceDescriptor createSdfServiceDescriptor() {
        // create the service descriptor
        def inputiods = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        def outputiods = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]


        return new DockerServiceDescriptor(
                "sdf",
                "sdf execute",
                "",
                null,
                null,
                null,
                null,
                null,
                inputiods,
                null, //IORoute[] inputRoutes,
                outputiods,
                null, //IORoute[] outputRoutes,
                null, //OptionDescriptor[] optionDescriptors,
                null, //ThinDescriptor[] thinDescriptors,
                // these are specific to docker execution
                null, //String executorClassName,
                "busybox",
                "cp input.sdf.gz output.sdf.gz",
                null)
    }

    private DockerServiceDescriptor createDatasetServiceDescriptor() {
        // create the service descriptor
        def inputiods = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)] as IODescriptor[]
        def outputiods = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)] as IODescriptor[]

        return new DockerServiceDescriptor(
                "dataet",
                "dataset execute",
                "",
                null,
                null,
                null,
                null,
                null,
                inputiods,
                null, //IORoute[] inputRoutes,
                outputiods,
                null, //IORoute[] outputRoutes,
                null, //OptionDescriptor[] optionDescriptors,
                null, //ThinDescriptor[] thinDescriptors,
                // these are specific to docker execution
                null, //String executorClassName,
                "busybox",
                "cp input.data.gz output.data.gz && cp input.metadata output.metadata",
                null)
    }

    int countSdf(input) {
        SDFReader reader = new SDFReader(input.getInputStream())
        int count = 0
        reader.each {
            count++
        }
        return count
    }

    void "createObjectsFromInputStreams for sdf"() {


        def data = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        def iod = new IODescriptor("input", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)
        def jobManager = new JobManager()

        when:
        def results = jobManager.createObjectsFromInputStreams([(iod.name + '_data'): data], [iod] as IODescriptor[])

        then:
        results.size() == 1
        def sdf = results.values().iterator().next()
        sdf instanceof SDFile
        countSdf(sdf) == 36

        cleanup:
        data?.close()
    }

    void "createObjectsFromInputStreams for dataset"() {


        def data = new FileInputStream("../../data/testfiles/Kinase_inhibs.json.gz")
        def meta = new FileInputStream("../../data/testfiles/Kinase_inhibs.metadata")
        def iod = new IODescriptor("input", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class)
        def jobManager = new JobManager()

        when:
        def results = jobManager.createObjectsFromInputStreams([(iod.name + '_data'): data, (iod.name + '_metadata'): meta], [iod] as IODescriptor[])

        then:
        results.size() == 1
        def ds = results.values().iterator().next()
        ds instanceof Dataset
        ds.items.size() == 36

        cleanup:
        data?.close()
        meta?.close()
    }

    void "sdf execute sync"() {

        // create the input
        def source = new File("../../data/testfiles/Kinase_inhibs.sdf.gz")
        def sd = createSdfServiceDescriptor()
        //println JsonHandler.getInstance().objectToJson(sd)
        JobManager mgr = new JobManager()


        when:
        def jobStatus = mgr.executeSync(USER, sd, null, ["input": new FileInputStream(source)])
        def results = mgr.getJobResultsAsObjects(USER, jobStatus.getJobId())

        then:
        results != null
        results.size() == 1
        countSdf(results["output"]) == 36

        cleanup:
        mgr?.cleanupJob(USER, jobStatus?.getJobId())
    }

    void "sdf execute async"() {

        // create the input
        def source = new File("../../data/testfiles/Kinase_inhibs.sdf.gz")
        def sd = createSdfServiceDescriptor()
        JobManager mgr = new JobManager()

        when:

        def jobStatus = mgr.executeAsync(USER, sd, null, ["input": new FileInputStream(source)])
        def jobId = jobStatus.getJobId()
        for (int i=0; i<10; i++) {
            sleep(1000)
            jobStatus = mgr.getJobStatus(USER,jobId)
            //println "status is " + jobStatus.getStatus()
            if (JobStatus.Status.RESULTS_READY == jobStatus.getStatus()) {
                break
            }
        }

        then:

        JobStatus.Status.RESULTS_READY == jobStatus.getStatus()
        def results = mgr.getJobResultsAsObjects(USER, jobStatus.getJobId())
        results != null
        results.size() == 1
        countSdf(results["output"]) == 36

        cleanup:
        mgr?.cleanupJob(USER, jobStatus?.getJobId())
    }


    void "dataset execute sync"() {

        // create the input
        def source1 = new File("../../data/testfiles/Kinase_inhibs.json.gz").bytes
        def source2 = new File("../../data/testfiles/Kinase_inhibs.metadata").bytes
        def sd = createDatasetServiceDescriptor()
        println JsonHandler.getInstance().objectToJson(sd)
        JobManager mgr = new JobManager()


        when:
        def jobStatus = mgr.executeSync(USER, sd, null, ["input_data": new ByteArrayInputStream(source1), "input_metadata": new ByteArrayInputStream(source2)])
        def results = mgr.getJobResultsAsDataSources(USER, jobStatus.getJobId())

        then:
        results != null
        results.size() == 2
        results[0].name == 'output_data'
        results[1].name == 'output_metadata'
        results[0].inputStream.bytes.length > 0
        results[1].inputStream.text.size() > 0

        cleanup:
        mgr?.cleanupJob(USER, jobStatus?.getJobId())
    }

}
