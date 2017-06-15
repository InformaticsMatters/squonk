/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.security;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

/** Basic UserDetailsManager that uses the servlet API to provide minimal information about the user.
 * Authentication by the container is required.
 *
 * Created by timbo on 04/01/16.
 */
public class DefaultUserDetailsManager implements UserDetailsManager {


    /** Get info about the user from the request using HttpServletRequest.getUserPrincipal().getName() as the username.
     * Other information is faked as the Servlet API does not provide anything beyond the Principal.
     * However, HttpServletRequest.isUserInRole(String role) should still work correctly.
     *
     * @param request
     * @return
     */
    @Override
    public UserDetails getAuthenticatedUser(HttpServletRequest request) {
        Principal p = request.getUserPrincipal();
        if (p != null) {
            String username = p.getName();
            return new UserDetails(UserDetails.AUTHENTICATOR_SERVLET, username, username + "@nowhere.com", username, username);
        }
        return null;
    }

    @Override
    public Map<String,String> getSecurityHeaders(HttpServletRequest request) {
        return Collections.emptyMap();
    }

}
