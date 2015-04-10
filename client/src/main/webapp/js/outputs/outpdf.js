function PDFSupport(application){
    this.application = application;
}

PDFSupport.prototype= {
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
            var itm = K5.api.ctx.item[selected];
            window.open("pdfforward/pdf/selection?pids="+ selected,"_blank");
        }, 

        asyncTitle:function(pid) {
            var itm = K5.api.ctx.item[pid];
            var children = itm.children;

            
            var pages = _.reduce(children, function(memo, value, index) {
                if (value["model"] === "page") {
                    memo.push(value);
                }
                return memo;
            }, []);

            
            if (this.ctx.configuration.pdf["limit"] && this.ctx.configuration.pdf["limit"] >=-1) {
                var number = Math.min(this.ctx.configuration.pdf.limit-1,pages.length);
                $(".opacityloading").show();
                $.getJSON("pdfforward/asyncpdf/parent?pid="+ pid+"&number="+number, _.bind(function(data) {
                    $(".opacityloading").hide();
                    var handle = data["handle"];
                    window.open("pdfforward/asyncpdf/handle?handle="+ handle,"_blank");
                    $("body").css("cursor", "default");
                }, this)).error(function(jqXHR, textStatus, errorThrown) {
                    $(".opacityloading").hide();
                    if (jqXHR.status === 400) {
                        function _message(cont) {
                            function _waitheader() {
                                var head = $("<div/>",{"class":"pdfbusy_head"});

                                var h2  = $("<h2/>");
                                h2.html(K5.i18n.translatable('generatepdf.busy.title'));
                                head.append(h2);
                                return head;
                            }
                            function _waitmessage() {
                                var message = $("<div/>",{"class":"pdfbusy_message"});
                                var span  = $("<span/>");
                                span.html(K5.i18n.translatable('generatepdf.busy.message'));
                                message.append(span);
                                return message;
                            }
                            
                            var m = $("<div/>",{"class":"pdfbusy_cont"});
                            m.append(_waitheader());
                            m.append(_waitmessage());

                            cont.append(m);
                        }

                        K5.gui.selected.messages.close();
                        K5.gui.selected.messages.open(_message);
                    } else if (jqXHR.status === 404) {
                        
                    } else {
                        console.log("error");
                    }
                    $("body").css("cursor", "default");
                });
            } else {
                $(".opacityloading").show();
                $.getJSON("pdfforward/asyncpdf/parent?pid="+ pid+"&number="+pages.length, _.bind(function(data) {
                    var handle = data["handle"];
                    window.open("pdfforward/asyncpdf/handle?handle="+ handle,"_blank");
                    $("body").css("cursor", "default");
                    $(".opacityloading").hide();
                }, this)).error(function(jqXHR, textStatus, errorThrown) {
                    $(".opacityloading").hide();
                    if (jqXHR.status === 400) {
			
                        function _message(cont) {
                            function _waitheader() {
                                var head = $("<div/>",{"class":"pdfbusy_head"});
                                var h2  = $("<h2/>");
                                h2.html(K5.i18n.translatable('generatepdf.busy.title'));
                                head.append(h2);
                                return head;
                            }
                            function _waitmessage() {
                                var message = $("<div/>",{"class":"pdfbusy_message"});
                                var span  = $("<span/>");
                                span.html(K5.i18n.translatable('generatepdf.busy.message'));
                                message.append(span);
                                return message;
                            }
                            
                            var m = $("<div/>",{"class":"pdfbusy_cont"});
                            m.append(_waitheader());
                            m.append(_waitmessage());

                            cont.append(m);
                        }

                        K5.gui.selected.messages.close();
                        K5.gui.selected.messages.open(_message);
			
                    } else if (jqXHR.status === 404) {
                        
                    } else {
                        console.log("error");
                    }
		    	

                });

                function _message(cont) {
                    function _waitheader() {
                        var head = $("<div/>",{"class":"pdfwait_head"});

                        var h2  = $("<h2/>");
                        h2.html(K5.i18n.translatable('generatepdf.wait.title'));
                        head.append(h2);
                        return head;
                    }
                    function _waitmessage() {
                        var message = $("<div/>",{"class":"pdfwait_message"});
                        var span  = $("<span/>");
                        span.html(K5.i18n.translatable('generatepdf.wait.message'));
                        message.append(span);
                        return message;
                    }
                    
                    var m = $("<div/>",{"class":"pdfwait_cont"});
                    m.append(_waitheader());
                    m.append(_waitmessage());

                    cont.append(m);
                }

            }
            
        },
        
        title: function(pid) {
            var itm = K5.api.ctx.item[pid];
            var children = itm.children;

            var pages = _.reduce(children, function(memo, value, index) {
                if (value["model"] === "page") {
                    memo.push(value);
                }
                return memo;
            }, []);

            if (this.ctx.configuration.pdf["limit"] && this.ctx.configuration.pdf["limit"] >=-1) {
                var number = Math.min(this.ctx.configuration.pdf.limit,pages.length);
                // safra.. jak na to ??
                window.open("pdfforward/pdf/parent?pid="+ pid+"&number="+number,"_blank");
            } else {
                window.open("pdfforward/pdf/parent?pid="+ pid+"&number="+pages.length,"_blank");
            }
        },
        

        
        
        part: function() {
            function ntab(url) {
                var win = window.open(url, '_blank');
                win.focus();
            }
            var sel = K5.api.ctx.item.selected;
            var itm = K5.api.ctx.item[sel];
            ntab("?page=printpart&item="+itm.pid);
        }
}

