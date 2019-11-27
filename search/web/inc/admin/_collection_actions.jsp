<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>


<scrd:securedContent action="display_admin_menu" sendForbidden="true">

<script type="text/javascript">
      function collectionRightDialog(pid){
          var structs = [
             {
               'models': "monograph",
               'pid':pid
             }
          ];

          // open dialog
          var affectedDialog = findObjectsDialog();
          affectedDialog.actions = null;
          affectedDialog.openDialog(structs);
      }
      
    
    
/** collection object administration */
function CollectionAdm() {

	/** 
	 * Basic open dialog
	 * @private
	 */
	this.dialog = null;

	/** 
	 * Image association dialog
	 * @private
	 */
	this.imageAssocDialog = null;

}

CollectionAdm.prototype.waiting=function() {
	$("#collections-content").hide();
	$('#collections-waiting').show();
}
CollectionAdm.prototype.refresh=function() {
	var url = "inc/admin/_collection_actions_content.jsp";
	this.waiting();
	$("#collections-content").html("");
    $.get(url, bind(function(data) {
		$("#collections-content").html(data);
		this.content();
    },this));
}
CollectionAdm.prototype.content=function() {
	$("#collections-content").show();
	$('#collections-waiting').hide();
}



CollectionAdm.prototype.edit=function(action, canLeave, czech, english, collection,czech_log_desc,english_log_desc) {
	//var url = "vc?action=CREATE&canLeave=" + canLeave;
    var url = "vc?action="+action+"&canLeave=" + canLeave;

	if (czech) {
        var escapedText = replaceAll(encodeURIComponent(czech), ',', '');
            escapedText = replaceAll(escapedText, '\n', '');
            escapedText = escapedText.replace(/ +(?= )/g,'');
            escapedText = escapedText.replace(/&/g,'%26');
            url = url + "&text_cs=" + escapedText;
	}
	
	if (english) {
        var escapedText = replaceAll(encodeURIComponent(english), ',', '');
            escapedText = replaceAll(escapedText, '\n', '');
            escapedText = escapedText.replace(/ +(?= )/g,'');
            escapedText = escapedText.replace(/&/g,'%26');
            url = url + "&text_en=" + escapedText;
    }
    
    if (collection) {
	    url = url + "&pid="+collection
    }
                
    this.waiting();
    $.get(url,  bind(function(pid){
        if (czech_log_desc) {
            var encodedData = Base64.encode(czech_log_desc);
            $.post("vc?action=LONG_TEXT_UPLOAD&pid="+collection+"&lang=cs", {
	           'encodedData' : encodedData
            });
        }
        
        if (english_log_desc) {
            var encodedData = Base64.encode(english_log_desc);
            $.post("vc?action=LONG_TEXT_UPLOAD&pid="+collection+"&lang=en", {
               'encodedData' : encodedData
            });
        }

        this.refresh();
    },this));
    
}


CollectionAdm.prototype.delete = function(pid) {
	showConfirmDialog(dictionary['administrator.dialogs.virtualcollectionsdeleteconfirm'], function(){
        var url = "lr?action=start&def=virtualcollections&out=text&params=removecollection,none,"+pid;
        processStarter("virtualcollectionsdelete").start(url);
    });
}

CollectionAdm.prototype.imgassoc=function(collection) {
    var url = "inc/admin/_collection_images_associate.jsp?collection="+collection;
    $.get(url, bind(function(data) {
        $.get(url, bind(function(data) {
    	if (this.imageAssocDialog) {
    		this.imageAssocDialog.dialog('open');
        } else {
            $(document.body).append('<div id="colImgAssoc">'+'</div>');
            this.imageAssocDialog = $('#colImgAssoc').dialog({
                width:400,
                height:250,
                modal:true,
                title:'',
                buttons: [
                    {
                        text: dictionary["common.ok"],
                        click: bind(function() {
                            $("form#vc_image_association").submit();
                            this.imageAssocDialog.dialog("close"); 
                        },this)
                    },
                    {
                        text: dictionary['common.close'],
                        click:function() {
                            $(this).dialog("close"); 
                        } 
                    }
                ] 
            });
        }
    	$("#colImgAssoc").html(data);
    	//$("#newCol").dialog('option','title',dictionary['rights.changepswd.title']);
    },this));
    
    },this));
    
}
CollectionAdm.prototype.collection=function(collection) {
    var url = "inc/admin/_new_collection.jsp";
    if (collection) { url = url +"?collection="+collection; }
    $.get(url, bind(function(data) {
    	if (this.dialog) {
    		this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="newCol">'+'</div>');
            this.dialog = $('#newCol').dialog({
                width:600,
                height:480,
                modal:true,
                title:'Collection',
                buttons: [
                    {
                        text: dictionary["common.ok"],
                        click: bind(function() {
                               var czech = $("#czech_text").val();
                               var english = $("#english_text").val();
                               var canLeave = $("#canLeave").is(":checked");
                               var collection = $("#vc_pid").val();
                               var action = (collection ? "CHANGE" : "CREATE");
                               
                               var czech_log_desc = $("#descs_cs_text").val();
                               var english_log_desc = $("#descs_en_text").val();

                               
                               this.edit(action,canLeave,czech,english, collection, czech_log_desc,english_log_desc);
                               
                               this.dialog.dialog("close"); 
                        },this)
                    },
                    {
                        text: dictionary['common.close'],
                        click:function() {
                            $(this).dialog("close"); 
                        } 
                    }
                ] 
            });
        }
    	$("#newCol").html(data);
    	//$("#newCol").dialog('option','title',dictionary['rights.changepswd.title']);
    },this));

}
      
var colAdm = new CollectionAdm();


$(document).ready(function(){
        colAdm.refresh();
});
</script>

<style>
<!--
.criteriums-table {
    width:100%;
}    
.criteriums-table thead tr td:last-child {
    width: 150px;
} 
.collections-buttons {
    float: right;
}
.collections-buttons-clear {
    clear: right;
}
-->
</style>


<div>
    
<div>    
    <div class="collections-buttons">
      <a href="javascript:colAdm.refresh();" class="ui-icon ui-icon-transferthick-e-w"></a>
    </div>
    <div class="collections-buttons">
        <a href="javascript:colAdm.collection();" class="ui-icon ui-icon-plusthick"></a>
    </div>
     <div class="collections-buttons-clear"></div>
</div>

<div class="collections-buttons-clear">
</div>


<div id="collections-waiting" style="display: none;"><span><view:msg>administrator.dialogs.waiting</view:msg></span>
</div>
    
<div id="collections-content"></div>

</scrd:securedContent>