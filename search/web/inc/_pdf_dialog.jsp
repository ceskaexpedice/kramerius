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
                    <input type="radio" id="${item.id}_radio" name="pdfSelection" ${item.checkedAttribute} onclick="pdf.onChange('${item.id}', '${item.type}','${item.pids}');"  value="${item.pids}"> <view:msg>pdf.${item.type}.generate</view:msg> ${item.name}  </input>    
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
    
    <hr></hr>
    <!--
    <a href="javascript:alert('show');" class="ui-icon ui-icon-triangle-1-n"></a> 
    <div style="float: left;"><strong>Parametry</strong></div>    
    <div style="clear: both;"></div>
    -->
    
    <div id="pdfsettings">
    
     <input type="radio" id="standard" name="device" checked="checked"   value="desktop" onclick="pdf.onSettingsChange('standard')" >Standardni PDF </input>     
     <input type="radio" id="device" name="device"   value="ereader" onclick="pdf.onSettingsChange('ereader')"> PDF pro ctecky(ruzne rozmery) </input>     
     
     <div id="pdfsettings_ereader" style="display: none">
         <div style="padding: 5px;">
          <div style="padding-bottom: 3px;">  
              <input type="checkbox" value="psfont">Pouzit postscript fonty (typ 1)</input> 
          </div>
          <div>
            <select>
                <option selected="selected"> A4 format (Pocket reader)</option>
                <option selected="selected"> Kindle format </option>
            </select>    
         </div>
         </div>
     </div>
    </div>
</div>
