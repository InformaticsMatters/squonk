package org.squonk.notebook.api

import org.squonk.types.NumberRange
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 04/04/16.
 */
class NotebookCanvasDTOSpec extends Specification {

    void "canvas json read write"() {

        when:
        NotebookCanvasDTO canvas1 = new NotebookCanvasDTO(123)
        String json = JsonHandler.getInstance().objectToJson(canvas1)
        NotebookCanvasDTO canvas2 = JsonHandler.getInstance().objectFromJson(json, NotebookCanvasDTO)

        then:
        json != null
        canvas2 != null
        canvas2.getLastCellId() == 123
        canvas2.getVersion() == NotebookCanvasDTO.LATEST_VERSION
    }

    void "canvas properties"() {

        when:
        NotebookCanvasDTO canvas1 = new NotebookCanvasDTO(123)
        canvas1.putProperty("hello", "world")
        canvas1.putProperty("earth", 3)
        canvas1.putProperty("pi-float", 3.14f)
        canvas1.putProperty("pi-double", 3.14d)
        canvas1.putProperty("pi-bd", new BigDecimal(3.14))
        canvas1.putProperty("three-int", new Integer(3))
        canvas1.putProperty("three-float", 3f)
        String json = JsonHandler.getInstance().objectToJson(canvas1)
        NotebookCanvasDTO canvas2 = JsonHandler.getInstance().objectFromJson(json, NotebookCanvasDTO)

        then:
        json != null
        canvas2 != null
        canvas2.getLastCellId() == 123
        canvas2.getVersion() == NotebookCanvasDTO.LATEST_VERSION
        canvas2.getProperty("hello") == "world"
        canvas2.getProperty("earth") == 3
        canvas2.getProperty("pi-float") == 3.14f
        canvas2.getProperty("pi-double") == 3.14d
        canvas2.getProperty("pi-bd") == new BigDecimal(3.14)
        canvas2.getProperty("three-int") == new Integer(3)
        canvas2.getProperty("three-float") == 3f
    }

    void "cell json read write"() {

        when:
        NotebookCanvasDTO canvas1 = new NotebookCanvasDTO(123).withCell(new NotebookCanvasDTO.CellDTO(99, 3, 'key', 'name', 10,20,30,40))
        String json = JsonHandler.getInstance().objectToJson(canvas1)
        NotebookCanvasDTO canvas2 = JsonHandler.getInstance().objectFromJson(json, NotebookCanvasDTO)

        then:
        json != null
        canvas2 != null
        canvas2.cells.size() == 1
        canvas2.cells[0].id == 99
        canvas2.cells[0].key == 'key'
        canvas2.cells[0].name == 'name'
        canvas2.cells[0].top == 10
        canvas2.cells[0].height == 40
    }

    void "option json read write"() {

        when:
        NotebookCanvasDTO canvas1 = new NotebookCanvasDTO(123)
                .withCell(new NotebookCanvasDTO.CellDTO(99, 3, 'key', 'name', 10,20,30,40)
                    .withOption('key1', 'value1')
                    .withOption("range1", new NumberRange.Integer(1,10))
                    .withOption("range2", new NumberRange.Integer(0,10))
                )
        String json = JsonHandler.getInstance().objectToJson(canvas1)
        println json
        NotebookCanvasDTO canvas2 = JsonHandler.getInstance().objectFromJson(json, NotebookCanvasDTO)

        then:
        json != null
        canvas2 != null
        canvas2.cells.size() == 1
        canvas2.cells[0].options.size() == 3
        canvas2.cells[0].options['key1'] == 'value1'
        canvas2.cells[0].options['range1'].toString() == '1|10'
        canvas2.cells[0].options['range2'].toString() == '0|10'
    }

    void "binding json read write"() {

        when:
        NotebookCanvasDTO canvas1 = new NotebookCanvasDTO(123)
                .withCell(new NotebookCanvasDTO.CellDTO(99, 3, 'key', 'name', 10,20,30,40)
                .withBinding(new NotebookCanvasDTO.BindingDTO('variablekey', 77, 'producervariablename')))
        String json = JsonHandler.getInstance().objectToJson(canvas1)
        println json
        NotebookCanvasDTO canvas2 = JsonHandler.getInstance().objectFromJson(json, NotebookCanvasDTO)

        then:
        json != null
        canvas2 != null
        canvas2.cells.size() == 1
        canvas2.cells[0].bindings.size() == 1
        canvas2.cells[0].bindings[0].variableKey == 'variablekey'
        canvas2.cells[0].bindings[0].producerId == 77
        canvas2.cells[0].bindings[0].producerVariableName == 'producervariablename'
    }
}
