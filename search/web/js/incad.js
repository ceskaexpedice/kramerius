var enableAjax = true; // jestli se ma strankovat s pomoci ajaxu

function showHelp(language, part){
    if(language=='') language = 'cs';
    var url = 'help/'+language+'.html';
    if (part!=null && part!='')
     url=url+'#'+part;
     temp=window.open(url,'HELP','width=608,height=574,menubar=0,resizable=0,scrollbars=1,status=0,titlebar=0,toolbar=0,z-lock=0,left=200,top=20');
     temp.opener=this;
     temp.focus(); 
}

function toggleFacet(facet){
    $('#facet_'+facet+' .moreFacets').toggle();
}


function uncollapse(pid, div, offset){
    var page = new PageQuery(window.location.search);
    page.setValue("offset", offset);
    var url =  "uncollapse.jsp?rows=10&" + page.toString() + 
        "&type=uncollapse&d="+div+"&collapsed=false&root_pid=" + pid + "&fq=root_pid:\"" + pid + "\"";
    $.get(url, function(xml) {
        $("#"+div).html(xml);
    });
}

function gotoItemDetail(id){
    var itemDetailUrl = "itemDetail.jsp";
    
    var page = new PageQuery(window.location.search);
    page.setValue("id", id) ;
    var url = window.location.protocol + itemDetailUrl + "?" + page.toString() ;
    var opts = "width=1000,height=575,toolbar=no, location=no,directories=no,status=no,menubar=no,scrollbars=yes,copyhistory=no,resizable=yes";

    w = window.open(url, "itemDetail", opts);
    w.focus();
    //window.location = url;
}

function showMoreLess(nav){
    $("#less" + nav).toggle();
    $("#more" + nav).toggle();

}

function removeFilters()
{
    var page = new PageQuery(window.location.search);
    page.removeParam("navigation");
    page.removeParam("defautNavigation");
    page.setValue(fromField, "");
    page.setValue(toField, "");
    var newurl = "?" + page.toString();

    document.location.href = newurl;
}

function sortBy( value ){
    var page = new PageQuery(window.location.search);
    page.setValue("sort", value);
    window.location = window.location.protocol + window.location.pathname + "?" + page.toString();
}

function gotoPageOffset2( value ){
    var page = new PageQuery(window.location.search);
    page.setValue("offset", value);
    window.location = searchPage + "?" + page.toString();
}

function gotoPageOffset( value, div, baseUrl ){
    var page = new PageQuery(window.location.search);
    if(div){
        page.setValue("offset", value);
        var url = baseUrl + "?" + page.toString() + "&d="+div+"&base="+baseUrl;
      $.get(url, function(xml){
          $("#"+div).html(xml);
      });
    }else{
      page.setValue("offset", value);
      window.location = searchPage + "?" + page.toString();
    }
}

function addNavigation(navigator, value){
    var page = new PageQuery(window.location.search);
    page.setValue("offset", "0");
    window.location = searchPage + "?" + 
        page.toString() + "&fq=" + navigator + ":\"" + value + "\"";
    
}

function gotoPageOffsetInTree(value,div,fq,pid){
    //$("#"+div).animate({marginLeft:'-500px'},'slow', loadPageOffsetInTree(value,div,fq,pid));
    
    
    var page = new PageQuery(window.location.search);
    page.setValue("offset", value);
     var url = searchInTreePage + "?" + 
        page.toString() + "&d="+div+"&pid="+pid+ fq;
    $.get(url, function(xml){
        $("#"+div).html(xml);
    });
    //$("#"+div).load(url);
    
}

function  loadPageOffsetInTree(value,div,fq,pid){
    var page = new PageQuery(window.location.search);
    page.setValue("offset", value);
    	
    var url = searchInTreePage + "?" + 
        page.toString() + "&d="+div+"&pid="+pid+ fq;
    $("#"+div).load(url, '', showNewContent(div));
}

 function showNewContent(div) {  
        $("#"+div).animate({marginLeft:'0px'},'slow');
    }  

function searchInTree(pid, filter, div){
    
    var page = new PageQuery(window.location.search);
    page.setValue("offset", "0");
    var url = searchInTreePage + "?" + 
        page.toString() + "&d="+div+"&pid=" + pid+"&fq=" + filter;
    $.get(url, function(xml) {
        $("#"+div).html(xml);
    });
}

