
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<style>
<!--
   #changeFlag_pids ul{
        margin: 2px;
        padding-left: 4px;
    }
    #changeFlag_pids li{
        list-style-type: none;
        margin: 0;
        padding: 0;
        line-height: 16px;
    }

    #changeFlag_pids li>span{
        width: 16px;
        height: 16px;
        overflow:hidden;
        text-indent: -99999px;
        display:block;
        float:left;
    }

    #changeFlag_pids .scope>span{
        font-weight: bold;
    }
    #changeFlag_pids {
        border-bottom:1px solid rgba(0, 30, 60, 0.9);
        margin-bottom:5px;
        padding-bottom:3px;
    }


-->
</style>

<div id="changeFlag">


<scrd:securedContent action="display_admin_menu" sendForbidden="true">
    
    <div id="changeFlag_pids" >
    </div>


    <input type="radio" value="setpublic" name="flag" checked="checked"><view:msg>administrator.dialogs.changevisibility.public</view:msg></input>
    <input type="radio" value="setprivate" name="flag"><view:msg>administrator.dialogs.changevisibility.private</view:msg></input><br/>
    <input type="checkbox" id="level" name="level"><view:msg>administrator.dialogs.changevisibility.level</view:msg></input>
</scrd:securedContent>

</div>