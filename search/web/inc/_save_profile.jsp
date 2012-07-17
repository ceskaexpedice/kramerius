<%--
    Print dialog
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="pfp"
 clz="cz.incad.Kramerius.views.PreparedForProfileView"></view:object>
<view:object name="favoritesView"
 clz="cz.incad.Kramerius.views.FavoritesViewObject"></view:object>

<style>
<!--
.r0 {
    background: white;
}

.r1 {
    background: #efefef;
}

.favoriteSelection {
    padding: 4px;
}

.favoriteSelected {
    
}

.pages {
    border: solid white 1px;
    overflow: hidden;
    background: #F1F1F1;
    margin: 0px 4px;
    -moz-box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);
    -webkit-box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);
    box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);
}
-->
</style>


<script type="text/javascript">
<!--

$("#profile").tabs();


$('#profile').bind('tabsselect', function(event, ui) {
    
    var v = "#"+$(ui.panel).attr("id")+"_content";
    $(v).show();
    
});
//-->
</script>

<div style="margin: 10px">

<div id="profile" style="width: 100%; height: 100%">

   <ul>
      <li><a href="#profileDefault">Ulozeni drive nastavenych hodnot</a></li>
      <li><a href="#profileFavorites">Zmena oblibenych</a></li>
   </ul>

   <div id="profileDefault">
    <div id="profileDefault_content" style="display:none; margin: 10px">
     <h3> <view:msg>userprofile.forsave.profiletitle</view:msg></h3>
     <table style="width: 100%">
      <c:forEach items="${pfp.profileCandidateItems}" var="item">
       <tr>
        <td><input type="checkbox" id="${item.key}" ${item.checked ? "checked= \"checked\"" : ""} value="${item.value}"> ${item.localizedKey} - ${item.localizedValue}</input></td>
       </tr>
      </c:forEach>
     </table>
     </div>
   </div>


    <div id="profileFavorites">
        <div id="profileFavorites_content" style="display:none; margin: 10px">
        <h3>${favoritesView.header}</h3>
            <div style="width:100%;">
                <table style="width: 100%; height: 100%">

                  <c:forEach items="${favoritesView.items}" var="item" varStatus="status">
                   <tr class="${(status.index mod 2 == 0) ? 'selection r0 ': 'selection r1 '}">
                    <td>
                    <div id="${item.id}"
                     class="favoriteSelection ${(item.checked) ? 'favoriteSelected': ''}">
                 
                    <input type="checkbox" id="${item.id}_radio" name="favoriteSelection"
                            ${item.checkedAttribute}   value="${item.pid}" /> <strong>${item.name}</strong>
                 
                    <c:if test="${item.descriptionDefined}">
                     <div
                      style="font-size: 85%; font-family: monospace; margin-left: 10px;">
                     <c:forEach items="${item.descriptions}" var="desc">
                      <c:out value="${desc}"></c:out>
                      <br>
                     </c:forEach></div>
                    </c:if></div>
                    </td>
                    <td width="15%" align="center"">
                    <div style="position: relative; width: 200px; height: 70px;"><span
                     class="pages" style="position: absolute; z-index: 2; right: 0px;">
                    <img
                     src="img?uuid=${item.pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=66" />
                    </span></div>
                    </td>
                   </tr>
                  </c:forEach>
            </table>
        </div>
</div>

</div>
</div>

</div>
