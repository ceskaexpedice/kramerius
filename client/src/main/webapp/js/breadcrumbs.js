K5.eventsHandler.addHandler(function(type, data) {
    if($('#viewer>div.breadcrumbs').length === 0){
        // No breadcrumbs
        return;
    }else{
        K5.gui["breadcrumbs"] = new BreadCrumbs();
    }
    if (type === "window/resized") {
        K5.gui["breadcrumbs"].resized();
    }
    
    var splitted = type.split("/");
    if (splitted.length === 3) {
        //api/item/
        if ((splitted[0] === "api") && (splitted[1] === "item")) {
            var pid = splitted[2];
            delayedEvent.pid=pid;    
            if (K5.initialized && K5.i18n.ctx.dictionary) {
                K5.gui["breadcrumbs"].refresh(pid);
                delayedEvent.enabled = false;
            } else {
                delayedEvent.enabled = true;
            }
        }
    }
    if (type === "i18n/dictionary ") {
        if (K5.initialized) {
            if ((delayedEvent.enabled)  && (K5.api.ctx["item"] && K5.api.ctx["item"][delayedEvent.pid])) {
                K5.gui["breadcrumbs"].refresh(delayedEvent.pid);
            }
        }
    }
    if (type === "application/init/end") {
        if (K5.i18n.ctx.dictionary) {
            if ((delayedEvent.enabled)  && (K5.api.ctx["item"] && K5.api.ctx["item"][delayedEvent.pid])) {
                K5.gui["breadcrumbs"].refresh(delayedEvent.pid);
            }
        }
    }
    

});
    
function BreadCrumbs(appl, elem) {
    this.application = (appl || K5);

    var jqSel = (elem || '#viewer>div.breadcrumbs');        
    this.elem = $(jqSel);

    this.init();
    
    
}

BreadCrumbs.prototype = {
    init: function() {

    },
    refresh: function(pid){
        this.elem.html("");
        var item = K5.api.ctx["item"][pid];
        var hash = hashParser();
        var idx = 0;
        if(hash.hasOwnProperty("pmodel") && item.context.length > 1){
            for(var c=0; c<item.context.length; c++){
                var ctx = item.context[c];
                if(ctx.length>1 && ctx[ctx.length-2].model === hash.pmodel){
                    idx = c;
                    break;
                }
            }
        }
        
        var item_ctx = item.context[idx];
        for(var i=0; i<item_ctx.length; i++){
            if(i > 0){
                this.elem.append('<div class="ui-icon ui-icon-triangle-1-e"> :: </div>');
            }
            var cpid = item_ctx[i].pid;
            var span = $('<div/>', {id: "bc_"+cpid});
            span.data("pid", cpid);
            span.data("model", item_ctx[i].model);
            if(cpid !== hash.pid){
                span.addClass('link')
                span.click(function(){
                    var hash2 = hashParser();
                    hash2.pid = $(this).data("pid");
                    hash2.pmodel = $(this).data("model");
                    hash2.hist = getHistoryDeep() + 1;
                    K5.api.gotoDisplayingItemPage(jsonToHash(hash2), $("#q").val());
                });
            }
            this.elem.append(span);
            
            if(K5.api.ctx["item"][cpid]){
                var info = {short: "", full: ""};
                K5.proccessDetails(K5.api.ctx["item"][cpid], info);
                $(jq("bc_"+cpid)).append(info.min);
            }else{
		var info = {short: "", full: ""};
                K5.api.askForItemContextData(cpid, _.partial(function(p, data){
		  K5.proccessDetails(data, info);
		  $(jq("bc_"+p)).append(info.min);
		}, cpid));
            }
        }
    },
    resized: function(){
        
    }
};