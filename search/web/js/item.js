
function getFirstAndContinue(pid, model, div, list, models){
    var url = 'inc/details/biblioToRdf.jsp?&pid=uuid:' + pid + "&xsl="+model+".jsp&language=" + language;
    $.get(url, function(xml) {
        $(div).html(xml);
        if($(div).hasClass("selected")){
            $(div).parent().parent().children(".relInfo").html(xml);
        }
        alert(xml);
        var pid2;
        for(var i=1;i<models.length;i++){
            pid2 = models[i]; 
            getBiblioInfo(pid2, model, list+">div[id="+pid2+"]");
        }
    });
}

function getBiblioInfo(pid, model, div){
    var url = 'inc/details/biblioToRdf.jsp?&pid=uuid:' + pid + "&xsl="+model+".jsp&language=" + language;
        
    $.get(url, function(xml) {
        $(div).html(xml);
    });
}

function scrollElement(container, element){
    $(container).animate({
        scrollTop: $(element).offset().top - $(container).offset().top + $(container).scrollTop(),
        scrollLeft: $(element).offset().left
    }, 750);
    //$(container).scrollTop($(element).offset().top - $(container).offset().top + $(container).scrollTop());
    //$(container).scrollLeft($(element).offset().left);
        
}


var imgLoading = "<img src=\"img/loading.gif\" />";
var imgLoadingBig = '<div align="center" style="height:300px;padding:50%;"><img src="img/item_loading.gif" /></div>';
function trim10 (str) {
    var whitespace = ' \n\r\t\f\x0b\xa0\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u2028\u2029\u3000';
    for (var i = 0; i < str.length; i++) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(i);
            break;
        }
    }
    for (i = str.length - 1; i >= 0; i--) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(0, i + 1);
            break;
        }
    }
    return whitespace.indexOf(str.charAt(0)) === -1 ? str : '';
}

function selectingPage(obj, level, model){
    //$(obj).parent().children(".relItem").removeClass('selected');
    //$(obj).addClass('selected');
    var d1 = "#tabs_" + level;
    var d2 = "#tabs_" + (level-1);
    //$(d1 + ">div>div[id=info-"+model+"]").html($(obj).text());
    
    //changeSelection($(obj).attr("id"), $(d1).attr("pid"));
    changeSelection($(d2).attr("pid"),$(obj).attr("id"));
    showInfo($(d1+">ul>li>img"), d1, model);
    
}

function selectPrevious(){
    var obj = $('#' + currentSelectedPage).prev();
    if($(obj).length>0){
        changeSelection(currentSelectedParent, $(obj).attr("id"));
    }
    
}

function selectNext(){
    var obj = $('#' + currentSelectedPage).next();
    if($(obj).length>0){
        changeSelection(currentSelectedParent, $(obj).attr("id"));
    }
}

function changeSelectedPage(pid){
    var obj = $("#" + pid);
    //alert($(obj).length);
    $(obj).parent().children(".relItem").removeClass('selected');
    $(obj).addClass('selected');
    $(obj).parent().parent().children("[id=info-page]").html($(obj).text());
    //setTimeout("scrollElement", 100, obj.parent(), obj);
    scrollElement($(obj).parent(), $(obj));
}

function selectItem(obj, level, model){
    if($(obj).hasClass("selected")) return;
    $(obj).parent().children(".relItem").removeClass('selected');
    
    $(obj).addClass('selected');
    var d1 = "#tabs_" + level;
    $(d1).attr('pid', $(obj).attr("id"));
    $(d1 + ">div>div[id=info-"+model+"]").html($(obj).text());
    var d2 = "#tabs_" + (level+1);
    var l = $(d2).tabs('length');
    for(var i=0;i<l;i++){
        $(d2).tabs("remove", 0);
    }
    //$(d2 + ">div").remove();
    var img = d1 + ">ul>li.ui-tabs-selected>img";
    showList(img, d1, model);
    //getItemRels($(obj).attr("id"), "", level, true);
    
    var target = level-1;
    var p = $(d2).parent();
    //$(d1).remove();
    //
    $(d2).remove();
    //if(!pid) return;
    var url ="itemMenu.jsp?language="+language+"&pid_path="+$(obj).attr("id")+"&path="+model+"&level="+target;
    $('#mainContent').html(imgLoadingBig);
    //$('#mainContent').html('imgLoadingBig');
    //alert(imgLoadingBig);
    $.get(url, function(data){
        $(p).append(data);
        getItemRels($(obj).attr("id"), "", level, true);
        changeSelection(initParent, initPage);
    });
    
}

