

var _persistentURLDialog;
function showPersistentURL(level, model) {
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');
	//if (level != 1) {
	//	pid = $("#tabs_"+level+">div."+model+">div.relList>div.selected").attr('pid');
	//}
	var currentURL = window.location.href;
	if (currentURL.match("^https")=='https') {
		currentURL = currentURL.substr('https://'.length, currentURL.length); 
		var urlparts = currentURL.split('/');
		currentURL="https://"+urlparts[0]+"/"+urlparts[1]+"/";
	} else {
		currentURL = currentURL.substr('http://'.length, currentURL.length); 
		var urlparts = currentURL.split('/');
		currentURL="http://"+urlparts[0]+"/"+urlparts[1]+"/";
	}

	currentURL=currentURL+"handle/uuid:"+pid;
	var textFieldID = 'persistentURLTextField';
	
	if (_persistentURLDialog) {
		_persistentURLDialog.dialog('open');
	} else {
		$(document.body).append('<div id="persistentURL">'+
				'<span>'+dictionary['administrator.dialogs.persistenturl.text']+'</span>'+
				'<input name="'+textFieldID+'"  style="width:100%;" type="text"  maxlength="255"'+ 
				' id="'+textFieldID+'" title="'+dictionary['administrator.menu.persistenturl']+'" /></div>');
				
		_persistentURLDialog = $('#persistentURL').dialog({
	           width:640,
	           height:100,
	           modal:true,
	           title:dictionary["administrator.menu.dialogs.persistenturl.title"],
	           buttons: {
					"Close": function() {
						$(this).dialog("close"); 
					} 
				} 
	       });
	}
	
	//var textField = $('#'+textFieldID);
	$('#'+textFieldID).val(currentURL);	
	$('#'+textFieldID).select();
	$('#'+textFieldID).focus(function() {
		$(this).select();
	});
}


function getBiblioInfo(pid, model, list, inf, setInf){
    var url = 'inc/details/biblioToRdf.jsp?pid=uuid:' + pid + "&xsl=default.jsp&model="+model;
    //var url = 'inc/details/biblioToRdf.jsp?pid=uuid:' + pid + "&xsl="+model+".jsp";
    //var url = 'inc/results/biblioToRdf.jsp?pid=uuid:' + pid + "&xsl="+model+".jsp" ;
    $.get(url, function(xml) {
        $(".relItem[pid='" + pid + "']").html(xml);
        $(".relItem[pid='" + pid + "']").attr('hasbiblio', 'true');
        if(setInf){
            $(inf).html(xml);
        }
    });
}

function scrollElement(container, element){
//    $(container).animate({
//        scrollTop: $(element).offset().top - $(container).offset().top + $(container).scrollTop(),
//        scrollLeft: $(element).offset().left
//    }, 750);
    $(container).scrollTop($(element).offset().top - $(container).offset().top + $(container).scrollTop());
    $(container).scrollLeft($(element).offset().left);
        
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
    var d1 = "#tabs_" + level;
    var d2 = "#tabs_" + (level-1);
    changeSelection($(d2).attr("pid"),$(obj).attr("pid"));
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
    $(".relItem[pid='" + pid + "']").each(function(i, obj){
       
      $(obj).parent().children(".relItem").removeClass('selected');
      $(obj).addClass('selected');
      var infoObj = $(obj).parent().parent().children("[id=info-page]");
      if($(obj).attr('hasbiblio')=='true'){
          infoObj.html($(obj).text());
      }
      scrollElement($(obj).parent(), $(obj));
    });
}

