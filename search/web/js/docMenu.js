

/*
 * fired on click in rels-ext item element
 */
function selectRelItem(obj){
    if($(obj).hasClass("selected")) return;
    currentSelectedPage=0;
    
    var divId = $(obj).parent().parent().attr('id');  // for example divId = tab3-page
    var level = parseInt(divId.substring(3, divId.indexOf("-")));
    var model = divId.substring(divId.indexOf("-")+1);
    if(model=="page"){
        selectPage($(obj).attr("pid"));
    }else{
        selectItem(obj, level, model);
    }
    hideRelsList(level, model);
}

/*
 *toggle rels-ext list
 */
function toggleRelsList(obj, model){
    var m = model;
    if(m.indexOf("-")>-1){
        m = m.split("-")[1];
    }
    var tab = "#" + $(obj).parent().parent().parent().attr('id');
    
    if($(tab + ">div>div[id=list-"+m+"]").is(":visible")){
        hideRelsList(tab.split("_")[1], m);
    }else{
        showRelsList(tab, m);
    }
}

/*
 *hides rels-ext list
 */
function hideRelsList(level, model){
    $("#tabs_" + level + ">ul>li." + model + ">img").removeClass('op_info');
    $("#tabs_" + level + ">div>div[id=list-"+model+"]").hide();
}


/*
 *shows rels-ext list
 */
function showRelsList(tab, m){
    if($(tab + ">div>div[id=info-"+m+"]").text()==""){
        $(tab + ">div>div[id=info-"+m+"]").html($(tab+">div>div[id=list-"+m+"]>div.selected").text());
    }
    var h = $(window).height() - $(tab).offset().top - $(tab).height();
    $(tab + ">div>div[id=list-"+m+"]").css('max-height', h);
    $(tab + ">div>div[id=list-"+m+"]").css('_height', 'expression(this.scrollHeight > '+h+'? "'+h+'px" : "auto" )');
    var w = $(tab + ">div>div[id=list-"+m+"]").parent().width() - 4;
    $(tab + ">div>div[id=list-"+m+"]").css('width', w);
    $(tab + ">div>div[id=list-"+m+"]").show();

    var selected = $(tab+">div>div[id=list-"+m+"]>div.selected");
    scrollElement($(selected).parent(), $(selected));

    //change arrow
    //$(obj).addClass('op_info');
    $(tab + ">ul>li." + m + ">img").addClass('op_info');
}

function slideToThumb(uuid){
    var to = $('#img'+getMaxLevel()+'_' + uuid).offset().left - tvContainerLeft + $("#tv_container").attr("scrollLeft") - ($("#tv_container").width()/2) ;
        
    var maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
    var to2 = 0;
    if(maxScroll > 0){
        to2 = to * 100 / maxScroll;
    }
       
    //canScroll = false;
    slideTo(to2);
}

/*
 * Change the selected page
 */
function selectPage(uuid){
    var changeImage = currentSelectedPage != uuid;
    // set thumb selection 
    $('.tv_image').removeClass('tv_img_selected');
    currentSelectedPage = uuid;
    var to = $('#img'+getMaxLevel()+'_' + uuid).offset().left - tvContainerLeft + $("#tv_container").attr("scrollLeft") - ($("#tv_container").width()/2) ;
//        
//    var maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
//    var to2 = 0;
//    if(maxScroll > 0){
//        to2 = to * 100 / maxScroll;
//    }
    //slideTo(to2, uuid);
       
    canScroll = false;
    slideToThumb(uuid);
    activateThumbs();
    
    $("#tabs_"+getMaxLevel()).attr('pid', currentSelectedPage);
    var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
        
    checkArrows();
    //alert($('#main').width()-60-$('.itemMenu').width());
    //$("#mainContent").css('width', $(window).width()-60-$('.itemMenu').width());
    $("#mainContent").css('width', $(window).width()-60-$('.itemMenu').width());

      $.ajax({
          url:"viewInfo?uuid="+uuid,
          complete:function(req,textStatus) {
              viewerOptions = eval('(' + req.responseText + ')');
              viewerOptions.uuid = uuid;	
              viewerOptions.status=req.status;
              
              if ((req.status==200) || (req.status==304)) {
                  securedContent = false;
                  currentMime = req.responseText;
                  showImage(viewerOptions);
              } else if (req.status==403){
                  currentMime = "unknown";
                  securedContent = true;
                  displaySecuredContent();
              } else if (req.status==404){
                    alert("Neni velky nahled !");
              } else {
                    alert("Jina Chyba");
                  // jina chyba serveru
              }
 
	  }
     });    

    $('#img'+getMaxLevel()+'_'+uuid).toggleClass('tv_img_selected');
    $("#tv_container").attr("scrollLeft", to);
    canScroll = true;
    // set selected page in menu
    changeSelectedItem(uuid);
}

