/* Interface variables */
var containerWidth = 179;
var containerHeight = 493;
var groupTitleHeight = 20;
var maxHeight = 80;
var barContainerHeight = 5;
var barContainerMargin = 1;
var spaceWidth = 14;
var bubbleYPosition = 40;
var barContainerHeights = [13, 3];


var currentLevel = 0;
var firstBar = null;
var lastBar = null;
var dateAxisActive = false;
var zacLeft;
var zacRight;
var groupWidth;
var totalHeight;
var dateAxisVisible = false;
var initialized = false;
var maxCount;
var maxScroll;
    
function initDateAxis(){
   totalHeight = (containerHeight + groupTitleHeight) + 2;
   
    cloneElements();
    formatBreadCrumb();
    //fillDateAxis(currentLevel);
    $('#da_loading').css("visibility", "hidden");
    
    bars = $('.da_bar_container');
    setBarsPositions();
    dateAxisVisible = true;
    maxScroll = $("#content-scroll").attr("scrollHeight") - $("#content-scroll").height();
    positionResizes();
    positionCurtainsOnLoad();
    setSelectHandles();
    setSelectContainmentBottom();
    setSelectContainmentTop();
    initialized =true;
    setBarsPositions();
    selectTime();
    $("#content-resizable").resizable({
        handles: 's',
        resize: function(event, ui) {resizeDateAxisContent()},
        minHeight: 150
    });
    setDatePicker();
    //$("#content-resizable>div.ui-resizable-s").append('<span class="ui-icon ui-icon-arrowthick-2-n-s">handle</span>');
    //setTimeout('toggleDASlow()', 1000);
}

function setMaxResize(h){
    $( "#content-resizable" ).resizable( "option", "maxHeight", h );
}

function setDatePicker(){
    var dates = $( "#f1, #f2" ).datepicker({
        changeMonth: true,
	changeYear: true,
        numberOfMonths: 1,
        dateFormat: "dd.mm.yy",
        minDate: "01.01."+times[currentLevel+1][firstBar][1],
        maxDate: "31.12."+times[currentLevel+1][lastBar][1],
        //beforeShow: function(input, inst) {
        //    $("#ui-datepicker-div").css("z-index", 100);
        //},
        onSelect: function( selectedDate ) {
            var option = this.id == "f1" ? "minDate" : "maxDate",
                instance = $( this ).data( "datepicker" ),
                date = $.datepicker.parseDate(
                    instance.settings.dateFormat ||
                    $.datepicker._defaults.dateFormat,
                    selectedDate, instance.settings );
            dates.not( this ).datepicker( "option", option, date );
        }
    });
    $('#ui-datepicker-div').hide();
}

function cloneElements(){
    $('#bubbleDiv').appendTo(document.body);
    return;
    $('#select-handle-top').appendTo(document.body);
    $('#select-handle-bottom').appendTo(document.body);
    $('#img_resize_right').appendTo(document.body);
    $('#img_resize_left').appendTo(document.body);
    $('#resizable-top').appendTo(document.body);
    $('#resizable-bottom').appendTo(document.body);
    $('#constraint_top').prependTo(document.body);
    $('#constraint_bottom').prependTo(document.body);
}

function positionResizes(){
    //return;
    var y = $('#resizable-top').height();
    var x = $('#resizable-bottom').offset().left;
    //$('#img_resize_top').css("left", x);
    $('#img_resize_top').css("top", y);

    y = $('#resizable-bottom').position().top;
    //$('#img_resize_bottom').css("left", x + "px");
    $('#img_resize_bottom').css("top", y + "px");
}

var bars;
var barPositions = [];
function setBarsPositions(){
    for(var i=0; i<bars.length-1;i++){
        var l1 = $("#"+bars[i].id).offset().top;
        var l2 = $("#"+bars[i+1].id).offset().top;
        barPositions[i] = [l1, l2];
    }
}
function findActiveBar(x){
    for(var i=0; i<barPositions.length-1;i++){
        var l1 = barPositions[i][0];
        var l2 = barPositions[i][1];
        if(x>=l1 && x<l2){
            return bars[i];
        }
    }
    return null;
} 