function checkLastRelsExt(model){
    
}

function browseInTree(pid, model, div){
    
    var url ="./inc/getItemForBrowse.jsp?pid="+pid+"&model="+model;
    $.get(url, function(xml) {
        if(div!=''){
            $("#"+div).html(xml);
        }else{
            $("#"+pid.substring(5)+":parent").append(xml);
        }
        
    });
    
    
}
/* odebrani navigace z url
 *
 */
function removeNavigation2(name, value){
    
    removeNavigation(name + ":\"" + value + "\"" );
    
     var page = new PageQuery(window.location.search);
    
    page.setValue("offset", "0");
    page.removeParam(name);
    var url = searchPage + "?" + page.toString();
    
    window.location = url;
    
}

function removeNavigation(value){
    
    var page = new PageQuery(window.location.search);
    if(value.indexOf("rok:")==0){
      page.removeParam(fromField);
      page.removeParam(toField);
    }
    page.setValue("offset", "0");
    var url = searchPage + "?" + page.toString();
    //var url = window.location.href;
    var modToRemove = "&fq=" + value;
    url = url.replace(new RegExp(modToRemove, "gi"), '');
    modToRemove = encodeURI("&fq=" + value);
    url = url.replace(new RegExp(modToRemove, "gi"), '');
    window.location = url;
}

function removeQuery(){
    
    var page = new PageQuery(window.location.search);
    
    page.setValue("offset", "0");
    page.setValue("q", "");
    var url = searchPage + "?" + page.toString();
    window.location = url;
}

function setLanguage(language){
    var page = new PageQuery(window.location.search);
    page.setValue("language", language);
    window.location = window.location.protocol + window.location.pathname + "?" + 
        page.toString();
    
}
function toggleAdv(){
    var y = $('#q').offset().top + $('#q').height() + 10;
    var x = $('#q').offset().left;
    $('#advSearch').css('left', x);
    $('#advSearch').css('top', y);
    $('#advSearch').toggle();
}
function openPage(pid, model, pageLabel){
    var itemDetailUrl = "item.jsp";
    var page = new PageQuery(window.location.search);
    var parentPid = page.getValue("pid");
    window.location = window.location.protocol + itemDetailUrl + "?pid=" + pid +
        "&model=" + model + "&page=" + pageLabel + "&parentPid=" + parentPid;
    
}

function openUnit(pid, obj){
    var itemDetailUrl = "item.jsp";
    var page = new PageQuery(window.location.search);
    var parentPid = page.getValue("pid");
    window.location = window.location.protocol + itemDetailUrl + "?pid=" + pid +
        "&model=info:fedora/model:monographunit&parentPid=" + parentPid;
}

function openVolume(pid, title){
    var itemDetailUrl = "item.jsp";
    var page = new PageQuery(window.location.search);
    var parentPid = page.getValue("pid");
    window.location = window.location.protocol + itemDetailUrl + "?pid=" + pid +
        "&model=info:fedora/model:periodicalvolume&title=" + title + "&parentPid=" + parentPid;
    
}

function openItem(pid, title){
    var itemDetailUrl = "item.jsp";
    var page = new PageQuery(window.location.search);
    var parentPid = page.getValue("pid");
    window.location = window.location.protocol + itemDetailUrl + "?pid=" + pid +
        "&model=info:fedora/model:periodicalitem&title=" + title + "&parentPid=" + parentPid;
    
}

function getVolumeList(pid){
    var url = 'GetRelsExt?relation=hasVolume&pid=' + pid;
    
    $("#volumes").html(readingVolumes + "...");
    $.post(url, {type: 'serial'}, function(xml) {
        if(xml==""){
            $("#volumes").html(xml);
        }else{
            var units = xml.split("#");
            var out = volumesTitle + ": ";
            for(var i = 0; i<units.length-1; i++){
                out += "<div id=\"volume_" + i + "\" ></div> ";
            }
            $("#volumes").html(out);
            for(var j = 0; j<units.length-1; j++){
                getVolumeInfo(units[j].split(" ")[1], j);
            }
        }
    });
}

