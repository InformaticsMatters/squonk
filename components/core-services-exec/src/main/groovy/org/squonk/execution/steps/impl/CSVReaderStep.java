package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import com.im.lac.types.BasicObject;
import com.squonk.dataset.Dataset;
import com.squonk.reader.CSVReader;
import com.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import org.apache.camel.CamelContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

/**
 * Reads a CSV or Tab delimited file and generates a
 * {@link com.squonk.dataset.Dataset} of {@link com.im.lac.types.BasicObject}s.
 * <p>
 * The SDFile is passed as an {@link java.io.InputStream} (can be gzipped). By
 default the input is expected in the variable named by the VAR_CSV_INPUT
 constant, though that name can be mapped to a different name. The resulting
 Dataset is contained in the variable named by the VAR_DATASET_OUTPUT
 constant, or a variable mapped to that name.
 <p>
 * The parsing is implemented using the Apache Common CSV 1.1 library. Options
 * here correspond for options available from that library (the CSVFormat class
 * in particular). See <a href="http://camel.apache.org/csv.html">here</a> for
 * specific details.
 * <p>
 * The {@link com.squonk.reader.CSVReader} class acts as a bridge between this
 * class and the Apache Commons CSV library.
 *
 *
 * @author timbo
 */
public class CSVReaderStep extends AbstractStep {

    /**
     * The type of CSV format. One of the CSVFormat constants found here:
     * http://commons.apache.org/proper/commons-csv/archives/1.1/apidocs/index.html
     * If not present then CSVFormat.DEFAULT is used.
     *
     */
    public static final String OPTION_FORMAT_TYPE = "csvFormatType";

    /**
     * Whether to include or skip the first line. Value expected to be a
     * boolean.
     */
    public static final String OPTION_SKIP_HEADER_LINE = "skipHeaderLine";
    /**
     * Use the first line for the name of the fields. Value expected to be a
     * boolean. If being set you probably also want to set
     * OPTION_SKIP_HEADER_LINE to true to make sure that line is not read as
     * data.
     */
    public static final String OPTION_USE_HEADER_FOR_FIELD_NAMES = "firstLineIsHeader";
    /**
     * Names to use for the fields. Use this only if you are not specifying
     * OPTION_USE_HEADER_FOR_FIELD_NAMES or that value is set to false. Value
     * expected to be a String[]
     */
    public static final String OPTION_FIELD_NAMES = "fieldNames";
    /**
     * Whether to ignore empty lines. Value expected to be a boolean.
     */
    public static final String OPTION_IGNORE_EMPTY_LINES = "ignoreEmptyLines";
    /**
     * Whether to ignore surrounding whitespace. Value expected to be a boolean.
     */
    public static final String OPTION_IGNORE_SURROUNDING_WHITESPACE = "ignoreSuroundingWhiteSpace";
    /**
     * String for null values. Value expected to be a String.
     */
    public static final String OPTION_NULL_STRING = "nullString";

    /**
     * Value for delimeter. Can be used to overrride the default set by the
     * OPTION_FORMAT_TYPE (of comma if this is not defined). Value expected to
     * be a Character
     */
    public static final String OPTION_DELIMITER = "delimiter";
    /**
     * Value for the quote character. Value expected to be a Character
     */
    public static final String OPTION_QUOTE_CHAR = "quoteCharacter";
    /**
     * Value for the escape character. Value expected to be a Character
     */
    public static final String OPTION_ESCAPE_CHAR = "escapeCharacter";
    /**
     * Value for the comment marker. Value expected to be a Character
     */
    public static final String OPTION_COMMENT_MARKER = "commentMarker";
    /**
     * Value for the quote mode. Value expected to be a String corresponding to
     * of the the {@link org.apache.commons.csv.QuoteMode} enums
     */
    public static final String OPTION_QUOTE_MODE = "quoteMode";
    /**
     * Value for the record separator. Value expected to be a String
     */
    public static final String OPTION_RECORD_SEPARATOR = "recordSeparator";

    /**
     * Whether to allow missing column names. Value expected to be a boolean.
     */
    public static final String OPTION_ALLOW_MISSING_COLUMN_NAMES = "allowMissingColumnNames";

