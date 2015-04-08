<%@page contentType="text/html" pageEncoding="UTF-8"%>
<style type="text/css">
    #fullImageContainer>div.header{
        height:25px;
        padding-top:5px;
        padding-right:10px;
        border-bottom:1px solid rgba(0, 30, 60, 0.9);
        background-color: white;
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
<script type="text/javascript">
    $(document).ready(function(){
        $('#fullImageContainer.viewer').bind('viewReady', function(event, viewerOptions){
            updateFullImage();
        });
        $('#fullImageContainer.viewer').bind('viewChanged', function(event, id){
            $('#imgFullImage').attr('src', 'img/empty.gif');
            $("#loadingFull").show();
            hideAltoFull();
        });
    });
    
     
    function nextFullImage(){
        nextImage();
    }
    function previousFullImage(){
        previousImage();
        
    }
    function updateFullImage(){
        if($('#fullImageContainer').is(":visible")){
           var fullUrl = "img?uuid="+viewerOptions.uuid+"&stream=IMG_FULL&action=GETRAW";
           var p = '';
            for(var i=0; i<k4Settings.selectedPathTexts.length; i++){
                p += '<span style="float:left;" class="ui-icon ui-icon-triangle-1-e">folder</span><span style="float:left;">' + k4Settings.selectedPathTexts[i] + '</span>' ;
            }
            $('#fullImageContainer>div.header>div.title').html(p);
           //$('#fullImageContainer>div.header>div.title').html(k4Settings.selectedPathTexts[k4Settings.selectedPathTexts.length -1]);

        }else{
            var fullUrl = "img/empty.gif";
        }
        
        if(viewerOptions.isContentDJVU()){
            $('#divFullImageZoom').hide();
            var h = $(window).height()- $('#fullImageContainer>div.header').height() - 20;
            $('#djvuContainer').css('height', h + "px");
            $('#djvuContainer>iframe').attr('src', fullUrl);
            $('#djvuContainer').show();
            
        }else if(viewerOptions.isContentPDF()){
            $('#divFullImageZoom').hide();
            fullUrl = fullUrl + "#page=" + $('#pdfContainer>input').val();
            $('#pdfContainer>iframe').attr('src', fullUrl);
            $('#pdfContainer').show();
        }else{
            $('#divFullImageZoom').show();
            $('#imgContainer').show();
            $('#imgFullImage').attr('src', fullUrl);
            setFullImageDimension();
        }
    }
    var fullImageWidth;
    var fullImageHeight;
    function setFullImageDimension(){
         var newImg = new Image();
         newImg.src = $('#imgFullImage').attr('src');
         fullImageWidth = newImg.width;
         fullImageHeight = newImg.height;
    }
    function changeFullImageZoom(){
        
        var zoom = $('#fullImageZoom').val();
        if(zoom=="width"){
            $('#imgFullImage').css({'width': $('#fullImageContainer').width(), 'height': ''});
        }else if(zoom=="height"){
            //var w = 
            $('#imgFullImage').css({'height': $(window).height()-
                    $('#fullImageContainer>div.header').outerHeight(true),
                'width': ''});
        }else{
            var w = Math.round(fullImageWidth * parseFloat(zoom));
            var h = Math.round(fullImageHeight * parseFloat(zoom));
            $('#imgFullImage').css({'width': w, 'height': h});
        }
        if(viewerOptions.hasAlto){
            showAltoFull(viewerOptions.uuid);
        }
    }

    function hideAltoFull(){
        $("#alto_full").html('');
        $("#alto_full").hide();
    }
    
    function showAltoFull(pid){
        var q = $("#q").val();
        if($('#insideQuery').length>0) q =$('#insideQuery').val();
        if(q=="") return;

        var w = $('#imgFullImage').width();
        var h = $('#imgFullImage').height();
        var url = "inc/details/alto.jsp?q="+q+"&w="+w+"&h="+h+"&uuid=" + pid;
        $.get(url, function(data){
            if(data.trim()!=""){
                if($("#alto_full").length==0){
                     $("#imgContainer").append('<div id="alto_full" style="position:absolute;z-index:1003;overflow:hidden;" ></div>');
                }
                positionAltoFull();
                $("#alto_full").html(data);
                $("#alto_full").show();
            }
        });
    }
    
    function positionAltoFull(){
        var h = $("#imgFullImage").height();
        var t = $("#imgFullImage").offset().top - $("#imgContainer").offset().top;
        var w = $("#imgFullImage").width();
        var l = $("#imgFullImage").offset().left - $("#imgContainer").offset().left;
        $("#alto_full").css('width', w);
        $("#alto_full").css('height', h);
        $("#alto_full").css('left', l);
        $("#alto_full").css('top', t);
    }
    
    function onLoadFullImage() {
        if($('#imgFullImage').attr('src')!='img/empty.gif'){
            setFullImageDimension();
            if(viewerOptions.hasAlto){
                showAltoFull(viewerOptions.uuid);
            }
            $("#loadingFull").hide();
        }
    }
</script>
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
    <a id="fullButtonPrev" href="javascript:previousFullImage();"><span class="ui-icon ui-icon-arrowthick-1-w">previous</span></a>
    <a id="fullButtonNext" href="javascript:nextFullImage();"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>
    <a href="javascript:hideFullImage();"><span class="ui-icon ui-icon-closethick">close</span></a>
    </div>
</div>
<div class="fullContent" style="width:100%;overflow:auto;">
    <div id="djvuContainer" style="display:none;">
        <iframe src="" frameborder="0" width="100%" height="100%"></iframe>
    </div>

    <c:if test="${param.format == 'application/pdf'}">
        <div id="pdfContainer" style="display:none;">
            <%--input type="hidden" id="pdfPage" name="pdfPage" value="${itemViewObject.page}" /--%>
            <iframe src="" width="100%" height="100%"></iframe>
        </div>
    </c:if>
    <div id="imgContainer" style="display:none;position:relative;" align="center">
        <img id="imgFullImage" class="view_div" src="img/empty.gif" onload="onLoadFullImage()" />
        <img id="loadingFull" src="img/loading11.gif" style="display:none;margin-top:30px;" />
    </div>
</div>