package org.squonk.execution

import org.squonk.core.DockerServiceDescriptor
import org.squonk.io.IODescriptor
import org.squonk.jobdef.JobStatus
import org.squonk.reader.SDFReader
import org.squonk.types.SDFile
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

class JobManagerSpec extends Specification {

    private DockerServiceDescriptor createServiceDescriptor() {
        // create the service descriptor
        def inputiods = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        def outputiods = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]


        return new DockerServiceDescriptor(
                "myid",
                "simple execute",
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


    void "simple execute sync"() {

        // create the input
        def source = new File("../../data/testfiles/Kinase_inhibs.sdf.gz")
        def sd = createServiceDescriptor()
        JobManager mgr = new JobManager()


        when:
        def jobStatus = mgr.executeSync(sd, null, ["input": new SDFile(new FileInputStream(source))], "squonkuser")
        def results = mgr.getJobResults(jobStatus.getJobId())

        then:
        results != null
        results.size() == 1
        countSdf(results["output"]) == 36

        cleanup:
        mgr?.cleanupJob(jobStatus?.getJobId())
    }

    void "simple execute async"() {

        // create the input
        def source = new File("../../data/testfiles/Kinase_inhibs.sdf.gz")
        def sd = createServiceDescriptor()
        JobManager mgr = new JobManager()

        when:

        def jobStatus = mgr.executeAsync(sd, null, ["input": new SDFile(new FileInputStream(source))], "squonkuser")
        def jobId = jobStatus.getJobId()
        for (int i=0; i<10; i++) {
            sleep(1000)
            jobStatus = mgr.getJobStatus(jobId)
            //println "status is " + jobStatus.getStatus()
            if (JobStatus.Status.RESULTS_READY == jobStatus.getStatus()) {
                break
            }
        }

        then:

        JobStatus.Status.RESULTS_READY == jobStatus.getStatus()
        def results = mgr.getJobResults(jobStatus.getJobId())
        results != null
        results.size() == 1
        countSdf(results["output"]) == 36

        cleanup:
        mgr?.cleanupJob(jobStatus?.getJobId())
    }

    int countSdf(input) {
        SDFReader reader = new SDFReader(input.getInputStream())
        int count = 0
        reader.each {
            count++
        }
        return count
    }
}
