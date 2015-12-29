package org.squonk.notebook.service;

import org.squonk.notebook.execution.*;
import org.squonk.notebook.api.*;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.notebook.client.CallbackContext;

import static org.squonk.notebook.api.OptionType.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("cell")
public class ExampleCellService {
    private static final List<CellType> CELL_TYPE_LIST = createDefinitions();
    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    @Inject
    private QndCellExecutorProvider qndCellExecutorProvider;
    @Inject
    private CallbackClient callbackClient;
    @Inject
    private CallbackContext callbackContext;

    private static List<CellType> createDefinitions() {
        List<CellType> list = new ArrayList<>();

        list.add(createPropertyCalculateCellType());
        list.add(createChemblActivitiesFetcherCellType());
        list.add( createTableDisplayCellType());
        //list.add(createScriptCellType());
        list.add(createSdfUploaderCellType());
        list.add(createCsvUploaderCellType());
        list.add(createDatasetMergerCellType());
        list.add(createConvertBasicToMoleculeObjectCellType());
        list.add(createValueTransformerCellType());
        list.add(createGroovyScriptTrustedCellType());

        return list;
    }

    private static CellType createDatasetMergerCellType() {
        CellType cellType = new CellType();
        cellType.setName(DatasetMergerCellExecutor.CELL_TYPE_NAME_DATASET_MERGER);
        cellType.setDescription("Dataset merger");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition<String> fieldNameOptionDefinition = new OptionDefinition<String>();
        fieldNameOptionDefinition.setName("mergeFieldName");
        fieldNameOptionDefinition.setDisplayName("Merge field name");
        fieldNameOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(fieldNameOptionDefinition);
        OptionDefinition<Boolean> keepFirstOptionDefinition = new OptionDefinition<>();
        keepFirstOptionDefinition.setName("keepFirst");
        keepFirstOptionDefinition.setDisplayName("Keep first");
        keepFirstOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(keepFirstOptionDefinition);
        for (int i = 0; i < 5; i++) {
            BindingDefinition bindingDefinition = new BindingDefinition();
            bindingDefinition.setDisplayName("Input dataset " + (i + 1));
            bindingDefinition.setName("input" + (i + 1));
            bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
            cellType.getBindingDefinitionList().add(bindingDefinition);
        }
        return cellType;
    }

