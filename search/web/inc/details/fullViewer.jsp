<%@page contentType="text/html" pageEncoding="UTF-8"%>
<style type="text/css">
    #fullImageContainer>div.header{
        height:25px;
        padding-top:5px;
        padding-right:10px;
        border-bottom:1px solid #E66C00;
    }
    #fullImageContainer>div.header>div.title{
        float:left;
        padding-left: 30px;
        font-size: 1.2em;
    }
    #fullImageContainer>div.header>div.buttons{
        float:right;
        
    }
    #fullImageContainer>div.header>div.buttons>a{
        float:left;
    }
     
         
</style>
<div class="header">
    <div class="title"></div>
    <div class="buttons">
    <div id="divFullImageZoom" style="float:left;">
        <span><fmt:message bundle="${lctx}">velikost</fmt:message>: 
            <select onchange="changeFullImageZoom()" id="fullImageZoom">
                <option value="width"><fmt:message bundle="${lctx}">šířka okna</fmt:message></option>
                <option value="height"><fmt:message bundle="${lctx}">výška okna</fmt:message></option>
                <option value="0.1">10%</option>
                <option value="0.2" >20%</option>
                <option value="0.3" >30%</option>
                <option value="0.4" >40%</option>
                <option value="0.5" >50%</option>
                <option value="0.6" >60%</option>
                <option value="0.7" >70%</option>
                <option value="0.8" >80%</option>
                <option value="0.9" >90%</option>
                <option value="1" selected="selected" >100%</option>
            </select>
        </span>
    </div>
    <a href="javascript:previousFullImage();"><span class="ui-icon ui-icon-arrowthick-1-w">previous</span></a>
    <a href="javascript:nextFullImage();"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>
    <a href="javascript:hideFullImage();"><span class="ui-icon ui-icon-closethick">close</span></a>
    </div>
</div>
<div id="djvuContainer" style="display:none;">
    <iframe src="" frameborder="0" width="100%" height="100%"></iframe>
</div>

<c:if test="${param.format == 'application/pdf'}">
    <div id="pdfContainer" style="display:none;">
        <%--input type="hidden" id="pdfPage" name="pdfPage" value="${itemViewObject.page}" /--%>
        <iframe src="" width="100%" height="100%"></iframe>
    </div>
</c:if>
<div id="imgContainer" style="display:none;" align="center">
    <img id="imgFullImage" src="img/empty.gif" />
</div>

<script type="text/javascript">
    $(document).ready(function(){
        $('#fullImageContainer.viewer').bind('viewReady', function(event, viewerOptions){
            updateFullImage();
        });
    });
    function nextFullImage(){
        nextImage();
        showFullImage();
    }
    function previousFullImage(){
        
    }
    function updateFullImage(){
        if($('#fullImageContainer').is(":visible")){
           var fullUrl = "img?uuid="+viewerOptions.uuid+"&stream=IMG_FULL&action=GETRAW";
           $('#fullImageContainer>div.header>div.title').html(k4Settings.selectedPathTexts[k4Settings.selectedPathTexts.length -1]);

        }else{
            var fullUrl = "img/loading.gif";
        }
        
        if(viewerOptions.isContentDJVU()){
            $('#djvuContainer>iframe').attr('src', fullUrl);
            $('#djvuContainer').show();
            
        }else if(viewerOptions.isContentPDF()){
            fullUrl = fullUrl + "#page=" + $('#pdfContainer>input').val();
            $('#pdfContainer>iframe').attr('src', fullUrl);
            $('#pdfContainer').show();
        }else{
            $('#imgContainer').show();
            $('#imgContainer>img').attr('src', fullUrl);
            setFullImageDimension();
        }

        $('#fullImageContainer').scroll(function(){
            if(viewerOptions.hasAlto){
                positionAlto('imgFullImage');
            }
            
        });
    }
    var fullImageWidth;
    var fullImageHeight;
    function setFullImageDimension(){
         var newImg = new Image();
         newImg.src = $('#imgContainer>img').attr('src');
         fullImageWidth = newImg.width;
         fullImageHeight = newImg.height;
    }
    function changeFullImageZoom(){
        
        var zoom = $('#fullImageZoom').val();
        
        if(zoom=="width"){
            $('#imgContainer>img').css({'width': $('#fullImageContainer').width(), 'height': ''});
        }else if(zoom=="height"){
            //var w = 
            $('#imgContainer>img').css({'height': $(window).height()-
                    $('#fullImageContainer>div.header').height(),
                'width': ''});
        }else{
            var w = Math.round(fullImageWidth * parseFloat(zoom));
            var h = Math.round(fullImageHeight * parseFloat(zoom));
            $('#imgContainer>img').css({'width': w, 'height': h});
        }
        if(viewerOptions.hasAlto){
            showAlto(viewerOptions.uuid, 'imgFullImage');
        }
    }
</script>