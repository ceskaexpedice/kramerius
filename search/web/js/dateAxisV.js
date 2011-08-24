/* Interface variables */
var containerWidth = 175;
var containerHeight = 300;
var groupTitleHeight = 20;
var maxHeight = 80;
var barContainerHeight = 5;
var barContainerMargin = 1;
var spaceWidth = 14;
var bubbleYPosition = 40;
var barContainerHeights = [13, 3];


var currentLevel = 1;
var firstBar = null;
var lastBar = null;
var dateAxisActive = false;
var sliderScroll;
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
    setTimeout("initDateAxisDelayed()", 100);
    formatBreadCrumb();
}

function initDateAxisDelayed(){
    fillDateAxis(currentLevel);
    $('#da_loading').css("visibility", "hidden");
    
    bars = $('.da_bar_container');
    checkScrollBar();
    setBarsPositions();
    dateAxisVisible = true;
    maxScroll = $("#content-scroll").attr("scrollHeight") - $("#content-scroll").height();
    $("#content-slider").slider("value", 0);
    positionResizes();
    positionCurtainsOnLoad();
    setSelectHandles();
    setSelectContainmentBottom();
    setSelectContainmentTop();
    //getRanges();
    initialized =true;
    setBarsPositions();
    selectTime();
    //setTimeout('toggleDASlow()', 1000);
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

function slideTo(pos){
    if($("#content-slider").slider("value")<pos){
        var l = $("#content-slider").slider("value") + 10;
        $("#content-slider").slider("value", l);
        setTimeout('slideTo('+pos+')', 10);
    }else{
        $("#content-slider").slider("value", pos);
        //positionCurtainsOnLoad();
        setSelectHandles();
        setSelectContainmentBottom();
        setSelectContainmentTop();
        //positionCurtains();
        initialized = true;
        setBarsPositions();
    }
}

var maxBar;
function calculateMaximum(){
    var groupInt;
    var newMax = 0;
    for (var group in times[currentLevel]){            
        groupInt = parseInt(group);
        if(groupInt>=startTime && groupInt<=endTime){
            var groupStart = parseInt(times[currentLevel][group][1]);
            var groupEnd = parseInt(times[currentLevel][group][2]);
            for (var item in times[currentLevel+1]){
                var itemInt = parseInt(item);
                if(itemInt>=groupStart && itemInt<=groupEnd){
                    if(newMax<times[currentLevel+1][item][0]){
                      newMax = times[currentLevel+1][item][0];
                      maxBar = item;
                    }
                }
            }
        }
    }
    maxCount = newMax;
    //return newMax;
}
    
function normalizeHeight(modCount){
    if(modCount==0) return 0;
    //var barHeight = maxHeight * modCount / maximums[currentLevel + 1] + 2;
    var barHeight = maxHeight * modCount / maxCount;
    if(barHeight<2){
        barHeight = 2;
    }
    return Math.round(barHeight);
}
    
function filterOnItem(e){
    if(window.event){ // IE check
        el = window.event.srcElement;
    }else if(e && e.target){ // standard-compliant browsers
        el = e.target;
    }
    if(currentLevel==levels-1){
        //alert(parseInt(el.item));
        $("#" + fromField).val(formatDate(el.selectStart));
        $("#" + toField).val(formatDate(el.selectEnd));
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
        //$('#content-scroll').scrollTop() +
        $('#resizable-top').offset().top;
    //$('#test').html(startX + " - " + endX);
    //alert(startX);
    var from = findActiveBar(startX);
    //alert(from);
    if(from){
        if(parseInt(times[currentLevel+1][from.item][0]>0) || from.item>firstBar){
            selectStart = times[currentLevel+1][from.item][1];
        }else{
            selectStart = times[currentLevel+1][firstBar][1];
        }
    }else{
        //set to the first
        selectStart = times[currentLevel+1][firstBar][1];
    }
    var to = findActiveBar(endX);
    if(to){
        if(parseInt(times[currentLevel+1][to.item][0]>0) || to.item<lastBar){
            selectEnd = times[currentLevel+1][to.item][2];
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
        alert('invalid date');
        return;
    }
  
    var page = new PageQuery(window.location.search);

    page.setValue("offset", "0");
    page.setValue(fromField, decodeDate($("#" + fromField).val()));
    page.setValue(toField, decodeDate($("#" + toField).val()));
    var newurl = "?" + page.toString() + dateAxisAdditionalParams;

    document.location.href = newurl;


}

function removeFilter(){      
    var page = new PageQuery(window.location.search);

    page.setValue(fromField, "");
    page.setValue(toField, "");
    var newurl = "?" + page.toString();
    document.location.href = newurl;

}

function zoomGroup(e){
    if(window.event){ // IE check
        el = window.event.srcElement;
    }else if(e && e.target){ // standard-compliant browsers
        el = e.target;
    }
    if(currentLevel==levels-1){
        return;
    } 
    startTime = parseInt(el.groupStart);
    endTime = parseInt(el.groupEnd);
    zooms[currentLevel+1] = [startTime, endTime];
    $(".da_group").remove();

    fillDateAxis(currentLevel+1);
    checkScrollBar();
    $("#content-slider").slider("value", 0);
    $('#da_zoom').toggle();
    //positionCurtains();               
}
    
function zoomOut(){
    $(".da_group").remove();
    fillDateAxis(currentLevel-1);
    $("#content-slider").slider("value", 0);
    $('#da_zoom').toggle();
    checkScrollBar();
    positionResizes();
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

    //var pos = new Object();
    //pos.left = curleft - $("#content-holder").scrollLeft();
    //pos.top = curtop - $("#content-holder").scrollTop();
    //return pos;
}

function setSelectHandles(){
    var y = $('#resizable-top').height();
    var x = $('#resizable-bottom').offset().left;
    //$('#select-handle-top').css("left", x);
    $('#select-handle-top').css("top", y);

    y = $('#resizable-bottom').position().top;
    //$('#select-handle-bottom').css("left", x);
    $('#select-handle-bottom').css("top", y);
    
    var w = $("#content-scroll").width()+1;
    $('#select-handle-top').css("width", w);
    $('#select-handle-bottom').css("width", w);
}

/* scroll functions*/

function setSelectContainmentBottom(){
    //$('#constraint_bottom').css("left", $("#resizable-top").offset().left);
    $('#constraint_bottom').css("width", $('#content-scroll').width());
    $('#constraint_bottom').css("top",  $('#resizable-top').height());
    $('#constraint_bottom').css("height", $('#content-scroll').height() - $('#resizable-top').height());
    $("#select-handle-bottom").draggable('option', 'containment', '#constraint_bottom');
    //selectTime();
    hideBubble(null);
}

function setSelectContainmentTop(){
    //$('#constraint_top').css("left", $("#content-scroll").offset().left);
    $('#constraint_top').css("height", $('#resizable-bottom').offset().top - $("#content-scroll").offset().top);
    //$('#constraint_top').css("top", $("#content-scroll").offset().top);
    $('#constraint_top').css("width", $('#content-scroll').width());
    $("#select-handle-top").draggable('option', 'containment', '#constraint_top');
    //selectTime();
    hideBubble(null);
}

function selectHandleChangeTop(e, ui){
    var wTop = ui.position.top;
    $("#resizable-top").css("height", wTop);
    selectTime();
    var bar = findActiveBar(ui.position.top);
    if(bar){
        //var y = $("#" + bar.id).offset().top;
        showBubble(bar, ui.position.top);
    } 
}

function selectHandleChangeBottom(e, ui){
    var pBottom = ui.position.top;
    var wBottom = $('#content-scroll').height() - pBottom;
    $('#resizable-bottom').css("top", pBottom);
    $('#resizable-bottom').css("height", wBottom);
    selectTime();
    var bar = findActiveBar(ui.position.top);
    if(bar){
        //var y = $("#" + bar.id).offset().top;
        showBubble(bar, ui.position.top);
    }
}

function daScrolled(){
    if(initialized){
      setBarsPositions();
    }
    selectTime();
}

function scrollSliderChange(e, ui){
    //maxScroll=0;
    $("#content-scroll").attr({
        scrollTop: maxScroll- (ui.value * (maxScroll / 100))
    });
    if(initialized){
      setBarsPositions();
    }
    selectTime();
}

function scrollSliderSlide(e, ui){
    
    //maxScroll=0;
    //$("#content-scroll").attr({
    //    scrollTop: ui.value * (maxScroll / 100)
    //});
    $("#content-scroll").scrollTop(maxScroll- (ui.value * (maxScroll / 100)));
    if(initialized){
      setBarsPositions();
    }
    //selectTime();
}

function checkScrollBar(){
    //var w1 = lastPosition;
    var w1 = $("#da_bar_" + lastBar).offset().top;
    var w2 = $('#content-scroll').height() + $('#content-scroll').offset().top;
    if(w2 >= w1){
        $('#content-slider').hide();
    }else{
        $('#content-slider').show();
        
        
        $("#content-slider").slider("value", 0);
        var maxScroll = $("#da_container").height();
        //alert(maxScroll);
        var to = $('#da_bar_container_' + maxBar).offset().top;
        //alert(to);
        to = to - $("#content-scroll").offset().top;
        //alert(to);
        to = 100 - to / maxScroll  * 100;
        //alert(to);
        slideTo(to);
        
        
    }
} 




function positionCurtainsOnLoad(){
    positionCurtains();
    /*
    var wTop = $('#da_bar_' + firstBar).offset().top - $('#content-scroll').offset().top;
    if(wTop<1) wTop = 10;
    if(wTop < $('#content-scroll').height()){
        $("#resizable-top").css("height", wTop);
    }else{
        var newscroll = ($('#content-scroll').height() + $('#da_container').height()) / wTop;
        $("#content-slider").slider("value", newscroll);
        $("#resizable-top").css("height", wTop);
    }
    
    var pBottom = $('#da_bar_' + lastBar).offset().top + barContainerHeight;
    var bottomSide = $('#content-scroll').height() + $('#content-scroll').offset().top;
    if(pBottom < bottomSide && bottomSide - pBottom>10 && pBottom>$('#content-scroll').offset().top ){
        var hBottom = bottomSide - pBottom;
        $('#resizable-bottom').css("top", pBottom);
        $('#resizable-bottom').css("height", hBottom);
    }
    */
}
function positionCurtains(){
    //return;
        //var w = $("#content-scroll").width()+1;
        var w = $("#da_container").width()+1;
        //$("#resizable-top").css("top", $("#content-scroll").offset().top);
        //$("#resizable-top").css("left", $("#content-scroll").offset().left);
        $("#resizable-top").css("width", w);
        var t = $("#content-scroll").offset().top + $("#content-scroll").height() - $("#resizable-bottom").height();
        t = $("#content-scroll").height() - $("#resizable-bottom").height();
        $("#resizable-bottom").css("top", t);
        //$("#resizable-bottom").css("left", $("#content-scroll").offset().left);
        $("#resizable-bottom").css("width", w);
        
        positionResizes();
        setSelectHandles();
        setSelectContainmentBottom();
        setSelectContainmentTop();
}
/* end scroll functions*/

/* display bars methods */

function createSpace(){
    var spaceDiv = document.createElement("DIV");                       
    spaceDiv.setAttribute("class", "da_space");
    spaceDiv.className = "da_space"; 
    return spaceDiv
}

function createGroup(group, groupStart, groupEnd, grouptitle, groupInt){
    grouptitle = formatGroupTitle(group,times[currentLevel][group][0]);
    var groupDiv = document.createElement("DIV");
    groupDiv.bubble = grouptitle;                
    groupDiv.setAttribute("class", "da_group");
    groupDiv.className = "da_group";  
    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
        groupDiv.addEventListener("click", zoomGroup, false);
    }else{ // e.g. Internet Explorer (also would work on Opera)
        groupDiv.attachEvent("onclick", zoomGroup);
    }
    groupDiv.group = groupInt;
    groupDiv.groupStart = groupStart;
    groupDiv.groupEnd = groupEnd;
    groupDiv.appendChild(createGroupTitle(grouptitle));
    return groupDiv;
}

function createGroupTitle(grouptitle){
    var titleDiv=document.createElement("DIV");
    titleDiv.innerHTML = grouptitle;  
    titleDiv.setAttribute("class", "da_group_title");
    titleDiv.className = "da_group_title"; 
    return titleDiv;
}

function createBar(title, groupStart, groupEnd, item, w){
    var barContainer = document.createElement("DIV");
    barContainer.setAttribute("class", "da_bar_container");
    barContainer.className = "da_bar_container";
    barContainer.setAttribute("id", "da_bar_container_" + item);
    
    var bar = document.createElement("DIV");
    bar.setAttribute("class", "da_bar");
    bar.className = "da_bar";
    bar.setAttribute("id", "da_bar_" + item);
    bar.style.width = w + "px";
    
    bar.setAttribute("zIndex", 3);
    bar.style.zIndex=3;
    
    
    barContainer.groupStart = groupStart;
    barContainer.groupEnd = groupEnd;
    barContainer.item = item; 
    barContainer.selectStart = times[currentLevel+1][item][1];
    barContainer.selectEnd = times[currentLevel+1][item][2];
    barContainer.hits = times[currentLevel+1][item][0];
    barContainer.bubble = title;
    
    bar.groupStart = groupStart;
    bar.groupEnd = groupEnd;
    bar.item = item; 
    bar.selectStart = times[currentLevel+1][item][1];
    bar.selectEnd = times[currentLevel+1][item][2];
    bar.hits = times[currentLevel+1][item][0];
    bar.bubble = title;
    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
        barContainer.addEventListener("mouseover", onShowBubble, true);
        barContainer.addEventListener("mouseout", hideBubble, true);
        barContainer.addEventListener("click", filterOnItem, true);
        //bar.addEventListener("mouseover", onShowBubble, true);
        //bar.addEventListener("mouseout", hideBubble, true);
    }else{ // e.g. Internet Explorer (also would work on Opera)
        barContainer.attachEvent("onmouseover", onShowBubble);
        barContainer.attachEvent("onmouseout", hideBubble);
        bar.attachEvent("onclick", filterOnItem);
        bar.attachEvent("onmouseover", onShowBubble);
        bar.attachEvent("onmouseout", hideBubble);
    }
    barContainer.appendChild(bar);
    return barContainer;
}

var lastPosition = 0;
function fillDateAxis(level){
    currentLevel = level;
    var container = document.getElementById("da_container");
    
    startTime = zooms[currentLevel][0];
    endTime = zooms[currentLevel][1];
    firstBar = null;
    lastBar = null;
    var grouptitle;
    lastPosition = 0;
    calculateMaximum();
    var index = 0;
    var lastGroupWidth = spaceWidth;
    
    for (var group in times[currentLevel]){
        var groupInt = parseInt(group);
        if(groupInt>=startTime && groupInt<=endTime){
            var groupStart = parseInt(times[level][group][1]);
            var groupEnd = parseInt(times[level][group][2]);
            grouptitle = formatGroupTitle(group) + " (" + times[currentLevel][group][0] + ")";
            var groupDiv = createGroup(group, groupStart, groupEnd, grouptitle, groupInt);    
            
            for (var item in times[level+1]){
                var itemInt = parseInt(item);
                if(itemInt>=groupStart && itemInt<=groupEnd){
                    var title = formatBarTitle(item) + " (" + times[level+1][item][0] + ")";
                    var w = normalizeHeight(times[currentLevel+1][item][0]);
                    groupDiv.appendChild(createSpace());
                    var barDiv = createBar(title, groupStart, groupEnd, item, w);
                    groupDiv.appendChild(barDiv);
                    
                    if(!firstBar && times[currentLevel+1][item][0]>0) firstBar = item;
                    index++;
                    groupWidth = groupWidth + barContainerHeight + barContainerMargin*2;
                    if(times[currentLevel+1][item][0]>0){
                        lastBar = item;
                        lastPosition = lastGroupWidth + groupWidth;
                    } 
                }
            }
            lastGroupWidth = lastGroupWidth + groupWidth;
            container.appendChild(groupDiv);
        }
    } 
    
    
   // $("#content-scroll").css("width", containerWidth + "px");
    $("#content-scroll").css("height", containerHeight + "px");
    var sliderh = $("#content-scroll").height();
    $("#content-slider2").css("height", sliderh);
    sliderh = sliderh - $(".ui-slider-handle").height();
    $("#content-slider").css("height", sliderh);
    $("#content-slider").css("top", $(".ui-slider-handle").height()/2);
    $('.da_bar').css('zIndex', 3);
}

/* ranges */
   
var usedRangeBarPos = new Array();   

function checkRangeBarPosUsed(i, beginDate, endDate){
    var rangesInPos = usedRangeBarPos[i];
    
    if(rangesInPos ==null){
        return false;
    }
    for(var j = 0; j<rangesInPos.length; j++){
        var range = rangesInPos[j];
        if( (range[0]<=beginDate && range[1]>=beginDate) ||
           (range[0]<endDate && range[1] >= endDate) ){
            return true;
           }
    }
    return false;
}

function getRangeBarPos(beginDate, endDate){
    
    var j = 0;
    while(checkRangeBarPosUsed(j.toString(), beginDate, endDate)){
        j++;
    }
    var currentRange = usedRangeBarPos[j.toString()];
    if(currentRange==null){
      usedRangeBarPos[j.toString()] = [[beginDate, endDate]];
    }else{
      currentRange[currentRange.length] = [beginDate, endDate];
      usedRangeBarPos[j.toString()] = currentRange;
    }
    return j;
    
}

function createRangeBar(datum, beginDate, endDate, count, i){
    var beginBarPos = $("#da_bar_container_" + beginDate).offset().top + $('#content-scroll').scrollTop()
        - $('#content-scroll').offset().top;
    var endBarPos = $("#da_bar_container_" + endDate).offset().top + $('#content-scroll').scrollTop()
        - $('#content-scroll').offset().top;
    var h = endBarPos - beginBarPos;
            
    var bar = document.createElement("DIV");
    bar.setAttribute("class", "da_range_bar");
    bar.className = "da_range_bar";
    bar.setAttribute("id", "da_range_bar_" + beginDate + "_" + endDate);
    bar.style.height = h + "px";
    bar.style.top = beginBarPos + "px";
    
    //var j = 0;
    //while(checkRangeBarPosUsed(j.toString(), beginDate, endDate)){
    //    j++;
    //}    
    //usedRangeBarPos[j.toString()] = [[beginDate, endDate]];
    var j = getRangeBarPos(beginDate, endDate);
    bar.style.left = (j*4) +"px";
    
    bar.setAttribute("zIndex", 3);
    bar.style.zIndex=3;
    
    bar.beginDate = beginDate;
    bar.endDate = endDate;
    bar.item = datum; 
    bar.selectStart = beginDate;
    bar.selectEnd = endDate;
    bar.hits = count;
    //bar.bubble = "od " + beginDate + " do " + endDate + " ( "+count+")";
    bar.bubble = count + " (od " + beginDate + " do " + endDate +")";
    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
        bar.addEventListener("mouseover", onShowBubble, true);
        bar.addEventListener("mouseout", hideBubble, true);
        bar.addEventListener("click", filterOnRange, true);
    }else{ // e.g. Internet Explorer (also would work on Opera)
        bar.attachEvent("onmouseover", onShowBubble);
        bar.attachEvent("onmouseout", hideBubble);
        bar.attachEvent("onclick", filterOnRange);
    }
    return bar;
}


function getRanges(){
    var url = "inc/dateRangeResults.jsp?" + (new PageQuery(window.location.search)).toString();
    //$.getJSON("inc/dateRangeFacet.jsp", function(data){
    $.getJSON(url, function(data){
            //alert(data);
        $.each(data.items, function(i,item){
            $("#da_range_container").append(createRangeBar(item.datum, item.beginDate, item.endDate, item.count, i));
        });
    //alert(usedRangeBarPos);
    });
    // writeRanges();
}

function filterOnRange(e){
    if(window.event){ // IE check
        el = window.event.srcElement;
    }else if(e && e.target){ // standard-compliant browsers
        el = e.target;
    }
    
    addNavigation("datum", el.item);
}

/* end ranges */