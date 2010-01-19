/* Interface variables */
var containerWidth = 460;
var containerHeight = 60;
var groupTitleHeight = 20;
var maxHeight = 60;
var barContainerWidth = 5;
var barContainerMargin = 1;
var spaceWidth = 14;
var bubbleYPosition = 40;
var barContainerWidths = [13, 5];


var currentLevel = 0;
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
    
function initDateAxis(){
    totalHeight = (containerHeight + groupTitleHeight) + 2;
    if(selectStart ==""){
        
        $("#content-scroll").css("height", totalHeight);
        $("#content-holder").css("height", totalHeight);
        $("#da_loading").css("height", totalHeight);
        $("#da_loading").css("width", containerWidth);
        if(dateAxisActive){
            cloneElements();
            setTimeout("initDateAxisDelayed()", 100);
            $('#zpet_link').hide(); 
        }else{
          $('#zpet_link').hide(); 
          $("#da_loading").hide();  
          $('#da_pouzit').hide(); 
          //$('#daText').hide(); 
          $('#daTable').hide(); 
          //$('#da_title').hide();
          $('#skryt_link').css('display', 'none');
          hideDateAxis();
        }
    }else{
        /*
        $("#da_loading").toggle();  
        $('#da_pouzit').hide(); 
        $('#daTable').hide(); 
        $('#skryt_link').css('display', 'none');
        */
        hideDateAxis();
    }
    formatBreadCrumb();
}

function showDateAxis(){
    //$('#daTable').toggle();
    //$('#daTable').css("display", "table");
    //$('#daText').toggle();
    $('#img_resize_left').css('display', 'block');
    $('#img_resize_right').css('display', 'block');
    $('#resizable-right').css('display', 'block');
    $('#resizable-left').css('display', 'block');
    //$('#da_title').css('display', 'block');
    $('#da_graph').css('display', 'block');
    //$('#daText').css('display', 'none');
    $('#zobrazit_link').css('display', 'none');
    $('#skryt_link').css('display', 'block');
    
    dateAxisVisible = true;
    positionCurtains();
    positionResizes();
    //selectTime();
    setSelectHandles();
    setSelectContainmentRight();
    setSelectContainmentLeft();

}

function hideDateAxis(){
    selectTime();
    //$('#img_resize_left').css('display', 'none');
    //$('#img_resize_right').css('display', 'none');
    //$('#resizable-right').css('display', 'none');
    //$('#resizable-left').css('display', 'none');
    //$('#da_title').css('display', 'none');
    //$('#daText').css('display', 'block');
    $('#da_graph').css('display', 'none');
    $('#zobrazit_link').css('display', 'block');
    $('#skryt_link').css('display', 'none');
    dateAxisVisible = false;
    
    
    $('#constraint_left').css('display', 'none');
    $('#constraint_right').css('display', 'none');
    $('#select-handle-left').css('display', 'none');
    $('#select-handle-right').css('display', 'none');
    $('#skryt_link').css('display', 'none');
    $('#skryt_link').css('display', 'none');
    $('#img_resize_left').css('display', 'none');
    $('#img_resize_right').css('display', 'none');
    $('#resizable-right').css('display', 'none');
    $('#resizable-left').css('display', 'none');
    
    $('#constraint_left').css('left', '-2000px');
    $('#constraint_right').css('left', '-2000px');
    $('#select-handle-left').css('left', '-2000px');
    $('#select-handle-right').css('left', '-2000px');
    $('#img_resize_right').css('left', '-2000px');
    $('#img_resize_left').css('left', '-2000px');
    $('#resizable-left').css('left', '-2000px');
    $('#resizable-right').css('left', '-2000px');
    $('#bubbleDiv').css('left', '-2000px');
    
}

