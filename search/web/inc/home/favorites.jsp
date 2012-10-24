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
<%@ page isELIgnored="false"%><view:object name="fav" clz="cz.incad.Kramerius.views.FavoritesViewObject"></view:object>



<script type="text/javascript">
<!--
    $(document).ready(function(){
        $("#fav_edit").button();
        $("#fav_cancel").button();
        $("#fav_save").button();
    });

   function FavoritesView() { this.removed = []; }

   FavoritesView.prototype.editMode = function() {
	    $("#fav_edit").hide();
        $("#fav_cancel").show();
        $("#fav_save").show();

        $(".fav_images").each(function(i,val) {
        	$(val).css("height","120");
        });
        $(".fav_remove").show();
   }

   FavoritesView.prototype.viewMode = function() {
       $("#fav_edit").show();
       $("#fav_cancel").hide();
       $("#fav_save").hide();

       $(".fav_images").each(function(i,val) {
           $(val).css("height","100");
       });
       $(".fav_remove").hide();
   }

   FavoritesView.prototype.cancel = function() {
	   $(".fav_images").show(); 
	   this.viewMode();
   }
   
   FavoritesView.prototype.save = function() {
       new Profile().modify(
            //data function
            bind(function(json){
                   if (!json.favorites) {
                       json["favorites"] = [];
                   }

                   var nfav = [];
                   nfav = reduce(bind(function(base, element, status){
                	   if (this.removed.indexOf(element) < 0) {
                           base.push(element);
                       }
                       return base;
                   },this),nfav,json.favorites);


                   json.favorites = nfav;
                   
                   return json;
               },this), 
               // ok function
               function () {
                   (new Message("favorites_save_success")).show();
               });
	       this.viewMode();

       
   }

   FavoritesView.prototype.remove = function(index, pid) {
	   this.removed.push(pid);
	   $("#fav_"+index+"_image").hide();     
   }
   
   var favortiesView = new FavoritesView();
   
//-->
</script>

<a id="fav_edit" style="float:right;" href="javascript:favortiesView.editMode();" title="<view:msg>common.refresh</view:msg>"><span class="ui-icon ui-icon-pencil">edit</span></a>
<a id="fav_cancel" style="float:right; display: none;" href="javascript:favortiesView.cancel();" title="<view:msg>common.refresh</view:msg>"><span class="ui-icon ui-icon-closethick">edit</span></a>
<a id="fav_save" style="float:right; display: none;" href="javascript:favortiesView.save();" title="<view:msg>common.refresh</view:msg>"><span class="ui-icon ui-icon-check">edit</span></a>

<c:forEach items="${fav.favorites}" var="pid" varStatus="pidStatus">
     <div id="fav_${pidStatus.index}_image" class="fav_images" align="center" style="overflow:hidden; border:1px solid #eeeeee; width:100px; height:100px; float:left; margin:5px;">

          <div class="fav_remove" style="display: none;">  
            <a href="javascript:favortiesView.remove(${pidStatus.index},'${pid}')">
                <span class="ui-icon ui-icon-circle-close"></span>
            </a>
          </div>
          <a href="i.jsp?pid=${pid}" >
              <img align="middle" vspace="2" id="img_favorite_${pid}" src="img?uuid=${pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" border="0"/>
          </a>
          
      </div>
</c:forEach>