var maxBar;
    

    
function filterOnItem(e){
    if(window.event){ // IE check
        el = window.event.srcElement;
    }else if(e && e.target){ // standard-compliant browsers
        el = e.target;
    }
    if(currentLevel==levels-1){
        //$("#" + fromField).val(formatDate(el.selectStart));
        //$("#" + toField).val(formatDate(el.selectEnd));
        $("#" + fromField).val("01.01."+el.selectStart);
        $("#" + toField).val("31.12."+el.selectEnd);
        if(el.hits==0){
            return;
        }
        doFilter();
    } else{
        zoomGroup(e);
    }
}

function windowResized(){
    setBarsPositions();
    positionCurtains();
}
   
    
function selectTime(){
    //if(!dateAxisVisible) return;
    var endX = $('#resizable-bottom').offset().top;// + $('#content-scroll').scrollTop();
    var startX = $('#resizable-top').height() + 
        $('#resizable-top').offset().top;
    var from = findActiveBar(startX);
    var id;
    if(from){
        //id = from.item;
        id = parseInt($(from).attr("id").split("_")[3]);
        if(parseInt(times[currentLevel+1][id][0]>0) || id>firstBar){
            selectStart = times[currentLevel+1][id][1];
        }else{
            selectStart = times[currentLevel+1][firstBar][1];
        }
    }else{
        //set to the first
        selectStart = times[currentLevel+1][firstBar][1];
    }
    var to = findActiveBar(endX);
    if(to){
        //id = to.item;
        id = parseInt($(to).attr("id").split("_")[3]);
        if(parseInt(times[currentLevel+1][id][0]>0) || id<lastBar){
            selectEnd = times[currentLevel+1][id][2];
        }else{
            selectEnd = times[currentLevel+1][lastBar][2];
        }
    }else{
        //set to the last
        selectEnd = times[currentLevel+1][lastBar][2];
    }
    formatSelectedTime();

}



function doFilter(){  
  
    if(!isValidDate($("#" + fromField).val()) || !isValidDate($("#" + toField).val())){
        alert(dictionary['filter.invalid.date'] );
        return;
    }
  
    var page = new PageQuery(window.location.search);
    page.setValue("offset", "0");
    page.setValue("forProfile", "dateaxis");
    //page.setValue(fromField, decodeDate($("#" + fromField).val()));
    //page.setValue(toField, decodeDate($("#" + toField).val()));
    
    page.setValue("da_od", decodeDate($("#" + fromField).val()));
    page.setValue("da_do", decodeDate($("#" + toField).val()));
    
    page.setValue("exactDay", $("#exactDay").is(':checked'));
    var newurl = "r.jsp?" + page.toString() + dateAxisAdditionalParams;
    
    document.location.href = newurl;

}

function removeFilter(){      
    var page = new PageQuery(window.location.search);

    page.setValue(fromField, "");
    page.setValue(toField, "");
    var newurl = "?" + page.toString();
    document.location.href = newurl;

}




function hideBubble(e){
    $("#bubbleText").html("");
    $("#bubbleDiv").css("left","-300px");
    $("#bubbleDiv").css("top", "-10px");
}
function onShowBubble(e){
    var el;
    var y;
    if(window.event){ // IE check
        el = window.event.srcElement;
        y = window.event.clientY + document.body.scrollTop - document.body.clientTop;
    }else if(e && e.target){ // standard-compliant browsers
        el = e.target;
        y = e.pageY;
        
    }
    if(!el.bubble) el = el.offsetParent;
    
    showBubble(el, y);
}
function showBubble(el, y){
    
    var l = $("#" + el.id).width()+$("#content-scroll").offset().left;
    $("#bubbleDiv").css("left", l+ "px");
    //$("#bubbleDiv").css("top", $("#" + el.id).offset().top + "px");
    $("#bubbleDiv").css("top", (y-25) + "px");
    
    /*
    var pos =  getPos(el);
    $("#bubbleDiv").css("left", pos[0]+ "px");
    $("#bubbleDiv").css("top", pos[1] + "px");
    */
    $("#bubbleText").html(el.bubble) ;
}

function getPos(el){
    var curleft = 0;
    var curtop = 0;

    if (el.offsetParent) {
        do {
            curleft += el.offsetLeft;
             curtop += el.offsetTop;
         } while (el = el.offsetParent);
    }
    return [curleft,curtop];
}

