package com.im.lac.portal.webapp;

import com.im.lac.portal.service.DatasetDescriptor;
import com.im.lac.portal.service.PrototypeServiceMock;
import com.im.lac.wicket.inmethod.EasyGrid;
import com.im.lac.wicket.inmethod.EasyGridBuilder;
import com.im.lac.wicket.inmethod.EasyListDataSource;
import com.im.lac.wicket.semantic.NotifierProvider;
import com.im.lac.wicket.semantic.SemanticResourceReference;
import com.inmethod.grid.common.AbstractGrid;
import com.vaynberg.wicket.select2.ApplicationSettings;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.CssResourceReference;

import javax.inject.Inject;
import java.util.List;

public class PortalHomePage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private PrototypeServiceMock prototypeServiceMock;

    private UploadFilePanel uploadFilePanel;
    private EasyGrid<DatasetDescriptor> datasetDescriptorGrid;
    private List<DatasetDescriptor> datasetDescriptorList;

    public PortalHomePage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addShowNotifierAction();
        addUploadFilePanel();
        addUploadFileAction();
        addDatasetDescriptorGrid();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(AbstractGrid.class, "res/style.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(ApplicationSettings.class, "res/select2.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
    }

    private void addShowNotifierAction() {
        add(new AjaxLink("notify") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                notifierProvider.getNotifier(PortalHomePage.this).notify("Title", "Some message here");
            }
        });
    }

    private void addUploadFileAction() {
        add(new AjaxLink("uploadFile") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadFilePanel.showModal();
            }
        });
    }

    private void addUploadFilePanel() {
        uploadFilePanel = new UploadFilePanel("uploadFilePanel", "modalElement");
        uploadFilePanel.setCallbacks(new UploadFilePanel.Callbacks() {
            @Override
            public void onSubmit() {
                if (uploadFilePanel.getDatasetDescriptor() != null) {
                    refreshVisualizerPanel();
                    refreshDatasetDescriptorsGrid();
                    System.out.println("File Imported");
                }
            }

            @Override
            public void onCancel() {
                uploadFilePanel.hideModal();
            }
        });
        add(uploadFilePanel);
    }

    private void refreshVisualizerPanel() {
        // TODO: impl
        System.out.println("Descriptor ID = " + uploadFilePanel.getDatasetDescriptor().getDatasetId());
    }

    private void refreshDatasetDescriptorsGrid() {
        datasetDescriptorList = prototypeServiceMock.listDatasetDescriptor();
        datasetDescriptorGrid.resetData();
        getRequestCycle().find(AjaxRequestTarget.class).add(datasetDescriptorGrid);
    }

    private void addDatasetDescriptorGrid() {
        datasetDescriptorList = prototypeServiceMock.listDatasetDescriptor();

        EasyGridBuilder<DatasetDescriptor> easyGridBuilder = new EasyGridBuilder<DatasetDescriptor>("datasetDescriptors");
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("ID", "datasetId", "datasetId"));
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("Description", "description", "description"));

        datasetDescriptorGrid = easyGridBuilder.build(new EasyListDataSource<DatasetDescriptor>(DatasetDescriptor.class) {

            @Override
            public List<DatasetDescriptor> loadData() {
                return datasetDescriptorList;
            }
        });
        add(datasetDescriptorGrid);

        addDatasetDescriptorGridActions();
    }

    private void addDatasetDescriptorGridActions() {
        add(new AjaxLink("addFromSDF") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadFilePanel.showModal();
            }
        });
    }


}
