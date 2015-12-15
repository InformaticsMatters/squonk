package org.squonk.notebook.service;

import org.squonk.notebook.execution.CalculatorsClient;
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

    private static final List<CellType> CELL_TYPE_DESCRIPTOR_LIST = createDescriptors();
    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    @Inject
    private QndCellExecutorProvider qndCellExecutorProvider;
    @Inject
    private CallbackClient callbackClient;
    @Inject
    private CallbackContext callbackContext;

    private static List<CellType> createDescriptors() {
        List<CellType> list = new ArrayList<>();

        CellType cellType = new CellType();
        cellType.setName("FileUpload");
        cellType.setDescription("File upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("file");
        variableDefinition.setDisplayName("Uploaded file");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition optionDefinition = new OptionDefinition();
        optionDefinition.setName("fileName");
        optionDefinition.setDisplayName("Output file name");
        optionDefinition.setOptionType(SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.FALSE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("PropertyCalculate");
        cellType.setDescription("Property calc.");
        cellType.setExecutable(Boolean.TRUE);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("outputFile");
        variableDefinition.setDisplayName("Output file");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input file");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("serviceName");
        optionDefinition.setDisplayName("Service");
        optionDefinition.setOptionType(OptionType.PICKLIST);
        for (String serviceName : CalculatorsClient.getServiceNames()) {
            optionDefinition.getPicklistValueList().add(serviceName);
        }
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("ChemblActivitiesFetcher");
        cellType.setDescription("Chembl activities fetcher");
        cellType.setExecutable(Boolean.TRUE);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        optionDefinition = new OptionDefinition();
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
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("TableDisplay");
        cellType.setDescription("Table display");
        cellType.setExecutable(Boolean.FALSE);
        bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input file");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.VALUE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        cellType.setExecutable(Boolean.FALSE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("Script");
        cellType.setDescription("Script");
        cellType.setExecutable(Boolean.TRUE);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("code");
        optionDefinition.setDisplayName("Code");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("errorMessage");
        optionDefinition.setDisplayName("Error message");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("outcome");
        variableDefinition.setDisplayName("Outcome");
        variableDefinition.setVariableType(VariableType.VALUE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.setExecutable(Boolean.TRUE);
        list.add(cellType);


        cellType = new CellType();
        cellType.setName("SdfUploader");
        cellType.setDescription("SDF upload");
        cellType.setExecutable(Boolean.TRUE);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("fileContent");
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("nameFieldName");
        optionDefinition.setDisplayName("Name field´s name");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("CsvUploader");
        cellType.setDescription("CSV upload");
        cellType.setExecutable(Boolean.TRUE);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("fileContent");
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName(OPTION_FILE_TYPE);
        optionDefinition.setDisplayName("File type");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName(OPTION_FIRST_LINE_IS_HEADER);
        optionDefinition.setDisplayName("First line is header");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("DatasetMerger");
        cellType.setDescription("Dataset merger");
        cellType.setExecutable(Boolean.TRUE);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("mergeFieldName");
        optionDefinition.setDisplayName("Merge field name");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("keepFirst");
        optionDefinition.setDisplayName("Keep first");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        list.add(cellType);

        return list;
    }

    @Path("listCellType")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CellType> listCellType() {
        return CELL_TYPE_DESCRIPTOR_LIST;
    }

    @Path("executeCell")
    @POST
    public void executeCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
        callbackContext.setNotebookId(notebookId);
        CellDTO cell = callbackClient.retrieveCell(cellName);
        qndCellExecutorProvider.resolveCellHandler(cell.getCellType()).execute(cellName);
    }

    @Path("retrieveCellType")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CellType retrieveCellType(@QueryParam("name") String name) {
        for (CellType cellType : CELL_TYPE_DESCRIPTOR_LIST) {
            if (cellType.getName().equals(name)) {
                return cellType;
            }
        }
        return null;
    }
}
