<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>
<script type="text/javascript" language="javascript" src="gwtviewers/gwtviewers.nocache.js"></script>
 
<script type="text/javascript">
	var __gwtViewersUUID = "${param.pid}";

	var __confHeight = 125;
	var __confWidth = 400;
	var __confDistance = 5;
	var __confDistance = 5;

	var __confNumberOfImages = 7;	
	var __confMoveStep = 1;
	
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


<script type="text/javascript">

	function _jqueryDialog() {
		$("#view").attr("src","na.png");
		$("#dialog").dialog({
			bgiframe: true,
			height: 140,
			modal: true,
			title:__dialog_title;
		});
	};

	
</script>


<button id="ahoj" onclick="_jqueryDialog();"> AHOJ </button>

<div id="dialog" title="Basic modal dialog">
	<img id="view" alt="" src=""></img>
</div>


