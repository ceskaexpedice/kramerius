<!--
Copyright (C) 2010 Jan Pokorsky

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
-->

<!--
singleSignOn.jsp permits other Kramerius web applications to access the login.jsp
inside the search web application to keep the login UI consistent.

In order to make it working it is necessary to enable 'Single Sign On' option
in tomcat/conf/server.xml first.
See http://tomcat.apache.org/tomcat-6.0-doc/config/host.html#Single%20Sign%20On

singleSignOn.jsp must be declared as web resource under security-constraint element
in search/web.xml.

Web apps have to declare FORM authentication with own login.jsp redirecting to
search/singleSignOn.jsp in its own web.xml.

Available parameters:

welcomeUrl - URL where to go after successful login; default is the search web app
-->

<%@page import="java.net.URI"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%
    String welcomeUrl = request.getParameter("welcomeUrl");
    welcomeUrl = welcomeUrl != null ? welcomeUrl : KConfiguration.getInstance().getApplicationURL();
    response.sendRedirect(welcomeUrl);
%>
