<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
String[] themes = {"smoothness",
                    "blitzer",
                    "dark-hive",
                    "redmond",
                    "ui-lightness",
                    "sunny"
                    };
pageContext.setAttribute("themes", themes);
%>
<select id="themes" onchange="setTheme();">
    <c:forEach var="cur_theme" items="${themes}">
        <c:set var="selected"><c:if test="${cur_theme eq theme}">selected="selected"</c:if></c:set>
        <option value="${cur_theme}" ${selected}>${cur_theme}</option>
    </c:forEach>
</select>
<script type="text/javascript">
    function setTheme(){
        var page = new PageQuery(window.location.search);
        page.setValue("theme", $('#themes').val());
            window.location = window.location.pathname + "?" +  page.toString();
        
    }
</script>