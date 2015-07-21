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

<view:object name="pdfView" clz="cz.incad.Kramerius.views.PdfGenerateViewObject"></view:object>

<style>
<!--
.r0{
    background: white;
}
.r1{
    background: #efefef;
}

.pdfSelection {
    padding: 4px;
}

.pdfSelected {
    border-left: 1px solid; 
}

.pages {
        border:solid white 1px;
        overflow:hidden;
        background:#F1F1F1;
        margin: 0px 4px;
        -moz-box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
        -webkit-box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
        box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
}
-->
</style>

<div style="margin: 10px">

    
    <h3>${pdfView.header}</h3>
    
    <div id="pdf_desc">
        ${pdfView.desc}
    </div>

    <br></br>
    <strong><view:msg>pdf.generate</view:msg></strong>


    <table style="width: 100%">
    <c:forEach items="${pdfView.items}" var="item" varStatus="status">
        <tr class="${(status.index mod 2 == 0) ? 'selection r0 ': 'selection r1 '}">
            <td>
                <div id="${item.id}" class="pdfSelection ${(item.checked) ? 'pdfSelected': ''}">

                    <input type="radio" id="${item.id}_radio" name="pdfSelection" ${item.checkedAttribute} onclick="pdf.onChange('${item.id}', '${item.type}','${item.pids}');"  value="${item.pids}"/> 
                    <strong>${item.name}</strong>
                    <c:if test="${item.invalidOption}">
                          <span id="${item.id}_error" style="color: red;"><view:msg>pdf.validationError.toomuch</view:msg></span>
                    </c:if>

                    <c:if test="${item.pidsMoreThenOne}">
                        <div style="font-size: 85%;font-family:monospace;" >
                            <ul>
                                <c:forEach items="${item.detailedItemNames}" var="detailName">
                                    <li>${detailName}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>
                    
                    <c:if test="${item.descriptionDefined}">
                        <div style="font-size: 85%;font-family:monospace; margin-left: 10px;">
                                <c:forEach items="${item.descriptions}" var="desc">
                                    <c:out value="${desc}"></c:out><br>                               
                                </c:forEach>                               
                        </div>
                    </c:if>

                <c:if test="${item.master}">
            
                <c:if test="${item.checked}">
                 <script type="text/javascript">
                     // set previous 
                     pdf.previous= "#${item.id}_option";
                     
                </script>
                </c:if>            
                <div style="font-size: small;font-family:monospace; margin-left: 10px;">
	                <div id="${item.id}_option" ${item.checked ? "style='display:block'" : "style='display:none'"} style="display:none">
                      <span id="${item.id}_error" style="color: red;"></span>
                        <div id="${item.id}_numberPagesConf" ${item.offPDFCheck ? "style='display:none'" : "style='display:block'" }>
                          <table>
                           <tr><td><view:msg>pdf.numberOfPages</view:msg></td> </tr>
                           <tr><td>
                              <input name="pdfSelection" id="${item.id}_input" type="text" onkeyup="pdf.onKeyup('${item.id}', '${item.type}','${item.pids}');" value="${pdfView.maxNumberOfPages}" size="4"></input>
                           </td> </tr>
                         </table> 
                        </div>
              </div>
              </div>
            
            </c:if>

                    
                </div>
                
            </td>
            <td width="40%">
                <div style="position: relative; width: 200px; height: 100px;" >
                  <c:forEach items="${item.pids}" var="pid" varStatus="pidStatus">
                    <c:if test="${pidStatus.index < 10}">
                      <span  class="pages" style="position: absolute; z-index: ${-1* pidStatus.index+10}; right:  ${-15+pidStatus.index*15}px;  "> 
                          <img src="img?uuid=${pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" />
                      </span>
                    </c:if>
                  </c:forEach>  
                </div>
            </td>
        </tr>

    </c:forEach>
    </table>
    
    <hr></hr>
    
    <div id="pdfsettings" style="display:none">
         <input type="radio" id="standard" name="device" checked="checked"   value="TEXT" >
         <view:msg>pdf.typeofreader.standard</view:msg></input>     
         <input type="radio" id="device" name="device"   value="IMAGE"> 
         <view:msg>pdf.typeofreader.ebook</view:msg> </input>     
    </div>
    
     <div id="pdfsettings_ereader">
         <div style="padding: 5px;">
          <div>
            <select>
                <option selected="selected" value="A4"><view:msg>pdf.typeofreader.ebook.format.a4</view:msg></option>
                <option value="A5"><view:msg>pdf.typeofreader.ebook.format.a5</view:msg></option>
                <option value="A3"><view:msg>pdf.typeofreader.ebook.format.a3</view:msg></option>
            </select>    
         </div>
         </div>
     </div>
    </div>
</div>