function getVolumeInfo(pid, index){
    var title = $('#periodicaltitle').html();
    var url = 'details/biblioToRdf.jsp?pid=' + pid + "&xsl=volume_from_biblio_mods.jsp&title=" + title + "&language=" + language;
    //$("#div_"+index).html("Načítám stranky...");
    $.post(url, function(xml) {
        var info = xml.split("@");
        var index2 = info[1];
        $("#volume_"+index).html(info[2])
    });
}

function getInternalPartList(pid){
    var url = 'GetRelsExt?relation=hasIntCompPart&pid=' + pid;
    
    $("#internalParts").html(readingIntarnalParts + "...");
    $.post(url, function(xml) {
        if(xml==""){
            $("#internalParts").html(xml);
        }else{
            var units = xml.split("#");
            var out = internalPartTitle + ": ";
            for(var i = 0; i<units.length-1; i++){
                out += "<div id=\"internalPart_" + i + "\" ></div> ";
            }
            $("#internalParts").html(out);
            for(var j = 0; j<units.length-1; j++){
                getInternalPartInfo(units[j].split(" ")[1], j);
            }
        }
    });
}

function getInternalPartInfo(pid, index){
    var title = $('#monographtitle').html();
    var url = 'details/biblioToRdf.jsp?pid=' + pid + "&xsl=internalPart_from_biblio_mods.jsp&title=" + title + "&language=" + language;
    //$("#div_"+index).html("Načítám stranky...");
    $.post(url, function(xml) {
        $("#internalPart_"+index).html(xml)
    });
}

function getIssuesList(pid){
    var url = 'GetRelsExt?relation=hasItem&pid=' + pid;
    
    $("#issues").html(readingIssues + "...");
    $.post(url, function(xml) {
        if(xml==""){
            $("#issues").html(xml);
        }else{
            var units = xml.split("#");
            var out = issuesTitle + ": ";
            for(var i = 0; i<units.length-1; i++){
                out += "<div id=\"issue_" + i + "\" >" + i + "</div> ";
            }
            $("#issues").html(out);
            for(var j = 0; j<units.length-1; j++){
                getIssueInfo(units[j].split(" ")[1], j);
            }
        }
    });
}

function getIssueInfo(pid, index){
    var title = $('#periodicaltitle').html();
    var url = 'details/biblioToRdf.jsp?pid=' + pid + "&xsl=issue_from_biblio_mods.jsp&title=" + title + "&language=" + language;
    //$("#div_"+index).html("Načítám stranky...");
    $.post(url, function(xml) {
        var info = xml.split("@");
        var index2 = info[1];
        $("#issue_"+index).html(info[2])
    });
}

function getPagesList(pid){
    var url = 'GetRelsExt?relation=hasPage&pid=' + pid;
    $("#pages").html(readingPages + "...");
    $.post(url, function(xml) {
        if(xml==""){
            $("#pages").html(xml);
        }else{
            var units = xml.split("#");
            var out = pagesTitle + ": ";
            for(var i = 0; i<units.length-1; i++){
                out += "<span id=\"page_" + i + "\" class=\"page\"></span> ";
            }
            out += "<div id=\"pageViewer\" ></div><div id=\"pageViewerFoot\" style=\"position:relative;top:0;left:0;\"></div> ";
            $("#pages").html(out);
            for(var j = 0; j<units.length-1; j++){
                getPageInfo(units[j].split(" ")[1], j);
            }
        }
    });
}

function getPageInfo(pid, index){
    var url = 'details/biblioToRdf.jsp?&pid=' + pid + "&xsl=page_from_biblio_mods.jsp&language=" + language;
    $.post(url, function(xml) {
        var info = xml.split("@");
        var index2 = info[1];
        $("#page_"+index2).html(info[2])
    });
}

function showPage(pid, page){
    var url = 'details/page.jsp?pid=' + pid + "&language=" + language + "&page=" + page;
    $("#pageViewer").height(600);
    $.post(url, {}, function(xml) {
        $("#pageViewer").html(xml)
        $.scrollToElement( $('#pageViewerFoot'));
    });
}

