<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>
<script type="text/javascript" language="javascript" src="gwtviewers/gwtviewers.nocache.js"></script>
 
<script type="text/javascript">
	//var __gwtViewersUUID = "uuid:${param.pid}";
	//pid_path=0eaa6730-9068-11dd-97de-000d606f5dc6&
	var __gwtViewersUUIDPATH = "${empty param.pid_path ? param.pid : param.pid_path };

	
	var __confHeight = 125;
	var __confWidth = 400;
	var __confDistance = 5;

	var __confNumberOfImages = 7;	
	var __confMoveStep = 1;

    // informace o vyberu   
	function selectPage(uuid){
	    alert("selectuju " + uuid);
	}

	// informace o prave zobrazovanych strankach
	function pages(from, to) {
	}
	
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




