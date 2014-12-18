/**
 * Forbidden - only show forbidden message  
 * @constructor
 */
function Forbidden() {
}


/**
* Show forbidden message
* @method      
*/       
Forbidden.prototype.open =  function(rect) {
        
        function __text() {
           var text = K5.i18n.ctx.texts["k5security_fail"];
           $('#viewer>div.container').html('<div id=\"forbidden\"  style=\"width: 100%; height: 100%;vertical-align: middle; text-align:center\" >'+text+'</div>');
        }

        if ( (!K5.i18n.isKeyReady("texts")) && (!K5.i18n.isKeyReady("texts/k5security_fail"))) {
           var callback = _.bind(function(data) {
              __text();                
           },this);
           K5.i18n.askForText("k5security_fail",K5.i18n.ctx.language, callback);                
        } else {
           __text();                         
        }

        //this.hideInfo();
        this.disabledDisplay = true;

        K5.i18n.k5translate('#forbidden');
}

Forbidden.prototype.clearContainer = function() {
        $('#forbidden').remove();
}

Forbidden.prototype.addContextButtons=  function() {
        $("#contextbuttons").html("");
        $("#item_menu>div").each(function() {
            if ($(this).data("ctx")) {
                var a = $(this).data("ctx").split(";");
                if (jQuery.inArray('all', a) > -1 || jQuery.inArray('forbidden', a) > -1) {
                    $("#contextbuttons").append($(this).clone());
                }
                
                if (jQuery.inArray('selected', a) > -1) {
                    if (K5.gui.clipboard.isCurrentSelected()) {
                        $("#contextbuttons").append($(this).clone());
                    }
                }

                if (jQuery.inArray('notselected', a) > -1) {
                    if (!K5.gui.clipboard.isCurrentSelected()) {
                        $("#contextbuttons").append($(this).clone());
                    }
                }

                // only clipboard
                if (jQuery.inArray('clipboardnotempty', a) > -1) {
                    if (K5.gui.clipboard.getSelected().length > 0) {
                        $("#contextbuttons").append($(this).clone());
                    }
                }
            }
        });

        if (!K5.gui["selected"].hasParent()) {
            $("#contextbuttons>div.parent").hide();
        }
}

