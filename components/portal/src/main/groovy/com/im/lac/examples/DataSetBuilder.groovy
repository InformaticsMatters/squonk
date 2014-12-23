package com.im.lac.examples

import com.im.lac.portal.service.api.*
import com.im.lac.portal.service.mock.*

import groovy.json.JsonSlurper
import groovy.util.logging.Log

/**
 *
 * @author timbo
 */
@Log
class DataSetBuilder {
    
    DataSetBuilder() {

    }
    
    DatasetMock createDatasetMock() {
        def datasetMock = new DatasetMock()
        return datasetMock
    }
        
    
    void addRows(DatasetMock mock, URL url) {
        InputStream is = url.openStream()
        try {
            JsonSlurper slurper = new JsonSlurper()
            def json = slurper.parse(is)
            int count = 0
            json.each {
                count++
                DatasetRow datasetRow = new DatasetRow()
                Long id = new Long(it.cd_id)
                datasetRow.setId(id)
                datasetRow.setProperty(DatasetServiceMock.STRUCTURE_FIELD_NAME, it.cd_structure)
                datasetRow.setProperty("molweight", it.cd_molweight)
                datasetRow.setProperty("formula", it.cd_formula)
                mock.addDatasetRow(id, datasetRow)
                log.fine("Added new parent row with id $id")
            }
            log.info("Added $count rows")
        } finally {
            is.close()
        }
    }
    
    void addProperty(DatasetMock mock, URL url) {
        InputStream is = url.openStream()
        try {
            JsonSlurper slurper = new JsonSlurper()
            def json = slurper.parse(is)
            int count = 0
            json.each {
                Long structureId = new Long(it.structure_id)               
                DatasetRow parent = mock.findDatasetRowById(structureId)
                if (parent) {
                    count++
                    Long id = new Long(it.property_id)
                    DatasetRow child = parent.createChild()
                    child.setId(id )
                    def data = it.property_data
                    child.setProperty("Assay name", "${data.assay_id} ${data.standard_type}") // need a proper name for the assay
                    child.setProperty("Value", data.standard_value)
                    child.setProperty("Qualifier", data.standard_relation) // combine this with the value as a QualifiedNumber
                    child.setProperty("Units", data.standard_units) // ditto as QualifiedNumberWithUnits
                    log.fine("Added prop $id of ${data.standard_value} for column")
                } else {
                    log.warning("No parent with ID $structureId")
                }
            }
            log.info("Added $count values")
        } finally {
            is.close()
        }
    }
    
    static void main(String[] args){
        
        // this assumes you have already run a search that generates the hit list
        // and that the ID of the hit list is 25
        // Doing this more fully will require a HTTP client.
        
        DataSetBuilder instance = new DataSetBuilder()
        DatasetMock datasetMock = instance.createDatasetMock()
        recordStats('Whole operation') {
            recordStats('create rows') {
                instance.addRows(datasetMock, new URL("http://localhost:8888/chemcentral/hitlists/25/structures"))
            }
            println "${datasetMock.datasetRowList.size()} rows"
            ['CHEMBL1613886', 'CHEMBL1613777', 'CHEMBL1614110', 'CHEMBL1614027'].each { id ->
                recordStats("create columns for $id") {
                    instance.addProperty(datasetMock, new URL("http://localhost:8888/chemcentral/hitlists/25/properties/" + id))
                }
            }
        }
    }
    
    static def recordStats(String desc, Closure closure) {

        Runtime rt = Runtime.getRuntime()
        rt.gc()
        long f0 = rt.freeMemory()
        long m0 = rt.totalMemory()
        long t0 = System.currentTimeMillis()
        def result = closure()
        long t1 = System.currentTimeMillis()
        rt.gc()
        long f1 = rt.freeMemory()
        long m1 = rt.totalMemory()
        println desc
        println "  took ${t1-t0}ms"
        println "  memory before: ${m0 - f0}"
        println "  memory after: ${m1 - f1}"
        println "  memory diff: ${(m1-f1) - (m0-f0)}"
        return result
    } 
	
}

