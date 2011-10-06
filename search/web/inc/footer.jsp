<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div align="center" >
    ©2008-2011. 
    Developed under GNU GPL by 
    <a href="http://www.incad.cz/">Incad</a>, <a href="http://www.nkp.cz/">NKČR</a>, <a href="http://www.lib.cas.cz/">KNAV</a> and <a href="http://www.mzk.cz/">MZK</a> <br/><br/>
</div>
<c:if test="${param.debug==true}">${url}</c:if>
<div id="test"></div>