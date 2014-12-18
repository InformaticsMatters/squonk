package com.im.lac.wicket.inmethod;

import com.inmethod.grid.IGridSortState;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.util.Iterator;
import java.util.List;

/**
 * @author simetrias
 */
public abstract class PagedDataSource<T, K> implements EasyDataSource.Delegate<T> {

    private List<K> keys;

    @Override
    public int getTotalCount() {
        if (keys == null) {
            keys = loadKeys(null);
        }
        return keys.size();
    }

    @Override
    public Iterator<? extends T> getIterator(int fromIndex, int toIndex) {
        List<K> page = keys.subList(fromIndex, toIndex);
        return loadPage(page).iterator();
    }

    @Override
    public void onSortStateChanged(AjaxRequestTarget target, IGridSortState.ISortStateColumn sortStateColumn) {
        keys = loadKeys(sortStateColumn);
    }

    @Override
    public void resetData() {
        keys = null;
    }

    public abstract List<K> loadKeys(IGridSortState.ISortStateColumn sortStateColumn);

    public abstract List<T> loadPage(List<K> pageKeys);
}