function selectItem(obj, level, model){
    if($(obj).hasClass("selected")) return;
    $(obj).parent().children(".relItem").removeClass('selected');
    
    clearThumbs();
    $(obj).addClass('selected');
    var d1 = "#tabs_" + level;
    var pid = $(obj).attr("pid");
    $(d1).attr('pid', pid);
    $(d1 + ">div>div[id=info-"+model+"]").html($(obj).text());
    var d2 = "#tabs_" + (level+1);
    var l = $(d2).tabs('length');
    for(var i=0;i<l;i++){
        $(d2).tabs("remove", 0);
    }
    var img = d1 + ">ul>li.ui-tabs-selected>img";
    showList(img, d1, model);
    
    var target = level-1;
    var p = $(d2).parent();
    $(d2).remove();
    var url ="itemMenu.jsp?pid_path="+pid+"&path="+model+"&level="+target;
    //$('#mainContent').html(imgLoadingBig);
    $('#imgBig').attr("src", "img/empty.gif");
    $.get(url, function(data){
        $(p).append(data);
        //getItemRels(pid, "", level, true);
    });
}

function getItemRels(pid, selectedpid, level, recursive, rootModel){
    if(!pid) return;
    var url ="GetRelsExt?relation=*&format=json&pid=uuid:"+pid;
    var target_level = level + 1;
    var newTab = false;
    $.getJSON(url, function(data){
        var obj = "#tabs_" + target_level;
        $.each(data.items, function(i,item){
            if($(obj).length==0){
                $("#tabs_" + level + ">div."+rootModel).append('<div id="tabs_' + target_level +'" pid="' + pid +'"><ul></ul></div>');
                var t = "#tab"+target_level+"-";
                t="";
                $(obj).tabs({ 
                    tabTemplate: '<li><a href="'+t+'#{href}">#{label}</a><img width="12" src="img/empty.gif" class="op_list" onclick="showList(this, \''+obj+'\', \'#{href}\')" /></li>',
                    panelTemplate: '<li></li>',
                    show: function(event, ui){
                        updateThumbs();
                    },
                    select: function(event, ui){
                        changingTab=true;
                    }
                });
                newTab = true;
            }
            var list;
            var str_div = "";
            $.each(item, function(m,model2){
                
                if(model2[0].indexOf("hasDonator")>-1){
                    var donatortext = 'proxy?pid=donator:'+model2[1]+'&dsname=TEXT-';
                    if(language==""){
                        donatortext += "CS";
                    }else{
                        donatortext += language.toUpperCase();
                    }
                    $.get(donatortext, function(data){
                        $("#donatorContainer").html('<div class="donator"><img height="50" src="proxy?pid=donator:'+model2[1]+'&dsname=LOGO" alt="'+data+'" title="'+data+'" /></div>');
                    });
                    
                }else{
                  list = obj + ">div>div[id=list-"+m+"]";
                  if($(list).length==0){
                      str_div ='<div id="tab'+target_level+'-'+m+'" class="'+m+' ui-tabs-panel ui-widget-content ui-corner-bottom">';
                      str_div +='<div style="display:none;" id="list-'+m+'" class="relList"></div>';
                      str_div +='<div class="relInfo"  id="info-'+m+'">a</div>';
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
        var hasPages = false;
        $.each(data.items, function(j,item){
            $.each(item, function(m,model2){
                    
                var list = "#tabs_" + (target_level) + ">div>div[id=list-"+m+"]";
                var inf = "#tabs_" + (target_level) + ">div>div[id=info-"+m+"]";
                var item;
                var pid2;
                for(var i=1;i<model2.length;i++){
                    pid2 = model2[i]; 
                    var itemClass = 'relItem '+m;
                    if(pid2==$("#tabs_" + target_level).attr('pid')){
                        itemClass += ' selected';
                    }
                    item = '<div pid="'+pid2+'" id="'+pid2+'" title="" hasbiblio="false" class="'+itemClass+'"' ;
                    if(m=='page'){
                        item+= ' onclick="selectingPage(this, '+target_level+', \''+ m +'\')" ';
                    }else{
                        item+= ' onclick="selectItem(this, '+target_level+', \''+ m +'\')" ';
                    }
                        
                    //item += '><img src="img/item_loading.gif" /></div>';
                    item += '>'+pid2+'</div>';
                        
                    $(list).append(item);
                    if(m=="page"){
                        hasPages = true;
                        var display = 'inline';
                        if(newTab){
                            display = 'none';
                        } 
                        addThumb(pid2, display, target_level);
                    } 
                }
                if(m=="page"){
                  totalThumbs = model2.length - 1;
                  if(totalThumbs==0){
                      $('#tv').hide();
                  }else{
                      $('#tv').show();
                  }
                }
                
                for(var i=1;i<model2.length;i++){
                    pid2 = model2[i]; 
                    getBiblioInfo(pid2, m, list+'>div[pid='+pid2+']', inf, recursive&&i==1);
                }
                  
            });
        });
        
          
        if(selectedpid!=""){
            $('#'+selectedpid).addClass('selected');
            //setTimeout("scrollElement('#"+selectedpid+":parent', '#"+selectedpid+"')", 100);
            scrollElement($('#'+selectedpid).parent(), $('#'+selectedpid));
        }else{
            $(obj+">div").each(function(index, o){
              list = "#"+o.id+">div.relList>div:first";
              var info = "#"+o.id+">div.relInfo";
              $(list).addClass('selected');
              $(info).html($(list).html());
            });
        }
        if(recursive){
            if($(obj).length>0){
                $(obj+">div").each(function(index, o){
                    var currModel = o.id.substring(o.id.indexOf('-')+1);
                    
                    getItemRels($("#"+o.id+">div.relList>div:first").attr("pid"), "", level+1, recursive, currModel);
                });
                
            }
        }
    });
}

//~~~~~~~  common objekt
var COMMON = function() {
	return {
		pidpath:function(level) {
			var path ="";
	        for(var i =1;i<=level;i++) {
	        	path = path+$("#tabs_"+i).attr('pid');
	        	if (i < level) {
		        	path = path +"/";
	        	}
	        }
	        return path;
		}
	}
}();


//~~~~~~~  PDF objekt
var PDF=function() {

	return {
		privateLevel: 1,

		dialogSummary: null ,
		// sestaveni parametru path
		path:function(level) {
			var path ="";
	        for(var i =1;i<=level;i++) {
	        	path = path+$("#tabs_"+i).attr('pid');
	        	path = path +"/";
	        }
	        return path;
		},
		// sestaveni url
		url:function(level) {
			hideAdminOptions(level);
			var uuid = $("#tabs_"+level).attr('pid');
			var u = "pdf?uuidFrom=" + uuid+"&uuidTo="+uuid+"&path="+PDF.path(level);
			window.location.href = u;
		},
		// sestaveni url dle parametru z dialogu
		urlFromDialog:function(level) {
			var pageLevel = -1;
			$.each($('div[id$=page]'), function(index, value) {
				var idVal = $(value).attr('id');
				if (idVal.indexOf('tab')==0) {
					var stringLevel =  idVal.substring('tab'.length, idVal.length - '-page'.length)
					var l = parseInt(stringLevel, 10);
					if ((pageLevel == -1) || (l<pageLevel)) {
						pageLevel = l;
					}
				}
			});

			var from = $("#genPdfStart").val()-1;
			var to = $("#genPdfEnd").val()-1;
			var elmFrom = $('#tv_container_row td:eq('+from+') img'); 
			var elmTo = $('#tv_container_row td:eq('+to+') img'); 
			
			var fromUuid = $($("#list-page>div.relItem")[$("#genPdfStart").val()-1]).attr('id');
            var fromUuid = $($("#list-page>div.relItem")[$("#genPdfStart").val()-1]).attr('id');
            var toUuid = $("#list-page>div.relItem")[$("#genPdfEnd").val()-1].attributes['pid'].value;
            var toUuid = $("#list-page>div.relItem")[$("#genPdfEnd").val()-1].attributes['pid'].value;

            
            var u = "pdf?uuidFrom=" + fromUuid+"&uuidTo="+toUuid+"&path="+PDF.path(pageLevel-1);
            window.location.href = u;
		},
		// generovani pdf
		generatePDF:function(level) {
			hideAdminOptions(level);
		    PDF.openGeneratePdfDialog(level);
		},
		
		changeRange:function(odkud,kam) {
			$('#tv_container_row td').each(function(i, o){
				var index = i+1;
				if ((index >= odkud ) && (index<=kam)) {
					if (!$(o).hasClass('tv_img_multiselect')) {
						$(o).addClass('tv_img_multiselect');
					}
				} else {
					if ($(o).hasClass('tv_img_multiselect')) {
						$(o).removeClass('tv_img_multiselect');
					}
				}
			});
		},
		
		findRange:function() {
			var fromIndex = -1;
			var toIndex = -1;
			var pagesCount = $("#list-page>div.relItem").length;
			$('#tv_container_row img.tv_image').each(function(i, o){
				if ($(o).hasClass('tv_img_selected')) {
					fromIndex = i+1;
					toIndex = fromIndex + generatePdfMaxRange-1;
					if (toIndex > pagesCount) {
						toIndex = pagesCount;
					}
				}
				if ((fromIndex > 0) && (i < toIndex)) {
			        $(o).parent().toggleClass('tv_img_multiselect');
				}
				if ((i>= toIndex) && (toIndex != -1 )) return false;
			});
			return {
				fromIndex: fromIndex,
				toIndex: toIndex
			}
		},

		
		// dialog pro vyber stranek 
		openGeneratePdfDialog:function (level){
			PDF.privateLevel = level;
			var range = PDF.findRange();
			var pagesCount = $("#list-page>div.relItem").length;
		    
			$("#genPdfEnd").val(range.toIndex);
		    $("#genPdfStart").val(range.fromIndex);

		    if(PDF.dialogSummary){
		        PDF.dialogSummary.dialog('open');
		    }else{
		    	
		        $.get("i18n?action=text&name=first_page_html", function(xml) {
		            var head = $(xml).find('head').text();
		            var body = $(xml).find('desc').text();
		            $("#pdf_desc_head").html(head);
		            $("#pdf_desc_content").html(body);
		        });
		    	
		        
		    	$('#genPdfStart').change(function() {
				    var start = $("#genPdfStart").val();
				    var stop = $("#genPdfEnd").val();
				    PDF.changeRange(parseInt(start), parseInt(stop));
	    		});
		    	$('#genPdfEnd').change(function() {
				    var start = $("#genPdfStart").val();
				    var stop = $("#genPdfEnd").val();
				    PDF.changeRange(parseInt(start), parseInt(stop));
	    		});
		    	
		        PDF.dialogSummary = $("#pdf_options").dialog({
		            bigframe: true,
		            width: 400,
		            height: 300,
		            modal: true,
		            title: dictionary['generatePdfTitle'],
		            
		            buttons: {
		                "Ok": function() {
		        	
		                    var from = $("#genPdfStart").val();
		                    var to = $("#genPdfEnd").val();
		                   
		                    if(isNaN(from) || isNaN(to)) {
		                        alert(dictionary['generatePdfErrorText']);
		                        return;
		                    }
		                    from = parseInt(from);
		                    to = parseInt(to); 
		                    if (to > (pagesCount)) {
		                    	to = pagesCount;
		                    	$("#genPdfEnd").val(to);
		                    }
		                    
		                    if(to - from + 1 > generatePdfMaxRange){
		                        alert("Maximalne "+generatePdfMaxRange+"!");
		                    }else if(to>pagesCount  || isNaN(from) || isNaN(to)) {
		                    	alert(dictionary['generatePdfErrorText']);
		                    }else if(to==pagesCount && from == '1'){
		                    	//PDF.urlFromDialog(level);
		                    	PDF.urlFromDialog(PDF.privateLevel);
		                    	
		                    	$(this).dialog("close");
		                    }else{
		                    	PDF.urlFromDialog(PDF.privateLevel);
		                        $(this).dialog("close");
		                    }

		                    // zruseni selekce
		                    $('#tv_container_row td').each(function(i, o){
		        				if ($(o).hasClass('tv_img_multiselect')) {
		        			        $(o).toggleClass('tv_img_multiselect');
		        				}
		        			});
		                    
		                } ,
		                "Cancel": function() {
		                	// zruseni selekce
		        			$('#tv_container_row td').each(function(i, o){
		        				if ($(o).hasClass('tv_img_multiselect')) {
		        			        $(o).toggleClass('tv_img_multiselect');
		        				}
		        			});
		                	$(this).dialog("close"); 
		                } 
		            } 
		        });
		    }
		}
	}
}();
// ~~~~~~~  Konec PDF objektu



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
    var h = $(window).height() - $(tab).offset().top - $(tab).height();
    $(tab + ">div>div[id=list-"+m+"]").css('max-height', h);
    $(tab + ">div>div[id=list-"+m+"]").css('_height', 'expression(this.scrollHeight > '+h+'? "'+h+'px" : "auto" )');
    var w = $(tab + ">div>div[id=list-"+m+"]").parent().width() - 4;
    $(tab + ">div>div[id=list-"+m+"]").css('width', w);
    $(tab + ">div>div[id=list-"+m+"]").toggle();
    
    var selected = $(tab+">div>div[id=list-"+m+"]>div.selected");
    scrollElement($(selected).parent(), $(selected));
    $(obj).toggleClass('op_info');
}

function showMets(level, model) {
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');
	window.open("mets?pid=uuid:"+pid, "_blank");
}

var _metadataDialog;
function showMainContent(level, model){
   var pid = $("#tabs_"+level).attr('pid');
   
   var titul = $("#tabs_"+level+">div>div[id=info-"+model+"]>div>ul").attr('title');
   var page = new PageQuery(window.location.search);
   var path = page.getValue("path");
   if(path=="") return;
   if(_metadataDialog){
       _metadataDialog.dialog('open');
   }else{
       $(document.body).append('<div id="metaDataDialog"><div id="metaData"></div></div>')
       _metadataDialog = $('#metaDataDialog').dialog({
           width:640,
           height:480,
           modal:true,
           title:titul,
           buttons: {
              "Close": function() {
                $(this).dialog("close"); 
              } 
            } 
       });
   }
   
    $('#metaData').html(imgLoadingBig);
    //var url = "inc/details/"+path.toString().split('/')[0]+".jsp?display=block";
    //var url = "inc/details/biblioToRdf.jsp?pid=uuid:"+pid+"&xsl="+path.toString().split('/')[0]+".jsp&display=full";
    var url = "inc/details/biblioToRdf.jsp?pid=uuid:"+pid+"&xsl=default.jsp&display=full&model="+model;
    //var url = 'item_1.jsp?pid='+pid+'&path='+path;
    $.get(url, function(data){
        $('#metaData').html(data);
    });
    hideAdminOptions(level);
}

function getPageTitle(pid){
    return $("#" + pid).text();
}

function toggleAdminOptions(div){
    //var divs = "#tabs_" + level + ">div>div.menuOptions";
    var il = $('#menu-'+div).parent().width() + $('#menu-'+div).parent().offset().left - $('#menu-'+div).width();
    $('#menu-'+div).css('left', il);
    $('#menu-'+div).toggle();
    $('#openmenu-'+div).toggle();
}
function hideAdminOptions(level){
    //var divs = "#tabs_" + level + ">div>div.menuOptions";
    //var il = $('#menu-'+div).parent().width() + $('#menu-'+div).parent().offset().left - $('#menu-'+div).width();
    //$('#menu-'+div).css('left', il);
    $("#tabs_" + level + ">div>div.menuOptions").toggle();
    //$('#openmenu-'+div).toggle();
}
    
    
    
    
