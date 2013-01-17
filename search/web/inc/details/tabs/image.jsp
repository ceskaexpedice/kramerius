
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
<view:object name="alto" clz="cz.incad.Kramerius.views.inc.details.tabs.AltoSupportViewObject"></view:object>



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

function onLoadPlainImage() {
    var optsUnDefined = (typeof viewerOptions ==="undefined");
    
    if(!optsUnDefined){
        if (viewerOptions.hasAlto) plainImage.showAlto(viewerOptions.uuid, viewerOptions.alto);
    }
}

</script>

<script type="text/javascript">
    var altoSearchQuery = ${alto.searchQuery};
</script>


<c:forEach var="script" items="${image.scripturls}">
    <script type="text/javascript" src="${script}"></script>
</c:forEach>

<div id="bigThumbZone" class="viewer">
    <div id="container"  class="view_div"  style="display:none;  height: 512px;">
    </div>
    
    <div id="ol-container" style="display:none; position: relative; top: 20px; left: 10px;">
    
     <div id="ol-wrapper-buttons" class="buttons" style="z-index: 1002">
       <a id="zoomifyPlusButton" onclick="javascript:zoomInit.plus();" style="z-index: 1002"><span class="ui-icon ui-icon-plus" >+</span></a>
       <a id="zoomifyMinusButton" onclick="javascript:zoomInit.minus();" style="z-index: 1002"><span class="ui-icon ui-icon-minus" >-</span></a>
       <a id="zoomifyButtonPrev" onclick="javascript:previousImage();" style="z-index: 1002"><span class="ui-icon ui-icon-arrowthick-1-w" >prev</span></a>
       <a id="zoomifyButtonNext" onclick="javascript:nextImage();" style="z-index: 1002"><span class="ui-icon ui-icon-arrowthick-1-e" >next</span></a>
      </div>
   
    <div id="ol-wrapper" style="height: 512px; position: relative; top:-25px;">
        <div id="ol-image" style="width: 100%; height: 100%"></div>
    </div>
    
    </div>


    <div id="securityError" class="ui-state-error-text" style="display:none;">
        ${image.notAllowedMessageText}            
    </div>

    <div id="loadingDeepZoomImage" class="view_div" style="display: none;">
        <view:msg>deep.zoom.loadingImage</view:msg>  
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
            <view:msg>img.display.downloadOriginal.text</view:msg>
       </div>
       <div>
            <a id="downloadOriginalHref" href="none">
                <view:msg>img.display.downloadOriginal</view:msg>
            </a>
        </div>    
    </div>
    
</div>
<script type="text/javascript">
    $(document).ready(function(){
        $(".buttons>a").button();
        $('#bigThumbZone.viewer').bind('viewChanged', function(event, id){
            if (console) console.log('viewChanged event with id = '+id);
            viewChanged(id);
        });
        $('#bigThumbZone.viewer').bind('viewReady', function(event, viewerOptions){
            if (console) console.log('viewReady event ');
            showPreviewImage(viewerOptions);
            checkArrows();
        });
        $('#bigThumbZone>div.preview').bind('click', function(event, viewerOptions){
            if (console) console.log('click event ');
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
        if (console) console.log("showing '"+contentToShow+"'");
        $.each([
            "#loadingDeepZoomImage",
            "#plainImage",
            "#pdfImage",
            "#container",
            "#ol-container",
            "#noImageError",
            "#securityError",
            "#download"],

        function(index,item) {
            if (item==contentToShow) {
                if (console) console.log(" ok ");
                $(item).show();
            } else {
                $(item).hide();
            }
        });
        
    }

    function showPreviewImage(viewerOptions){
        if(!viewerOptions) return;
        if (plainImage != null)     plainImage.hideAlto();
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
                    if (zoomInit) {
                        zoomInit.init();
                    }
                    displayImageContainer('#${image.divContainer}');
                    if (viewerOptions.hasAlto) {
                        zoomInit.open(viewerOptions.uuid, viewerOptions.alto);
                    } else {
                        zoomInit.open(viewerOptions.uuid);
                    }

                } else {
                    displayImageContainer("#plainImage");
                    
                    $("#plainImageImg").attr('src','img/empty.gif');
                    if (viewerOptions.previewStreamGenerated) { 
                        $("#plainImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_PREVIEW&action=GETRAW');
                    } else {
                        // this should be directed by property or removed
                        $("#plainImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700');
                    }
                    plainImage = new PlainImageObject();
                    if(viewerOptions.hasAlto){
                        plainImage.showAlto(viewerOptions.uuid, viewerOptions.alto);
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
        if (console) console.log("changing view "+id);
        hidePreviewImage();
        displayImageContainer("#loadingDeepZoomImage");
        var uuid = id.split('_')[1];
        var viewInfoUrl = "viewInfo?uuid="+uuid+(altoSearchQuery == null ? "" : "&q="+altoSearchQuery);
        if (console) console.log("requesting view info   "+viewInfoUrl);
        $.ajax({
            url:viewInfoUrl,
            complete:function(req,textStatus) {
                              
                if ((req.status==200) || (req.status==304)) {
                    if (console) console.log("returning viewerOptions:"+viewerOptions);
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

    
    /** PlainImage alto */
    function PlainImageObject() { if (console) console.log('created plain alto'); } 

    PlainImageObject.prototype.showAlto = function(pid, options) {
        var img = 'plainImageImg';
        
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
        var q = $("#q").val();
        if($('#insideQuery').length>0) q =$('#insideQuery').val();
        if(q=="") return;

        var w = $('#'+img).width();
        var h = $('#'+img).height();

        
        
        var url = "inc/details/alto.jsp?q="+q+"&w="+w+"&h="+h+"&uuid=" + pid;
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
    PlainImageObject.prototype.hideAlto = function(pid, options) {
        $("#alto").html('');
        $("#alto").hide();
    }

    
    var plainImage = null;    

    /** Manages all next and prev buttons */
    function ImageButtons() {
        this.buttons = [];
        this.buttons['prev'] = [
            'seadragonButtonPrev',
            'plainButtonPrev',
            'fullButtonPrev',
            'zoomifyButtonPrev'
        ];
        
        this.buttons['next'] = [
             'seadragonButtonNext',
             'plainButtonNext',
             'fullButtonNext',
             'zoomifyButtonNext'
        ];
    }

    ImageButtons.prototype._hideArr=function(arr) {
        $.each(arr, function(index,item) { $("#"+item).hide();});
    }
    
    ImageButtons.prototype._showArr=function(arr) {
        $.each(arr, function(index,item) { $("#"+item).show();});
    }
    
    ImageButtons.prototype.hideNext=function() {
        var arr = this.buttons['next'];
        this._hideArr(arr);
    }

    ImageButtons.prototype.showNext=function() {
        var arr = this.buttons['next'];
        this._showArr(arr);
    }


    ImageButtons.prototype.hidePrev=function() {
        var arr = this.buttons['prev'];
        this._hideArr(arr);
    }
    
    ImageButtons.prototype.showPrev=function() {
        var arr = this.buttons['prev'];
        this._showArr(arr);
    }
    
    function checkArrows(){
        if (console) console.log("activepids: "+k4Settings.activeUuids);
        if(k4Settings.activeUuids[0]==k4Settings.activeUuid){
            new ImageButtons().hidePrev();
        }else{
            new ImageButtons().showPrev();
        }
        if(k4Settings.activeUuids[k4Settings.activeUuids.length-1]==k4Settings.activeUuid){
            new ImageButtons().hideNext();
        }else{
            new ImageButtons().showNext();
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