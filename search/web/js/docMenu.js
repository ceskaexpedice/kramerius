

var changingTab = false;
/*
 * fired on click in rels-ext item element
 */
function selectRelItem(obj){
    if($(obj).hasClass("selected")) return;
    currentSelectedPage=0;
    
    var divId = $(obj).parent().parent().attr('id');  // for example divId = tab3-page
    var level = parseInt(divId.substring(3, divId.indexOf("-")));
    var model = divId.substring(divId.indexOf("-")+1);
    if($(obj).hasClass('viewable')){
    //if(model=="page"){
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
    $("#tabs_" + level + ">ul>li." + model + ">img:first").removeClass('op_info');
    $("#tabs_" + level + ">div>div[id=list-"+model+"]").hide();
}

/*
 *shows rels-ext list
 */
function showRelsList(tab, m){
    if($(tab + ">div>div[id=info-"+m+"]").text()==""){
        $(tab + ">div>div[id=info-"+m+"]").html($(tab+">div>div[id=list-"+m+"]>div.selected").text());
    }
    var h = $(window).height() - $(tab).offset().top - $(tab+">ul").height() - 10;
    $(tab + ">div>div[id=list-"+m+"]").css('max-height', h);
    $(tab + ">div>div[id=list-"+m+"]").css('_height', 'expression(this.scrollHeight > '+h+'? "'+h+'px" : "auto" )');
    var w = $(tab + ">div>div[id=list-"+m+"]").parent().width() - 4;
    $(tab + ">div>div[id=list-"+m+"]").css('display', 'block');
    $(tab + ">div>div[id=list-"+m+"]").css('height', '');
    $(tab + ">div>div[id=list-"+m+"]").css('width', w);

    var selected = $(tab+">div>div[id=list-"+m+"]>div.selected");
    scrollElement($(selected).parent(), $(selected));

    //change arrow
    //$(obj).addClass('op_info');
    $(tab + ">ul>li." + m + ">img:first").addClass('op_info');
}

function slideToThumb(uuid){
    if($('#img'+getMaxLevel()+'_' + uuid).length){
        var to = $('#img'+getMaxLevel()+'_' + uuid).offset().left - tvContainerLeft + $("#tv_container").attr("scrollLeft") - ($("#tv_container").width()/2) ;
        
        var maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
        var to2 = 0;
        if(maxScroll > 0){
            to2 = to * 100 / maxScroll;
        }
        slideTo(to2);
    }
}

/*
 * Change the selected page
 */
function selectPage(uuid){
    hideAlto();
    var changeImage = currentSelectedPage != uuid;
    // set thumb selection 
    $('.tv_image').parent().removeClass('tv_img_selected');
    currentSelectedPage = uuid;
       
    canScroll = false;
    slideToThumb(uuid);
    activateThumbs();
    
    $("#tabs_"+getMaxLevel()).attr('pid', currentSelectedPage);
    var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
        
    checkArrows();
    

    //getViewInfo(uuid, showImage);
    getViewInfo(uuid, function(viewerOptions) {
    	// initializing new object	
    	imageContainerObject = new ImageContainer(viewerOptions);
    	// display object
    	imageContainerObject.display();
    }, function(viewerOptions) {
    	imageContainerObject = new ImageContainer(viewerOptions);
    	imageContainerObject.noImage();
    }, function (viewerOptions) {
    	imageContainerObject = new ImageContainer(viewerOptions);
    	imageContainerObject.securityError();
    });
    
    var maxLevel = getMaxLevel();
    $('#img'+getMaxLevel()+'_'+uuid).parent().toggleClass('tv_img_selected');
    if($('#img'+getMaxLevel()+'_' + uuid).length>0){
        var to = $('#img'+getMaxLevel()+'_' + uuid).offset().left - tvContainerLeft + $("#tv_container").attr("scrollLeft") - ($("#tv_container").width()/2) ;
        $("#tv_container").attr("scrollLeft", to);
    }
    canScroll = true;
    // set selected page in menu
    changeSelectedItem(uuid);
    
    //if(!$("#fullImageContainer").dialog("isOpen")){
    if($("#centralContent").is(":visible")){    
        var model = $('#tabs_'+maxLevel+'>div:visible').attr('id').split('-')[1];
        getExtendedModelMetadata(uuid, maxLevel, false, model);
    }
    setMainContentWidth();
}

function getViewInfo(uuid, f, noImageF, securityErrorF){
    $.ajax({
        url:"viewInfo?uuid="+uuid,
        complete:function(req,textStatus) {
              
            if ((req.status==200) || (req.status==304)) {
                viewerOptions = eval('(' + req.responseText + ')');
                viewerOptions.uuid = uuid;
                viewerOptions.status=req.status;
            	  
                if ((viewerOptions.rights["read"][uuid]) && (viewerOptions.imgfull)) {
                    securedContent = false;
                    currentMime = req.responseText;
                    f(viewerOptions);
                } else if (!viewerOptions.imgfull) {
                    currentMime = "unknown";
                    securedContent = false;
                    if (noImageF) noImageF(viewerOptions);
                } else {
                    currentMime = "unknown";
                    securedContent = true;
                    if (securityErrorF) securityErrorF(viewerOptions);
                }
            	  
                  
            //              } else if (req.status==403){
            //                  currentMime = "unknown";
            //                  securedContent = true;
            //                  displaySecuredContent();
            } else if (req.status==404){
                alert("Neni velky nahled !");
            //              } else {
            //                    alert("Jina Chyba");
            }
 
        }
    });
}

function selectThumb(uuid){
    $('.tv_image').parent().removeClass('tv_img_selected');
    $('#img'+getMaxLevel()+'_'+uuid).parent().toggleClass('tv_img_selected');
}

/*
 *selects previous page
 */
function selectPrevious(){
    initPage = "";
    var curMaxLevel = maxLevelForFullImageShow > -1 ? maxLevelForFullImageShow : getMaxLevel();

    var obj = maxLevelForFullImageShow > -1 ? $('#'+selectedListForFullImageShow+'>div.relList>div.selected').prev() : $('#tabs_'+curMaxLevel+'>div:visible>div.relList>div.selected').prev();

    //if($('#'+id).is(':visible') && cur>maxLevel)
    //var obj =$('#tabs_'+curMaxLevel+'>div:visible>div.relList>div.selected').prev();
    //var obj =$('#tab'+curMaxLevel+'-page>div.relList>div.selected').prev();
    if($(obj).length>0){
        selectPage( $(obj).attr("pid"));
    }
}

/*
 *selects next page
 */
function selectNext(){
    initPage = "";
    var curMaxLevel = maxLevelForFullImageShow > -1 ? maxLevelForFullImageShow : getMaxLevel();
    var obj = maxLevelForFullImageShow > -1 ? $('#'+selectedListForFullImageShow+'>div.relList>div.selected').next() : $('#tabs_'+curMaxLevel+'>div:visible>div.relList>div.selected').next();

    //var obj =$('#tabs_'+curMaxLevel+'>div:visible>div.relList>div.selected').next();
    //var obj =$('#tab'+curMaxLevel+'-page>div.relList>div.selected').next();
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
    initPage = "";
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
                var tabtempl = '<li><a href="#{href}">#{label}</a>'+
                '<img width="12" src="img/empty.gif" class="op_list" onclick="toggleRelsList(this, \'#tabs_'+level+'\', \'#{href}\')" /><img width="12" src="img/lupa.png" class="searchInsideButton" alt="search" ' +
                'onclick="showSearchInside('+level+', \''+model+'>\')" /></li>';
                $("#tabs_" + level).tabs({ 
                    tabTemplate: tabtempl,
                    panelTemplate: '<div></div>',
                    show: function(event, ui){
                        updateThumbs();
                    },
                    select: function(event, ui){
                        initPage = "";
                        changingTab=true;
                    }
                });
            }
            translate(level);
            if(recursive){
                getNextLevel(level);
            }
        }else{
            var obj =$('#tab'+getMaxLevel()+'-page>div.relList>div:first');
            changingTab=true;
            updateThumbs();
            changingTab=false;
            if($(obj).length>0 && !currentSelectedPage){
                selectPage( $(obj).attr("pid"));
            }
            getExtendedMetadata();
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
        var model = o.id.substring(o.id.indexOf("-")+1);
        getItemMenuOptions(pid, level, model);
    });
}