function selectThumb(uuid){
    $('.tv_image').removeClass('tv_img_selected');
    $('#img'+getMaxLevel()+'_'+uuid).toggleClass('tv_img_selected');
}

/*
 *selects previous page
 */
function selectPrevious(){
    var curMaxLevel = maxLevelForFullImageShow > -1 ? maxLevelForFullImageShow : getMaxLevel();	
    var obj =$('#tab'+curMaxLevel+'-page>div.relList>div.selected').prev();
    if($(obj).length>0){
        selectPage( $(obj).attr("pid"));
    }
}

/*
 *selects next page
 */
function selectNext(){
    var curMaxLevel = maxLevelForFullImageShow > -1 ? maxLevelForFullImageShow : getMaxLevel();	
    var obj =$('#tab'+curMaxLevel+'-page>div.relList>div.selected').next();
    if($(obj).length>0){
        selectPage( $(obj).attr("pid"));
    }
}

/*
 *Changes the selected item in rels-ext list, and update selected item info
 */
function changeSelectedItem(pid){
    $(".relItem[pid='" + pid + "']").each(function(i, obj){
        $(obj).parent().children(".relItem").removeClass('selected');
        $(obj).addClass('selected');
        var infoObj = $(obj).parent().parent().children("[id|=info]");
        infoObj.html($(obj).text());
        scrollElement($(obj).parent(), $(obj));
    });
}

/*
 * Change selected element on menu. Not a page
 */
var currentLevel;
function selectItem(obj, level, model){
    // change selection
    slideTo(0);
    inactiveWidthSet = false;
    $(obj).parent().children(".relItem").removeClass('selected');
    $(obj).addClass('selected');
    var target = level+1;
    clearThumbsFromLevel(target);
    
    var d1 = "#tabs_" + level;
    var pid = $(obj).attr("pid");
    $(d1).attr('pid', pid);
    $(d1 + ">div>div[id=info-"+model+"]").html($(obj).text());
    var img = d1 + ">ul>li.ui-tabs-selected>img";
    $('#imgBig').attr("src", "img/empty.gif");
    
    //clear menu level
    var d2 = "#tabs_" + target;
    var p = "#tab" + level + "-" + model;
    if($(d2).length>0){
        //var p = $(d2).parent();
        var l = $(d2).tabs('length');
        for(var i=0;i<l;i++){
            $(d2).tabs("remove", 0);
        }
        $(d2).remove();
    }
    
    // get level content for the model
    getItemLevel(pid, target, p, true);
}

function getItemLevel(pid, level, container, recursive, onlyrels, model){
    var url ="inc/details/getItemForBrowse.jsp?pid="+pid+"&level="+level+"&onlyrels="+onlyrels;
    if(model){
        url += "&model=" + model;
    }
    $.get(url, function(data){
        var html = trim10(data)
        if(html!=""){
            $(container).append(html);
            changingTab = false;
            addThumbs(level);
            if(!onlyrels){
                $("#tabs_" + level).tabs({ 
                    tabTemplate: '<li><a href="#{href}">#{label}</a><img width="12" src="img/empty.gif" class="op_list" onclick="toggleRelsList(this, \'#tabs_'+level+'\', \'#{href}\')" /></li>',
                    panelTemplate: '<div></div>',
                    show: function(event, ui){
                        updateThumbs();
                    },
                    select: function(event, ui){
                        changingTab=true;
                    }
                });
            }
            translate(level);
            if(recursive){
                getNextLevel(level);
            }
        }else{
            updateThumbs(level-1);
            var obj =$('#tab'+getMaxLevel()+'-page>div.relList>div:first');
            if($(obj).length>0 && !currentSelectedPage){
                        changingTab=true;
                updateThumbs();
                        changingTab=false;
                selectPage( $(obj).attr("pid"));
            }
        }
        
        
    });
}

function getNextLevel(level){
    var pid;
    var target = level + 1;
    //var d2 = "#tabs_" + target;
    $('#tabs_'+level+'>div').each(function(index, o){
        //alert(o.id);
        pid = $("#"+o.id+">div.relList>div:first").attr('pid');
        getItemLevel(pid, target, "#"+o.id, true, false);
    });
}

