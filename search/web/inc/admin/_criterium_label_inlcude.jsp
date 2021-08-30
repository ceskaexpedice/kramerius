<%--
    Only include page - included from _new_right.jsp
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="r" clz="cz.incad.Kramerius.views.rights.DisplayRightView"></view:object>


<div>

    <div style="border-top: 1px solid gray;">
        <table><tr>

            <td><label for="name"><strong>Jmeno label:</strong></label></td>
            <td><select size="1" name="usedLabel" id="usedLabel" onchange="right.paramTabs['label'].onSelectLabel();" >
                <option value=""></option>
                <c:forEach var="p"  items="${r.labels}">
                    <option value="${p.id}">${p.name}</option>
                </c:forEach>

            </select></td>

        </tr></table>
    </div>



</div>