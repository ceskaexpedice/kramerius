<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>

<%@ page isELIgnored="false"%>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<view:object name="buttons" clz="cz.incad.Kramerius.views.inc.MenuButtonsViewObject"></view:object>

<scrd:securedContent action="display_admin_menu" sendForbidden="true">


<style type="text/css">
    #coll_table{
        width: 100%;
        margin:0px;

    }
    #coll_table td{
        border-bottom: 1px solid silver;
    }
    #coll_add_row td{
        border-bottom: none;
    }
    #coll_loading{
        width: 100%;
        margin:0px;
        display:none;
        position:absolute;
        left:0;
        top:0;
        text-align: center;
        background:white;
    }
</style>
<table id="coll_table" cellpadding="0" cellspacing="0">
    <thead class="ui-widget-header">
    <th>pid</th>
    <c:forEach items="${buttons.languageItems}" var="langitm">
        <th>${langitm.name}</th>
    </c:forEach>
    <th><view:msg>administrator.dialogs.virtualcollections.canLeave</view:msg></th>
        <th></th>
    </thead>
    <c:forEach var="col" items="${cols.virtualCollectionsFromFedora}">
        <tr id="vc_${col.pid}">
            <td>${col.pid}</td>
            <c:forEach items="${buttons.languageItems}" var="langitm">
                <td class="editable lang">
                    <span class="val">${col.descriptionsMap[langitm.key]}</span>
                    <input style="display:none;" type="text" class="val" value="${col.descriptionsMap[langitm.key]}" />
                    <input type="hidden" class="id" value="${langitm.key}" />
                </td>
            </c:forEach>
                <td class="editable canLeave"><span class="ui-icon ui-icon-cancel <c:if test="${col.canLeave}"  >ui-icon-check</c:if> ">canleave</span>
                    <input style="display:none;" type="checkbox" class="canLeave"
                       <c:if test="${col.canLeave}"  >checked="checked"</c:if> 
                        />
            </td>
            <td class="buttons">
                <a class="edit" title="<view:msg>administrator.dialogs.virtualcollections.edit</view:msg>" href="javascript:vcBeginEdit('${col.pid}');"><span class="ui-icon ui-icon-pencil">edit</span></a>
                <a class="save" title="<view:msg>administrator.dialogs.virtualcollections.save</view:msg>" style="display:none;" href="javascript:vcSaveEdit('${col.pid}');"><span class="ui-icon ui-icon-disk">save</span></a>
                <a class="delete" title="<view:msg>administrator.dialogs.virtualcollections.delete</view:msg>" href="javascript:vcDelete('${col.pid}');"><span class="ui-icon ui-icon-trash">delete</span></a>
            </td>
        </tr>
    </c:forEach>
    <tr id="coll_add_row">
        <td></td>
    <c:forEach items="${buttons.languageItems}" var="langitm">
        <td class="coll_add_lang">
            <input type="text" class="val" value="" />
            <input type="hidden" class="id" value="${langitm.key}" />
        </td>
    </c:forEach>
        <td><input type="checkbox" class="canLeave" /></td>
        <td class="buttons">
            <a href="javascript:vcAdd();" title="<view:msg>administrator.dialogs.virtualcollections.add</view:msg>"><span class="ui-icon ui-icon-plus">add</span></a>
        </td>
    </tr>
