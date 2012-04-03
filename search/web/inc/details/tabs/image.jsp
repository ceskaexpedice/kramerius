
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="image" clz="cz.incad.Kramerius.views.inc.details.tabs.ImageViewObject"></view:object>



<style type="text/css">
    .buttons>a{
        margin-right: 3px;
    }
    
    .buttons>a>span.ui-button-text{
        padding:3px;
    }

/*    
    span.ui-button-text {
        padding: 3px;
    }
    */
    
</style>

<script type="text/javascript">
function onLoadPDFImage(){
    
}

function onLoadPlainImage() {
    if(viewerOptions.hasAlto){
        showAlto(viewerOptions.uuid, 'plainImageImg');
    }
}

</script>

<div id="bigThumbZone" class="viewer">
    <div id="container"  class="view_div"  style="display:none;  height: 512px;">
    </div>

    <div id="securityError" style="display:none;">
        ${image.notAllowedMessageText}            
    </div>

    <div id="loadingDeepZoomImage" class="view_div">
        <fmt:message bundle="${lctx}" key="deep.zoom.loadingImage" />
    </div>

    <div id="pdfImage" style="position:relative;text-align:center;">
        <img class="view_div" id="pdfImageImg" onclick="showBornDigitalPDF(viewerOptions.uuid,${image.pagePid ? image.page : "1"})"
             onload='onLoadPDFImage()' border="0" alt="" src="" height="650px" />
        
    </div>

    <div id="plainImage" style="position:relative;text-align:center;">
        <img id="plainImageImg" class="view_div" onclick="showFullImage()" onload="onLoadPlainImage()" border="0"  src="img/empty.gif" alt="" />

        <div class="buttons" style="position:absolute; top:10px; left:10px;">
            <a id="plainButtonFull" onclick="javascript:showFullImage();"><span class=" ui-icon ui-icon-arrow-4-diag" >full</span></a>
            <a id="plainButtonPrev" onclick="javascript:previousImage();"><span class="ui-icon ui-icon-arrowthick-1-w" >prev</span></a>
            <a id="plainButtonNext" onclick="javascript:nextImage();"><span class="ui-icon ui-icon-arrowthick-1-e" >next</span></a>
        </div>
    </div>
   
   <div id="download" style="display:none;padding-top:10px; height:650px; width:700px;  color: black; border:1px; position:relative;">
        
       <div> 
           <fmt:message bundle="${lctx}" key="img.display.downloadOriginal.text"></fmt:message> 
       </div>
       <div>
           <a id="downloadOriginalHref" href="none"><fmt:message bundle="${lctx}" key="img.display.downloadOriginal"></fmt:message></a>
        </div>    
    </div>
    
