<%--
    PDF dialog
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>

<view:object name="pdfView" clz="cz.incad.Kramerius.views.PDFGenerateViewObject"></view:object>

<div style="margin: 10px">

    
    <h3>${pdfView.header}</h3>
    
    <div id="pdf_desc">
        ${pdfView.desc}
    </div>

    <br></br>
    <strong><view:msg>pdf.generate</view:msg></strong>
    <table style="width: 100%">
    <c:forEach items="${pdfView.items}" var="item">
        <tr>
            <td>
                <div id="${item.id}">
                    <input type="radio" id="${item.id}_radio" name="pdfSelection" ${item.checkedAttribute} onchange="pdf.onChange('${item.id}', '${item.type}','${item.pids}');"  value="${item.pids}"> <view:msg>pdf.${item.type}.generate</view:msg> ${item.name}  </input>    
                </div>
            </td>
        </tr>
            <c:if test="${item.master}">
            
            <c:if test="${item.checked}">
            <script type="text/javascript">
                 // set previous 
                pdf.previous= "#${item.id}_option";
            </script>
            </c:if>            
            
            <tr>
                <td>
                    <div id="${item.id}_option" ${item.checked ? "style='display:block'" : "style='display:none'"} style="display:none">
                        ${pdfView.numberOfGeneratedPages}:<span id="${item.id}_error" style="color: red;"></span>
                        <div>
                            <input name="pdfSelection" id="${item.id}_input" type="text" onkeyup="pdf.onKeyup('${item.id}', '${item.type}','${item.pids}');" value="${pdfView.maxNumberOfPages}"></input>
                        </div>
                    </div>
                </td>
             </tr>
            </c:if>
    </c:forEach>
    
    </table>
    
</div>
