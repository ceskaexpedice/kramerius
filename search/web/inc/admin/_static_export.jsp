<%--
    Static export dialog
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>

<view:object name="static" clz="cz.incad.Kramerius.views.StaticExportViewObject"></view:object>

<div style="margin: 10px">
    
    <h3>${static.header}</h3>
    <br></br>
    <strong>Generovani statickeho exportu pro:</strong>
    <table style="width: 100%">
	    <c:forEach items="${static.items}" var="item">
	        <tr>
	            <td>
	                <div id="${item.id}">
	                    <input type="radio" id="${item.id}_radio" name="pdfSelection" ${item.checkedAttribute} onclick="pdf.onChange('${item.id}', '${item.type}','${item.pids}');"  value="${item.pids}"> <view:msg>pdf.${item.type}.generate</view:msg> ${item.name}  </input>    
	                </div>
	            </td>
	        </tr>
	    </c:forEach>
    </table>
    
</div>