function initDateAxisDelayed(){
    fillDateAxis(0);
    //fillTextSelects();
    if($('#daTable')){
        //$('#daTable').toggle();
        $('#daTable').css("visibility", "visible");
        $('#daTable').css("position", "relative");
        $('#daTable').css("height", $("#content-scroll").css("height"));
        $('#img_resize_left').toggle();
        $('#img_resize_right').toggle();
        $('#resizable-left').toggle();
        $('#resizable-right').toggle();
    }
    showDateAxis();
    //$("#da_loading").toggle();
    $('#da_loading').css("visibility", "hidden");
    //$('#dateAxisText').toggle();
    bars = $('.da_barContainer');
    setBarsPositions();
    selectTime();
    dateAxisVisible = true;
    positionCurtainsOnLoad();
    setSelectHandles();
    setSelectContainmentRight();
    setSelectContainmentLeft();
}

function test(){
    positionCurtainsOnLoad();
    setSelectHandles();
    setSelectContainmentRight();
    setSelectContainmentLeft();
}

function slideToRight(){
    if($("#content-slider").slider("value")<100){
        var l = $("#content-slider").slider("value") + 10;
        $("#content-slider").slider("value", l);
        setTimeout('slideToRight()', 10);
    }else{
        positionCurtainsOnLoad();
    setSelectHandles();
    setSelectContainmentRight();
    setSelectContainmentLeft();
        //positionCurtains();
        initialized = true;
        setBarsPositions();
    }
}
function setSelectHandles(){
    var x = $('#resizable-left').offset().left + $('#resizable-left').width();
    var y = $('#resizable-right').offset().top;
    $('#select-handle-left').css("left", x);
    $('#select-handle-left').css("top", y);

    x = $('#resizable-right').offset().left;
    $('#select-handle-right').css("left", x);
    $('#select-handle-right').css("top", y);
    
    var h = $("#content-scroll").height()+1;
    $('#select-handle-left').css("height", h);
    $('#select-handle-right').css("height", h);
}

function cloneElements(){
    $('#select-handle-left').appendTo(document.body);
    $('#select-handle-right').appendTo(document.body);
    $('#img_resize_right').appendTo(document.body);
    $('#img_resize_left').appendTo(document.body);
    $('#resizable-left').appendTo(document.body);
    $('#resizable-right').appendTo(document.body);
    $('#bubbleDiv').appendTo(document.body);
    $('#constraint_left').prependTo(document.body);
    $('#constraint_right').prependTo(document.body);
}
function setSelectContainmentRight(){
    $('#constraint_right').css("left", $("#resizable-left").offset().left + $('#resizable-left').width());
    $('#constraint_right').css("width", $('#content-scroll').width() - $('#resizable-left').width());
    $('#constraint_right').css("top", $("#content-scroll").offset().top);
    $('#constraint_right').css("height", $('#content-scroll').height());
    $("#select-handle-right").draggable('option', 'containment', '#constraint_right');
    //selectTime();
    hideBubble(null);
}
function setSelectContainmentLeft(){
    $('#constraint_left').css("left", $("#content-scroll").offset().left);
    $('#constraint_left').css("width", $('#resizable-right').offset().left - $("#content-scroll").offset().left);
    $('#constraint_left').css("top", $("#content-scroll").offset().top);
    $('#constraint_left').css("height", $('#content-scroll').height());
    $("#select-handle-left").draggable('option', 'containment', '#constraint_left');
    //selectTime();
    hideBubble(null);
}

function selectHandleChangeLeft(e, ui){
    var wLeft = ui.position.left - $('#content-scroll').offset().left;
    $("#resizable-left").css("width", wLeft);
    selectTime();
    var bar = findActiveBar(ui.position.left);
    if(bar){
        showBubble(bar);
    } 
}
function selectHandleChangeRight(e, ui){
    var pRight = ui.position.left;
    var wRight = $('#content-scroll').width() + $('#content-scroll').offset().left - pRight;
    $('#resizable-right').css("left", pRight);
    $('#resizable-right').css("width", wRight);
    selectTime();
    var bar = findActiveBar(ui.position.left);
    if(bar){
        showBubble(bar);
    }
}
function scrollSliderChange(e, ui){
    var maxScroll = $("#content-scroll").attr("scrollWidth") - $("#content-scroll").width();
    //$("#content-scroll").animate({scrollLeft: ui.value * (maxScroll / 100) }, 1000);
    //setTimeout("selectTime()", 1010);
    $("#content-scroll").attr({
        scrollLeft: ui.value * (maxScroll / 100)
    });
    if(initialized){
      setBarsPositions();
    }
    selectTime();
}

