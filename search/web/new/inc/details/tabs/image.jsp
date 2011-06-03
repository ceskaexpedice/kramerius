<div id="bigThumbZone" class="viewer">
    <div id="container" ></div>

    <div id="securityError" style="display:none;">
        <fmt:message bundle="${lctx}" key="rightMsg"></fmt:message>
    </div>

    <div id="loadingDeepZoomImage">
        <fmt:message bundle="${lctx}" key="deep.zoom.loadingImage" />
    </div>

    <div id="pdfImage">
        <img id="pdfImageImg" onclick="showBornDigitalPDF('${itemViewObject.imagePid}','${itemViewObject.page}' )"
             onload='onLoadPDFImage()' border="0" alt="" src="${itemViewObject.firstPageImageUrl}" height="650px" />
        <img id="pdfZoomButton" border='0' alt="" onclick='showBornDigitalPDF("${itemViewObject.imagePid}","${itemViewObject.page}" )'  src='img/lupa_shadow.png' style='position:relative; left:-60px; top:30px;' />
    </div>

    <div id="plainImage" style="position:relative;text-align:center;">
        <img id="plainImageImg" onclick="switchDisplay(viewerOptions)" onload="onLoadPlainImage()" border="0"  src="../img/empty.gif" alt="" />

        <div style="position:absolute; top:10px; left:10px;">
            <span><img id="seadragonButton" border='0' onclick='switchDisplay(viewerOptions)'  src='../img/fullpage_grouphover.png' />
            </span>
            <span><img id="leftButtonPlainImage" class="prevArrow" src="../img/prev_grouphover.png" />
            </span>
            <span><img id="rightButtonPlainImage" class="nextArrow" src="../img/next_grouphover.png" />
            </span>
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
    });
    function onLoadPlainImage() {
        //if (imageInitialized) {
        //    $("#plainImageImg").fadeIn();
        //}
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
            "#securityError"],

        function(index,item) {
            if (item==contentToShow) {
                $(item).show();
            } else {
                $(item).hide();
            }
        });
        
    }

    function showPreviewImage(viewerOptions){
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
                
                    $("#plainImageImg").attr('src','../img/empty.gif');
                    if (viewerOptions.previewStreamGenerated) {
                        $("#plainImageImg").attr('src','../img?uuid='+viewerOptions.uuid+'&stream=IMG_PREVIEW&action=GETRAW');
                    } else {
                        // this should be directed by property or removed
                        $("#plainImageImg").attr('src','../img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700');
                    }
                

            }
        }
        imageInitialized = true;
    }

    function viewChanged(id){
        var uuid = id.split('_')[1];
        currentSelectedPage = uuid;
        $.ajax({
            url:"../viewInfo?uuid="+uuid,
            complete:function(req,textStatus) {
              
                if ((req.status==200) || (req.status==304)) {
                    viewerOptions = eval('(' + req.responseText + ')');
                    viewerOptions.uuid = uuid;
                    viewerOptions.fullid = id;
                    viewerOptions.status=req.status;
            	  
                    if ((viewerOptions.rights["read"][uuid]) && (viewerOptions.imgfull)) {
                        securedContent = false;
                        currentMime = req.responseText;
                    } else if (!viewerOptions.imgfull) {
                        currentMime = "unknown";
                        securedContent = false;
                        displayImageContainer("#noImageError");
                    } else {
                        currentMime = "unknown";
                        securedContent = true;
                        displayImageContainer("#securityError");
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
        viewer = new Seadragon.Viewer("container");
        viewer.clearControls();
        viewer.addControl(nextButton(),Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(prevButton(),Seadragon.ControlAnchor.TOP_RIGHT);
        viewer.addControl(viewer.getNavControl(),  Seadragon.ControlAnchor.TOP_RIGHT);

        //Seadragon.Config.maxZoomPixelRatio=1;
        //Seadragon.Config.imageLoaderLimit=1;

        // lokalizace
        Seadragon.Strings.setString("Tooltips.FullPage",dictionary["deep.zoom.Tooltips.FullPage"]);
        Seadragon.Strings.setString("Tooltips.Home",dictionary["deep.zoom.Tooltips.Home"]);
        Seadragon.Strings.setString("Tooltips.ZoomIn",dictionary["deep.zoom.Tooltips.ZoomIn"]);
        Seadragon.Strings.setString("Tooltips.ZoomOut",dictionary["deep.zoom.Tooltips.ZoomOut"]);

        Seadragon.Strings.setString("Errors.Failure",dictionary["deep.zoom.Errors.Failure"]);
        Seadragon.Strings.setString("Errors.Xml",dictionary["deep.zoom.Errors.Xml"]);
        Seadragon.Strings.setString("Errors.Empty",dictionary["deep.zoom.Errors.Empty"]);
        Seadragon.Strings.setString("Errors.ImageFormat",dictionary["deep.zoom.Errors.ImageFormat"]);
    }


</script>