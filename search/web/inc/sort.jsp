<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<div id="sort" style="float:right;padding-right:3px;">
<c:if test="${fn:containsIgnoreCase(param.sort ,'root_title_cs')}" >
   <b>
       <c:if test="${fn:containsIgnoreCase(param.sort ,'desc')}" >
       <a  href="javascript:sortBy('root_title_cs+asc')"><fmt:message bundle="${lctx}">filter.sortby.name</fmt:message></a>
   </c:if>
   <c:if test="${fn:containsIgnoreCase(param.sort ,'asc')}" >
       <a  href="javascript:sortBy('root_title_cs+desc')"><fmt:message bundle="${lctx}">filter.sortby.name</fmt:message></a>
   </c:if>
   </b> 
</c:if> 
<c:if test="${! fn:containsIgnoreCase(param.sort ,'root_title_cs')}" >
   <a  href="javascript:sortBy('root_title_cs+asc')"><fmt:message bundle="${lctx}">filter.sortby.name</fmt:message></a>
</c:if>

<c:if test="${fn:containsIgnoreCase(param.sort ,'score') || empty param.sort}" >
    <a class="box"><b><fmt:message bundle="${lctx}">filter.sortby.relevation</fmt:message></b></a>
</c:if> 
<c:if test="${! fn:containsIgnoreCase(param.sort ,'score') && !empty param.sort}" >
    <a  href="javascript:sortBy('score+desc')"><fmt:message bundle="${lctx}">filter.sortby.relevation</fmt:message></a>
</c:if>
</div>