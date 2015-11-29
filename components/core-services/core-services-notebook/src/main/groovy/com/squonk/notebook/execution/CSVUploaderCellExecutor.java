package com.squonk.notebook.execution;

import com.im.lac.job.jobdef.StepDefinition;
import static com.im.lac.job.jobdef.StepDefinitionConstants.*;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.execution.steps.CSVReaderStep;

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

        StepDefinition step = new StepDefinition(STEP_CSV_READER)
                .withFieldMapping(CSVReaderStep.VAR_CSV_INPUT, "FileInput")
                .withFieldMapping(VARIABLE_OUTPUT_DATASET, "Results"); 

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