function createTab(level, model){
    $('#tabs_'+level).tabs("add", "#tab"+level+"-"+model, dictionary[model]);
    l = $('#tabs_'+level+'>ul>li').length - 1;
    $('#tabs_'+level+'>ul>li:last').addClass(model);
    $('#tabs_'+level+'>ul>li:last').removeClass("#tab"+level+"-"+model);
    $("#tab"+level+"-"+model).append('<div id="list-'+model+'" class="relList" style="display: none;"></div>');
    $("#tab"+level+"-"+model).append('<div id="info-'+model+'"></div>');
}

function getChildModels(level, recursive){
    var pid = $('#tabs_'+level).attr('pid');
    var url ="inc/details/getChildModels.jsp?pid="+pid;
    var model;
    var target = level + 1;
    $.getJSON(url, function(data){
        for(var i in data){
            model = data[i];
            if($('#tab'+target+'-'+model).length == 0){
                //neexistuje zalozku. Pridame
                createTab(target, model);
                fillRels(pid, target, 0, model, true);
            
                //getChildModels(target, recursive);
            }
        }
    });
}

function fillRels(pid, level, offset, model, recursive){
    var url ="inc/details/getItemForBrowse.jsp?pid="+pid+"&level="+level+"&offset="+offset+"&rows="+rowsPerQuery+"&onlyrels=true";
    if(model){
        url += "&model=" + model;
    }
    $.get(url, function(data){
        $("#tab"+level+"-"+model+">div.relList").append(data);
        changingTab = false;
        addThumbs(level);
        translate(level);
        changeSelectedItem(currentSelectedPage);
        var t = $("#tab"+level+"-"+model+">div[id=info-"+model+"]").html();
        if(t==""){
            t = $("#tab"+level+"-"+model+">div.relList>div.relItem:first").html()
            $("#tab"+level+"-"+model+">div[id=info-"+model+"]").html(t);
        }
        var numDocs = parseInt($("#tab"+level+"-"+model+">div.relList>div.numDocs").html());
        if(numDocs+offset>rowsPerQuery){
            $("#tab"+level+"-"+model+">div.relList>div.numDocs").remove();
            fillRels(pid, level, offset+rowsPerQuery, model, recursive);
        }else{
            if(recursive){
                var target = level+1;
                var uuid = $("#tab"+level+"-"+model+">div.relList>div.relItem:first").attr("pid");
                getItemLevel(uuid, target, "#tab"+level+"-"+model, true, false);
                //getNextLevel(level);
            } 
        }
    });
}

var rowsPerQuery = 200;
function getRelsInLevel(level, recursive, offset){
    var pid;
    var model;
    //var d2 = "#tabs_" + target;
    $('#tabs_'+level+'>div[id|=tab'+level+']>div.relList').each(function(index, o){
        //alert(o.id);
        pid = $("#"+o.id).parent().parent().parent().attr('pid');
        var divId = $("#"+o.id).parent().attr('id');
        model = divId.substring(divId.indexOf("-")+1);
        fillRels(pid, level, offset, model, recursive);
    });
}

function getRels(recursive){
    var maxLevel = getMaxLevel();
    for(var i=2;i<=maxLevel;i++){
        getRelsInLevel(i,recursive, 0);
    }
    for(var i=2;i<=maxLevel;i++){
        getChildModels(i, recursive);
    }
}

/*
 * translate elements enclosed in span tags with class="translate"
 */
function translate(level){
    var text;
    
    $('#tabs_'+level+'>div>div>div.relItem>span.translate').each(function(index, o){
        text = $(o).html();
        if(typeof(dictionary[text])!= 'undefined'){
            $(o).html(dictionary[text]);
        }
    });
    $('#tabs_'+level+'>ul>li>a>span.translate').each(function(index, o){
        text = $(o).html();
        if(typeof(dictionary[text])!= 'undefined'){
            $(o).html(dictionary[text]);
        }
    });
}

/*
 * Adds thumbnails from menu tree to thumbs scroller
 */
function addThumbs(level){
    var uuid;
    var display = level == getMaxLevel()? 'default' : 'none';
    $('#tab'+level+'-page>div.relList>div.relItem').each(function(index){
        uuid = $(this).attr('pid');
        //totalThumbs++;
        addThumb(uuid, display, level);
    });
    if(level == getMaxLevel()){
        setTimeout('checkScrollPosition()', 100);
    }
    
    //canScroll = true;
    setTimeout('activateThumbs()', 200);
}


/*
 * load thumb when becomes visible
 */  
