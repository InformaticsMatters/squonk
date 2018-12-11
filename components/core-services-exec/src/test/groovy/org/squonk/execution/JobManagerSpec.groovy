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

import io.swagger.v3.oas.models.OpenAPI
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.camel.typeConverters.MoleculeStreamTypeConverter
import org.squonk.core.DockerServiceDescriptor
import org.squonk.core.ServiceDescriptorToOpenAPIConverter
import org.squonk.dataset.Dataset
import org.squonk.io.FileDataSource
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
                "org.squonk.execution.steps.impl.DefaultDockerExecutorStep", //String executorClassName,
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
                "org.squonk.execution.steps.impl.DefaultDockerExecutorStep", //String executorClassName,
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

        def data = new FileDataSource("sdf", CommonMimeTypes.MIME_TYPE_MDL_SDF,
                new File("../../data/testfiles/Kinase_inhibs.sdf.gz"), true)
        def iod = new IODescriptor("input", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)
        def jobManager = new JobManager(false, true)
        jobManager.putServiceDescriptors(Collections.singletonList(createSdfServiceDescriptor()))

        when:
        def results = jobManager.createObjectsFromDataSources([(iod.name + '_data'): data], [iod] as IODescriptor[])

        then:
        results.size() == 1
        def sdf = results.values().iterator().next()
        sdf instanceof SDFile
        countSdf(sdf) == 36

        cleanup:
        data?.inputStream.close()
    }

    void "createObjectsFromInputStreams for dataset"() {

        def data = new FileDataSource("data", "input_data", CommonMimeTypes.MIME_TYPE_MOLECULE_OBJECT_JSON,
                new File("../../data/testfiles/Kinase_inhibs.json.gz"), true)
        def meta = new FileDataSource("metadata", "input_metadata", CommonMimeTypes.MIME_TYPE_DATASET_METADATA,
                new File("../../data/testfiles/Kinase_inhibs.metadata"), false)
        def iod = new IODescriptor("input", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON,
                Dataset.class, MoleculeObject.class)
        def jobManager = new JobManager(false, true)
        jobManager.putServiceDescriptors(Collections.singletonList(createDatasetServiceDescriptor()))

        when:
        def results = jobManager.createObjectsFromDataSources([(iod.name + '_data'): data, (iod.name + '_metadata'): meta], [iod] as IODescriptor[])

        then:
        results.size() == 1
        def ds = results.values().iterator().next()
        ds instanceof Dataset
        ds.items.size() == 36

        cleanup:
        data?.inputStream.close()
        meta?.inputStream.close()
    }

    JobStatus waitTillResultsReady(mgr, jobStatus) {
        def jobId = jobStatus.getJobId()
        for (int i=0; i<10; i++) {
            sleep(1000)
            jobStatus = mgr.getJobStatus(USER,jobId)
            //println "status is " + jobStatus.getStatus()
            if (JobStatus.Status.RESULTS_READY == jobStatus.getStatus()) {
                break
            }
        }
        return jobStatus
    }

    void "sdf execute"() {

        // create the input
        def source = new FileDataSource("sdf", CommonMimeTypes.MIME_TYPE_MDL_SDF,
                new File("../../data/testfiles/Kinase_inhibs.sdf.gz"), true)
        def sd = createSdfServiceDescriptor()
        JobManager mgr = new JobManager(false, true)
        mgr.putServiceDescriptors(Collections.singletonList(createSdfServiceDescriptor()))

        when:

        def jobStatus = mgr.executeAsync(USER, sd.getId(), [:], ["input": source])
        jobStatus = waitTillResultsReady(mgr, jobStatus)

        then:

        JobStatus.Status.RESULTS_READY == jobStatus.getStatus()
        def results = mgr.getJobResultsAsObjects(USER, jobStatus.getJobId())
        results != null
        results.size() == 1
        countSdf(results["output"]) == 36

        cleanup:
        mgr?.cleanupJob(USER, jobStatus?.getJobId())
    }

    void "dataset execute"() {

        // create the input
        def data = new FileDataSource("data", "input_data", CommonMimeTypes.MIME_TYPE_MOLECULE_OBJECT_JSON,
                new File("../../data/testfiles/Kinase_inhibs.json.gz"), true)
        def meta = new FileDataSource("metadata", "input_metadata", CommonMimeTypes.MIME_TYPE_DATASET_METADATA,
                new File("../../data/testfiles/Kinase_inhibs.metadata"), false)
        def sd = createDatasetServiceDescriptor()
        println JsonHandler.getInstance().objectToJson(sd)
        JobManager mgr = new JobManager(false, true)
        mgr.putServiceDescriptors(Collections.singletonList(createDatasetServiceDescriptor()))

        when:
        def jobStatus = mgr.executeAsync(USER, sd.getId(), [:], ["input_data": data, "input_metadata": meta])
        jobStatus = waitTillResultsReady(mgr, jobStatus)
        def results = mgr.getJobResultsAsDataSources(USER, jobStatus.getJobId())

        then:
        results != null
        results.size() == 1
        def outputs = results['output']
        outputs.size() == 2
        outputs[0].inputStream.text.size() > 0
        outputs[1].inputStream.bytes.length > 0

        cleanup:
        mgr?.cleanupJob(USER, jobStatus?.getJobId())
    }

    void "dataset execute with convert from sdf"() {

        // create the input as SDF
        def sdf = new FileDataSource("sdf", CommonMimeTypes.MIME_TYPE_MDL_SDF,
                new File("../../data/testfiles/Kinase_inhibs.sdf.gz"), true)
        // but the service descriptor specifies dataset
        def sd = createDatasetServiceDescriptor()
        println JsonHandler.getInstance().objectToJson(sd)
        JobManager mgr = new JobManager(false, true)
        def camelContext = new DefaultCamelContext()
        camelContext.typeConverterRegistry.addTypeConverters(new MoleculeStreamTypeConverter())
        mgr.setCamelContext(camelContext)
        mgr.putServiceDescriptors(Collections.singletonList(createDatasetServiceDescriptor()))

        when:
        def jobStatus = mgr.executeAsync(USER, sd.getId(), [:], ["input": sdf])
        jobStatus = waitTillResultsReady(mgr, jobStatus)
        def results = mgr.getJobResultsAsDataSources(USER, jobStatus.getJobId())

        then:
        results != null
        results.size() == 1
        def outputs = results['output']
        outputs.size() == 2
        outputs[0].inputStream.text.size() > 0
        outputs[1].inputStream.bytes.length > 0

        cleanup:
        mgr?.cleanupJob(USER, jobStatus?.getJobId())
    }

    void "generate swagger"() {
        JobManager mgr = new JobManager(true, true)

        when:
        OpenAPI oai = mgr.fetchServiceDescriptorSwagger("http://squonk.it")
        String json = ServiceDescriptorToOpenAPIConverter.openApiToJson(oai)
        println json

        then:
        json.length() > 100
        json.contains("http://squonk.it")


    }

}
