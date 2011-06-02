<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<form name="autocompleteForm" method="GET" action="./" autocomplete="Off">
        <div class="ui-tabs-panel ui-corner-bottom">
            <table width="100%">
                <tr>
                    <td width="48%" valign="top"><fmt:message bundle="${lctx}" key="filter.maintitle" /></td>
                    <td style="border-right:1px solid silver;"></td><td></td><td></td>
                    <td width="48%" valign="top"><fmt:message bundle="${lctx}" key="common.author" /></td>
                </tr>
                <tr>
                    <td valign="top">
                        <div id="title" class="autocomplete"></div>
                    </td>
                    <td align="center" valign="top" id="letters_title" class="letters letters_l">
<%
String letters = "0,A,Á,B,C,Č,D,Ď,E,É,Ě,F,G,H,CH,I,Í,J,K,L,M,N,Ň,O,Ó,P,Q,R,Ř,S,Š,T,Ť,U,Ú,Ů,V,W,X,Y,Ý,Z,Ž";
String[] pismena = letters.split(",");
for(String p:pismena){
    out.print(String.format("<div class=\"%s\"><a href=\"#\">%s</a></div>", p, p));
}
%>
                    </td>
                    <td><div style="border-left:1px solid silver;width:3px;margin-left:2px;height:586px"></div></td>
                    <td align="center" valign="top" id="letters_search_autor" class="letters letters_r">
<%
for(String p:pismena){
    out.print(String.format("<div class=\"%s\"><a href=\"#\">%s</a></div>", p, p));
}
%>
                    </td>
                    <td valign="top">
                    <div id="search_autor" class="autocomplete"></div>
                    </td>
                </tr></table>
        </div>
</form>
                    <div id="test"></div>
<script type="text/javascript">

    function doBrowse(value, field){
        var url = 'terms_1.jsp?field=' + field + '&t=' + value;
        $.get(url, function(data){
            $('#'+field).html(data);
            $('#'+field).scrollTop(0);
        });
    }

    function getMoreTerms(field){
        var term = $('#'+field+">div.term:last>span").html();
        var url = 'terms_1.jsp?i=false&field=' + field + '&t=' + term;
        $('#'+field+" div.more_terms").remove();
        $.get(url, function(data){
            $('#'+field).append(data);
        });
    }

    var titleDivTopBorder;
    var titleDivBottomBorder;
    $(document).ready(function(){
        titleDivTopBorder = $('#title').offset().top;
        titleDivBottomBorder = titleDivTopBorder + $('#title').height() ;
        doBrowse('', 'title');
        doBrowse('', 'search_autor');

        $(".autocomplete").bind('scroll', function(event){
            var id = $(this).attr('id');
            if($('#'+id+">div.more_terms").length>0 && isTermVisible(id)){
                getMoreTerms(id);
            }
            selectLetter(id);
        });

        $('.term').live('click', function(){
            var field = $(this).parent().attr('id');
            var value = $(this).children("span").html();
            if(field=='title'){
                window.location = "r.jsp?title=\"" + value + "\"";
            } else{
                window.location = "r.jsp?fq=" + field + ":\"" + value + "\"";
            }
        });

        $('.letters>div>a').click(function(){
           var field = $(this).parent().parent().attr('id').substring("letters_".length);
           var value = $(this).html();
           doBrowse(value, field);
        });

    });

    function selectLetter(field){
        var letter = '0';
        $('#'+field+'>div.term').each(function(){
            if($(this).position().top>0){
                letter = $(this).children('span').html().substring(0,1);
                return false;
            }
        });
        if(letter<'A') letter = '0';
        $('#letters_'+field+'>div').removeClass('sel');
        $('#letters_'+field+'>div.'+letter).addClass('sel');
    }
    
    function isTermVisible(id){
        var t = $('#'+id+">div.more_terms").offset().top;
        var b = $('#'+id+">div.more_terms").offset().top + $('#'+id+">div.more_terms").height();
        var reserve = 20;
        if(t<titleDivBottomBorder+reserve && b>titleDivTopBorder-reserve){
            return true;
        }
        return false;
    }
</script>