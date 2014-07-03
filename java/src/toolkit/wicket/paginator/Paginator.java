package toolkit.wicket.paginator;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;

/**
 * @author simetrias
 */
public class Paginator extends AjaxPagingNavigator {

    public Paginator(String id, IPageable pageable) {
        super(id, pageable);
        addBehavior();
    }

    public Paginator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
        addBehavior();
    }

    private void addBehavior() {
        PaginatorBehavior paginatorBehavior = new PaginatorBehavior();
        add(paginatorBehavior);
    }
}
