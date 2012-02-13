<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.Kramerius.views.WellcomeViewObject"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>


<style type="text/css">
    .suggest{
        position: absolute;
        top:20px;
        z-index:3;
        height:200px;
        width:400px;
        overflow: auto;
        padding:5px;
        display:none;
    }
    .suggest>div.content{
        clear:right;
    }
</style>
<div id="intro" >
    <ul>
    <c:forEach varStatus="status" var="tab" items="${cols.homeTabs}">
        <li><a href="#intro${status.count}"><fmt:message bundle="${lctx}">home.tab.${tab}</fmt:message></a></li>
    </c:forEach>
    </ul>

    <c:forEach varStatus="status" var="tab" items="${cols.homeTabs}">
        <div id="intro${status.count}" style="height: 510px; overflow:auto;"></div>
        <script type="text/javascript">
            $.get('inc/home/${tab}.jsp', function(data){
               $('#intro${status.count}').html(data) ;
            });
        </script>
    </c:forEach>
</div>
<script type="text/javascript" language="javascript">

    var letters = "0,A,Á,B,C,Č,D,Ď,E,É,Ě,F,G,H,CH,I,Í,J,K,L,M,N,Ň,O,Ó,P,Q,R,Ř,S,Š,T,Ť,U,Ú,Ů,V,W,X,Y,Ý,Z,Ž";
    
    function hideSuggest(obj){
        $(obj).hide();
    }
    function doSuggest(input){
        var input_id = $(input).attr("id");
        var value = $("#"+input_id).val();
        var res_id = input_id + "_res";
        var field_id = input_id.substring(3);
        doBrowse(value, field_id);
        return;
        var url = 'terms.jsp?i=false&field=' + field_id + '&t=' + value;
        $.get(url, function(data){
            $('#'+res_id+">div.content").html(data);
            if(data!=""){
                $('#'+res_id).show();
            }else{
                $('#'+res_id).hide();
            }
        });
    }
    
    function escapeValue(value){
        return '#' + value.replace(/(\"|\.)/g,'\\$1');
    }
    
    function checkScroll(id, id2){
        if($('#'+id+">div.more_terms").length>0 && isTermVisible(id)){
           getMoreTerms(id);
        }
        selectLetter(id);
    }
    
    function doBrowse(value, field){
        var url = 'terms.jsp?field=' + field + '&t=' + value;
        $.get(url, function(data){
            $('#'+field).html(data);
            $('#'+field).scrollTop(0);
            selectLetter(field);
            
            
            //$('#'+field).bind('scroll', function(event){
            //    var id = $(this).attr('id');
            //    if($('#'+id+">div.more_terms").length>0 && isTermVisible(id)){
            //        getMoreTerms(id);
            //    }
            //    selectLetter(id);
            //});
        
        });
    }

    function getMoreTerms(field){
        var term = $('#'+field+">div.term:last>span").html();
        var url = 'terms.jsp?i=false&field=' + field + '&t=' + term;
        $('#'+field+" div.more_terms").remove();
        $.get(url, function(data){
            $('#'+field).append(data);
        });
    }

    var titleDivTopBorder;
    var titleDivBottomBorder;
    $(document).ready(function(){
        $('.term').live('click', function(){
            var field = $(this).parent().attr('id');
            var value = $(this).children("span").html();
            if(field.indexOf('browse_title')>-1){
                window.location = "r.jsp?suggest=true&browse_title=" + value + "&forProfile=search";
            } else{
                window.location = "r.jsp?author=\"" + value + "\"&forProfile=search";
            }
        });

        $('.letters>div>a').live('click', function(){
           var field = $(this).parent().parent().attr('id').substring("letters_".length);
           var value = $(this).html();
           
           doBrowse(value, field);
           
        });
        
        $('#intro').tabs();
        $('#homedabox').tabs();

    });

    function selectLetter(field){
        var letter = '0';
        $('#'+field+'>div.term').each(function(){
            if($(this).position().top>0){
                letter = $(this).children('span').html().substring(0,1).toUpperCase();
                return false;
            }
        });
        if(letters.indexOf(letter)<0){
            letter = '0';
        }  
        $('#letters_'+field+'>div').removeClass('sel');
        $('#letters_'+field+'>div.'+letter).addClass('sel');
    }
    
    function setBrowseScrollPosition(){
        if(titleDivTopBorder==null){
            titleDivTopBorder = $('#browse_title').offset().top;
            titleDivBottomBorder = titleDivTopBorder + $('#browse_title').height() ;
        }
    }
    
    function isTermVisible(id){
        var t = $('#'+id+">div.more_terms").position().top;
        var b = $('#'+id+">div.more_terms").position().top + $('#'+id+">div.more_terms").height();
        var reserve = 40;
        setBrowseScrollPosition();
        if(t<titleDivBottomBorder+reserve && b>titleDivTopBorder-reserve){
            return true;
        }
        return false;
    }

</script>