function getUnitsList(pid){
    var url = 'GetRelsExt?relation=hasUnit&pid=' + pid;
    $("#units").html(readingUnits + "...");
    $.post(url, {type: 'serial'}, function(xml) {
        if(xml==""){
            $("#units").html(xml);
        }else{
            var units = xml.split("#");
            var out = unitsTitle + ": ";
            for(var i = 0; i<units.length-1; i++){
                out += "<div id=\"unit_" + i + "\" class=\"unit\"></div>";
            }
            $("#units").html(out);
            for(var j = 0; j<units.length-1; j++){
                getUnitInfo(units[j].split(" ")[1], j);
            }
        }
    });
}

function getUnitInfo(pid, index){
    var url = 'inc/details/biblioToRdf.jsp?&pid=' + pid + "&xsl=monographunit_from_biblio_mods.jsp&language=" + language;
    $.post(url, {}, function(xml) {
        $("#unit_"+index).html(xml)
    });
}

function getPageInfo2(pagePid){
    var url = 'GetPageInfo?pid=' + pagePid ;
    $("#div_"+pagePid).html(readingPages + "...");
    $.post(url, {type: 'serial'}, function(xml) {
        $("#unit_"+pagePid).html(xml);
    });
}

function reIndexWithCount(pid, div, full, count){
    var indexUrl = "admin/indexByPid.jsp?pid=" + pid +
        "&pagesCount=" + count;
    /*
    $.get(indexUrl, function(xml) {
        if(xml!=""){
          $("#reindex_"+div).html(xml);
        }
    });
     */
    var s = $("#reindex_"+div).html();
    $.ajax({
        //url: 'GetRelsExt?relation=*&pid=' + pid + "&model='" + m,
        url: indexUrl,
        async: false,
        success: function(xml){
            if(!xml==""){
                s += "<br/>" + pid + ": " + xml;
            }
        }
    })
    $("#reindex_"+div).html(s);
    
    if(full){
        var url = 'GetRelsExt?relation=*&pid=' + pid;
        $.get(url, function(xml) {
            if(!xml==""){
                var rels = xml.split("#");
                for(var j = 0; j<rels.length-1; j++){
                    reIndexWithCount(rels[j].split(" ")[1], div, '');
                }
            }
        });
    }
}

function index(model){
    $("#indexing").html("indexing " + model + " ...");
    var url = 'IndexModel?model=' + model;
    $.post(url, {}, function(xml) {
        if(xml!=""){
            $("#indexing").html(xml);
        }
    });
}
  
function reIndex(pid, div, full){
    var count = $("#pages_"+div).html();
    reIndexWithCount(pid, div, full, count);
    
}
  
function getTotalPages(pid, div){
    var count = $("#pages_"+div).html();
    if(count==''){
        var url = 'GetTotalPages?pid=' + pid;
        $.post(url, {}, function(xml) {
            if(xml!=""){
                $("#pages_"+div).html(xml);
            }
        });
    }else{
        //alert(count);
    }
   
    /*
    $.ajax({
        //url: 'GetRelsExt?relation=*&pid=' + pid + "&model='" + m,
        url: 'GetTotalPages?pid=' + pid,
        async: false,
        success: function(xml){
            if(!xml==""){
                var rels = xml.split("#");
                for(var j = 0; j<rels.length-1; j++){
                    var model = rels[j].split(" ")[0];
                    //alert(model);
                    if(model == "kramerius:hasPage"){
                        nump = nump + 1;
                    }else{
                        nump = nump + getTotalPages(rels[j].split(" ")[1], model);
                    }
                }
                nump = xml;
            }
        }

    })
     */
}


function getTotalPagesInResults(){
    
    
    $('.pages').each(function(){
        var uuid = this.id.substring(6);
        getTotalPages('uuid:' + uuid, uuid);
    });
}

/*
 * ScrollToElement 1.0
 * Copyright (c) 2009 Lauri Huovila, Neovica Oy
 *  lauri.huovila@neovica.fi
 *  http://www.neovica.fi
 *  
 * Dual licensed under the MIT and GPL licenses.
 */

;(function($) {
    $.scrollToElement = function( $element, speed ) {

        speed = speed || 750;

        $("html, body").animate({
            scrollTop: $element.offset().top,
            scrollLeft: $element.offset().left
        }, speed);
        return $element;
    };

    $.fn.scrollTo = function( speed ) {
        speed = speed || "normal";
        return $.scrollToElement( this, speed );
    };
})(jQuery);

