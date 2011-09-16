<div id="bigThumbZone" class="viewer">
    <div id="container"  class="view_div"  style="display:none; min-height: 512px; min-width: 512px; width: 512px; height: 512px;">
    </div>

    <div id="securityError" style="display:none;">
        <fmt:message bundle="${lctx}" key="rightMsg"></fmt:message>
    </div>

    <div id="loadingDeepZoomImage" class="view_div">
        <fmt:message bundle="${lctx}" key="deep.zoom.loadingImage" />
    </div>

    <div id="pdfImage" style="position:relative;text-align:center;">
        <img class="view_div" id="pdfImageImg" onclick="showBornDigitalPDF()"
             onload='onLoadPDFImage()' border="0" alt="" src="${itemViewObject.firstPageImageUrl}" height="650px" />
        <div style="position:absolute; top:10px; left:10px;">
            <img id="pdfZoomButton" border='0' alt="" onclick='showBornDigitalPDF()'  src='img/mime/application/pdf.png' />
        </div>
    </div>

    <div id="plainImage" style="position:relative;text-align:center;">
        <img id="plainImageImg" class="view_div" onclick="showFullImage()" onload="onLoadPlainImage()" border="0"  src="img/empty.gif" alt="" />

        <div style="position:absolute; top:10px; left:10px;">
            <span><img id="seadragonButton" border='0' onclick='showFullImage()'  src='img/fullpage_grouphover.png' />
            </span>
            <span><img id="leftButtonPlainImage" class="prevArrow" onclick="previousImage()" src="img/prev_grouphover.png" />
            </span>
            <span><img id="rightButtonPlainImage" class="nextArrow" onclick="nextImage()" src="img/next_grouphover.png" />
            </span>
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
        $('#bigThumbZone.viewer').bind('viewChanged', function(event, id){
            viewChanged(id);
        });
        $('#bigThumbZone.viewer').bind('viewReady', function(event, viewerOptions){
            showPreviewImage(viewerOptions);
        });
        $('#bigThumbZone>div.preview').bind('click', function(event, viewerOptions){
            showPreviewImage(viewerOptions);
        });
    });
    
    function showBornDigitalPDF(pid, page){
        if  (!page) {
		page = "1";	
	}
	var url ='img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=GETRAW#page='+page;
	var pdfWindow = window.open(url, '_blank');
	pdfWindow.focus();
    }
    
    function onLoadPDFImage(){
        
    }
    
    function onLoadPlainImage() {
        if(viewerOptions.hasAlto){
            showAlto(viewerOptions.uuid, 'plainImageImg');
        }
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
        } else if (!viewerOptions.displayableContent) {
            // no displayable content
            displayImageContainer("#noImageError");
        } else {
            // has right
        	if (viewerOptions.isContentPDF()) {
                displayImageContainer("#pdfImage");
                if (viewerOptions.previewStreamGenerated) {
                    $("#pdfImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_PREVIEW&action=GETRAW');
                } else {
                    $("#pdfImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700');
                }
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

        function prevButton() {
            var control = document.createElement("img");
            control.setAttribute('src','img/prev_grouphover.png');
            control.setAttribute('id','prevButton');

            control.onmouseover = function(event) {
                document.getElementById('prevButton').setAttribute('src','img/prev_hover.png');
            };
            control.onmouseout =function(event) {
                document.getElementById('prevButton').setAttribute('src','img/prev_grouphover.png');
            };
            control.onclick = function(event) {
                selectPrevious();
            };


            control.className = "control prevArrow";
            return control;
        }


        function nextButton() {
            var control = document.createElement("img");
            control.setAttribute('src','img/next_grouphover.png');
            control.setAttribute('id','nextButton');

            control.className = "control nextArrow";

            control.onmouseover = function(event) {
                document.getElementById('nextButton').setAttribute('src','img/next_hover.png');
            };
            control.onmouseout =function(event) {
                document.getElementById('nextButton').setAttribute('src','img/next_grouphover.png');
            };
            control.onclick = function(event) {
                selectNext();
            };

            return control;
        }

    	
    	
        viewer = new Seadragon.Viewer("container");
        viewer.clearControls();
        viewer.addControl(nextButton(),Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(prevButton(),Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(viewer.getNavControl(),  Seadragon.ControlAnchor.TOP_RIGHT);

        
        //Seadragon.Config.maxZoomPixelRatio=1;
        //Seadragon.Config.imageLoaderLimit=1;

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
    
    function nextImage(){
        var id;
        for(var i=0; i<k4Settings.activeUuids.length-1; i++){
            if(k4Settings.activeUuids[i]==k4Settings.activeUuid){
                index = i;
                id = k4Settings.activeUuids[i+1];
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