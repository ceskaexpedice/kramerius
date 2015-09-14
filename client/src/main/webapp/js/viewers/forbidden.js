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
           $('#viewer>div.container>div.ol').html('<div id=\"forbidden\"  style=\"width: 100%; height: 100%;vertical-align: middle; text-align:center\" >'+text+'</div>');
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



Forbidden.prototype.containsLeftStructure = function() {
    return true;
}

Forbidden.prototype.clearContainer = function() {
        $('#forbidden').remove();
}


Forbidden.prototype.addContextButtons=  function() {
    _ctxbuttonsrefresh();
}

