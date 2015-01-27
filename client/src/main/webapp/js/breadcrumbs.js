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
        //console.log(K5.gui["selected"]);
        //var pid = K5.api.ctx["item"]["selected"];
        var item = K5.api.ctx["item"][pid];
        var item_ctx = item.context[0];
        for(var i=0; i<item_ctx.length; i++){
            if(i > 0){
                this.elem.append('<div class="sep"> :: </div>');
            }
            var span = $('<div/>', {class: "link"});
            var cpid = item_ctx[i].pid;
            span.data("pid", cpid);
            
            if(K5.api.ctx["item"][cpid]){
                
                var info = {short: "", full: ""};
                K5.proccessDetails(K5.api.ctx["item"][cpid], info);
                span.append(info.min);
            }
            
            span.click(function(){
                var cpid = $(this).data("pid");
                K5.api.gotoItemPage(cpid, $("#q").val());
            });
            this.elem.append(span);
        }
    },
    resized: function(){
        
    }
};