function scrollSliderSlide(e, ui){
    var maxScroll = $("#content-scroll").attr("scrollWidth") - $("#content-scroll").width();
    //$("#content-scroll").animate({scrollLeft: ui.value * (maxScroll / 100) }, 1000);
    $("#content-scroll").attr({
        scrollLeft: ui.value * (maxScroll / 100)
    });
    if(initialized){
      setBarsPositions();
    }
    selectTime();
}

var lastPosition = 0;
function fillDateAxis2(level){
    return;
    var index = 0;
    currentLevel = level;
    var row = document.createElement("DIV");
    $("#content-holder").append(row);
    //row = $("#content-holder");

    row.setAttribute("class", "da-table");
    row.className = "da-table";
    row.id = "da_table";
    row.style.height = totalHeight + "px";
    
    startTime = zooms[currentLevel][0];
    endTime = zooms[currentLevel][1];
    var groupIdx = 0;
    firstBar = null;
    lastBar = null;
    var grouptitle;
    var lastGroupWidth = 0;
    lastPosition = 0;
    calculateMaximum();
        
    var cell = document.createElement("DIV");                       
    cell.setAttribute("class", "da_group_space");
    cell.className = "da_group_space";  
    cell.style.height = totalHeight + "px"; 
    var l = lastGroupWidth;
    //cell.style.left = l + "px";
    cell.style.top = "0px";
    cell.style.width = spaceWidth + "px"; 
    row.appendChild(cell);  
        
    lastGroupWidth = spaceWidth;
    var barsNum = 0;
    var testStr = "";
    for (var group in times[currentLevel]){
            
        var groupInt = parseInt(group);
        if(groupInt>=startTime && groupInt<=endTime){
            groupWidth = 0;
            
            var groupStart = parseInt(times[level][group][1]);
            var groupEnd = parseInt(times[level][group][2]);
            grouptitle = formatGroupTitle(group,times[currentLevel][group][0]);
            cell=document.createElement("DIV");
            cell.bubble = grouptitle;                
            cell.setAttribute("class", "da_group_cell");
            cell.className = "da_group_cell";  
            cell.style.height = totalHeight + "px";
                
              
            if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
                cell.addEventListener("click", zoomGroup, false);
            }else{ // e.g. Internet Explorer (also would work on Opera)
                cell.attachEvent("onclick", zoomGroup);
            }
            cell.group = groupInt;
            cell.groupStart = groupStart;
            cell.groupEnd = groupEnd;
                
            index = 0;
            
            var titleDiv=document.createElement("DIV");
            titleDiv.innerHTML = grouptitle;
            titleDiv.style.left = "4px";
            titleDiv.style.top = "0px";          
            titleDiv.setAttribute("class", "da_group_title");
            titleDiv.className = "da_group_title"; 
            titleDiv.style.height = groupTitleHeight + "px";                
            cell.appendChild(titleDiv);
            for (var item in times[level+1]){
                var itemInt = parseInt(item);
                if(itemInt>=groupStart && itemInt<=groupEnd){
                    var title = formatBarTitle(item, times[level+1][item][0]);
                    var h = normalizeHeight(times[currentLevel+1][item][0]);
                    var t = Math.max(containerHeight - h, 0);
                    l = index * barContainerWidth;
                    
                    var barContainer=document.createElement("DIV");
                    barContainer.bubble =title;
                    barContainer.setAttribute( "class", "da_barContainer" );
                    barContainer.setAttribute("id", "da_bar_container_" + item);
                    barContainer.className = "da_barContainer";
                    barContainer.style.height = containerHeight + "px";
                    barContainer.style.margin = barContainerMargin + "px";
                    //barContainer.style.left = l + "px"; 

                    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
                        barContainer.addEventListener("mouseover", onShowBubble, true);
                        barContainer.addEventListener("mouseout", hideBubble, true);
                        barContainer.addEventListener("click", filterOnItem, true);
                    }else{ // e.g. Internet Explorer (also would work on Opera)
                        barContainer.attachEvent("onmouseover", onShowBubble);
                        barContainer.attachEvent("onmouseout", hideBubble);
                        barContainer.attachEvent("onclick", filterOnItem);
                    }
                    
                    barContainer.group=group;
                    barContainer.groupStart=groupStart;
                    barContainer.groupEnd=groupEnd;
                    barContainer.hits = times[level+1][item][0];
                    
                    barContainer.item=item;
                    barContainer.selectStart = times[currentLevel+1][item][1];
                    barContainer.selectEnd = times[currentLevel+1][item][2];
                    cell.appendChild(barContainer);
                    
                    var bar=document.createElement("DIV");
                    bar.setAttribute("class", "da_bar");
                    bar.className = "da_bar";
                    bar.setAttribute("id", "da_bar_" + item);
                        
                    bar.style.height = t + "px";
                    //bar.style.height = containerHeight - t + "px";
                    //bar.style.top = t + "px"; 
                    bar.groupStart=groupStart;
                    bar.groupEnd=groupEnd;
                    bar.item=item; 
                    bar.selectStart = times[currentLevel+1][item][1];
                    bar.selectEnd = times[currentLevel+1][item][2];
                    bar.hits = times[level+1][item][0];
                    bar.bubble = title;
                    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
                        //bar.addEventListener("click", filterOnItem, true);
                        bar.addEventListener("mouseover", onShowBubble, true);
                        bar.addEventListener("mouseout", hideBubble, true);
                    }else{ // e.g. Internet Explorer (also would work on Opera)
                        bar.attachEvent("onmouseover", showBubble);
                        bar.attachEvent("onmouseout", hideBubble);
                        //bar.attachEvent("onclick", filterOnItem);
                    }
                    barContainer.style.width = barContainerWidth + "px";  
                        
                    barContainer.appendChild(bar);
                    if(!firstBar && times[currentLevel+1][item][0]>0) firstBar = item;
                    index++;
                    groupWidth = groupWidth + barContainerWidth + barContainerMargin*2;
                    if(times[currentLevel+1][item][0]>0){
                        lastBar = item;
                        lastPosition = lastGroupWidth + groupWidth;
                    } 
                    barsNum++;
                }
            }
            l = lastGroupWidth;
            cell.style.left = l + "px";
            cell.style.top = "0px";
            cell.style.width = (groupWidth) + "px";
            lastGroupWidth = lastGroupWidth + groupWidth;
            //lastPosition = lastGroupWidth;
            row.appendChild(cell);
            groupIdx++;
        }
    }     
    cell=document.createElement("DIV");                       
    cell.setAttribute("class", "da_group_space");
    cell.className = "da_group_space";  
    cell.style.height = totalHeight + "px"; 
    l = lastGroupWidth;
    cell.style.left = l + "px";
    cell.style.top = "0px";
    cell.style.width = spaceWidth + "px"; 
    row.appendChild(cell);
    //alert(barsNum);
    l = l + spaceWidth + spaceWidth;
    $("#content-holder").css("width", l + "px");
    $("#content-scroll").css("width", containerWidth + "px");
    $("#content-slider").css("width", containerWidth + "px");
    $("#scrollBar-right").css("width", containerWidth + "px");
    $("#test").html(testStr);
}
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
                    newMax = Math.max(newMax, times[currentLevel+1][item][0]);
                }
            }
        }
    }
    maxCount = newMax;
    //return newMax;
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
function checkScrollBar(){
  
    var w1 = lastPosition;
    var w2 = $('#content-scroll').width();
    if(w2 >= w1){
        $('#content-slider').hide();
    }else{
        $('#content-slider').show();
    }
  
}  
function positionCurtainsOnLoad(){
    positionCurtains();
    var wLeft = $('#da_bar_' + firstBar).offset().left - $('#content-scroll').offset().left;
    if(wLeft < $('#content-scroll').width()){
        $("#resizable-left").css("width", wLeft);
    }else{
        var newscroll = ($('#content-scroll').width() + $('#content-holder').width()) / wLeft;
        $("#content-slider").slider("value", newscroll);
    }

    var pRight = $('#da_bar_' + lastBar).offset().left + barContainerWidth + 2;
    var rightSide = $('#content-scroll').width() + $('#content-scroll').offset().left;
    if(pRight < rightSide){
        var wRight = rightSide - pRight;
        $('#resizable-right').css("left", pRight);
        $('#resizable-right').css("width", wRight);
    }
    positionResizes();
    checkScrollBar();
}

