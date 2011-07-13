<div id="bigThumbZone" class="viewer" style="overflow:auto;">
    <div id="container" style="padding-top:10px; height: 434px;  color: black; display:none;"></div>

    <div id="securityError" style="padding-top:10px; height: 400px; width:700px; color: black; display:none;">
        <fmt:message bundle="${lctx}" key="rightMsg"></fmt:message>
    </div>
    <div id="noImageError" style="padding-top:10px; height: 400px; width:700px; color: black; display:none;">
        <fmt:message bundle="${lctx}" key="img.display.noimage"></fmt:message>
    </div>
    <div id="loadingDeepZoomImage" style="padding-top:10px; height: 500px; width:700px; color: black; display:none;">
        <fmt:message bundle="${lctx}" key="deep.zoom.loadingImage"></fmt:message>
    </div>

    <div id="pdfImage" style="padding-top:10px; height:650; width:700px;  color: black; border:1px; position:relative; display:none; overflow:hidden;">
        <img id="pdfImageImg"
             onclick='showBornDigitalPDF("${itemViewObject.imagePid}","${itemViewObject.page}" )'
             onload='onLoadPDFImage()'
             border="0"  src="${itemViewObject.firstPageImageUrl}" height="650px" ></img>
        <img id="pdfZoomButton" border='0' onclick='showBornDigitalPDF("${itemViewObject.imagePid}","${itemViewObject.page}" )'  src='img/lupa_shadow.png' style='position:relative; left:-60px; top:30px;'></img>
    </div>

    <div id="plainImage" style="padding-top:10px; height:650; width:700px;  color: black; border:1px; position:relative;">
        <img id="plainImageImg"
             onclick="switchDisplay(viewerOptions)"
             onload="onLoadPlainImage()"
             border="0"  src="img/empty.gif" alt="" />

        <div style="position:absolute; top:10px; right:0px;">
            <span>
                <img id="seadragonButton" border='0' onclick='switchDisplay(viewerOptions)'  src='img/fullpage_grouphover.png'></img>
            </span>
            <span>
                <img id="leftButtonPlainImage" class="prevArrow" src="img/prev_grouphover.png" />
            </span>
            <span>
                <img id="rightButtonPlainImage" class="nextArrow" src="img/next_grouphover.png" />
            </span>
        </div>
    </div>
    </div>