var enableAjax = true; // jestli se ma strankovat s pomoci ajaxu

function showHelp(language, part){
    var url = 'help/help.jsp?';
    if (part!=null && part!='')
     url=url+'#'+part;
     temp=window.open(url,'HELP','width=608,height=574,menubar=0,resizable=0,scrollbars=1,status=0,titlebar=0,toolbar=0,z-lock=0,left=200,top=20');
     temp.opener=this;
     temp.focus(); 
}

function toggleFacet(facet){
    $('#facet_'+facet+' .moreFacets').toggle();
}

function toggleCollapsed(pid, div, offset){
    if($("#"+div).attr('opened')=="true"){
        $("#"+div).toggle();
        //$(img).addClass('uncollapseIcon');
        $('#uimg_' + pid ).toggleClass('uncollapseIcon');
    }else{
        uncollapse(pid, div, offset);
        $('#uimg_' + pid).addClass('uncollapseIcon');
    }
}
function uncollapse(pid, div, offset){
      var page = new PageQuery(window.location.search);
      page.setValue("offset", offset);
      var url =  "uncollapse.jsp?rows=10&" + page.toString() + 
          "&type=uncollapse&d="+div+"&collapsed=false&root_pid=" + pid + "&fq=root_pid:\"" + pid + "\"";
      $.get(url, function(xml) {
          $("#"+div).html(xml);
          $("#"+div).attr('opened', 'true');
          translateDiv(div);
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
    var nav = "fq=" + navigator + ":\"" + value + "\"";
    if(window.location.search.indexOf(nav)==-1){
        window.location = searchPage + "?" + 
        page.toString() + "&" + nav;
    }
    
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

function checkSearching() {
    if ($('#q').val()==dictionary['form.search'] && $('#q searching').length==0) {
        $('#q').val('');
        $('#q').addClass('searching');
        $('#q').focus();
    }
}

function checkQuery(){
    if ($('#q.searching').length==0) {
        $('#q').val('');
    }
    return true;
}

/* odebrani navigace z url
 *
 */
function removeFacet(index){
    $('#fq'+index).remove();
    checkQuery();
    $('#searchForm').submit();
}
function removeSuggest(){
    $('#suggest').remove();
    $('#suggest_q').remove();
    checkQuery();
    $('#searchForm').submit();
}


function removeDateAxisFilter(f1, f2){
    $('#f1').remove();
    $('#f2').remove();
    checkQuery();
    $('#searchForm').submit();
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

