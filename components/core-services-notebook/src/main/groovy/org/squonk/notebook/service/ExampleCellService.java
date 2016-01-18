package org.squonk.notebook.service;

import org.squonk.notebook.execution.*;
import org.squonk.notebook.api.*;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.notebook.client.CallbackContext;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;

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

        list.add(createChemblActivitiesFetcherCellType());
        list.add( createTableDisplayCellType());
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "mergeFieldName", "Merge field name",
                "Name of field to use to match items"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(Boolean.class, "keepFirst", "Keep first",
                "When merging keep the original value (or the new one)"));
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, OPTION_FILE_TYPE, "File type",
                "Type of CSV or TAB file")
                .withValues(new String [] {"TDF", "EXCEL", "MYSQL", "RFC4180", "DEFAULT"}).withDefaultValue("DEFAULT"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(Boolean.class, OPTION_FIRST_LINE_IS_HEADER, "First line is header",
                "First line contains field names"));
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "nameFieldName", "Name field´s name",
                "Field name to use for the molecule name (the part before the CTAB block").withMinValues(0));
        return cellType;
    }

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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "assayId", "Assay ID", "ChEBML Asssay ID"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "prefix", "Prefix", "Prefix for result fields"));
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

//    private static CellType createPropertyCalculateCellType() {
//        CellType cellType = new CellType();
//        cellType.setName("PropertyCalculate");
//        cellType.setDescription("Property calc.");
//        cellType.setExecutable(Boolean.TRUE);
//        VariableDefinition variableDefinition = new VariableDefinition();
//        variableDefinition.setName("outputFile");
//        variableDefinition.setDisplayName("Output file");
//        variableDefinition.setVariableType(VariableType.FILE);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        BindingDefinition bindingDefinition = new BindingDefinition();
//        bindingDefinition.setDisplayName("Input file");
//        bindingDefinition.setName("input");
//        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
//        cellType.getBindingDefinitionList().add(bindingDefinition);
//        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "serviceName", "Service", "Service to call")
//                .withValues(CalculatorsClient.getServiceNames()));
//        cellType.setExecutable(Boolean.TRUE);
//        return cellType;
//    }

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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "structureFieldName", "Structure Field Name",
                "Name of property to use for the structure"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "structureFormat",
                "Structure Format", "Format of the structures e.g. smiles, mol")
                .withValues(new String[] {"smiles", "mol"}));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(Boolean.class, "preserveUuid", "Preserve UUID", "Keep the existing UUID or generate a new one").withMinValues(1));
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(new MultiLineTextTypeDescriptor(10, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "transformDefinitions", "Transform Definitions",
                "Definition of the transforms to perform"));
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Groovy Script", "Groovy script to execute"));
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
            throw new IllegalStateException("Cell " + cellName + " not found");
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

}
