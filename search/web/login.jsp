<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<%
	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	pageContext.setAttribute("lrProcessManager",inj.getInstance(LRProcessManager.class));
	pageContext.setAttribute("dfManager",inj.getInstance(DefinitionManager.class));
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.RESTHelper"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
	<body>
    <form name=login id="loginForm" action="j_security_check" method="post">
      <table align=center >
        <tr>
          <td>
             Jméno:<br>
             <input type="text" size="30" name="j_username" ><br>
             Heslo:<br>
             <input type="password" name="j_password" size="30"><br>
             <input type="submit"  value="Přihlášení" alt="Login">
          </td>
        </tr>
      </table>
    </form>
	</body>
</html>