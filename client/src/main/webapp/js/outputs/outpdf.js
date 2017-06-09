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
            //this.ctx.configuration["pdf"]=data;
        },

        page:function(pid) {
            var selected = K5.api.ctx.item.selected; 
            //var itm = K5.api.ctx.item[selected];
            var page = removeHistoryPostfix(K5.api.ctx.item.selected);
            window.open("pdfforward/pdf/selection?pids="+ page,"_blank");
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
                
                $.getJSON("pdfforward/pdf/parent?pid="+ pid+"&number="+number, _.bind(function(data) {
                    $(".opacityloading").hide();
                    var handle = data["handle"];
                    window.open("pdfforward/pdf/handle?handle="+ handle,"_blank");
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

		siblingspages:function(arrayofPids) {
            $.getJSON("api/pdf", _.bind(function(conf) {
                    if (!conf.resourceBusy) {
                        if (conf.pdfMaxRange === "unlimited") {
                        	var sel = K5.api.ctx.item.selected;
                        	var itm = K5.api.ctx.item[sel];
                        	if (itm) {
            					var arr = K5.api.ctx.item[sel].context[0];
            					var item = arr[arr.length-2].pid;
	                            window.open("pdfforward/pdf/parent?pid="+ item.pid,"_blank");
                        	} else {
	                        	K5.api.askForItem("api/item/"+arrayofPids[0], function(data) {
	            					var arr = data.context[0];
        	    					var item = arr[arr.length-2].pid;
		                            window.open("pdfforward/pdf/parent?pid="+ item.pid,"_blank");
        	                	});
                        	} 
                    	} else {
                            var string = _.reduce(arrayofPids, function(memo, value, ctx) {
                                if (ctx > 0) {
                                    memo = memo+",";
                                }
                                memo = memo +value;
                                return memo;
                            }, "");
                    	
                    	    window.open("pdfforward/pdf/selection?pids="+string,"_blank");
						}
					}				
            },this));
		},

        siblings: function(pid) {
            $.getJSON("api/pdf", _.bind(function(conf) {
                var itm = K5.api.ctx.item[pid];
                var sData = itm.siblings;
                if (sData.length > 0) {
                    var sPath = sData[0].path;

                    if (!conf.resourceBusy) {
                        var parent = sPath[sPath.length-2];
                        if (conf.pdfMaxRange === "unlimited") {
                        	
                            window.open("pdfforward/pdf/parent?pid="+ parent.pid,"_blank");
                        } else {
                            //var max = Math.min(pages.length, parseInt(conf.pdfMaxRange));
                            var pages = _.reduce(sData[0].siblings, function(memo, value, ctx) {
                                if (!memo.enabled) { memo.enabled = value.selected; }
                                
                                if (value["model"] === "page") {
                                    if (memo.enabled) {
                                        memo.data.push(value.pid);
                                    }
                                    if (memo.data.length >=  parseInt(conf.pdfMaxRange)) {
                                        memo.enabled = false;
                                    }
                                }
                                return memo;
                            }, {"data":[],"enabled":false});
                            
                            var string = _.reduce(pages.data, function(memo, value, ctx) {
                                if (ctx > 0) {
                                    memo = memo+",";
                                }
                                memo = memo +value;
                                return memo;
                            }, "");
                            
                            window.open("pdfforward/pdf/selection?pids="+string,"_blank");
                        }
                    } else {
                        // zobrazeni busy..
                    }
     
                    
                }
                /*
                var itm = K5.api.ctx.item[pid];
                var children = itm.children;

                var pages = _.reduce(children, function(memo, value, index) {
                    if (value["model"] === "page") {
                        memo.push(value);
                    }
                    return memo;
                }, []);

                if (!conf.resourceBusy) {
                    console.log("conf is :"+conf);
                    if (conf.pdfMaxRange === "unlimited") {
                        window.open("pdfforward/pdf/parent?pid="+ pid,"_blank");
                    } else {
                        var val = Math.min(pages.length, parseInt(conf.pdfMaxRange));
                        window.open("pdfforward/pdf/parent?pid="+ pid+"&number="+val,"_blank");
                    }
                } else {
                    // zobrazeni busy..
                }*/
                
            },this));
            
        },
        
        title: function(pid) {
            $.getJSON("api/pdf", _.bind(function(conf) {
                var itm = K5.api.ctx.item[pid];
                var children = itm.children;

                var pages = _.reduce(children, function(memo, value, index) {
                    if (value["model"] === "page") {
                        memo.push(value);
                    }
                    return memo;
                }, []);

                if (!conf.resourceBusy) {
                    console.log("conf is :"+conf);
                    if (conf.pdfMaxRange === "unlimited") {
                        window.open("pdfforward/pdf/parent?pid="+ pid,"_blank");
                    } else {
                        var val = Math.min(pages.length, parseInt(conf.pdfMaxRange));
                        window.open("pdfforward/pdf/parent?pid="+ pid+"&number="+val,"_blank");
                    }
                } else {
                    // zobrazeni busy..
                }
            },this));

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