function activateThumbs(){
    var level = getMaxLevel();
    var w = getTvContainerWidth();
    var leftBorder = getTvContainerLeft();
    var rightBorder = leftBorder + w;
    //alert(rightBorder);
    var l;
    var src;
    var i = 0;
    $('#tv_container_row>td.inlevel_'+level+'>div>img.tv_img_inactive').each(function(){
        l = $(this).offset().left;
        if( 
        //$(this).attr('src')=='img/empty.gif' &&
        l + $(this).width() > leftBorder && 
            l < rightBorder)
        {
          
            src = 'thumb?outputFormat=RAW&uuid='+$(this).parent().attr('pid');
            $(this).attr('src', src);
            $(this).bind('load', function(){
                //alert(1);
                $(this).removeClass('tv_img_inactive');
            });
            
            i++;
        }
        //$(this).parent().parent().append('<br/>' + l + ' - ' + leftBorder + ' - ' + rightBorder);
    });
}
    
 
function updateThumbs(level){
    if(changingTab){
        initPage = null;
        var maxLevel;
        if(level){
            maxLevel = level;
        }else{
            maxLevel = getMaxLevel();
        }
         
        $('.thumb').hide();
        $('.inlevel_'+maxLevel).show();
        if($('#img'+maxLevel+'_'+currentSelectedPage).is(':visible')){
            //alert(1);
            //changeSelection(currentSelectedPage);
            selectThumb(currentSelectedPage);
            slideToThumb(currentSelectedPage);
        }else{
            var pid = $("#tab"+maxLevel+"-page>div.relList>div:first").attr("pid");
            selectPage(pid);
            //selectThumb(pid);
        }
        activateThumbs();
        checkArrows();
    }
}
    
function addThumb(uuid, display, level){
    //var img = '<img onload="checkScrollPosition()" id="img'+level+'_'+uuid+'" class="tv_image';
    var img = '<img id="img'+level+'_'+uuid+'" class="tv_image';
    if(currentSelectedPage==uuid){
        img += ' tv_img_selected" onclick="selectPage(\''+uuid+'\');" src="thumb?outputFormat=RAW&amp;uuid='+uuid+'" />';
    }else{
        img += ' tv_img_inactive" onclick="selectPage(\''+uuid+'\');" src="img/empty.gif" />';
    }
    var td = '<td align="center" style="display:'+display+';" class="thumb inlevel_'+level+'"><div pid="'+uuid+'">' + img + '</div></td>';
    $('#tv_container_row').append(td);
    //if(totalThumbs==0){
    //    $('#tv').hide();
    //}else{
        $('#tv').show();
    //}
    checkArrows();
}
    
function clearThumbsFromLevel(startLevel){
    var maxlevel = 10;
    for(var level=startLevel;level<=maxlevel;level++){
        //totalThumbs = totalThumbs - $('#tv_container_row>td.inlevel_'+level).length;
        $('#tv_container_row>td.inlevel_'+level).remove();
    }
        
}
function clearThumbs(){
    var level = getMaxLevel();
    //totalThumbs = totalThumbs - $('#tv_container_row>td.inlevel_'+level).length;
    $('#tv_container_row>td.inlevel_'+level).remove();
}

/*
 *       find max active level in menu
 */       
function getMaxLevel(){
    var maxLevel = 1;
    var id;
    var cur;
    //alert($('.ui-tabs').length);
    $('.ui-tabs').each(function(index){
        id = $(this).attr('id');
        cur = parseInt(id.substr(5));
        if($('#'+id).is(':visible') && cur>maxLevel){
            maxLevel = cur;
        }
    });
    return maxLevel;
}

/*
 *  Display next previous arrows 
 */
function checkArrows(){
    var curMaxLevel = maxLevelForFullImageShow > -1 ? maxLevelForFullImageShow : getMaxLevel();	
    var selImg = $('#img'+curMaxLevel+'_' + currentSelectedPage).parent().parent();
    var obj = selImg.prev();
    if($(obj).length>0){
        $('.prevArrow').show();
    }else{
        $('.prevArrow').hide();
    }
    obj = selImg.next();
    if($(obj).length>0){
        $('.nextArrow').show();
    }else{
        $('.nextArrow').hide();
    }
}
    
var tvContainerWidth = 0;
function setTvContainerWidth(){
     
    tvContainerWidth = $(window).width() - 40;
    $("#tv_container").css("width", tvContainerWidth);
}
function getTvContainerWidth() {
    if(tvContainerWidth==0){
        setTvContainerWidth();
    }
    return tvContainerWidth;	
}

var tvContainerLeft = 0;
function getTvContainerLeft() {
    if(tvContainerLeft){
        tvContainerLeft = $("#tv_container").offset().left;
    }
    return tvContainerLeft;	
}

