function PrintSupport(application){
    this.application = application;
}

PrintSupport.prototype= {
        ctx:{
            "configuration":{}
        },

        /** tests if given key is present in the context */
        isKeyReady: function(keys) {
                    return lookUpKey(keys, this.ctx);
        },
        
        isLimitDefined:function() {
            return (this.ctx.configuration["pdf"] && this.ctx.configuration["pdf"]["limit"]);
        },
        
        limit:function() {
            return this.ctx.configuration["pdf"]["limit"];
        },
        
        initConfiguration: function(data) {
            this.ctx.configuration["pdf"]=data;
        },

        page:function(pid) {
            var selected = K5.api.ctx.item.selected; 
            window.open('print?pids='+selected,'_blank');
        }, 

        
        title: function(pid) {
            window.open('print?parentPid='+pid,'_blank');
        }
}