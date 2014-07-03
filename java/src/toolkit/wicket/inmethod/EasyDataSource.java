package toolkit.wicket.inmethod;

import com.inmethod.grid.IDataSource;
import com.inmethod.grid.IGridSortState;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author simetrias
 */
public class EasyDataSource<T extends Serializable> implements IDataSource<T> {

    private final Delegate<T> delegate;

    public EasyDataSource(Delegate<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void query(IQuery query, IQueryResult<T> result) {
        result.setTotalCount(delegate.getTotalCount());
        result.setItems(delegate.getIterator((int) query.getFrom(), (int) (query.getFrom() + query.getCount())));
    }

    @Override
    public IModel<T> model(final T object) {
        return new Model<T>(object);
    }

    @Override
    public void detach() {
    }

    public Delegate<T> getDelegate() {
        return delegate;
    }

    interface Delegate<T> extends Serializable {

        int getTotalCount();

        Iterator<? extends T> getIterator(int fromIndex, int toIndex);

        void onSortStateChanged(AjaxRequestTarget target, IGridSortState.ISortStateColumn sortStateColumn);

        void resetData();
    }

}
