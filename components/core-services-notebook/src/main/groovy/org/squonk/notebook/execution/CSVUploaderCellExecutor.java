package org.squonk.notebook.execution;

import org.squonk.execution.steps.StepDefinition;
import static org.squonk.execution.steps.StepDefinitionConstants.*;
import org.squonk.notebook.api.CellDTO;
import org.squonk.execution.steps.impl.CSVReaderStep;
import org.squonk.notebook.api.VariableKey;

import java.util.Map;

/**
 * Reads a CSV file (or similar with user specified delimiter) and generates a
 * Dataset of BasicObjects containing the properties found in the file. The file
 * input is read from the FileInput variable (which must be set prior to
 * execution) and the results set to the Results variable.
 *
 *
 * Created by timbo on 10/11/15.
 */
public class CSVUploaderCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_NAME_CSV_UPLOADER = "CsvUploader";
    
    public CSVUploaderCellExecutor() {
        super(CELL_TYPE_NAME_CSV_UPLOADER);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        for (Map.Entry e: cell.getOptionMap().entrySet()) {
            System.out.println("OPT: " + e.getKey() + " -> " + e.getValue());
        }

        StepDefinition step = new StepDefinition(STEP_CSV_READER)
                .withInputVariableMapping(CSVReaderStep.VAR_CSV_INPUT, new VariableKey(cell.getName(), "fileContent"))
                .withOutputVariableMapping(VARIABLE_OUTPUT_DATASET, "results");

        step = configureOption(step, cell, CSVReaderStep.OPTION_ALLOW_MISSING_COLUMN_NAMES);     // Boolean
        step = configureOption(step, cell, CSVReaderStep.OPTION_COMMENT_MARKER);                 // Character
        step = configureOption(step, cell, CSVReaderStep.OPTION_DELIMITER);                      // Character
        step = configureOption(step, cell, CSVReaderStep.OPTION_ESCAPE_CHAR);                    // Character
        step = configureOption(step, cell, CSVReaderStep.OPTION_FIELD_NAMES);                    // String[]
        step = configureOption(step, cell, CSVReaderStep.OPTION_FORMAT_TYPE);                    // one of the CSVFormat enums
        step = configureOption(step, cell, CSVReaderStep.OPTION_IGNORE_EMPTY_LINES);             // Boolean
        step = configureOption(step, cell, CSVReaderStep.OPTION_IGNORE_SURROUNDING_WHITESPACE);  // Boolean
        step = configureOption(step, cell, CSVReaderStep.OPTION_NULL_STRING);                    // String
        step = configureOption(step, cell, CSVReaderStep.OPTION_QUOTE_CHAR);                     // Character
        step = configureOption(step, cell, CSVReaderStep.OPTION_QUOTE_MODE);                     // one of the QuoteMode enums
        step = configureOption(step, cell, CSVReaderStep.OPTION_RECORD_SEPARATOR);               // String
        step = configureOption(step, cell, CSVReaderStep.OPTION_SKIP_HEADER_LINE);               // Boolean
        step = configureOption(step, cell, CSVReaderStep.OPTION_USE_HEADER_FOR_FIELD_NAMES);     // Boolean

        return new StepDefinition[]{step};
    }

}