    /**
     * Expected variable name for the input
     */
    public static final String VAR_CSV_INPUT = "_CSVReaderCSVInput";
    /**
     * Variable name for the MoleculeObjectDataset output
     */
    public static final String VAR_DATASET_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public String[] getInputVariableNames() {
        return new String[]{VAR_CSV_INPUT};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{VAR_DATASET_OUTPUT};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws IOException {
        InputStream is = fetchMappedValue(VAR_CSV_INPUT, InputStream.class, PersistenceType.BYTES, varman);
        CSVReader reader = createReader(IOUtils.getGunzippedInputStream(is));
        Stream<BasicObject> mols = reader.asStream();
        Dataset dataset = new Dataset(BasicObject.class, mols);
        System.out.println("CSV rows: " + dataset.getItems().size());
        createMappedVariable(VAR_DATASET_OUTPUT, Dataset.class, dataset, PersistenceType.DATASET, varman);
    }

    private CSVReader createReader(InputStream input) throws IOException {

        String csvFormatOption = getOption(OPTION_FORMAT_TYPE, String.class);
        System.out.println("CSVFormat string = " + csvFormatOption);
        CSVFormat csv;
        if (csvFormatOption == null) {
            csv = CSVFormat.DEFAULT;
        } else {
            switch (csvFormatOption) {
                case "TDF":
                    csv = CSVFormat.TDF;
                    break;
                case "EXCEL":
                    csv = CSVFormat.EXCEL;
                    break;
                case "MYSQL":
                    csv = CSVFormat.MYSQL;
                    break;
                case "RFC4180":
                    csv = CSVFormat.RFC4180;
                    break;
                default:
                    csv = CSVFormat.DEFAULT;
            }
        }

        Character delim = getOption(OPTION_DELIMITER, Character.class);
        if (delim != null) {
            csv = csv.withDelimiter(delim);
        }
        Boolean ignore = getOption(OPTION_IGNORE_EMPTY_LINES, Boolean.class);
        if (ignore != null) {
            csv = csv.withIgnoreEmptyLines(ignore);
        }
        Boolean whitespace = getOption(OPTION_IGNORE_SURROUNDING_WHITESPACE, Boolean.class);
        if (whitespace != null) {
            csv = csv.withIgnoreSurroundingSpaces(whitespace);
        }
        String nullString = getOption(OPTION_NULL_STRING, String.class);
        if (nullString != null) {
            csv = csv.withNullString(nullString);
        }
        Character quoteChar = getOption(OPTION_QUOTE_CHAR, Character.class);
        if (quoteChar != null) {
            csv = csv.withQuote(quoteChar);
        }
        Character escChar = getOption(OPTION_ESCAPE_CHAR, Character.class);
        if (escChar != null) {
            csv = csv.withEscape(escChar);
        }
        Character commentMarker = getOption(OPTION_COMMENT_MARKER, Character.class);
        if (commentMarker != null) {
            csv = csv.withCommentMarker(commentMarker);
        }
        String quoteMode = getOption(OPTION_QUOTE_MODE, String.class);
        if (quoteMode != null) {
            csv = csv.withQuoteMode(QuoteMode.valueOf(quoteMode));
        }
        String recSep = getOption(OPTION_RECORD_SEPARATOR, String.class);
        if (recSep != null) {
            csv = csv.withRecordSeparator(recSep);
        }

        Boolean useHeaderForFields = getOption(OPTION_USE_HEADER_FOR_FIELD_NAMES, Boolean.class);
        if (useHeaderForFields != null) {
            csv = csv.withHeader();
        } else {
            String fieldnames = getOption(OPTION_FIELD_NAMES, String.class);
            if (fieldnames != null) {
                csv = csv.withHeader(fieldnames);
            }
        }
        Boolean skipHeaderLine = getOption(OPTION_SKIP_HEADER_LINE, Boolean.class);
        if (skipHeaderLine != null) {
            csv = csv.withSkipHeaderRecord(skipHeaderLine);
        }

        Boolean allowMisingColNames = getOption(OPTION_ALLOW_MISSING_COLUMN_NAMES, Boolean.class);
        if (allowMisingColNames != null) {
            csv = csv.withAllowMissingColumnNames(allowMisingColNames);
        }
        System.out.println("CSV: " + csv);

        return new CSVReader(input, csv);
    }

}
