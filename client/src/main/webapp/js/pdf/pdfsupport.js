function PDFSupport(application){
    this.application = application;
}

PDFSupport.prototype= {
        ctx:{},

        /** tests if given key is present in the context */
        isKeyReady: function(keys) {
                    return lookUpKey(keys, this.ctx);
        },

        initConfiguration: function(data) {
            this.ctx.configuration["pdf"]=configuration;
        },

        pdfPage:function(pid) {
            var selected = K5.api.ctx.item.selected; 
            var itm = K5.api.ctx.item[selected];
            window.open("api/pdf/selection?pids="+ selected,"_blank");
        }, 

        pdfTitle: function(pid, number) {
            var selected = K5.api.ctx.item.selected; 
            var itm = K5.api.ctx.item[selected];
            var children = itm.children;
            // safra.. jak na to ??
            window.open("api/pdf/parent?pid="+ selected+"&number="+children.length,"_blank");
        },
        

        printPart: function() {
            function ntab(url) {
                var win = window.open(url, '_blank');
                win.focus();
            }
            var sel = K5.api.ctx.item.selected;
            var itm = K5.api.ctx.item[sel];
            ntab("?page=printpart&item="+itm.pid);
        }
}

    