function getItemRels(pid, selectedpid, level, recursive){
    if(!pid) return;
    var url ="GetRelsExt?language="+language+"&relation=*&format=json&pid=uuid:"+pid;
    var target_level = level + 1;
    $.getJSON(url, function(data){
        var obj = "#tabs_" + target_level;
        //alert(obj);
        $.each(data.items, function(i,item){
            if($(obj).length==0){
                $("#tabs_" + level + ">div").append('<div id="tabs_' + target_level +'" pid="' + pid +'"><ul></ul></div>');
                var t = "#tab"+target_level+"-";
                t="";
                //alert(t);
                $(obj).tabs({ 
                    tabTemplate: '<li><a href="'+t+'#{href}">#{label}</a><img width="12" src="img/empty.gif" class="op_list" onclick="showList(this, \''+obj+'\', \'#{href}\')" /></li>',
                    panelTemplate: '<li></li>'
                });
            }
            var list;
            var str_div = "";
            $.each(item, function(m,model2){
                
                if(model2[0]=="kramerius:hasDonator"){
                    //alert($("#tabs_" + level + ">div>div[id|=info]").length);
                    $("#itemTree").append('<img src="http://194.108.215.227:8080/fedora/get/donator:'+model2[1]+'/LOGO" />');
                    
                }else{
                  list = obj + ">div>div[id=list-"+m+"]";
                  //alert(list + " length: " + $(list).length);
                  if($(list).length==0){
                      //alert(m);
                      //alert($(obj).tabs('option' ,'tabTemplate'));
                      str_div ='<div id="tab'+target_level+'-'+m+'">';
                      str_div +='<div class="relInfo"  id="info-'+m+'">a</div>';
                      str_div +='<div style="display:none;" id="list-'+m+'" class="relList"></div>';
                      str_div +='</div>';
                      $(obj).append(str_div);
                      $(obj).tabs("add", "#tab"+target_level+"-"+m, model2[0]);
                      //$(obj).tabs("add", m, model2[0]);

                      $(obj+">ul>li>img."+m).toggleClass('op_info');
                  }else{

                  }
                }
            });
          
        });  
        $.each(data.items, function(i,item){
            $.each(item, function(m,model2){
                    
                var list = "#tabs_" + (target_level) + ">div>div[id=list-"+m+"]";
                var item;
                var pid2;
                for(var i=1;i<model2.length;i++){
                    pid2 = model2[i]; 
                    item = '<div id="'+pid2+'" class="relItem" title=""' ;
                    if(m=='page'){
                        item+= ' onclick="selectingPage(this, '+target_level+', \''+ m +'\')" ';
                    }else{
                        item+= ' onclick="selectItem(this, '+target_level+', \''+ m +'\')" ';
                    }
                        
                    //item += '><img src="img/item_loading.gif" /></div>';
                    item += '>'+pid2+'</div>';
                        
                    $(list).append(item);
                }
                
                for(var i=1;i<model2.length;i++){
                    pid2 = model2[i]; 
                    getBiblioInfo(pid2, m, list+">div[id="+pid2+"]", false);
                }
                  
            });
        });
            
        if(selectedpid!=""){
            $('#'+selectedpid).addClass('selected');
            //setTimeout("scrollElement('#"+selectedpid+":parent', '#"+selectedpid+"')", 100);
            scrollElement($('#'+selectedpid).parent(), $('#'+selectedpid));
        }else{
            list = obj+">div>div[class=relList]>div:first";
            var info = obj+">div>div[class=relInfo]";
            //var img = $(obj+">ul>li>img");
            $(list).addClass('selected');
            //alert($(list).html());
            $(info).html($(list).html());
            //showList(img, obj, $(obj+">ul>li:first").text());
        }
        if(recursive){
            if($(obj).length>0)
                getItemRels($(obj+">div:first>div[class=relList]>div:first").attr("id"), "", level+1, recursive);
        }
    });
}

function generatePdf(level){
    //show options window
    openGeneratePdfDialog(level);
}

