<%--

--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>


<view:object name="securedStreams" clz="cz.incad.Kramerius.views.rights.DisplaySecuredStreamsView"></view:object>


<scrd:loggedusers>
    
        <table style="width: 100%;" class="ui-dialog-content ui-widget-content">
            <thead style="border-bottom: 1px dashed;">
                <tr>
                    <td style="width:80%"><strong> <view:msg>common.stream</view:msg></strong></td>
                    <td><strong><view:msg>common.change</view:msg></strong></td>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${securedStreams.securedStreams}" var="stream" varStatus="status">
                    <tr class="${(status.index mod 2 == 0) ? 'result r0': 'result r1'}">
                       <td>${stream.streamName}</td>
                       <td><button onclick="findObjectsDialog('${stream.streamName}').openDialog(${stream.pidStructsArguments})">Editace</button></td>
                   </tr>
                </c:forEach>
            </tbody>
        </table>

</scrd:loggedusers>
