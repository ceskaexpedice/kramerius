<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<view:object name="ga" clz="cz.incad.Kramerius.views.GoogleAnalyticsViewObject"></view:object>

<div align="center" >
    ©2008-2011. 
    Developed under GNU GPL by 
    <a href="http://www.incad.cz/">Incad</a>, <a href="http://www.nkp.cz/">NKČR</a>, <a href="http://www.lib.cas.cz/">KNAV</a> and <a href="http://www.mzk.cz/">MZK</a> <br/><br/>
</div>
<c:if test="${param.debug==true}">${url}</c:if>
<div id="test"></div>

