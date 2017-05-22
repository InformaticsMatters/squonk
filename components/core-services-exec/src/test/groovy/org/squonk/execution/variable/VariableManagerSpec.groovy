package org.squonk.execution.variable

import org.squonk.dataset.Dataset
import org.squonk.execution.variable.impl.FilesystemWriteContext
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.types.PDBFile
import spock.lang.Specification

import java.util.zip.GZIPInputStream

/**
 *
 * @author timbo
 */
class VariableManagerSpec extends Specification {

    Long producer = 1l
    
    void "simple put/get variable"() {

        VariableManager manager = new VariableManager(null, 1, 1);

        when:

        manager.putValue(new VariableKey(producer, "text"), String.class, "John Doe")
        manager.putValue(new VariableKey(producer, "age"), Integer.class, new Integer(60))

        then:
        manager.getValue(new VariableKey(producer, "text"), String.class) == "John Doe"
        manager.getValue(new VariableKey(producer, "age"), Integer.class) == 60

    }

    void "put/get dataset"() {

        def objs1 = [
                new BasicObject([id:1, a:"1", hello:'world']),
                new BasicObject([id:2,a:"99",hello:'mars',foo:'bar']),
                new BasicObject([id:3,a:"100",hello:'mum'])
        ]

        Dataset ds1 = new Dataset(BasicObject.class, objs1)

        VariableManager manager = new VariableManager(null, 1, 1);

        when:
        manager.putValue(new VariableKey(producer, "ds1"), Dataset.class, ds1)
        Dataset ds2 = manager.getValue(new VariableKey(producer, "ds1"), Dataset.class)


        then:

        ds2 != null
        ds2.items.size() == 3

    }


    void "read pdb"() {
        VariableManager varman = new VariableManager(null, 1, 1);
        String pdbcontent = "I'm a pdb file"
        PDBFile value = new PDBFile(new ByteArrayInputStream(pdbcontent.getBytes()))
        VariableKey var = new VariableKey(99, "protein")
        varman.putValue(var, PDBFile, value)
        println varman.tmpVariableInfo

        when:
        def result = varman.getValue(var, PDBFile.class);

        then:
        result != null
        def c
        (c = result.inputStream.text) == pdbcontent
        println c
    }

    void "write pdb"() {

        File dir = new File(System.getProperty("java.io.tmpdir"))
        VariableManager varman = new VariableManager(null, 1, 1);
        String content = "I'm a pdb file"
        PDBFile value = new PDBFile(new ByteArrayInputStream(content.getBytes()))
        File expectedFile = new File(dir, "protein.pdb.gz")

        when:
        FilesystemWriteContext writeContext = new FilesystemWriteContext(dir, "protein")
        varman.putValue(PDBFile.class, value, writeContext);

        then:
        dir != null
        expectedFile.exists()
        new GZIPInputStream(new FileInputStream(expectedFile)).text == content

        cleanup:
        expectedFile.delete()
    }
	
}

