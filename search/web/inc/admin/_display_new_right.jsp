<%--
    Dialog for rights - display objects that can be managed
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="rights" clz="cz.incad.Kramerius.views.rights.DisplayRightsForObjectsView"></view:object>

<style>
<!--

.rightsTableContent-tr-detail {
    display: hidden;
    height: 0px;
}



.buttonTD{
    text-align:right;
    height:30px;
}

.line {
    /*
    height:30px;
    width:1px;
    background-color:black;
    */
}
-->
</style>


<div id="newRight"><scrd:loggedusers> 


</scrd:loggedusers></div>
