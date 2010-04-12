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
	
        var currentSelectedPage;
        var currentSelectedParent;
        
	function changeSelection(masterUuid, selection) {
          currentSelectedPage = selection;
            currentSelectedParent = masterUuid;
		requestToSelect(masterUuid, selection);
        }
        
        // callbacks from component
        
        var prev = '<span style="padding:15px;cursor:pointer;" onclick="selectPrevious();"><img src="img/la.png" /></span>';
        var next = '<span style="padding:15px;cursor:pointer;" onclick="selectNext();"><img src="img/ra.png" /></span>';
	function selectPage(uuid){
            var pageUrl = "<%=KConfiguration.getKConfiguration().getDJVUServletUrl()%>?uuid="+uuid+"&scaledHeight=600";
            var img = '<img src="'+pageUrl+'" height="600px" />';
            $('#mainContent').html('<div align="center" style="">' + prev + img + next + '</div>');
            changeSelectedPage(uuid);
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
