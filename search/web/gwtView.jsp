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
        var currentMime = "unknown";
	function selectPage(uuid, mimetype){
            var pageUrl = "djvu?uuid="+uuid+"&scaledHeight=600";
            var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
            //var fullUrl = "<%=KConfiguration.getKConfiguration().getDJVUServletUrl()%>?uuid="+uuid+"&outputFormat=RAW";
            //var img = '<a href="javascript:showImageFull(\''+fullUrl+'\', \''+ mimetype +'\'"><img src="'+pageUrl+'" height="600px" /></a>';
            var img = '<a class="lighbox" href="javascript:showFullImage(\''+uuid+'\')"><img id="imgBig" src="'+pageUrl+'" height="600px" border="0"  /></a>';
            //var img = '<a class="lighbox" href="'+fullUrl+'" target="_blank" ><img id="imgBig" src="'+pageUrl+'" height="600px" border="0" mimetype="unknown" /></a>';
            $('#mainContent').html('<div align="center" style="">' + prev + img + next + '</div>');
            //$('.lighbox').lightBox();
            
            $.get(mimeUrl, function(data){
                currentMime = data;
            });
            changeSelectedPage(uuid);
	}
        var fullDialog;
        function showFullImage(uuid){
            
            var fullUrl = "djvu?uuid="+currentSelectedPage+"&outputFormat=RAW";
            $('#mainItemTable').hide();
            //return;
            if(fullDialog){
                fullDialog.dialog('open');
            }
            else{
                fullDialog = $('#fullImageContainer').dialog({
                    left:0, 
                    top:0, 
                    height:$(window).height()-20, 
                    width:$(window).width()-17,
                    modal:true,
                    resizable:false,
                    draggable:false,
                    close: function(event, ui) {$('#mainItemTable').show(); }

                });
                $('.ui-dialog-titlebar').append('<a href="javascript:previousFull();" class=" ui-corner-all ui-dialog-titlebar-prev"><span class="ui-icon ui-icon-arrowthick-1-w">prev</span></a>' +
                                                '<a href="javascript:nextFull();" class=" ui-corner-all ui-dialog-titlebar-next"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>');

            }
            //alert(currentMime);
            if(currentMime== 'image/djvu'){
                //$('#djvuContainer').hide();
                $('#djvuContainer>object>param[name="src"]').attr('value', fullUrl);
                $('#djvuContainer>object>embed').attr('src', fullUrl);
                $('#djvuContainer').show();
            }else{
                $('#imgContainer>img').attr('src', fullUrl);
                $('#imgContainer').show();
            }
        }
        function previousFull(){
            selectPrevious();
            showFullImage();
        }
        function nextFull(){
            selectNext();
            showFullImage();
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