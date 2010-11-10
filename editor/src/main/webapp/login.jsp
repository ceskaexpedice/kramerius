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
Delegates login to the search web app.
-->
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%
    KConfiguration kconfig = KConfiguration.getInstance();
    String query = "welcomeUrl=" + kconfig.getEditorURL();
    String ssoUrl = kconfig.getApplicationURL()
            + "singleSignOn.jsp?" + query;

    response.sendRedirect(response.encodeRedirectURL(ssoUrl));
%>
