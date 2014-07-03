package toolkit.wicket.pagerenderer;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author simetrias
 */
@ApplicationScoped
public class PageRenderer {

    public String renderPage(final Page page) {
        final PageProvider pageProvider = new PageProvider(page);
        final RenderPageRequestHandler handler = new RenderPageRequestHandler(pageProvider, RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT);
        final org.apache.wicket.request.handler.render.PageRenderer pageRenderer = Application.get().getPageRendererProvider().get(handler);

        RequestCycle requestCycle = RequestCycle.get();

        final Response oldResponse = requestCycle.getResponse();
        BufferedWebResponse tempResponse = new BufferedWebResponse(null);

        try {
            requestCycle.setResponse(tempResponse);
            pageRenderer.respond(requestCycle);
        } finally {
            requestCycle.setResponse(oldResponse);
        }

        return tempResponse.getText().toString();
    }
}
