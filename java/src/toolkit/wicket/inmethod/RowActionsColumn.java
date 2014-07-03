package toolkit.wicket.inmethod;

import com.inmethod.grid.column.AbstractColumn;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class RowActionsColumn<T extends Serializable> extends AbstractColumn<EasyDataSource<T>, T, String> {

    private final List<String> actionNameList;
    private final RowActionsCallbackHandler<T> rowActionsCallbackHandler;

    public RowActionsColumn(List<String> actionNameList, RowActionsCallbackHandler<T> rowActionsCallbackHandler) {
        super("actions", new Model<String>("&nbsp;"));
        this.actionNameList = actionNameList;
        this.rowActionsCallbackHandler = rowActionsCallbackHandler;
    }

    @Override
    public Component newCell(WebMarkupContainer wmc, String id, final IModel<T> imodel) {
        return new RowActionsPanel<T>(id, actionNameList, imodel.getObject(), rowActionsCallbackHandler);
    }

    @Override
    public Component newHeader(String componentId) {
        return super.newHeader(componentId).setEscapeModelStrings(false);
    }
}
