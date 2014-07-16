package webapp;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class DatasetsPanel extends Panel {

    public DatasetsPanel(String id) {
        super(id);
        addDatasets();
    }

    private void addDatasets() {
        List<DatasetData> datasetDataList = new ArrayList<DatasetData>();
        datasetDataList.add(new DatasetData());
        datasetDataList.add(new DatasetData());
        datasetDataList.add(new DatasetData());

        ListView<DatasetData> datasets = new ListView<DatasetData>("datasets", datasetDataList) {

            @Override
            protected void populateItem(ListItem<DatasetData> components) {
                components.add(new WebMarkupContainer("dataset"));
            }
        };
        add(datasets);
    }

    class DatasetData implements Serializable {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
