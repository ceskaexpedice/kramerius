<%--
    Dialog for managing labels
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"  %>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>

<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<style>
    <!--
    .labels-table {
        width:100%;
    }
    .labels-table thead tr td:last-child {
        width: 150px;
    }
    .labels-buttons {
        float: right;
    }
    .labels-buttons-clear {
        clear: right;
    }
    .buttons span.ui-button-text {
        padding: 3px;
    }
    -->
</style>

<view:object name="labels" clz="cz.incad.Kramerius.views.rights.LabelsManageView"></view:object>
<div>

    <script type="text/javascript">
        $(".label-button").button();


    </script>



    <div id="labels-manage-content">

        <table width="100%">
            <tr>
                <td style="text-align:right">
                    <div class="buttons">
                        <span class="label-button">
                            <a title="<view:msg>labels.dialog.button.creare</view:msg>" href="javascript:labelsManager.createOrEditLabel();"  class="ui-icon ui-icon-plus"> </a>
                        </span>
                        <span class="label-button">
                            <a title="<view:msg>labels.dialog.button.importfromsolr</view:msg>" href="javascript:labelsManager.importFromSolr();"  class="ui-icon ui-icon-arrowthickstop-1-s"> </a>
                        </span>
                        <span class="label-button">
                            <a title="<view:msg>common.refresh</view:msg>" href="javascript:labelsManager.refresh();"  class="ui-icon ui-icon-arrowrefresh-1-e"></a>
                        </span>
                    </div>
                </td>
            </tr>
        </table>

        <div id="labels-content-reloading" style="display: none">
            <div style="font-style: italic"> <view:msg>administrator.dialogs.waiting</view:msg>  </div>
        </div>

        <div id="labels-content">
            <table style="width: 100%">
                <thead style="border-bottom: 1px dashed; background-image: url('img/bg_rights_table.png'); background-repeat: repeat-x; height: 28px;">
                <tr>
                    <td style="border-top: 1px solid black; text-align:center" width="25px"><strong><view:msg>labels.dialog.table.priority</view:msg></strong></td>
                    <td style="border-top: 1px solid black; text-align:center"><strong><view:msg>labels.dialog.table.name</view:msg></strong></td>
                    <td style="border-top: 1px solid black; text-align:center"><strong><view:msg>labels.dialog.table.description</view:msg></strong></td>
                    <td style="border-top: 1px solid black; text-align:center"><strong><view:msg>labels.dialog.table.usedinrights</view:msg></strong></td>
                    <td style="border-top: 1px solid black; text-align:center"><strong><view:msg>labels.dialog.table.usedinsolr</view:msg></strong></td>
                    <td style="border-top: 1px solid black; text-align:center" width="120px"><strong><view:msg>common.change</view:msg></strong></td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${labels.labels}" var="cLabel" varStatus="status">

                    <tr>

                        <td style="text-align:center" width="25px">${cLabel.priority}</td>
                        <td style="text-align:center">${cLabel.name}</td>
                        <td style="text-align:center">${cLabel.description}</td>


                        <c:choose>
                            <c:when  test="${cLabel.usedInRights}">
                                <td style="text-align:center"><input type="checkbox" disabled checked  readonly/></td>
                            </c:when>
                            <c:otherwise>
                                <td style="text-align:center"><input type="checkbox" disabled readonly/> </td>
                            </c:otherwise>
                        </c:choose>

                        <c:choose>
                            <c:when  test="${cLabel.usedInSolr}">
                                <td style="text-align:center"><input type="checkbox" disabled checked  readonly/></td>
                            </c:when>
                            <c:otherwise>
                                <td style="text-align:center"><input type="checkbox" disabled readonly/> </td>
                            </c:otherwise>
                        </c:choose>

                        <td style="text-align:center" width="120px">

                        <c:choose>
                            <c:when  test="${status.first}">
                                <div class="buttons first">
                            </c:when>
                            <c:when  test="${status.last}">
                                    <div class="buttons last">
                            </c:when>
                            <c:otherwise>
                                <div class="buttons">
                            </c:otherwise>
                        </c:choose>



                            <span id="label-button-edit-${cLabel.id}" class="label-button label-edit-button">
                                <a title="<view:msg>labels.dialog.button.edit</view:msg>" href="javascript:labelsManager.createOrEditLabel(${cLabel.id});" class="ui-icon ui-icon-pencil"> </a>
                            </span>

                            <span id="label-button-remove-${cLabel.id}" class="label-button label-remove-button">
                                <a  title="<view:msg>labels.dialog.button.remove</view:msg>" href="javascript:labelsManager.removeLabel(${cLabel.id}, '${cLabel.name}', ${cLabel.usedInRights});" class="ui-icon ui-icon-minusthick"> </a>
                            </span>

                            <span id="label-button-moveup-${cLabel.id}" class="label-button label-moveup-button">
                                <a  title="<view:msg>labels.dialog.button.moveup</view:msg>" href="javascript:labelsManager.moveUp(${cLabel.id});" class="ui-icon ui-icon-arrowthick-1-n"> </a>
                            </span>

                            <span  id="label-button-movedoen-${cLabel.id}" class="label-button label-movedown-button">
                                <a title="<view:msg>labels.dialog.button.movedown</view:msg>" href="javascript:labelsManager.moveDown(${cLabel.id});" class="ui-icon  ui-icon-arrowthick-1-s"> </a>
                            </span>
                            </div>
                        </td>

                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

    </div>

</div>

