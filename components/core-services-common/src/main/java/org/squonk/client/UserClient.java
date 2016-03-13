package org.squonk.client;

import org.squonk.core.user.User;

/**
 * Created by timbo on 12/03/16.
 */
public interface UserClient {

    User getUser(String username) throws Exception;
}
