<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="ga" clz="cz.incad.Kramerius.views.rights.GlobalActionsView"></view:object>


<div>
    
    <table  style="width:100%">
        <thead>
            <tr> 
                <td><strong>Akce</strong></td>
                <td><strong>Popis</strong></td>
                <td><strong>Editace</strong></td>
            </tr>
        </thead>    
        <tbody>
	            <c:forEach var="ga" items="${ga.wrappers}">
                    <tr>
	                <td>${ga.formalName}</td>
                    <td>${ga.description}</td>
                    <td><button type="button" onclick="globalActions.rigthsForAction('${ga.formalName}');">Editace</button></td>
                    </tr>
	            </c:forEach>
        </tbody>
    </table>
</div>