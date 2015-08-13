package com.im.lac.services.user;

import com.im.lac.services.CommonConstants;
import com.im.lac.services.ServerConstants;
import com.im.lac.services.util.Utils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class UserHandler {

    private final UserService service;

    public UserHandler(UserService service) {
        this.service = service;
    }

    static UserHandler getUserHandler(Exchange exch) {
        return getUserHandler(exch.getContext());
    }

    static UserHandler getUserHandler(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(ServerConstants.USER_HANDLER, UserHandler.class);
    }

    public static void putUser(Exchange exch) {
        UserHandler h = getUserHandler(exch);
        String username = Utils.fetchUsername(exch);
        User user = h.fetchUser(username);
        exch.getIn().setBody(user);
    }

    public User fetchUser(String username) {
        return service.getUser(username);
    }

}
