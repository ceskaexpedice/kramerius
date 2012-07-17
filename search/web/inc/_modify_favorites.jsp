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

<view:object name="modifyView" clz="cz.incad.Kramerius.views.FavoritesViewObject"></view:object>


<style>
<!--
.r0{
    background: white;
}
.r1{
    background: #efefef;
}

.favoriteSelection {
    padding: 4px;
}

.favoriteSelected {}

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

    
    <h3> ${modifyView.header} </h3>

    <table style="width: 100%">

    <c:forEach items="${modifyView.items}" var="item" varStatus="status">
        <tr class="${(status.index mod 2 == 0) ? 'selection r0 ': 'selection r1 '}">
            <td>
                <div id="${item.id}" class="favoriteSelection ${(item.checked) ? 'favoriteSelected': ''}">

                    <input type="checkbox" id="${item.id}_radio" name="favoriteSelection" ${item.checkedAttribute}   value="${item.pid}"/> 
                    <strong>${item.name}</strong>

                    <c:if test="${item.descriptionDefined}">
                        <div style="font-size: 85%;font-family:monospace; margin-left: 10px;">
                            <c:forEach items="${item.descriptions}" var="desc">
                                <c:out value="${desc}"></c:out><br>                               
                            </c:forEach>                               
                        </div>
                    </c:if>
                </div>
            </td>
            <td width="15%" align="center"">
                <div style="position: relative; width: 200px; height: 100px;" >
                      <span class="pages" style="position: absolute; z-index: 2; right:  0px;  ">
                          <img src="img?uuid=${item.pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" />
                      </span>
                </div>
            </td>
        </tr>

    </c:forEach>
    </table>
    
    
</div>
