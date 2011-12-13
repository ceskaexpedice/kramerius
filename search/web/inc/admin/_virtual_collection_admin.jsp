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
<%@ page isELIgnored="false"%>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<view:object name="buttons" clz="cz.incad.Kramerius.views.inc.MenuButtonsViewObject"></view:object>
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
    <th>label</th>
    <c:forEach items="${buttons.languageItems}" var="langitm">
        <th>${langitm.name}</th>
    </c:forEach>
        <th></th>
    </thead>
<c:forEach var="col" items="${cols.virtualCollections}">
    <tr id="vc_${col.pid}">
        <td>${col.pid}</td>
        <td class="editable">
            <span class="val">${col.label}</span>
            <input style="display:none;" type="text" class="label val" value="${fn:replace(col.label, "\"", "")}" />
        </td>
        <c:forEach items="${col.descriptions}" var="desc">
            <td class="editable lang">
                <span class="val">${desc.text}</span>
                <input style="display:none;" type="text" class="val" value="${desc.text}" />
                <input type="hidden" class="id" value="${desc.id}" />
            </td>
        </c:forEach>
        <td class="buttons">
            <a class="edit" href="javascript:vcBeginEdit('${col.pid}');"><span class="ui-icon ui-icon-pencil">edit</span></a>
            <a style="display:none;" class="save" href="javascript:vcSaveEdit('${col.pid}');"><span class="ui-icon ui-icon-disk">save</span></a>
            <a href="javascript:vcDelete('${col.pid}');"><span class="ui-icon ui-icon-trash">delete</span></a>
        </td>
    </tr>
</c:forEach>
    <tr id="coll_add_row">
        <td><input type="text" id="coll_add_id" value="" /></td>
        <td><input type="text" id="coll_add_label" value="" /></td>
    <c:forEach items="${buttons.languageItems}" var="langitm">
        <td class="coll_add_lang">
            <input type="text" class="val" value="" />
            <input type="hidden" class="id" value="${langitm.key}" />
        </td>
    </c:forEach>
        <td class="buttons">
            <a href="javascript:vcAdd();"><span class="ui-icon ui-icon-plus">add</span></a>
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
        $(jq("vc_"+pid)+" a.edit>span>span").toggleClass('ui-icon-cancel');
        
    }

    function vcSaveEdit(pid){
        var url = "vc?action=CHANGE&pid="+pid+
            "&label=" + encodeURIComponent($(jq("vc_"+pid)+" input.label").val());
        $(jq("vc_"+pid)+" td.lang").each(function(){
            url = url + "&text_" + $(this).children("input.id").val() +
                "=" + encodeURIComponent($(this).children("input.val").val());
        });
        $("#coll_loading").css("height", $("#vcAdminDialog").height());
        $("#coll_loading").show();
        $.get(url, function(data){
            $(jq("vc_"+pid)+" td.editable").each(function(){
                $(this).children("span.val").html($(this).children("input.val").val());
            });
            vcToggleEdit(pid);
        }).error(function(data, msg, status){
            alert(status + ": " + data.responseText);
            $("#coll_loading").hide();
        });
        
    }
    
    
    function vcAdd(){
        var pid = $("#coll_add_id").val();
        if(!pid.startsWith('vc:')){
            pid = "vc:" + pid;
        }
        var label = $("#coll_add_label").val();
        var url = "vc?action=CREATE&pid="+pid+
            "&label=" + encodeURIComponent(label);
        $(".coll_add_lang").each(function(){
            url = url + "&text_" + $(this).children("input.id").val() +
                "=" + encodeURIComponent($(this).children("input.val").val());
        });
        $("#coll_loading").css("height", $("#vcAdminDialog").height());
        $("#coll_loading").show();
        $.get(url, function(data){
            var tr = '<tr id="vc_'+pid+'">' +
                '<td>'+pid+'</td>'+
                '<td class="editable">'+
                '<span class="val">'+label+'</span>'+
                '<input style="display:none;" type="text" class="label val" value="'+label+'" />'+
                '</td>';
            $(".coll_add_lang").each(function(){
                tr = tr + 
                    '<td class="editable lang">'+
                    '<span class="val">'+$(this).children("input.val").val()+'</span>'+
                    '<input style="display:none;" type="text" class="val" value="'+$(this).children("input.val").val()+'" />'+
                    '<input type="hidden" class="id" value="'+$(this).children("input.val").val()+'" />'+
                    '</td>';
            });
            tr = tr + '<td class="buttons">'+
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
            alert(status + ": " + data.responseText);
            $("#coll_loading").hide();
        });
    }
</script>