function setSelectHandles(){
    var y = $('#resizable-top').height();
    var x = $('#resizable-bottom').offset().left;
    $('#select-handle-top').css("top", y);

    y = $('#resizable-bottom').position().top;
    $('#select-handle-bottom').css("top", y);
    
    var w = $("#content-scroll").width()+1;
    $('#select-handle-top').css("width", w);
    $('#select-handle-bottom').css("width", w);
}

/* scroll functions*/

function setSelectContainmentBottom(){
    //$('#constraint_bottom').css("left", $("#resizable-top").offset().left);
    //$('#constraint_bottom').css("width", $('#content-scroll').width());
    $('#constraint_bottom').css("top",  $('#resizable-top').height());
    $('#constraint_bottom').css("height", $('#content-scroll').height() - $('#resizable-top').height());
    $("#select-handle-bottom").draggable('option', 'containment', '#constraint_bottom');
    hideBubble(null);
}

function setSelectContainmentTop(){
    $('#constraint_top').css("height", $('#resizable-bottom').offset().top - $("#content-scroll").offset().top);
    //$('#constraint_top').css("width", $('#content-scroll').width());
    $("#select-handle-top").draggable('option', 'containment', '#constraint_top');
    hideBubble(null);
}

function selectHandleChangeTop(e, ui){
    var wTop = ui.position.top;
    $("#resizable-top").css("height", wTop);
    selectTime();
    
    
    var startX = wTop + $('#resizable-top').offset().top;
    
    
    var bar = findActiveBar(startX);
    if(bar){
        //showBubble(bar, $(bar).parent().offset().top);
        
        var l = $(bar).width()+$("#content-scroll").offset().left;
        var id = $(bar).attr("id").split("_")[3];
        $("#bubbleDiv").css("left", l+ "px");
        $("#bubbleDiv").css("top", ($(bar).offset().top-25) + "px");
        $("#bubbleText").html(id + " (" + $(bar).text() + ")");
            
            
    } 
}

function selectHandleChangeBottom(e, ui){
    var pBottom = ui.position.top;
    var wBottom = $('#content-scroll').height() - pBottom;
    $('#resizable-bottom').css("top", pBottom);
    $('#resizable-bottom').css("height", wBottom);
    selectTime();
    
    var endX = $('#resizable-bottom').offset().top;
    
    var bar = findActiveBar(endX);
    if(bar){
        //showBubble(bar, endX);
        
        var l = $(bar).width()+$("#content-scroll").offset().left;
        var id = $(bar).attr("id").split("_")[3];
        $("#bubbleDiv").css("left", l+ "px");
        $("#bubbleDiv").css("top", ($(bar).offset().top-25) + "px");
        $("#bubbleText").html(id + " (" + $(bar).text() + ")");
        
    }
}

function daScrolled(){
    if(initialized){
      setBarsPositions();
    }
    selectTime();
}

function daGetMaxBar(){
    var id;
    var bar;
    var wmax = $(bars[0]).width();
    for(var i=0; i<bars.length;i++){
        id = $(bars[i]).attr("id");
        bar = $("#"+id+">div.da_bar");
        if($(bar).width()==wmax){
            return bars[i];
        }
    }
    return null;
    
}

function daScrollToMax(){
    var bar = daGetMaxBar();
    //alert($(bar).offset().top);
    var pos = $(bar).offset().top 
        - $('#da_container').offset().top
        + $('#content-scroll').scrollTop() 
        - ($('#content-scroll').height()/2);

        $('#content-scroll').scrollTop(pos);
}

function positionCurtainsOnLoad(){
    positionCurtains();
}

function positionCurtains(){
        var w = $("#da_container").width();
        var t = $("#content-scroll").offset().top + $("#content-scroll").height() - $("#resizable-bottom").height();
        t = $("#content-scroll").height() - $("#resizable-bottom").height();
        $("#resizable-bottom").css("top", t);
        
        positionResizes();
        setSelectHandles();
        setSelectContainmentBottom();
        setSelectContainmentTop();
        $("#resizable-top").css("width", w);
        $("#resizable-bottom").css("width", w);
}
/* end scroll functions*/


var lastPosition = 0;

function resizeDateAxisContent(){
    $("#content-scroll").css("height", $("#content-resizable").height()-7);
    positionCurtains();
}