function createTab(level, model){
    $('#tabs_'+level).tabs("add", "#tab"+level+"-"+model, dictionary['fedora.model.'+model]);
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
        if(data.length>0){
            for(var i in data){
                model = data[i];
                if($('#tab'+target+'-'+model).length == 0){
                    //neexistuje zalozku. Pridame
                    createTab(target, model);
                    fillRels(pid, target, 0, model, true);
                    getItemMenuOptions(pid, target, model);
                //getChildModels(target, recursive);
                }
            }
        }else{
            //var model = $('#tabs_'+level+'>div:visible').attr('id').split('-')[1];
            //getExtendedModelMetadata(pid, level, false, model);
            
            getExtendedMetadata();
        }
    });

}

function getItemMenuOptions(pid, level, model){
    //alert(level);
    if($("#openmenu"+level+"-"+model).length > 0) return;
    var pid_path = "";
    var path = "";
    var id;
    //    for(var i=level;i>0;i--){
    //        id = $("#tabs_"+i+">div:first").attr('id');
    //        if(i==level){
    //          pid = $("#tabs_"+i).attr('pid');
    //          path = id.substring(id.indexOf("-") + 1 );
    //        }else{
    //          pid_path = $("#tabs_"+i).attr('pid') + "/" + pid_path;
    //          path = id.substring(id.indexOf("-") + 1 ) + "/" + path;
    //        }
    //    }
    var i = level;
    var cmodel = model;
    var lastpid = "";
    while(i>0){
        //alert(i);
        //alert(cmodel);
        //alert("#tab"+i + "-"+cmodel);
        id = $("#tab"+i + "-"+cmodel).attr('id');
        if(!id){
            break;
        }
        if(i==level){
            pid_path = $("#tabs_"+i).attr('pid');
            lastpid = pid_path;
            path = id.substring(id.indexOf("-") + 1 );
        }else{
            pid_path = $("#tabs_"+i).attr('pid') + "/" + pid_path;
            path = id.substring(id.indexOf("-") + 1 ) + "/" + path;
        }
        if($("#tabs_"+i).parent().parent()==0){
            i=0;
        }else{
            var idi = $("#tabs_"+i).parent().parent().attr('id');
            var modeli = $("#tabs_"+i).parent().attr('id');
            i = idi.substring(5);
            //alert(modeli);
            cmodel = modeli.substring(5);
        //alert(cmodel);
        }
        
    //i--;
    }
    
    var url ="inc/details/itemMenuOptions.jsp?pid="+lastpid+"&pid_path="+pid_path+"&path="+path;
    //if(level==4) alert(model);
    $.get(url, function(data){
        $("#tab"+level+"-"+model).prepend(data);
        
    });
}