</div>
<script type="text/javascript">
    $(document).ready(function(){
        $(".buttons>a").button();
        $('#bigThumbZone.viewer').bind('viewChanged', function(event, id){
            viewChanged(id);
        });
        $('#bigThumbZone.viewer').bind('viewReady', function(event, viewerOptions){
            showPreviewImage(viewerOptions);
            checkArrows();
        });
        $('#bigThumbZone>div.preview').bind('click', function(event, viewerOptions){
            showPreviewImage(viewerOptions);
        });
        
    });
    
    function showBornDigitalPDF(pid, page){
        if  (!page) {
		page = "1";	
	}
	var url ='nimg/IMG_FULL/'+viewerOptions.uuid+'#page='+page;
    
	var pdfWindow = window.open(url, '_blank');
	pdfWindow.focus();
    }
    

    function displayImageContainer(contentToShow) {
        
        $.each([
            "#loadingDeepZoomImage",
            "#plainImage",
            "#pdfImage",
            "#container",
            "#noImageError",
            "#securityError",
            "#download"],

        function(index,item) {
            if (item==contentToShow) {
                $(item).show();
            } else {
                $(item).hide();
            }
        });
        
    }

    function showPreviewImage(viewerOptions){
        if(!viewerOptions) return;
        hideAlto();
        if (!viewerOptions.rights["read"][viewerOptions.pid]) {
            // no right
            displayImageContainer("#securityError");
        } else if (!viewerOptions.displayableContent) {
            // no displayable content
            displayImageContainer("#download");
            var url ='img?action=GETRAW&stream=IMG_FULL&pid='+viewerOptions.uuid;
            $('#downloadOriginalHref').attr('href',url);
        } else if (!viewerOptions.displayableContent) {
            // no displayable content
            displayImageContainer("#noImageError");
            
        } else {
            // has right
        	if (viewerOptions.isContentPDF()) {
                displayImageContainer("#pdfImage");

                var requestedImg = null;
                var pagePid = ${image.pagePid};
                // neni page pid
                if ((!pagePid) && viewerOptions.previewStreamGenerated) {
                    requestedImg = 'img?uuid='+viewerOptions.uuid+'&stream=IMG_PREVIEW&action=SCALE&scaledHeight=700';
                } else if ((!pagePid) && (!viewerOptions.previewStreamGenerated)) {
                	requestedImg = 'img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700&page=1';
                } else {
                    requestedImg = 'img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700&page=${image.pageInt}';
                }      
                
                $("#pdfImageImg").attr('src',requestedImg);
                
            } else {
                var tilesPrepared = viewerOptions.deepZoomGenerated || viewerOptions.imageServerConfigured;
                var deepZoomDisplay = ((viewerOptions.deepZoomCofigurationEnabled) && (tilesPrepared));
                if (deepZoomDisplay) {
                    if (viewer == null) {
                        initViewer();
                    }
                    displayImageContainer("#container");
                    viewer.openDzi("deepZoom/"+viewerOptions.uuid+"/");
                } else {
                    displayImageContainer("#plainImage");
                    
                    $("#plainImageImg").attr('src','img/empty.gif');
                    if (viewerOptions.previewStreamGenerated) { 
                        $("#plainImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_PREVIEW&action=GETRAW');
                    } else {
                        // this should be directed by property or removed
                        $("#plainImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700');
                    }
                    if(viewerOptions.hasAlto){
                        showAlto(viewerOptions.uuid, 'plainImageImg');
                    }

                }
            }

        }
            
        imageInitialized = true;
    }
    
    function hidePreviewImage(){
        var tilesPrepared = viewerOptions.deepZoomGenerated || viewerOptions.imageServerConfigured;
        var deepZoomDisplay = ((viewerOptions.deepZoomCofigurationEnabled) && (tilesPrepared));
        if (deepZoomDisplay) {
            
        } else {
            $("#plainImageImg").attr('src','img/empty.gif');
        }
        
    }
    
    function showLoadingImage(){
        $("#loadingDeepZoomImage").show();
    }

    function viewChanged(id){
        hidePreviewImage();
        displayImageContainer("#loadingDeepZoomImage");
        var uuid = id.split('_')[1];
        $.ajax({
            url:"viewInfo?uuid="+uuid,
            complete:function(req,textStatus) {
              
                if ((req.status==200) || (req.status==304)) {
                    viewerOptions = eval('(' + req.responseText + ')');
                    viewerOptions.uuid = uuid;
                    viewerOptions.fullid = id;
                    viewerOptions.status=req.status;

                    // TODO: Vyhodit            	  
                    if ((viewerOptions.rights["read"][uuid]) && (viewerOptions.imgfull)) {
                        securedContent = false;
                        currentMime = req.responseText;
                    } else if (!viewerOptions.imgfull) {
                        currentMime = "unknown";
                        securedContent = false;
                    } else {
                        currentMime = "unknown";
                        securedContent = true;
                    }
                } else if (req.status==404){
                    alert("Neni velky nahled !");
                }
                k4Settings.activeUuid = id;
                
                $(".viewer").trigger('viewReady', [viewerOptions]);
            }
        });
    }

    var viewer = null;

    
    
    function initViewer() {

    	var zooming = false; // whether we should be continuously zooming
    	var zoomFactor = null; // how much we should be continuously zooming by
    	var lastZoomTime = null;
     
        function prevButton() {
            var control = document.createElement("a");
            $(control).html("<span class='ui-icon ui-icon-arrowthick-1-w' >full</span>")

            control.onclick = function(event) {
                previousImage();
            };

            $(control).attr('id','seadragonButtonPrev');
            $(control).button();
            return control;
        }

        function goHomeButton() {
            var control = document.createElement("a");
            $(control).html("<span class='ui-icon ui-icon-home' >full</span>")
            control.setAttribute('id','goHome');
            control.onclick = function(event) {
                if (viewer.viewport) {
                    viewer.viewport.goHome();
                }
            };
            $(control).button();
            
            return control;
        }
        
        function nextButton() {
            var control = document.createElement("a");
            
            var span = document.createElement("span");
            $(control).html("<span class='ui-icon ui-icon-arrowthick-1-e' >full</span>")
            control.setAttribute('id','nextButton');

            control.className = "control";
            control.onclick = function(event) {
                nextImage();
            };

            $(control).attr('id','seadragonButtonNext');
            $(control).button();
            return control;
        }


        function fullPageButton() {
            var control = document.createElement("a");
            $(control).html("<span class='ui-icon ui-icon-arrowthick-2-ne-sw' >full</span>")
            control.setAttribute('id','fullPageButton');
         
            control.className = "control";
            control.onclick = function(event) {
            	viewer.setFullPage(!viewer.isFullPage());
                 
                if (viewer.viewport) {
                    viewer.viewport.ensureVisible();
                }
            };

            $(control).button();
            return control;
        }    	

        function endZooming() {
        	zooming = false;
      	}

        function scheduleZoom() {
        	window.setTimeout(doZoom, 10);
       	}

        function doZoom() {
            if (zooming && viewer.viewport) {
             var currentTime = new Date().getTime();
             var deltaTime = currentTime - lastZoomTime;
             var adjustedFactor = Math.pow(zoomFactor, deltaTime / 1000);
             
             viewer.viewport.zoomBy(adjustedFactor);
             viewer.viewport.ensureVisible();
             lastZoomTime = currentTime;
             scheduleZoom();
             }
       	}


        function beginZoomingIn() {
        	lastZoomTime = new Date().getTime();
        	zoomFactor = Seadragon.Config.zoomPerSecond;
        	zooming = true;
        	scheduleZoom();
       	}
        	 

      	function beginZoomingOut() {
        	lastZoomTime = new Date().getTime();
        	zoomFactor = 1.0 / Seadragon.Config.zoomPerSecond;
        	zooming = true;
        	scheduleZoom();
      	}
           
        function zoomInButton() {
        	var control = document.createElement("a");
            var span = document.createElement("span");
            $(control).html("<span class='ui-icon ui-icon-plusthick' >full</span>")
            control.setAttribute('id','plusButton');
         
            control.className = "control";

            var tracker = new Seadragon.MouseTracker(control);
            tracker.clickHandler = function(tracker, position, quick, shift) {
                if (viewer.viewport) {
                    zooming = false;
                    viewer.viewport.zoomBy(Seadragon.Config.zoomPerClick / 1.0);
                    viewer.viewport.ensureVisible();
                }
            }         

            tracker.pressHandler = function(tracker, position) {
            	beginZoomingIn();
            }

            tracker.enterHandler = function(tracker,position, buttonDownElm, buttonDownAny) {
                beginZoomingIn();
            }


            tracker.releaseHandler = function(tracker, position, insideElmtPress,insideElmtRelease ) {
            	endZooming();
            }

            tracker.exitHandler = function(tracker, position, buttonDownElmt, buttonDownAny) {
                endZooming();
            }
            
            
            $(control).button();
            tracker.setTracking(true);
            return control;
        }
        

        function zoomOutButton() {
            var control = document.createElement("a");
            var span = document.createElement("span");
            $(control).html("<span class='ui-icon ui-icon-minusthick' >full</span>")
            control.setAttribute('id','plusButton');
         
            control.className = "control";

            var tracker = new Seadragon.MouseTracker(control);
            tracker.clickHandler = function(tracker, position, quick, shift) {
            	if (viewer.viewport) {
            		zooming = false;
            		viewer.viewport.zoomBy(1.0 / Seadragon.Config.zoomPerClick);
            		viewer.viewport.ensureVisible();
           		}           
          }   

            tracker.pressHandler = function(tracker, position) {
                beginZoomingOut();
            }

            tracker.enterHandler = function(tracker,position, buttonDownElm, buttonDownAny) {
                beginZoomingOut();
            }


            tracker.releaseHandler = function(tracker, position, insideElmtPress,insideElmtRelease ) {
                endZooming();
            }

            tracker.exitHandler = function(tracker, position, buttonDownElmt, buttonDownAny) {
                endZooming();
            }
            
            
            $(control).button();
            tracker.setTracking(true);
            return control;
        }
        
        viewer = new Seadragon.Viewer("container");
        viewer.clearControls();
        viewer.addControl(nextButton(),Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(prevButton(),Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(goHomeButton(),  Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(fullPageButton(),  Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(zoomOutButton(),Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(zoomInButton(),Seadragon.ControlAnchor.TOP_RIGHT);

        
        

        // lokalizacenextImage
        Seadragon.Strings.setString("Tooltips.FullPage",dictionary["deep.zoom.Tooltips.FullPage"]);
        Seadragon.Strings.setString("Tooltips.Home",dictionary["deep.zoom.Tooltips.Home"]);
        Seadragon.Strings.setString("Tooltips.ZoomIn",dictionary["deep.zoom.Tooltips.ZoomIn"]);
        Seadragon.Strings.setString("Tooltips.ZoomOut",dictionary["deep.zoom.Tooltips.ZoomOut"]);

        Seadragon.Strings.setString("Errors.Failure",dictionary["deep.zoom.Errors.Failure"]);
        Seadragon.Strings.setString("Errors.Xml",dictionary["deep.zoom.Errors.Xml"]);
        Seadragon.Strings.setString("Errors.Empty",dictionary["deep.zoom.Errors.Empty"]);
        Seadragon.Strings.setString("Errors.ImageFormat",dictionary["deep.zoom.Errors.ImageFormat"]);
    }

    function hideAlto(){
        $("#alto").html('');
        $("#alto").hide();
    }

    function showAlto(uuid, img){
        var q = $("#q").val();
        if($('#insideQuery').length>0) q =$('#insideQuery').val();
        if(q=="") return;

        var w = $('#'+img).width();
        var h = $('#'+img).height();
        var url = "inc/details/alto.jsp?q="+q+"&w="+w+"&h="+h+"&uuid=" + uuid;
        $.get(url, function(data){
            if(data.trim()!=""){
                if($("#alto").length==0){
                    $("#bigThumbZone").append('<div id="alto" style="position:absolute;z-index:1003;overflow:hidden;" onclick="showFullImage()"></div>');
                }
                positionAlto(img);
                $("#alto").html(data);
                $("#alto").show();
            }
        });
    }

    function positionAlto(){
        var img = '.view_div:visible';
        var h = $(img).height();
        var t = $(img).offset().top - $("#preview").offset().top - 4;
        var w = $(img).width();
        var l = $(img).offset().left - $("#preview").offset().left - 4;
        
            $("#alto").css('width', w);
            $("#alto").css('height', h);
            $("#alto").css('left', l);
            $("#alto").css('top', t);
    }
    
    function checkArrows(){
        if(k4Settings.activeUuids[0]==k4Settings.activeUuid){
            $('#plainButtonPrev').hide();
            $('#seadragonButtonPrev').hide();
            $('#fullButtonPrev').hide();
        }else{
            $('#plainButtonPrev').show();
            $('#seadragonButtonPrev').show();
            $('#fullButtonPrev').show();
        }
        
        if(k4Settings.activeUuids[k4Settings.activeUuids.length-1]==k4Settings.activeUuid){
            $('#plainButtonNext').hide();
            $('#seadragonButtonNext').hide();
            $('#fullButtonNext').hide();
        }else{
            $('#plainButtonNext').show();
            $('#seadragonButtonNext').show();
            $('#fullButtonNext').show();
        }
    }
    
    function nextImage(){
        var id;
        for(var i=0; i<k4Settings.activeUuids.length-1; i++){
            if(k4Settings.activeUuids[i]==k4Settings.activeUuid){
                index = i;
                id = k4Settings.activeUuids[i+1];
                initView = false;
                $(".viewer").trigger('viewChanged', [id]);
                break;
            }
        }
    }
    
    function previousImage(){
        var id;
        for(var i=1; i<k4Settings.activeUuids.length; i++){
            if(k4Settings.activeUuids[i]==k4Settings.activeUuid){
                index = i;
                id = k4Settings.activeUuids[i-1];
                initView = false;
                $(".viewer").trigger('viewChanged', [id]);
                break;
            }
        }
        
    }
    
    var fullDialog;
    var vertMargin = 20;
    var horMargin = 17;
    var fullImageWidth;
    var fullImageHeight;
    var maxScroll = 0;
    function hideFullImage(){
        $('#main').show();
        $('#footer').show();
        $('#fullImageContainer').hide();
    }
    function showFullImage(){
        $('#main').hide();
        $('#footer').hide();
        
        $('#fullImageContainer').show();
        $("#fullImageContainer>div.fullContent").css("height", $(window).height()-
            $('#fullImageContainer>div.header').outerHeight(true));
        updateFullImage();
    }
</script>