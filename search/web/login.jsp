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

<%@ include file="inc/initVars.jsp" %>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta name="description" content="National Library of Czech Republic digitized documents (periodical, monographs) access aplication." />
    <meta name="keywords" content="periodical, monograph, library, National Library of Czech Republic, book, publication, kramerius" />
    <meta name="AUTHOR" content="INCAD, www.incad.cz" />
    <link rel="icon" href="img/favicon.ico"/>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
    <link type="text/css" href="css/themes/base/ui.base.css" rel="stylesheet" />
    <link type="text/css" href="css/themes/base/ui.theme.css" rel="stylesheet" />
    <link type="text/css" href="css/themes/base/ui.dialog.css" rel="stylesheet" />
    <link type="text/css" href="css/themes/base/ui.slider.css" rel="stylesheet" />
    <link rel="stylesheet" href="css/dateAxisV.css" type="text/css"/>
    <link rel="stylesheet" href="css/dtree.css" type="text/css"/>
    <link rel="StyleSheet" href="css/styles.css" type="text/css"/>

        
    <script src="js/jquery-1.3.2.min.js" type="text/javascript" ></script>
    <script src="js/jquery-ui-1.7.2.custom.min.js" language="javascript" type="text/javascript"></script>
    <script src="js/jquery.cookie.js" type="text/javascript"></script>
    <script src="js/jquery.history.js" type="text/javascript"></script>

    <script src="js/jquery.backgroundPosition.js" type="text/javascript"></script>
    <script src="js/pageQuery.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/item.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/incad.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/dateAxis_formatV.js" language="javascript" type="text/javascript"></script>
    <script src="js/dateAxisV.js" language="javascript" type="text/javascript"></script>
    <script src="js/dtree.js" language="javascript" type="text/javascript"></script>
    <script  src="js/autocomplete.js" language="javascript" type="text/javascript"></script>
    <title>Kramerius 4</title>
    
</head>
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
	             <input type="text" size="30" name="j_username" style="border:1px solid silver;" ><br>
	             Heslo:<br>
	             <input type="password" name="j_password" size="30" style="border:1px solid silver;"><br>
	             <input type="submit"  value="Přihlášení" alt="Login">
	          </td>
	        </tr>
	      </table>
	    </form>
	</div>
	</body>
</html>