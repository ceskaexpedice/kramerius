<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.kramerius.MostDesirable"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
<div style="position:relative;border:1px solid red;">
    <div style="float:right;">1</div>
    <div style="float:right;">2</div>
    <div style="float:right;">3</div>
    
</div>
        <%=request.getRequestURI()%>
    </body>
</html>
