package org.squonk.core.client

import com.im.lac.job.jobdef.JobStatus
import org.squonk.types.MoleculeObject
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.variable.impl.VariableWriteContext
import org.squonk.notebook.api.NotebookDTO
import org.squonk.notebook.api.NotebookEditableDTO
import org.squonk.types.DatasetHandler
import org.squonk.types.io.JsonHandler
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 10/06/16.
 */
abstract class AbstractExecuteDocker extends Specification {

    static String username = 'squonkuser'
    static JsonHandler JSON = JsonHandler.instance
    static boolean waited = false
    @Shared
    JobStatusRestClient jobClient = new JobStatusRestClient()
    @Shared
    NotebookRestClient notebookClient = new NotebookRestClient()

    @Shared
    NotebookEditableDTO editable
    @Shared
    Long notebookId
    @Shared
    Long editableId
    @Shared
    Long cellId = 1

    void doSetupSpec(String name) {
        waitForStartup() // need to wait for everything to start
        NotebookDTO notebookDTO = notebookClient.createNotebook(username, name+" name", name+" description")
        editable = notebookClient.listEditables(notebookDTO.getId(), username)[0]
        DatasetHandler dh = new DatasetHandler(MoleculeObject.class)
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        VariableWriteContext vhcontext = new VariableWriteContext(notebookClient, editable.notebookId, editable.id, 1, "input")
        dh.writeVariable(dataset, vhcontext)
        notebookId = editable.notebookId
        editableId = editable.id
    }

    void cleanupSpec() {
        notebookClient.deleteNotebook(editable.getNotebookId())
    }


    static void waitForStartup() {
        if (!waited) {
            sleep(10000)
            waited = true
        }
    }

    int findResultSize(notebookId, editableId, cellId, varname) {
        def metaJson = notebookClient.readTextValue(notebookId, editableId, cellId, varname)
        return metaJson ? JSON.objectFromJson(metaJson, DatasetMetadata.class).size : -1
    }

    DatasetMetadata readMetadata(notebookId, editableId, cellId, varname) {
        def metaJson = notebookClient.readTextValue(notebookId, editableId, cellId, varname)
        return metaJson ? JSON.objectFromJson(metaJson, DatasetMetadata.class) : null
    }

    JobStatus waitForJob(def jobId) {
        JobStatus status
        for (int i=0; i<30; i++) {
            sleep(500)
            status = jobClient.get(jobId)
            if (status.status == JobStatus.Status.COMPLETED || status.status == JobStatus.Status.ERROR) {
                //println "job completed"
                break
            }
            //println "trying again ..."
        }
        return status
    }


}
