<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@page trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.RESTHelper"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>

<%
	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	pageContext.setAttribute("lrProcessManager",inj.getInstance(LRProcessManager.class));
	pageContext.setAttribute("dfManager",inj.getInstance(DefinitionManager.class));
%>


<%@ include file="inc/initVars.jsp" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="inc/html_header.jsp" %>
	<body>
	
	<script type="text/javascript">
	var dialogForm = null;
		function loginDialog() {
			$("#dialogForm").show();
			if (dialogForm) {
				dialogForm("open");
			} else {
				dialogForm =  $("#dialogForm").dialog({
			        modal: true,
			        draggable:false,
			        resizable:false,
			        title:"Přihlášení"
			    });
			}
		}

		$(document).ready(function(){
			loginDialog();
		});
			
	</script>
	<div id="dialogForm" style="display: none;">
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
	</div>
	</body>
</html>