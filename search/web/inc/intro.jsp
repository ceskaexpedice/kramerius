<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />

<div id="intro">
     <ul>
         <li><a href="#intro1"><fmt:message bundle="${lctx}">Nejnovější</fmt:message></a></li>
         <li><a href="#intro2"><fmt:message bundle="${lctx}">Nejžádanější</fmt:message></a></li>
         <li><a href="#intro3"><fmt:message bundle="${lctx}">Informace</fmt:message></a></li>
     </ul>
     <div id="intro1" style="height:220px;"></div>
         <script>
            //$(document).ready(function(){
                $.get('inc/newest.jsp', function(data){
                   $('#intro1').html(data) ;
                });
            //});
         </script>
     <div id="intro2" style="height:220px;">
     </div>
         <script>
            //$(document).ready(function(){
                $.get('inc/mostDesirables.jsp', function(data){
                   $('#intro2').html(data) ;
                });
            //});
         </script>
     <div id="intro3">
        <%  if(request.getRemoteUser()!=null) {  %>
        <%@ include file="text/edit_intro.jsp" %>
	<% }else{  %>
            <c:choose>
                <c:when test="${param.language == 'en'}"><%@ include file="text/intro_en.jsp" %></c:when>
                <c:when test="${param.language == 'cs'}"><%@ include file="text/intro_cs.jsp" %></c:when>
                <c:otherwise><%@ include file="text/intro_cs.jsp" %></c:otherwise>
            </c:choose>
	<% } %>
         </div>
</div>
<script language="javascript">
        $(document).ready(function(){
            $('#intro').tabs();
        });
    </script>