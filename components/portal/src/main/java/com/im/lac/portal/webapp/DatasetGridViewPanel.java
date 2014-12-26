package com.im.lac.portal.webapp;


import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.ListDatasetDescriptorFilter;
import com.im.lac.portal.service.mock.DatasetServiceMock;
import com.im.lac.wicket.inmethod.EasyGrid;
import com.im.lac.wicket.inmethod.EasyGridBuilder;
import com.im.lac.wicket.inmethod.EasyListDataSource;
import com.im.lac.wicket.inmethod.RowActionsCallbackHandler;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class DatasetGridViewPanel extends Panel {

    private static final String LARGE_ARROW_CIRCLE_OUTLINE_RIGHT_LINK_ICON = "large arrow circle outline right link icon";

    @Inject
    private DatasetServiceMock prototypeServiceMock;
    private UploadModalPanel uploadModalPanel;
    private EasyGrid<DatasetDescriptor> datasetDescriptorGrid;
    private List<DatasetDescriptor> datasetDescriptorList;

    public DatasetGridViewPanel(String id) {
        super(id);
        addModals();
        addDatasetDescriptorGrid();
    }

    private void addModals() {
        uploadModalPanel = new UploadModalPanel("uploadFilePanel", "modalElement");
        uploadModalPanel.setCallbacks(new UploadModalPanel.Callbacks() {

            @Override
            public void onSubmit() {
                if (uploadModalPanel.getDatasetDescriptor() != null) {
                    refreshDatasetDescriptorsGrid();
                }
            }

            @Override
            public void onCancel() {
                uploadModalPanel.hideModal();
            }
        });
        add(uploadModalPanel);
    }

    private void addDatasetDescriptorGrid() {
        datasetDescriptorList = prototypeServiceMock.listDatasetDescriptor(new ListDatasetDescriptorFilter());
        EasyGridBuilder<DatasetDescriptor> easyGridBuilder = new EasyGridBuilder<DatasetDescriptor>("datasetDescriptors");
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("Id", "id", "id"));
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("Description", "description", "description"));
        List<String> actionNameList = Arrays.asList(LARGE_ARROW_CIRCLE_OUTLINE_RIGHT_LINK_ICON);
        easyGridBuilder.getColumnList().add(easyGridBuilder.newActionsColumn(actionNameList, new RowActionsCallbackHandler<DatasetDescriptor>() {

            @Override
            public void onAction(AjaxRequestTarget target, String name, DatasetDescriptor datasetDescriptor) {
                if (LARGE_ARROW_CIRCLE_OUTLINE_RIGHT_LINK_ICON.equals(name)) {
                    TreeGridVisualizerPage page = new TreeGridVisualizerPage(datasetDescriptor);
                    setResponsePage(page);
                }
            }
        }).setInitialSize(70));
        datasetDescriptorGrid = easyGridBuilder.build(new EasyListDataSource<DatasetDescriptor>(DatasetDescriptor.class) {

            @Override
            public List<DatasetDescriptor> loadData() {
                return datasetDescriptorList;
            }
        });
        add(datasetDescriptorGrid);
        addDatasetDescriptorGridActions();
    }

    private void refreshDatasetDescriptorsGrid() {
        datasetDescriptorList = prototypeServiceMock.listDatasetDescriptor(new ListDatasetDescriptorFilter());
        datasetDescriptorGrid.resetData();
        getRequestCycle().find(AjaxRequestTarget.class).add(datasetDescriptorGrid);
    }

    private void addDatasetDescriptorGridActions() {
        add(new AjaxLink("addFromSDF") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadModalPanel.showModal();
            }
        });
    }
}
