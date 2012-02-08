<%--
    Print dialog
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>

<view:object name="pfp" clz="cz.incad.Kramerius.views.PreparedForProfileView"></view:object>

<div style="margin: 10px">
    <strong> <view:msg>userprofile.forsave.profiletitle</view:msg> </strong>
    <table style="width: 100%">
       <c:forEach items="${pfp.profileCandidateItems}" var="item">
            <tr>
                <td>
                    <input type="checkbox" id="${item.key}" ${item.checked ? "checked=\"checked\"" : ""} value="${item.value}">  
                        ${item.localizedKey} -  ${item.localizedValue}</input>
                </td>
            </tr>
        </c:forEach>
    </table>
    
</div>
