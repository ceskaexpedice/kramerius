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
        
        K5.eventsHandler.trigger("application/menu/ctxchanged", null);
}

Forbidden.prototype.clearContainer = function() {
        $('#forbidden').remove();
}

Forbidden.prototype.addContextButtons=  function() {
    $("#contextbuttons").html("");
    $("#item_menu>div")
            .each(
                    function() {
                        if ($(this).data("ctx")) {
                            var a = $(this).data("ctx").split(";");
                            if (viewer) {
                                if (jQuery.inArray(viewer, a) > -1) {
                                    $("#contextbuttons").append($(this).clone());
                                }
                            }

                            // all context
                            if (jQuery.inArray('all', a) > -1) {
                                $("#contextbuttons").append($(this).clone());
                            }
                           

                            // next context
                            if (jQuery.inArray('next', a) > -1) {
                                if (K5.api.ctx["item"][selected]["siblings"]) {
                                    var data = K5.api.ctx["item"][selected]["siblings"];
                                    var arr = data[0]['siblings'];
                                    var index = _.reduce(arr, function(memo,
                                            value, index) {
                                        return (value.selected) ? index : memo;
                                    }, -1);
                                    if (index < arr.length - 1) {
                                        $("#contextbuttons").append(
                                                $(this).clone());
                                    }
                                }
                            }

                            // prev context
                            if (jQuery.inArray('prev', a) > -1) {
                                if (K5.api.ctx["item"][selected]["siblings"]) {
                                    var data = K5.api.ctx["item"][selected]["siblings"];
                                    var arr = data[0]['siblings'];
                                    var index = _.reduce(arr, function(memo,
                                            value, index) {
                                        return (value.selected) ? index : memo;
                                    }, -1);
                                    if (index > 0) {
                                        $("#contextbuttons").append(
                                                $(this).clone());
                                    }
                                }
                            }

                            if (jQuery.inArray('parent', a) > -1) {
                                var pid = K5.api.ctx["item"]["selected"];
                                var data = K5.api.ctx["item"][pid];
                                var itemContext = data.context[0]; // jinak?
                                if (itemContext.length > 1) {
                                    $("#contextbuttons").append($(this).clone());
                                }
                            }
                        }
                    });
}

