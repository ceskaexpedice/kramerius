<%-- 
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
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration" %>
<%@page import="cz.incad.kramerius.utils.ApplicationURL" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link type="text/css" rel="stylesheet" href="Editor.css" />
        <title>Kramerius Editor</title>

        <script type="text/javascript">
            var EditorConfiguration = {
                krameriusURL: "<%=response.encodeURL(ApplicationURL.getServerAndPort(request)+"/search"/*KConfiguration.getInstance().getApplicationURL()*/)%>"
                , pids: "<%= request.getParameter("pids") %>"
            };
        </script>
        <script type="text/javascript"  src="kramerius_editor/kramerius_editor.nocache.js"></script>

    </head>
    <body>

        <!-- OPTIONAL: include this if you want history support -->
<!--        <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>-->

        <!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
        <noscript>
            <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
                Your web browser must have JavaScript enabled
                in order for this application to display correctly.
            </div>
        </noscript>

    </body>
</html>