function windowResized(){
    setBarsPositions();
    positionCurtains();
}
function positionCurtains(){
    if( dateAxisVisible){
    
        var h = $("#content-scroll").height()+1;
        $("#resizable-left").css("left", $("#content-scroll").offset().left);
        $("#resizable-left").css("top", $("#content-scroll").offset().top);
        $("#resizable-left").css("height", h);
        var l = $("#content-scroll").offset().left + $("#content-scroll").width() - $("#resizable-right").width();
        $("#resizable-right").css("left", l);
        $("#resizable-right").css("top", $("#content-scroll").offset().top);
        $("#resizable-right").css("height", h);
        
        positionResizes();
        setSelectHandles();
        setSelectContainmentRight();
        setSelectContainmentLeft();
    }
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
    $("#da_table").remove();

    fillDateAxis(currentLevel+1);
    $("#content-slider").slider("value", 0);
    $('#da_zoom').toggle();
    checkScrollBar();
    //positionCurtains();               
}
    
function zoomOut(){
    $("#da_table").remove();
    fillDateAxis(currentLevel-1);
    //sliderScroll.setValue(0);
    $("#content-scroll").scrollLeft = 0;
    $('#da_zoom').toggle();
    checkScrollBar();
    positionResizes();
    //positionCurtains();
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
function hideBubble(e){
    $("#bubbleText").html("");
    $("#bubbleDiv").css("left","-300px");
    $("#bubbleDiv").css("top", "-10px");
}
function onShowBubble(e){
    
    if(window.event){ // IE check
        el = window.event.srcElement;
    }else if(e && e.target){ // standard-compliant browsers
        el = e.target;
    }
    if(!el.bubble) el = el.offsetParent;
    showBubble(el);
}
function showBubble(el){
    var pos =  getPos(el);
    $("#bubbleDiv").css("left", pos.left + "px");
    $("#bubbleDiv").css("top", (pos.top-bubbleYPosition) + "px");
    $("#bubbleText").html(el.bubble) ;
    
    //alert(el.title);
}

function getPos(el){
    var curleft = 0;
    var curtop = 0;
    var pos = new Object();

    //if (el.offsetParent) {
    //    do {
    //        curleft += el.offsetLeft;
    //         curtop += el.offsetTop;
    //     } while (el = el.offsetParent);
    //}

    //pos.left = curleft - $("#content-holder").scrollLeft();
    //pos.top = curtop - $("#content-holder").scrollTop();
    
    pos.left = $("#" + el.id).offset().left;
    pos.top = $("#" + el.id).offset().top;
    return pos;
    //return [curleft,curtop];
}
    
var bars;
var barPositions = [];
function setBarsPositions(){
    for(var i=0; i<bars.length-1;i++){
        var l1 = $("#"+bars[i].id).offset().left;
        var l2 = $("#"+bars[i+1].id).offset().left;
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

function selectTime(){
    if(!dateAxisVisible) return;
    var endX = $('#resizable-right').offset().left + $('#content-holder').scrollLeft();
    var startX = $('#resizable-left').width() + 
        $('#content-holder').scrollLeft() +
        $('#resizable-left').offset().left;
    
    var from = findActiveBar(startX);
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

function positionResizes(){
    var x = $('#resizable-left').offset().left + $('#resizable-left').width() - 7;
    var y = $('#resizable-right').offset().top + 15;
    $('#img_resize_left').css("left", x);
    $('#img_resize_left').css("top", y);

    x = $('#resizable-right').offset().left  - 7;
    $('#img_resize_right').css("left", x + "px");
    $('#img_resize_right').css("top", y + "px");
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

function createSpace(){
    var spaceDiv = document.createElement("DIV");                       
    spaceDiv.setAttribute("class", "da_group_space");
    spaceDiv.className = "da_group_space";  
    spaceDiv.style.height = totalHeight + "px";
    //cell.style.left = l + "px";
    spaceDiv.style.top = "0px";
    spaceDiv.style.width = spaceWidth + "px"; 
    return spaceDiv
}

function createGroup(group, groupStart, groupEnd, grouptitle, groupInt){
    grouptitle = formatGroupTitle(group,times[currentLevel][group][0]);
    var groupDiv = document.createElement("DIV");
    groupDiv.bubble = grouptitle;                
    groupDiv.setAttribute("class", "da_group_cell");
    groupDiv.className = "da_group_cell";  
    groupDiv.style.height = totalHeight + "px";
    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
        groupDiv.addEventListener("click", zoomGroup, false);
    }else{ // e.g. Internet Explorer (also would work on Opera)
        groupDiv.attachEvent("onclick", zoomGroup);
    }
    groupDiv.group = groupInt;
    groupDiv.groupStart = groupStart;
    groupDiv.groupEnd = groupEnd;
    return groupDiv;
}

function createGroupTitle(grouptitle){
    var titleDiv=document.createElement("DIV");
    titleDiv.innerHTML = grouptitle;
    titleDiv.style.left = "4px";
    titleDiv.style.top = "0px";          
    titleDiv.setAttribute("class", "da_group_title");
    titleDiv.className = "da_group_title"; 
    titleDiv.style.height = groupTitleHeight + "px"; 
    
    return titleDiv;
}

function createBarContainer(title, group, groupStart, groupEnd, item){
    var barContainer = document.createElement("DIV");
      barContainer.bubble = title;
      barContainer.setAttribute( "class", "da_barContainer" );
      barContainer.setAttribute("id", "da_bar_container_" + item);
      barContainer.className = "da_barContainer";
     
      barContainer.style.width = barContainerWidth + "px";  
      barContainer.style.height = containerHeight + "px";
      barContainer.style.margin = barContainerMargin + "px";
      //barContainer.style.left = l + "px"; 

      if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
          barContainer.addEventListener("mouseover", onShowBubble, true);
          barContainer.addEventListener("mouseout", hideBubble, true);
          barContainer.addEventListener("click", filterOnItem, true);
      }else{ // e.g. Internet Explorer (also would work on Opera)
          barContainer.attachEvent("onmouseover", onShowBubble);
          barContainer.attachEvent("onmouseout", hideBubble);
          barContainer.attachEvent("onclick", filterOnItem);
      }

      barContainer.group = group;
      barContainer.groupStart = groupStart;
      barContainer.groupEnd = groupEnd;
      barContainer.hits = times[currentLevel+1][item][0];

      barContainer.item = item;
      barContainer.selectStart = times[currentLevel+1][item][1];
      barContainer.selectEnd = times[currentLevel+1][item][2];
      return barContainer;
}

function createBar(title, groupStart, groupEnd, item, t){
    var bar = document.createElement("DIV");
    bar.setAttribute("class", "da_bar");
    bar.className = "da_bar";
    bar.setAttribute("id", "da_bar_" + item);

    bar.style.height = t + "px";
    //bar.style.height = containerHeight - t + "px";
    //bar.style.top = t + "px"; 
    bar.groupStart = groupStart;
    bar.groupEnd = groupEnd;
    bar.item = item; 
    bar.selectStart = times[currentLevel+1][item][1];
    bar.selectEnd = times[currentLevel+1][item][2];
    bar.hits = times[currentLevel+1][item][0];
    bar.bubble = title;
    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
        //bar.addEventListener("click", filterOnItem, true);
        bar.addEventListener("mouseover", onShowBubble, true);
        bar.addEventListener("mouseout", hideBubble, true);
    }else{ // e.g. Internet Explorer (also would work on Opera)
        bar.attachEvent("onmouseover", showBubble);
        bar.attachEvent("onmouseout", hideBubble);
        //bar.attachEvent("onclick", filterOnItem);
    }
    return bar;
}

var lastPosition = 0;
function fillDateAxis(level){
    var index = 0;
    currentLevel = level;
    barContainerWidth = barContainerWidths[currentLevel];
    var totalHeight = (containerHeight + groupTitleHeight) + 2;
    $("#content-scroll").css("height", totalHeight);
    $("#content-holder").css("height", totalHeight);
    var row = document.createElement("DIV");
    $("#content-holder").append(row);
    //row = $("#content-holder");

    row.setAttribute("class", "da-table");
    row.className = "da-table";
    row.id = "da_table";
    row.style.height = totalHeight + "px";
    
    startTime = zooms[currentLevel][0];
    endTime = zooms[currentLevel][1];
    var groupIdx = 0;
    firstBar = null;
    lastBar = null;
    var grouptitle;
    var lastGroupWidth = 0;
    lastPosition = 0;
    calculateMaximum();
        
    var cell=document.createElement("DIV");                       
    cell.setAttribute("class", "da_group_space");
    cell.className = "da_group_space";  
    cell.style.height = totalHeight + "px"; 
    var l = lastGroupWidth;
    //cell.style.left = l + "px";
    cell.style.top = "0px";
    cell.style.width = spaceWidth + "px"; 
    row.appendChild(cell);  
        
    lastGroupWidth = spaceWidth;
    var barsNum = 0;
    for (var group in times[currentLevel]){
            
        var groupInt = parseInt(group);
        if(groupInt>=startTime && groupInt<=endTime){
            groupWidth = 0;
            
            var groupStart = parseInt(times[level][group][1]);
            var groupEnd = parseInt(times[level][group][2]);
            grouptitle = formatGroupTitle(group) + " (" + times[currentLevel][group][0] + ")";
            cell=document.createElement("DIV");
            cell.bubble = grouptitle;                
            cell.setAttribute("class", "da_group_cell");
            cell.className = "da_group_cell";  
            cell.style.height = totalHeight + "px";
            
            if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
                cell.addEventListener("click", zoomGroup, false);
            }else{ // e.g. Internet Explorer (also would work on Opera)
                cell.attachEvent("onclick", zoomGroup);
            }
            cell.group = groupInt;
            cell.groupStart = groupStart;
            cell.groupEnd = groupEnd;
                
            index = 0;
            
            var titleDiv=document.createElement("DIV");
            titleDiv.innerHTML = grouptitle;
            titleDiv.style.left = "4px";
            titleDiv.style.top = "0px";          
            titleDiv.setAttribute("class", "da_group_title");
            titleDiv.className = "da_group_title"; 
            titleDiv.style.height = groupTitleHeight + "px";                
            cell.appendChild(titleDiv);
            for (var item in times[level+1]){
                var itemInt = parseInt(item);
                if(itemInt>=groupStart && itemInt<=groupEnd){
                    var title = formatBarTitle(item) + " (" + times[level+1][item][0] + ")";
                    var h = normalizeHeight(times[currentLevel+1][item][0]);
                    var t = Math.max(containerHeight - h, 0);
                    l = index * barContainerWidth;
                    
                    var barContainer=document.createElement("DIV");
                    barContainer.bubble =title;
                    barContainer.setAttribute( "class", "da_barContainer" );
                    barContainer.setAttribute("id", "da_bar_container_" + item);
                    barContainer.className = "da_barContainer";
                    barContainer.style.height = containerHeight + "px";
                    barContainer.style.margin = barContainerMargin + "px";
                    //barContainer.style.left = l + "px"; 

                    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
                        barContainer.addEventListener("mouseover", onShowBubble, true);
                        barContainer.addEventListener("mouseout", hideBubble, true);
                        barContainer.addEventListener("click", filterOnItem, true);
                    }else{ // e.g. Internet Explorer (also would work on Opera)
                        barContainer.attachEvent("onmouseover", onShowBubble);
                        barContainer.attachEvent("onmouseout", hideBubble);
                        barContainer.attachEvent("onclick", filterOnItem);
                    }
                    
                    barContainer.group=group;
                    barContainer.groupStart=groupStart;
                    barContainer.groupEnd=groupEnd;
                    barContainer.hits = times[level+1][item][0];
                    
                    barContainer.item=item;
                    barContainer.selectStart = times[currentLevel+1][item][1];
                    barContainer.selectEnd = times[currentLevel+1][item][2];
                    cell.appendChild(barContainer);
                    
                    var bar=document.createElement("DIV");
                    bar.setAttribute("class", "da_bar");
                    bar.className = "da_bar";
                    bar.setAttribute("id", "da_bar_" + item);
                        
                    bar.style.height = t + "px";
                    //bar.style.height = containerHeight - t + "px";
                    //bar.style.top = t + "px"; 
                    bar.groupStart=groupStart;
                    bar.groupEnd=groupEnd;
                    bar.item=item; 
                    bar.selectStart = times[currentLevel+1][item][1];
                    bar.selectEnd = times[currentLevel+1][item][2];
                    bar.hits = times[level+1][item][0];
                    bar.bubble = title;
                    if (document.addEventListener != null){ // e.g. Firefox, Opera, Safari
                        //bar.addEventListener("click", filterOnItem, true);
                        bar.addEventListener("mouseover", onShowBubble, true);
                        bar.addEventListener("mouseout", hideBubble, true);
                    }else{ // e.g. Internet Explorer (also would work on Opera)
                        bar.attachEvent("onmouseover", showBubble);
                        bar.attachEvent("onmouseout", hideBubble);
                        //bar.attachEvent("onclick", filterOnItem);
                    }
                    barContainer.style.width = barContainerWidth + "px";  
                        
                    barContainer.appendChild(bar);
                    if(!firstBar && times[currentLevel+1][item][0]>0) firstBar = item;
                    index++;
                    groupWidth = groupWidth + barContainerWidth + barContainerMargin*2;
                    if(times[currentLevel+1][item][0]>0){
                        lastBar = item;
                        lastPosition = lastGroupWidth + groupWidth;
                    } 
                    barsNum++;
                }
            }
            l = lastGroupWidth;
            cell.style.left = l + "px";
            cell.style.top = "0px";
            cell.style.width = (groupWidth + 2) + "px";
            lastGroupWidth = lastGroupWidth + groupWidth;
            //lastPosition = lastGroupWidth;
            row.appendChild(cell);
            groupIdx++;
        }
    }     
    cell=document.createElement("DIV");                       
    cell.setAttribute("class", "da_group_space");
    cell.className = "da_group_space";  
    cell.style.height = totalHeight + "px"; 
    l = lastGroupWidth;
    cell.style.left = l + "px";
    cell.style.top = "0px";
    cell.style.width = spaceWidth + "px"; 
    row.appendChild(cell);
    l = l + spaceWidth + spaceWidth;
    $("#content-holder").css("width", l + "px");
    $("#content-scroll").css("width", containerWidth + "px");
    $("#content-slider").css("width", containerWidth + "px");
    $("#scrollBar-right").css("width", containerWidth + "px");
}