function getFirstLevelMenu(pid, model){
    if($("#openmenu1-"+model).length > 0) return;
    var url ="inc/details/itemMenuOptions.jsp?pid="+pid+"&pid_path="+pid+"&path="+model;
    $.get(url, function(data){
        $("#tab1-"+model+">div.relList").parent().prepend(data);
        
    });
}

function fillRels(pid, level, offset, model, recursive){
    var url ="inc/details/getItemForBrowse.jsp?pid="+pid+"&level="+level+"&offset="+offset+"&rows="+rowsPerQuery+"&onlyrels=true";
    if(model){
        url += "&model=" + model;
    }
    $.get(url, function(data){
        $("#tab"+level+"-"+model+">div.relList").append(data);
        //changingTab = false;
        //addThumbs(level);
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
            changingTab = false;
            addThumbs(level);
            translate(level);
            changeSelectedItem(currentSelectedPage);
            if(recursive){
                var target = level+1;
                var uuid = $("#tab"+level+"-"+model+">div.relList>div.relItem:first").attr("pid");
                getItemLevel(uuid, target, "#tab"+level+"-"+model, true, false);
                
            //getNextLevel(level);
            } else{
                changingTab=true;
                updateThumbs();
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
        getItemMenuOptions(pid, level, model);
    });
}

function getRels(recursive){
    var maxLevel = getMaxLevelAbsolute();
    for(var i=2;i<=maxLevel;i++){
        getRelsInLevel(i,recursive, 0);
    }
    for(var i=1;i <= maxLevel;i++){
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

function translateDiv(div){
    var text;

    $('#'+div+' span.translate').each(function(index, o){
        text = $(o).html();
        if(typeof(dictionary[text])!= 'undefined'){
            $(o).html(dictionary[text]);
        }
    });
}

function checkThumbs(){
    var n = $('#tv_container_row>td').length;
    //alert(n);
    if(n==0){
        $('#tv').css("height","0px");
        $('#tv').css("overflow","hidden");
    }else{
        $('#tv').css("height","");
        $('#tv').css("overflow","");
    }
}

/*
 * Adds thumbnails from menu tree to thumbs scroller
 */
function addThumbs(level){
    var uuid;
    var model;
    var display = level == getMaxLevel()? 'default' : 'none';
    $('#tabs_'+level+'>div').each(function(i){
        model = $(this).attr('id').split('-')[1];
        $('#tab'+level+'-'+model+'>div.relList>div.relItem.viewable').each(function(index){
            uuid = $(this).attr('pid');
            //totalThumbs++;
            addThumb(uuid, display, level, model);
        });
    });
    checkArrows();
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
    checkThumbs();
    var level = getMaxLevel();
    var w = getTvContainerWidth();
    var leftBorder = getTvContainerLeft();
    var rightBorder = leftBorder + w *2; 
    var l;
    var src;
    var i = 0;
    var model;
    if(maxLevelForFullImageShow > -1){
        model = $( '#'+selectedListForFullImageShow ).attr("id").split("-")[1];
    }else{
        model = $( "#tabs_" + level+">div:visible" ).attr("id").split("-")[1];
    } 


    $('#tv_container_row>td.inlevel_'+level+'.'+model+'>div>img.tv_img_inactive').each(function(){
        l = $(this).offset().left;
        if( 
            l + $(this).width() > leftBorder &&
            l < rightBorder)
            {
        	
        	// XXX: Changed from thumb servlet to img servlet
            //src = 'thumb?outputFormat=RAW&uuid='+$(this).attr('id').split("_")[1];
            src = 'img?uuid='+$(this).attr('id').split("_")[1]+'&stream=IMG_THUMB&action=GETRAW';
            
            $(this).attr('src', src);
            $(this).bind('load', function(){
                $(this).removeClass('tv_img_inactive');
            });
            
            i++;
        }
    });
}
    
function updateThumbs(level){
    if(changingTab){
        
        var maxLevel;
        if(level){
            maxLevel = level;
        }else{
            maxLevel = getMaxLevel();
        }
        var model = $( "#tabs_" + maxLevel+">div:visible" ).attr("id").split("-")[1];
         
        $('.thumb').hide();
        $('.inlevel_'+maxLevel+'.'+model).show();
        if($('#img'+maxLevel+'_'+currentSelectedPage).is(':visible')){
            selectThumb(currentSelectedPage);
            slideToThumb(currentSelectedPage);
        }else{
            var pid = $( "#tabs_" + maxLevel+">div:visible>div.relList>div.relItem:first" ).attr("pid");
            if (initPage=="") selectPage(pid);
        }
        activateThumbs();
        checkArrows();
    }
}
    
function addThumb(uuid, display, level, model){
    var div;
    var img = '<img id="img'+level+'_'+uuid+'" onload="onLoadThumb(this);" class="tv_image';
    if(currentSelectedPage==uuid){
        div = '<div class="tv_img_selected">';
        img += '"  onclick="selectPage(\''+uuid+'\');" src="thumb?outputFormat=RAW&amp;uuid='+uuid+'" />';
    }else{
        div = '<div>';
        img += ' tv_img_inactive" onclick="tc(this);" src="img/empty.gif" />';
    }
    var td = '<td align="center" style="display:'+display+';" class="thumb inlevel_'+level+' '+model+'">' + div + img + '</div></td>';
    $('#tv_container_row').append(td);
    //if(totalThumbs==0){
    //    $('#tv').hide();
    //}else{
    $('#tv').show();
//}
    
}

function tc(obj){
    var pid = $(obj).attr('id');
    pid = pid.split("_")[1];
    selectPage(pid);
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
 *       find max level in menu
 */       
function getMaxLevelAbsolute(){
    var maxLevel = 1;
    var id;
    var cur;
    //alert($('.ui-tabs').length);
    $('.ui-tabs').each(function(index){
        id = $(this).attr('id');
        cur = parseInt(id.substr(5));
        if(cur>maxLevel){
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

function _checkDonator(pid){
    if(!pid) return;
    var url ="GetRelsExt?relation=donator&format=json&pid=uuid:"+pid;
    //var url ="inc/details/getItemForBrowse.jsp?pid="+pid+"&level=0&onlyrels=true&model=donator";
    
    
    $.getJSON(url, function(data){
        $.each(data.items, function(i,item){
            $.each(item, function(m,model2){
                if(model2[0].indexOf("donator")>-1){
                    var donatortext = 'proxy?pid=donator:'+model2[1]+'&dsname=TEXT-';
                    if(language==""){
                        donatortext += "CS";
                    }else{
                        donatortext += language.toUpperCase();
                    }
                    $.get(donatortext, function(data){
                        $("#donatorContainer").html('<div class="donator"><img height="50" src="proxy?pid=donator:'+model2[1]+'&dsname=LOGO" alt="'+data+'" title="'+data+'" /></div>');
                    });
                }
            });
        }); 
    });
}

function checkDonator(pid){
    if(!pid) return;
    var url ="inc/details/donator.jsp?uuid="+pid;
    $.get(url, function(data){
		if(data!=""){
			var donatortext =  dictionary['donator.'+data];
			$("#donatorContainer").html('<div class="donator"><img height="50" src="proxy?pid=donator:'+data+'&dsname=LOGO" alt="'+donatortext+'" title="'+donatortext+'" /></div>');
		}
    });
}

function getExtendedMetadata(){
    var model = $('#tabs_1>div').attr('id').split('-')[1];
    var pid = $('#tabs_1').attr('pid');
    getExtendedModelMetadata(pid, 1, true, model);
}

function getExtendedModelMetadata(pid, level, next, model){
    var url = "inc/details/metadata.jsp?pid=" + pid + "&model=" + model + "&level=" + level;
    $.get(url, function(data){
        if($("#extendedMetadata div.level"+level).length==0){
            $("#extendedMetadata div.level"+(level-1)).append('<div class="level'+level+'">'+data+'</div>');
        }else{
            $("#extendedMetadata div.level"+level).html(data);
        }
        if(next && level<getMaxLevel()){
            var nextLevel = level + 1;
            var model = $('#tabs_'+nextLevel+'>div:visible').attr('id').split('-')[1];
            var pid = $('#tabs_'+nextLevel+'>div:visible').parent().attr('pid');
            getExtendedModelMetadata(pid, nextLevel, next, model);
        }
    });
}
