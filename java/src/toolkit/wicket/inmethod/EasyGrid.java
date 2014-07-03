package toolkit.wicket.inmethod;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.IGridSortState;
import com.inmethod.grid.datagrid.DefaultDataGrid;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class EasyGrid<T extends Serializable> extends DefaultDataGrid<EasyDataSource<T>, T, String> {

    public EasyGrid(String id, EasyDataSource<T> dataSource, List<IGridColumn<EasyDataSource<T>, T, String>> columns) {
        super(id, dataSource, columns);
    }

    @Override
    protected void onSortStateChanged(AjaxRequestTarget ajaxRequestTarget) {
        IGridSortState.ISortStateColumn sortStateColumn = getSortState().getColumns().get(0);
        this.getDataSource().getDelegate().onSortStateChanged(ajaxRequestTarget, sortStateColumn);
        ajaxRequestTarget.add(this);
    }

    public void resetData() {
        this.getDataSource().getDelegate().resetData();
        this.setCurrentPage(0);
    }

}
