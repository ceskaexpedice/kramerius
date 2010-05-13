<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.io.*, cz.incad.kramerius.TextsService"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<script type="text/javascript" src="js/ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="js/ckeditor/adapters/jquery.js"></script>

<script type="text/javascript">
    //<![CDATA[

    
    var _lang = <c:choose>
       <c:when test="${param.language == 'en'}">'en'</c:when>
       <c:when test="${param.language == 'cs'}">'cs'</c:when>
       <c:otherwise>'cs'</c:otherwise>
   </c:choose>;
    
    CKEDITOR.plugins.registered['save']=
        {
        init : function( editor )
        {
            var command = editor.addCommand( 'save',
            {
                modes : { wysiwyg:1, source:1 },
                exec : function( editor ) {
                    saveIntro();
                }
            }
        );
            editor.ui.addButton( 'Save',{label : 'save',command : 'save'});
        }
    }

    $(function()
    {
        var config = {
            language: _lang,
            resize_enabled: false,
            toolbar:
                [
                ['Save'], 
                ['Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-', 'Link', 'Unlink'],
                ['UIColor']
            ]
        };

        // Initialize the editor.
        // Callback function can be passed and executed after full instance creation.
        $('.jquery_ckeditor').ckeditor(config);
        
        
    });
        
    function saveIntro(){
        $.post('inc/text/saveIntroText.jsp', {text: $('.jquery_ckeditor').val(), language: _lang}, function(data){
            alert(data); 
        });
        return false;
    }

       //]]>
</script>

<textarea class="jquery_ckeditor" cols="80" id="intro_text" name="intro_text" rows="10"><% 
    String lang = request.getParameter("language");
    if (lang == null || lang.length() == 0) {
        lang = "cs";
    }
    try {
        String text = TextsService.getText("intro", lang);
        out.println(text);
    } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println("Loading default");
%><c:choose>
    <c:when test="${param.language == 'en'}"><%@ include file="intro_en.jsp" %></c:when>
    <c:when test="${param.language == 'cs'}"><%@ include file="intro_cs.jsp" %></c:when>
    <c:otherwise><%@ include file="intro_cs.jsp" %></c:otherwise>
</c:choose><%
    }
%></textarea>