<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>

<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%><script type="text/javascript" language="javascript" src="gwtviewers/gwtviewers.nocache.js"></script>
<script type="text/javascript">

	var __gwtViewersUUIDPATH = "${empty param.pid_path ? param.pid : param.pid_path }";
	var __confHeight = 140;
	var __confWidth = 400;
	var __confDistance = 10;
	var __confNumberOfImages = 7;	
	var __confMoveStep = 1;
	var __debug = false;

	function changeSelection(masterUuid, selection) {
		requestToSelect(masterUuid, selection);
        }
	// callbacks from component
	function selectPage(masterUuid, format){
            var pageUrl = "<%=KConfiguration.getKConfiguration().getDJVUServletUrl()%>?uuid="+masterUuid+"&scaledHeight=600";
            var img = '<div align="center"><img src="'+pageUrl+'" height="600px" /></div>';
            $('#mainContent').html(img);
            changeSelectedPage(masterUuid);
	}
	function pages(from, to){  }
</script>
 
<table align="center">
	<tr>
		<td colspan="3" id="label"></td>
	</tr>
	<tr>
		<td id="container"></td>
	</tr>
	<tr>
		<td><div id="slider" style="width: 100%"></div> </td>
	</tr>
</table>