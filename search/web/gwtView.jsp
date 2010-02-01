<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>
<script type="text/javascript" language="javascript" src="gwtviewers/gwtviewers.nocache.js"></script>
 
<script type="text/javascript">
	var __gwtViewersUUID = "${param.pid}";

	var __confHeight = 200;
	var __confWidth = 400;
	var __confDistance = 5;
	var __confDistance = 5;
	var __thumbUrl = 'http://localhost:8080/search/thumb';

</script>
 
<h2> GWT Prohlizecka</h2>
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
		$(document).ready(function(){
			if ((window.loadMyBusinessWidget != 'undefined') && 
				(window.loadMyBusinessWidget != null)) {
				alert(' metoda definovana ...');	
			}	
			window.loadMyBusinessWidget();                        
		});
</script>



</table>