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

<view:object name="print" clz="cz.incad.Kramerius.views.PrintViewObject"></view:object>

<div style="margin: 10px">
    
    <h3>${print.header}</h3>
    
    <div id="print_desc">
        ${print.desc}
    </div>

    <br></br>
    <strong><view:msg>print.generate</view:msg></strong>
    <table style="width: 100%">
	    <c:forEach items="${print.items}" var="item">
	        <tr>
	            <td>
	                <div id="${item.id}">
	                    <input type="radio" id="${item.id}_radio" name="print" ${item.checkedAttribute} onclick="print.onChange('${item.id}', '${item.type}','${item.pids}');" value="${item.pids}"> <view:msg>print.${item.type}.generate</view:msg> ${item.name}  </input>    
	                </div>
	            </td>
	        </tr>
	    </c:forEach>
    </table>
    
</div>