    private static CellType createCsvUploaderCellType() {
        CellType cellType = new CellType();
        cellType.setName(CSVUploaderCellExecutor.CELL_TYPE_NAME_CSV_UPLOADER);
        cellType.setDescription("CSV upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("fileContent");
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition<String> fileTypeOptionDefinition = new OptionDefinition<String>();
        fileTypeOptionDefinition.setName(OPTION_FILE_TYPE);
        fileTypeOptionDefinition.setDisplayName("File type");
        fileTypeOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(fileTypeOptionDefinition);
        OptionDefinition<Boolean> firstLineIsHeaderOptionDefinition = new OptionDefinition<Boolean>();
        firstLineIsHeaderOptionDefinition.setName(OPTION_FIRST_LINE_IS_HEADER);
        firstLineIsHeaderOptionDefinition.setDisplayName("First line is header");
        firstLineIsHeaderOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(firstLineIsHeaderOptionDefinition);
        return cellType;
    }

    private static CellType createSdfUploaderCellType() {
        CellType cellType = new CellType();
        cellType.setName(SDFUploaderCellExecutor.CELL_TYPE_NAME_SDF_UPLOADER);
        cellType.setDescription("SDF upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("fileContent");
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition optionDefinition = new OptionDefinition();
        optionDefinition.setName("nameFieldName");
        optionDefinition.setDisplayName("Name field´s name");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        return cellType;
    }

//    private static CellType createScriptCellType() {
//        CellType cellType = new CellType();
//        cellType.setName("Script");
//        cellType.setDescription("Script");
//        cellType.setExecutable(Boolean.TRUE);
//        OptionDefinition<String> optionDefinition = new OptionDefinition<String>();
//        optionDefinition.setName("code");
//        optionDefinition.setDisplayName("Code");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        optionDefinition = new OptionDefinition<String>();
//        optionDefinition.setName("errorMessage");
//        optionDefinition.setDisplayName("Error message");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        VariableDefinition variableDefinition = new VariableDefinition();
//        variableDefinition.setName("outcome");
//        variableDefinition.setDisplayName("Outcome");
//        variableDefinition.setVariableType(VariableType.VALUE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        return cellType;
//    }

    private static CellType createTableDisplayCellType() {
        CellType cellType = new CellType();
        cellType.setName("TableDisplay");
        cellType.setDescription("Table display");
        cellType.setExecutable(Boolean.FALSE);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.VALUE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        return cellType;
    }

    private static CellType createChemblActivitiesFetcherCellType() {
        CellType cellType = new CellType();
        cellType.setName(ChemblActivitiesFetcherCellExecutor.CELL_TYPE_NAME_CHEMBL_ACTIVITIES_FETCHER);
        cellType.setDescription("Chembl activities fetcher");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition optionDefinition = new OptionDefinition();
        optionDefinition.setName("assayId");
        optionDefinition.setDisplayName("Assay ID");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("prefix");
        optionDefinition.setDisplayName("Prefix");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellType createPropertyCalculateCellType() {
        CellType cellType = new CellType();
        cellType.setName("PropertyCalculate");
        cellType.setDescription("Property calc.");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("outputFile");
        variableDefinition.setDisplayName("Output file");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input file");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        OptionDefinition<String> optionDefinition = new OptionDefinition<String>();
        optionDefinition.setName("serviceName");
        optionDefinition.setDisplayName("Service");
        optionDefinition.setOptionType(OptionType.PICKLIST);
        for (String serviceName : CalculatorsClient.getServiceNames()) {
            optionDefinition.getPicklistValueList().add(serviceName);
        }
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

//    private static CellType createFileUploadCellType() {
//        CellType cellType = new CellType();
//        cellType.setName("FileUpload");
//        cellType.setDescription("File upload");
//        cellType.setExecutable(Boolean.TRUE);
//        VariableDefinition variableDefinition = new VariableDefinition();
//        variableDefinition.setName("file");
//        variableDefinition.setDisplayName("Uploaded file");
//        variableDefinition.setVariableType(VariableType.FILE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        OptionDefinition<String> optionDefinition = new OptionDefinition<String>();
//        optionDefinition.setName("fileName");
//        optionDefinition.setDisplayName("Output file name");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        cellType.setExecutable(Boolean.FALSE);
//        return cellType;
//    }

    private static CellType createConvertBasicToMoleculeObjectCellType() {
        CellType cellType = new CellType();
        cellType.setName(BasicObjectToMoleculeObjectCellExecutor.CELL_TYPE_BASICOBJECT_TO_MOLECULEOBJECT);
        cellType.setDescription("Convert Dataset from BasicObjects to MoleculeObjects");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("output");
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        OptionDefinition structFieldptionDefinition = new OptionDefinition();
        structFieldptionDefinition.setName("structureFieldName");
        structFieldptionDefinition.setDisplayName("Structure Field Name");
        structFieldptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(structFieldptionDefinition);
        OptionDefinition structFormatOptionDefinition = new OptionDefinition();
        structFormatOptionDefinition.setName("structureFormat");
        structFormatOptionDefinition.setDisplayName("Structure Format");
        structFormatOptionDefinition.setOptionType(OptionType.SIMPLE); // should be picklist
        cellType.getOptionDefinitionList().add(structFormatOptionDefinition);
        OptionDefinition preserveUuidOptionDefinition = new OptionDefinition();
        preserveUuidOptionDefinition.setName("preserveUuid");
        preserveUuidOptionDefinition.setDisplayName("PreserveUUID");
        preserveUuidOptionDefinition.setOptionType(OptionType.SIMPLE); // should be boolean
        cellType.getOptionDefinitionList().add(preserveUuidOptionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellType createValueTransformerCellType() {
        CellType cellType = new CellType();
        cellType.setName(ValueTransformerCellExecutor.CELL_TYPE_NAME_VALUE_TRANSFORMER);
        cellType.setDescription("Transform dataset values");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("output");
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        OptionDefinition optionDefinition = new OptionDefinition();
        optionDefinition.setName("transformDefinitions");
        optionDefinition.setDisplayName("Transform Definitions");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellType createGroovyScriptTrustedCellType() {
        CellType cellType = new CellType();
        cellType.setName(TrustedGroovyDatasetScriptCellExecutor.CELL_TYPE_NAME_TRUSTED_GROOVY_DATASET_SCRIPT);
        cellType.setDescription("Groovy Script (trusted)");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("output");
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        OptionDefinition optionDefinition = new OptionDefinition();
        optionDefinition.setName("script");
        optionDefinition.setDisplayName("Groovy Script");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    @Path("listCellType")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CellType> listCellType() {
        return CELL_TYPE_LIST;
    }

    @Path("executeCell")
    @POST
    public void executeCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
        callbackContext.setNotebookId(notebookId);
        CellDTO cell = callbackClient.retrieveCell(cellName);
        if (cell == null) {
            throw new IllegalStateException("Executor for cell " + cellName + " not found");
        }
        qndCellExecutorProvider.resolveCellHandler(cell.getCellType()).execute(cellName);
    }

    @Path("retrieveCellType")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CellType retrieveCellType(@QueryParam("name") String name) {
        for (CellType cellType : CELL_TYPE_LIST) {
            if (cellType.getName().equals(name)) {
                return cellType;
            }
        }
        return null;
    }

//@ApplicationScoped
//@Path("cell")
//public class ExampleCellService {
//
//    private static final List<CellType> CELL_TYPE_DESCRIPTOR_LIST = createDescriptors();
//    public static final String OPTION_FILE_TYPE = "csvFormatType";
//    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
//    @Inject
//    private QndCellExecutorProvider qndCellExecutorProvider;
//    @Inject
//    private CallbackClient callbackClient;
//    @Inject
//    private CallbackContext callbackContext;
//
//    private static List<CellType> createDescriptors() {
//        List<CellType> list = new ArrayList<>();
//
//        CellType cellType = new CellType();
//        cellType.setName("FileUpload");
//        cellType.setDescription("File upload");
//        cellType.setExecutable(Boolean.TRUE);
//        VariableDefinition variableDefinition = new VariableDefinition();
//        variableDefinition.setName("file");
//        variableDefinition.setDisplayName("Uploaded file");
//        variableDefinition.setVariableType(VariableType.FILE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        OptionDefinition optionDefinition = new OptionDefinition();
//        optionDefinition.setName("fileName");
//        optionDefinition.setDisplayName("Output file name");
//        optionDefinition.setOptionType(SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        cellType.setExecutable(Boolean.FALSE);
//        list.add(cellType);
//
//        cellType = new CellType();
//        cellType.setName("PropertyCalculate");
//        cellType.setDescription("Property calc.");
//        cellType.setExecutable(Boolean.TRUE);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("outputFile");
//        variableDefinition.setDisplayName("Output file");
//        variableDefinition.setVariableType(VariableType.FILE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        BindingDefinition bindingDefinition = new BindingDefinition();
//        bindingDefinition.setDisplayName("Input file");
//        bindingDefinition.setName("input");
//        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
//        cellType.getBindingDefinitionList().add(bindingDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("serviceName");
//        optionDefinition.setDisplayName("Service");
//        optionDefinition.setOptionType(OptionType.PICKLIST);
//        for (String serviceName : CalculatorsClient.getServiceNames()) {
//            optionDefinition.getPicklistValueList().add(serviceName);
//        }
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        cellType.setExecutable(Boolean.TRUE);
//        list.add(cellType);
//
//        cellType = new CellType();
//        cellType.setName("ChemblActivitiesFetcher");
//        cellType.setDescription("Chembl activities fetcher");
//        cellType.setExecutable(Boolean.TRUE);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("results");
//        variableDefinition.setDisplayName("Results");
//        variableDefinition.setVariableType(VariableType.DATASET);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("assayId");
//        optionDefinition.setDisplayName("Assay ID");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("prefix");
//        optionDefinition.setDisplayName("Prefix");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        cellType.setExecutable(Boolean.TRUE);
//        list.add(cellType);
//
//        cellType = new CellType();
//        cellType.setName("TableDisplay");
//        cellType.setDescription("Table display");
//        cellType.setExecutable(Boolean.FALSE);
//        bindingDefinition = new BindingDefinition();
//        bindingDefinition.setDisplayName("Input file");
//        bindingDefinition.setName("input");
//        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
//        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
//        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.VALUE);
//        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
//        cellType.getBindingDefinitionList().add(bindingDefinition);
//        cellType.setExecutable(Boolean.FALSE);
//        list.add(cellType);
//
//        cellType = new CellType();
//        cellType.setName("Script");
//        cellType.setDescription("Script");
//        cellType.setExecutable(Boolean.TRUE);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("code");
//        optionDefinition.setDisplayName("Code");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("errorMessage");
//        optionDefinition.setDisplayName("Error message");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("outcome");
//        variableDefinition.setDisplayName("Outcome");
//        variableDefinition.setVariableType(VariableType.VALUE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        cellType.setExecutable(Boolean.TRUE);
//        list.add(cellType);
//
//
//        cellType = new CellType();
//        cellType.setName("SdfUploader");
//        cellType.setDescription("SDF upload");
//        cellType.setExecutable(Boolean.TRUE);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("fileContent");
//        variableDefinition.setDisplayName("File content");
//        variableDefinition.setVariableType(VariableType.FILE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("results");
//        variableDefinition.setDisplayName("Results");
//        variableDefinition.setVariableType(VariableType.DATASET);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("nameFieldName");
//        optionDefinition.setDisplayName("Name field´s name");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        list.add(cellType);
//
//        cellType = new CellType();
//        cellType.setName("CsvUploader");
//        cellType.setDescription("CSV upload");
//        cellType.setExecutable(Boolean.TRUE);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("fileContent");
//        variableDefinition.setDisplayName("File content");
//        variableDefinition.setVariableType(VariableType.FILE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("results");
//        variableDefinition.setDisplayName("Results");
//        variableDefinition.setVariableType(VariableType.DATASET);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName(OPTION_FILE_TYPE);
//        optionDefinition.setDisplayName("File type");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName(OPTION_FIRST_LINE_IS_HEADER);
//        optionDefinition.setDisplayName("First line is header");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        list.add(cellType);
//
//        cellType = new CellType();
//        cellType.setName("DatasetMerger");
//        cellType.setDescription("Dataset merger");
//        cellType.setExecutable(Boolean.TRUE);
//        variableDefinition = new VariableDefinition();
//        variableDefinition.setName("results");
//        variableDefinition.setDisplayName("Results");
//        variableDefinition.setVariableType(VariableType.DATASET);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("mergeFieldName");
//        optionDefinition.setDisplayName("Merge field name");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        optionDefinition = new OptionDefinition();
//        optionDefinition.setName("keepFirst");
//        optionDefinition.setDisplayName("Keep first");
//        optionDefinition.setOptionType(OptionType.SIMPLE);
//        cellType.getOptionDefinitionList().add(optionDefinition);
//        for (int i = 0; i < 5; i++) {
//            bindingDefinition = new BindingDefinition();
//            bindingDefinition.setDisplayName("Input dataset " + (i + 1));
//            bindingDefinition.setName("input" + (i + 1));
//            bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
//            cellType.getBindingDefinitionList().add(bindingDefinition);
//        }
//        list.add(cellType);
//
//        return list;
//    }
//
//    @Path("listCellType")
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public List<CellType> listCellType() {
//        return CELL_TYPE_DESCRIPTOR_LIST;
//    }
//
//    @Path("executeCell")
//    @POST
//    public void executeCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
//        callbackContext.setNotebookId(notebookId);
//        CellDTO cell = callbackClient.retrieveCell(cellName);
//        qndCellExecutorProvider.resolveCellHandler(cell.getCellType()).execute(cellName);
//    }
//
//    @Path("retrieveCellType")
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public CellType retrieveCellType(@QueryParam("name") String name) {
//        for (CellType cellType : CELL_TYPE_DESCRIPTOR_LIST) {
//            if (cellType.getName().equals(name)) {
//                return cellType;
//            }
//        }
//        return null;
//    }

}
