<%-- 
    Document   : test
    Created on : 8.4.2010, 15:16:36
    Author     : Administrator
--%>

<%@page contentType="text/html" pageEncoding="windows-1250"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1250">
        <title>JSP Page</title>
    </head>
    <body>
        <h2>Hello World!</h2>
        
        <%
        System.out.println(Class.forName("cz.incad.Kramerius.AbstracThumbnailServlet").getResource("AbstracThumbnailServlet.class"));
        %>
    </body>
</html>