var dialogSummary;
function openGeneratePdfDialog(level){
    var pagesCount = $("#list-page>div.relItem").length;
    $("#genPdfEnd").val(pagesCount);
    $("#genPdfStart").val(1);
    if(dialogSummary){
        dialogSummary.dialog('open');
    }else{
        dialogSummary = $("#pdf_options").dialog({
            bgiframe: true,
            width: 200,
            height: 100,
            modal: true,
            title: generatePdfTitle,
            
            buttons: {
                "Ok": function() {
                   
                    var from = $("#genPdfStart").val();
                    var to = $("#genPdfEnd").val();
                   
                    if(isNaN(from) || isNaN(to)) {
                        alert(generatePdfErrorText);
                        return;
                    }
                    from = parseInt(from);
                    to = parseInt(to); 

                    if(to - from + 1 > generatePdfMaxRange){
                        alert("Maximalne "+generatePdfMaxRange+"!");
                    }else if(to>pagesCount  || isNaN(from) || isNaN(to)) {
                    	alert(generatePdfErrorText);
                    }else if(to==pagesCount && from == '1'){
                        var path ="";
                        for(var i =0;i<level;i++) {
                        	path = path+$("#tabs_"+level).attr('pid');
                        	if (i != level-1) { path = path +"/"; }
                        }
                        var fromUuid = $("#list-page>div.relItem")[$("#genPdfStart").val()-1].attributes['id'].value;
        				var toUuid = $("#list-page>div.relItem")[$("#genPdfEnd").val()-1].attributes['id'].value;
                        var url = "pdf?uuidFrom=" + fromUuid+"&uuidTo="+toUuid+"&path="+path;
                        window.location.href = url;
                        $(this).dialog("close");
                    }else{
                        var path ="";
                        for(var i =0;i<level;i++) {
                        	path = path+$("#tabs_"+level).attr('pid');
                        	if (i != level-1) { path = path +"/"; }
                        }
                        //alert($("#genPdfEnd").val() - $("#genPdfStart").val());
                        var fromUuid = $("#list-page>div.relItem")[$("#genPdfStart").val()-1].attributes['id'].value;
        				var toUuid = $("#list-page>div.relItem")[$("#genPdfEnd").val()-1].attributes['id'].value;
                        var url = "pdf?uuidFrom=" + fromUuid+"&uuidTo="+toUuid+"&path="+path;
                        window.location.href = url;
                        $(this).dialog("close");
                    }

                } ,
                "Cancel": function() {
                    $(this).dialog("close"); 
                } 
            } 
              
        });
    }
}
$( ".selector" ).dialog( { buttons: { "Ok": function() { $(this).dialog("close"); } } } );

function showInfo(obj, tab, model){
    $(obj).toggleClass('op_info');
    $(tab + ">div>div[id=list-"+model+"]").toggle();
}

function showList(obj, tab, model){
    var m = model;
    if(m.indexOf("-")>-1){
        m = m.split("-")[1];
    }
    
    if($(tab + ">div>div[id=info-"+m+"]").text()==""){
        
        $(tab + ">div>div[id=info-"+m+"]").html($(tab+">div>div[id=list-"+m+"]>div.selected").text());
    }
    
    $(tab + ">div>div[id=list-"+m+"]").toggle();
    
    var selected = $(tab+">div>div[id=list-"+m+"]>div.selected");
    scrollElement($(selected).parent(), $(selected));
    $(obj).toggleClass('op_info');
}

function showMainContent(pid, path){
    if(path=="") return;
    $('#mainContent').html(imgLoadingBig);
    //var url = "inc/details/"+path.toString().split('/')[0]+".jsp?display=block&language=";
    var url = "inc/details/biblioToRdf.jsp?pid=uuid:"+pid+"&xsl="+path.toString().split('/')[0]+".jsp&display=block&language=";
    //var url = 'item_1.jsp?pid='+pid+'&path='+path;
    $.get(url, function(data){
        $('#mainContent').html(data);
    });
    
}

function getPageTitle(pid){
    return $("#" + pid).text();
}


    function toggleAdminOptions(div){
        var il = $('#menu-'+div).parent().width() + $('#menu-'+div).parent().offset().left - $('#menu-'+div).width();
        $('#menu-'+div).css('left', il);
        $('#menu-'+div).toggle();
        $('#openmenu-'+div).toggle();
    }