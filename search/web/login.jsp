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


<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta name="description" content="Digitized documents access aplication." />
    <meta name="keywords" content="periodical, monograph, library,  book, publication, kramerius, fedora" />
    <meta name="author" content="INCAD, www.incad.cz" />

    <link rel="icon" href="img/favicon.ico"/>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />

        <%
    String theme;
    String parameter = request.getParameter("theme");
    if (parameter != null) {
        theme = parameter;
        session.setAttribute("theme",parameter);
    } else if (session.getAttribute("theme") != null) {
        theme = (String)session.getAttribute("theme");
    } else {
        theme = "smoothness";
    }
    pageContext.setAttribute("theme", theme);
    %>
    <link type="text/css" href="css/${theme}/jquery-ui.custom.css" rel="stylesheet" />
    <!--link type="text/css" href="css/ui-lightness/jquery-ui-1.8.11.custom.css" rel="stylesheet" /-->

    <link rel="stylesheet" href="css/dateAxisV.css" type="text/css"/>
    <link rel="StyleSheet" href="css/styles.css" type="text/css" />
    <link rel="StyleSheet" href="css/autocomplete.css" type="text/css" />
    <link rel="StyleSheet" href="css/layout-default-latest.css" type="text/css" />
    
    <!--[if IE ]>
    <link rel="StyleSheet" href="css/ie.css" type="text/css" />
    <![endif]-->


    <script src="js/jquery-1.5.1.min.js" type="text/javascript" ></script>
    <script src="js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>

    <script  src="js/settings.js" language="javascript" type="text/javascript"></script>
    <script src="js/jquery.mousewheel.js" type="text/javascript" ></script>
    <script src="js/jquery.splitter.js" type="text/javascript" ></script>
    <script src="js/jquery.cookie.js" type="text/javascript"></script>
    <script src="js/jquery.layout-latest.js" type="text/javascript" ></script>

    <script src="js/pageQuery.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/main.js" language="JavaScript" type="text/javascript"></script>
    <script  src="js/autocomplete.js" language="javascript" type="text/javascript"></script>

    <script type="text/javascript"  src="js/seadragon-min.js"></script>
    <script  src="js/pdf/pdf.js" language="javascript" type="text/javascript"></script>
    <title>Kramerius 4</title>
    
</head>
	<body>
	<script type="text/javascript">
    var dictionary = null;
	var dialogForm = null;

		// differ in different browsers
		function dialogHeight() {
			if (jQuery.browser.msie) {
				return 250;
			} else {
				return 170;				
			}
		}

		function loginDialog() {
			$("#dialogForm").show();
			if (dialogForm) {
				dialogForm("open");
			} else {
				dialogForm =  $("#dialogForm").dialog({
			        modal: true,
			        draggable:false,
				height:dialogHeight(),
				width:300,

			        resizable:false,
			        close: function(event, ui) { 
						var url = "search.jsp";    
						$(location).attr('href',url);
			        },
			        title:""
			    });
			}
			$("#dialogForm").dialog('option','title',dictionary['login.dialog.title']);
			$("#name").text(dictionary['login.dialog.name']);		    
            $("#password").text(dictionary['login.dialog.password']);           
            $("#button").val(dictionary['login.dialog.button']);           
		}

		$(document).ready(function(){

		    var i18nurl = "i18n?action=bundle&name=labels&format=json";
		    $.getJSON(i18nurl, function(data){
		    	dictionary=data.bundle;
		    	loginDialog();
	            <%if (request.getParameter("failure") != null) { %>
	                $("#status").html(dictionary['login.dialog.authenticationfailed']); 
	            <%}%>
		    });

		    
            var i18ntexts = "i18n?action=text&name=logininfo&format=json";
            $.getJSON(i18ntexts, function(data){
                $("#logininfo").html(data["text"].value); 
            });
            		    
		});
	</script>
   <!-- login info -->
   <div id="logininfo" style="text-align: center;"></div>

	<div id="dialogForm" style="display: none;">
	    
	    <form name=login id="loginForm" action="j_security_check" method="post">
	      <table align=center >
	        <tr>
	            <td>
	               <div id="status" style="color:red;"></div>
	            </td>   
            </tr>
            
            
            <tr>
	          <td>
	             <span id="name">Jméno:</span><br>
	             <input type="text" size="30" name="j_username" style="border:1px solid silver;" ><br>
	             <span id="password">Heslo:</span><br>
	             <input type="password" name="j_password" size="30" style="border:1px solid silver;"><br>
                 <div style="padding:3px;"></div>   
	             <input id="button" type="submit"  value="Přihlášení" alt="Login">
	          </td>
	        </tr>
	      </table>
	    </form>
	</div>
	</body>
</html>
