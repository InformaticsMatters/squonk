<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
		 
<%@ page import="org.keycloak.constants.ServiceUrlConstants" %>
<%@ page import="org.keycloak.common.util.KeycloakUriBuilder" %>
<%@ page import="org.keycloak.representations.IDToken" %>
<%@ page import="org.keycloak.KeycloakSecurityContext" %>

<%@ page import="com.squonk.security.impl.KeycloakUserDetailsManager" %>
<%@ page import="com.squonk.security.UserDetails" %>
<%@ page import="com.squonk.security.UserDetailsManager" %>
<%@ page import="java.net.URI" %>

<%@ page session="false" %>
<html>
<head>
    <title>Authenticated Page</title>
</head>
<body bgcolor="#E3F6CE">
<%
    String logoutUri = KeycloakUriBuilder.fromUri("https://192.168.59.103/auth").path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH)
            .queryParam("redirect_uri", "http://192.168.59.103:8080/sampleapp/index.html").build("samplerealm").toString();
    KeycloakSecurityContext session = (KeycloakSecurityContext)request.getAttribute(KeycloakSecurityContext.class.getName());
    IDToken idToken = session.getIdToken();
%>
	
<%
	UserDetailsManager manager = new KeycloakUserDetailsManager();
	UserDetails details = manager.getAuthenticatedUser(request);
	URI logout = manager.getLogoutUrl(request, "http://foo.com/logout");
	%>
<p>Goto: <a href="<%=logoutUri%>">logout</a> 
</p>
Servlet User Principal <b><%=request.getUserPrincipal().getName()%>
</b> made this request.
<p><b>Caller IDToken values</b> (<i>You can specify what is returned in IDToken in the customer-portal claims page in the admin console</i>:</p>
<p>Username: <%=idToken.getPreferredUsername()%></p>
<p>Email: <%=idToken.getEmail()%></p>
<p>Full Name: <%=idToken.getName()%></p>
<p>First: <%=idToken.getGivenName()%></p>
<p>Last: <%=idToken.getFamilyName()%></p>
<p>Issuer: <%=idToken.getIssuer()%></p>
<p>Issued for: <%=idToken.getIssuedFor()%></p>

<h2>User Details</h2>
<p>Username: <%=details.getUserid()%></p>
<p>Email: <%=details.getEmail()%></p>
<p>First: <%=details.getFirstName()%></p>
<p>Last: <%=details.getLastName()%></p>
<p>Logout: <%= logout %></p>

<br><br>
</body>
</html>