</table>
<div id="coll_loading"><br/><img src="img/loading.gif" alt="loading"/></div>
<script type="text/javascript">
    $("td.buttons>a").button();
    $("td.buttons>a").css("float", "left");
    $("td.buttons>a>span").css("padding", "2");
    function vcDelete(pid){
        showConfirmDialog(dictionary['administrator.dialogs.virtualcollectionsdeleteconfirm'], function(){
            var url = "lr?action=start&def=virtualcollections&out=text&params=removecollection,none,"+pid;
            processStarter("virtualcollectionsdelete").start(url);
        });

    }

    function vcBeginEdit(pid){
        vcToggleEdit(pid);
    }
    function vcToggleEdit(pid){
        $(jq("vc_"+pid)+">td.editable").children().toggle();
        $(jq("vc_"+pid)+" a.save").toggle();
        $(jq("vc_"+pid)+" a.delete").toggle();
        $(jq("vc_"+pid)+" a.edit>span>span").toggleClass('ui-icon-cancel');

    }

    function vcSaveEdit(pid){
        var escapedText;
        var url = "vc?action=CHANGE&pid="+pid+ "&canLeave=" + $(jq("vc_"+pid)+" input.canLeave").is(":checked");
        var canAdd = true;
        $(jq("vc_"+pid)+" td.lang").each(function(){
            var s = $(this).children("input.val").val();
            if(s.trim().length==0){
                canAdd = false;
                return;
            }
            escapedText = replaceAll(s, ',', '');
            escapedText = replaceAll(escapedText, '\n', '');
            //escapedText = escapedText.replace(/ +(?= )/g,'');
            escapedText = escapedText.replace(/&/g,'%26');
            url = url + "&text_" + $(this).children("input.id").val() +
                "=" + escapedText ;
        });
        if(!canAdd){
            alert(dictionary['administrator.dialogs.virtualcollections.emptyError']);
            return;
        } 
        $("#coll_loading").css("height", $("#vcAdminDialog").height());
        $("#coll_loading").show();
        $.get(url, function(data){
            $(jq("vc_"+pid)+" td.editable").each(function(){
                $(this).children("span.val").html($(this).children("input.val").val());
            });
            if($(jq("vc_"+pid)+" input.canLeave").is(":checked")){
                $(jq("vc_"+pid)+" td.canLeave>span").addClass("ui-icon-check");
            }else{
                $(jq("vc_"+pid)+" td.canLeave>span").removeClass("ui-icon-check");
            }
            vcToggleEdit(pid);
            $("#coll_loading").hide();
        }).error(function(data, msg, status){
            alert(status + ": " + data.responseText);
            $("#coll_loading").hide();
        });

    }


    function vcAdd(){
        var escapedText;
        var canLeave = $("#coll_add_row input.canLeave").is(":checked");
        var check = canLeave ? 'checked="checked"' : '';
        
        var url = "vc?action=CREATE&canLeave=" + canLeave;
        var canAdd = true;
        $(".coll_add_lang").each(function(){
            var s = $(this).children("input.val").val();
            if(s.trim().length==0){
                canAdd = false;
                return;
            }
            escapedText = replaceAll(encodeURIComponent(s), ',', '');
            //escapedText = replaceAll(s, '\+', '%2B');
            escapedText = replaceAll(escapedText, '\n', '');
            escapedText = escapedText.replace(/ +(?= )/g,'');
            escapedText = escapedText.replace(/&/g,'%26');
            url = url + "&text_" + $(this).children("input.id").val() +
                "=" + escapedText;
        });
        if(!canAdd){
            alert(dictionary['administrator.dialogs.virtualcollections.emptyError']);
            return;
        } 
        $("#coll_loading").css("height", $("#vcAdminDialog").height());
        $("#coll_loading").show();
        $.get(url, function(pid){
            var tr = '<tr id="vc_'+pid+'">' +
                '<td>'+pid+'</td>';
                
            $(".coll_add_lang").each(function(){
                tr = tr +
                    '<td class="editable lang">'+
                    '<span class="val">'+$(this).children("input.val").val()+'</span>'+
                    '<input style="display:none;" type="text" class="val" value="'+$(this).children("input.val").val()+'" />'+
                    '<input type="hidden" class="id" value="'+$(this).children("input.val").val()+'" />'+
                    '</td>';
            });
            
            tr = tr + '<td><input type="checkbox" class="canLeave" ' + check + ' /></td>' +
                '<td class="buttons">'+
                '<a class="edit" href="javascript:vcBeginEdit(\''+pid+'\');"><span class="ui-icon ui-icon-pencil">edit</span></a>'+
                '<a style="display:none;" class="save" href="javascript:vcSaveEdit(\''+pid+'\');"><span class="ui-icon ui-icon-disk">save</span></a>'+
                '<a href="javascript:vcDelete(\''+pid+'\');"><span class="ui-icon ui-icon-trash">delete</span></a>'+
                '</td></tr>'
            $("#coll_add_row").before(tr);

            $("td.buttons>a").button();
            $("td.buttons>a").css("float", "left");
            $("td.buttons>a>span").css("padding", "2");
            $("#coll_loading").hide();

        }).error(function(data, msg, status){
            //alert(status + ": " + data.responseText);
            alert("Error trying to create virtual collection");
            $("#coll_loading").hide();
        });
    }
</script>

</scrd:securedContent>