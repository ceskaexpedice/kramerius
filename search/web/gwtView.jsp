<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>
<script type="text/javascript" language="javascript" src="gwtviewers/gwtviewers.nocache.js"></script>
<script type="text/javascript">

	var __gwtViewersUUIDPATH = "${empty param.pid_path ? param.pid : param.pid_path }";
	var __confHeight = 125;
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
            //changeSelection(uuid);
            
            var pageUrl = fedoraImg + uuid + '/IMG_FULL';
            var img = '';
            if(format=='image/djvu' || format =="" || format==null){
                img = '<div style="width:100%; height:500px;">'+
                '<object width="100%" border="0" height="100%" style="border: 0px none ;" codebase="http://www.lizardtech.com/download/files/win/djvuplugin/en_US/DjVuControl_en_US.cab" classid="clsid:0e8d0700-75df-11d3-8b4a-0008c7450c4a" id="docframe" name="docframe">'+
                '<param name="src" value="'+pageUrl+'" />'+
                '<param name="zoom" value="100" />'+ 
                '<embed width="100%" height="100%" ' +  
                'src="'+pageUrl+'" type="image/vnd.djvu" id="docframe2" name="docframe2"/>'+
                '<br/></object></div>';
            }else if(format=='image/jpeg'){
                img = '<div align="center"><img src="'+pageUrl+'" width="400px" /></div>';
            }else{
                img = '<div align="center"><img src="'+pageUrl+'" width="400px" /></div>';
            }
            $('#mainContent').html(img);
            
            changeSelectedPage(masterUuid, selection);
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