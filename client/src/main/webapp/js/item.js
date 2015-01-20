
/**
 * Register listener -> Create viewer 
 */
var delayedEvent = {'pid':'','enabled':true};
K5.eventsHandler.addHandler(function(type, configuration) {
    var splitted = type.split("/");
    if (splitted.length === 3) {
        //api/item/
        if ((splitted[0] === "api") && (splitted[1] === "item")) {
            var pid = splitted[2];
            delayedEvent.pid=pid;    
            if (K5.initialized) {
                _eventProcess(pid);
                delayedEvent.enabled = false;
            } else {
                delayedEvent.enabled = true;
            }
        }
    }
    if (type === "application/init/end") {
        if ((delayedEvent.enabled)  && (K5.api.ctx["item"] && K5.api.ctx["item"][delayedEvent.pid])) {
                _eventProcess(delayedEvent.pid);
        }
    }
    if (type === "widow/url/hash") {
        if (K5.gui.page && K5.gui.page ==="doc") {
            var phash = location.hash;
            var pid = phash.startsWith("#!") ? phash.substring(2) : phash.substring(1);
            if (K5.api.ctx.item && K5.api.ctx.item[pid]) {
                if (K5.api.ctx.item[pid].pid) {
                        var data = K5.api.ctx.item[pid];
                        K5.eventsHandler.trigger("api/item/" + pid, data);
                } else {
                        K5.api.askForItem(pid);
                }       
            } else {
                K5.api.askForItem(pid);
            }
        }
    }
        
    if (type === "application/keys/left") {
        K5.gui["selected"].prev();
    }

    if (type === "application/keys/right") {
        K5.gui["selected"].next();
    }

    // changes in context buttons
    if (type === "application/menu/ctxchanged") {
        K5.gui["selected"].addContextButtons();
    }

});

//var phash = location.hash;
var phash = location.hash;
var pid = phash.startsWith("#!") ? phash.substring(2) : phash.substring(1);
if (pid) K5.api.askForItem(pid);

var maxwidth = $('html').css('max-width');

var w = (window.innerWidth > 0) ? window.innerWidth : screen.width;
//K5.serverLog("window width :"+w);

function _eventProcess(pid) {

    var data = K5.api.ctx["item"][pid];
    var viewer = K5.gui["viewers"].select(data);

    K5.api.ctx["item"]["selected"] = pid;
    if (K5.gui.selected) {
        K5.gui.selected.clearContainer();
        if (K5.gui.selected.download) {
            K5.gui.selected.download.cleanDialog();
        }
    }

    var okfunc = _.bind(function() {
        var instance = K5.gui["viewers"].instantiate(viewer.object);        
        K5.gui["selected"] = mixInto(new ItemSupport(K5), instance);
        K5.gui["selected"].initItemSupport();
        K5.gui["selected"].open();
        //K5.gui["selected"].ctxMenu();    

        K5.gui["selected"]["ctx"] = {};    

        //_metadatainit();


        K5.gui.selected["disabledDisplay"] = false;
    });
    var failfunc = _.bind(function() {
        var nviewer = K5.gui["viewers"].findByName('forbidden');
        var instance = K5.gui["viewers"].instantiate(nviewer.object);        

        K5.api.ctx["item"][pid]['forbidden'] = true;

        K5.gui["selected"] = mixInto(new ItemSupport(K5), instance);
        K5.gui["selected"].initItemSupport();
        K5.gui["selected"].open();

        // initialization
        //K5.gui["selected"].ctxMenu();    
        
        //_metadatainit();

        K5.gui.selected["disabledDisplay"] = false;
    });

    K5.gui["viewers"].forbiddenCheck(viewer.object,okfunc,failfunc); 
        

    function _metadatainit() {
            // metadata initialization 
            $("#metadata").hide();
            if (data.model === "page") {
                $("#model").show();
                $("#title").show();
                $("#root_title").hide();
            } else {
                $.get("metadata?pid=" + pid + "&model=" + K5.api.ctx.item[pid].model, _.bind(function(data) {
                    $("#model").hide();
                    $("#title").hide();
                    $("#root_title").hide();
                    $("#metadata").html(data);
                    $(".infobox .label").each(function(index, val) {
                        var txt = $(val).text();
                        txt = txt.trim();
                        if (txt.indexOf(":") === 0) {
                            $(val).text('');
                        }
                    });
                   $(".infobox .label").each(function(index, val) {
                        var valueText = $(val).siblings(".value").text();
                        valueText = valueText.trim();
                        if ("" === valueText) {
                            $(val).siblings(".value").remove();
                            $(val).remove();
                        }
                    });
                    $("#metadata").show();
                }, this));
            }
    }    
        
}


/**
 * Basic item support. <br> Instance is mixed with concrete implementation ( {@link Zoomify},{@link ZoomifyStaticImage})  and it is accessible via property K5.gui.selected <br>
 * @constructor
 * @param {Application} application - The application instance  {@link Application}.
 */
function ItemSupport(application) {
    this.application = application;
}


ItemSupport.prototype = {
    initItemSupport: function() {
        if (this.application.i18n.ctx && this.application.i18n.ctx.dictionary) {
            this._initInfo();

            // ?? reorganizovat?
            this.download= new DownloadItem();
            this.download.init();
            
            this.messages = new Messages();
            this.messages.init();
            
            this.shares = new ShareItem();
            this.shares.init();
            
            
        } else {
            this.application.eventsHandler.addHandler(_.bind(function(type, configuration) {
                console.log("event type " + type);
                if (type === "i18n/dictionary") {
                    this._initInfo();
                }
            }, this));

            // ?? reorganizovat?
            this.download = new DownloadItem();
            this.download.init();

            this.messages = new Messages();
            this.messages.init();

            this.shares = new ShareItem();
            this.shares.init();

        }
    },

    _initInfo: function() {
        var pid = K5.api.ctx["item"]["selected"];
        var root_title = K5.api.ctx["item"][pid].root_title;
        $(document).prop('title', K5.i18n.ctx.dictionary['application.title'] + ". " + root_title);
        this.renderContext();
    },
    
    addContextButtons: function() {
        _ctxbuttonsrefresh();
    },

   /**
    * Render ctx menu 
    * @method
    */     
   ctxMenu: function() {
        $("#acts_container").empty();
        var menuDiv = $("<div/>", {'id': 'ctxmenu'});
        var ul = $('<ul/>');
        var items = _.map(K5.gui.nmenu.ctx.actions, function(a) {
                var li = $('<li/>', {'id': 'ctxmenu-'+a.name});
                var item = $('<a/>', {'href': 'javascript:K5.gui.nmenu.action("' + a.name+'")', 'data-key': a.i18nkey});
                item.addClass("translate");
                li.append(item);
                return li;
        });

        _.each(items, function(itm) {
            if (itm !== null) ul.append(itm);
        });
        menuDiv.append(ul);
        $("#acts_container").append(menuDiv);
        if (K5.i18n.ctx.dictionary) {
                K5.i18n.k5translate(menuDiv);
        }
    },
    renderModsXml: function(elem, pid, data){
        var modsid = "mods_"+pid;
        var e = $('<div class="modsxml"></div>');
        //elem.append(e);
        
        var div = $('<div style="display:block;" />');
        div.attr("id", modsid);
        
        div.append(e);
        $('#viewer').append(div);
         
        var modsXml = new ModsXml(e);
        modsXml.loadXmlFromDocument(data, e);
        modsXml.renderAsHTML();
        modsXml.translateNodes();
        modsXml.compact();
        modsXml.showTranslated();
        elem.append(div);
        return modsXml;

    },
    biblioModsXml: function(elem, pid, model){
        var m = $('<div>', {class: "model"});
            
        m.html(K5.i18n.translatable("fedora.model." + model));
        $(elem).append(m);
        K5.api.askForItemConcreteStream(pid, "BIBLIO_MODS", _.bind(function(data) {
            //this.parseBiblioModsXml(elem, pid, data);
            var modsxml = this.renderModsXml(elem, pid, data);
            var b = $(elem).find("div.model");
            b.addClass("button");
            b.attr("data-id", pid);
            b.data("id", pid);
            b.append(' <xml>');
            b.attr('title', 'show mods');
            b.click(function(){
                modsxml.toggle();
            
            });
        }, this));
    },


    renderContext: function() {
        $(".context").remove();
        var pid = K5.api.ctx["item"]["selected"];
        var data = K5.api.ctx["item"][pid];

        this.itemContext = data.context[0];
        var contextDiv = $("<div/>", {class: "context"});
        contextDiv.append('<h2>' + K5.api.ctx["item"][pid]['root_title'] + '</h2>');
        for (var i = 0; i < this.itemContext.length; i++) {
            var p = this.itemContext[i].pid;
            var div = $('<div/>');
            
            this.biblioModsXml(div, p, this.itemContext[i].model);
            contextDiv.append(div);
        }
        contextDiv.insertBefore("#metadata");
        //contextDiv.insertBefore(".mtd_footer");
    },

    hidePages: function() {
        $("#itemparts").hide();
    },
    showItemNavigation: function() {
        this.hideInfo();
    },
    /**
     * Siblings request
     * @method
     */    
    siblings: function() {
        $("#itemparts").append("<div id='itempartssiblings' style='overflow:scroll; width:100%; height:40%; text-align:center'><h1>Siblings</h1></div>");

        var selected = K5.api.ctx["item"].selected;
        if (K5.api.ctx["item"] && K5.api.ctx["item"][selected] &&  K5.api.ctx["item"][selected]["siblings"]) {
            var arr = K5.api.ctx["item"][selected]["siblings"][0]['siblings'];
            var str = _.reduce(arr, function(memo, value, index) {
                var pid = value.pid;
                memo +=
                        "<div style='float:left'> <a href='?page=doc&pid=" + pid + "'> <img src='api/item/" + pid + "/thumb'/></a> </div>";
                return memo;
            }, "");

            $("#itempartssiblings").append(str + "<div style='clear:both'></div>");
        } else {
            K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                var arr = data[0]['siblings'];
                console.log('array length:' + arr);
                var str = _.reduce(arr, function(memo, value, index) {
                    var pid = value.pid;
                    memo +=
                            "<div style='float:left'> <a href='?page=doc&pid=" + pid + "'> <img src='api/item/" + pid + "/thumb'/></a> </div>";
                    return memo;
                }, "");
                $("#itempartssiblings").append(str + "<div style='clear:both'></div>");

            });
        }

    },
    /**
     * Children request
     * @method
     */
    children: function() {
        $("#itemparts").append("<div id='itempartschildren' style='overflow:scroll; width:100%; height:40%;text-align:center'><h1>Children</h1></div>");
        K5.api.askForItemChildren(K5.api.ctx["item"]["selected"], _.bind(function(data) {
            console.log("received data");
            var arr = data;
            var str = _.reduce(data, function(memo, value, index) {
                var pid = value.pid;
                memo +=
                        "<div style='float:left'> <a href='?page=doc&pid=" + pid + "'> <img src='api/item/" + pid + "/thumb'/></a> </div>";
                return memo;
            }, "");
            $("#itempartschildren").html(str + "<div style='clear:both'></div>");
        }, this));
    },
    /**
     * Returns true if the current item has parent
     * @method      
     */    
    hasParent: function() {
        var pid = K5.api.ctx["item"]["selected"];
        var data = K5.api.ctx["item"][pid];
        var itemContext = data.context[0];

        return (itemContext.length > 1);
    },
    /**
     * Returns parent pid
     * @method      
     */       
    parent: function() {
        cleanWindow();
        var pid = K5.api.ctx["item"]["selected"];
        var data = K5.api.ctx["item"][pid];
        var itemContext = data.context[0];
        
        if (this.itemContext.length > 1) {
            var parentPid = itemContext[itemContext.length - 2].pid;
            K5.api.gotoItemPage(parentPid, $("#q").val());
        }
    },
    /**
     * Next item
     * @method      
     */
    next: function() {

        cleanWindow();

        if (K5.api.isKeyReady("item/selected") && (K5.api.isKeyReady("item/" + K5.api.ctx.item.selected + "/siblings"))) {
            var data = K5.api.ctx["item"][ K5.api.ctx["item"]["selected"] ]["siblings"];
            var arr = data[0]['siblings'];
            var index = _.reduce(arr, function(memo, value, index) {
                return (value.selected) ? index : memo;
            }, -1);
            if (index <= arr.length - 2) {
                var nextPid = arr[index + 1].pid;
                K5.api.gotoItemPage(nextPid, $("#q").val());
            }
        } else {
            K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                    return (value.selected) ? index : memo;
                }, -1);
                if (index < arr.length - 2) {
                    var nextPid = arr[index + 1].pid;
                    K5.api.gotoItemPage(nextPid, $("#q").val());
                }
            });
        }
    },
    /**
     * Previous item
     * @method      
     */       
    prev: function() {

        cleanWindow();

        //this.clearContainer();
        if (K5.api.isKeyReady("item/selected") && (K5.api.isKeyReady("item/" + K5.api.ctx.item.selected + "/siblings"))) {
            var data = K5.api.ctx["item"][ K5.api.ctx["item"]["selected"] ]["siblings"];
            var arr = data[0]['siblings'];
            var index = _.reduce(arr, function(memo, value, index) {
                return (value.selected) ? index : memo;
            }, -1);
            if (index > 0) {
                var prevPid = arr[index - 1].pid;
                K5.api.gotoItemPage(prevPid, $("#q").val());
            }

        } else {
            K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                    return (value.selected) ? index : memo;
                }, -1);
                if (index > 0) {
                    var prevPid = arr[index - 1].pid;
                    K5.api.gotoItemPage(prevPid, $("#q").val());
                }
            });
        }
    },
        
    /**
     * Toggle info panel
     * @method      
     */       
    togglePin: function() {
        $("#viewer>div.info").toggleClass("pin");
    },

    /**
     * Hide info panel
     * @method      
     */       
    hideInfo: function() {
        this.hidePanel("#viewer>div.info", 290, -500, 200);
        $.cookie('item_showinfo', "false");
    },

    /**
     * Search inside document
     * @method      
     */       
    searchInside: function() {
        cleanWindow();

        $("#searchinside_q").val($("#q").val());

        divopen("#viewer>div.searchinside");
        $("#searchinside_q").focus();
        $("#searchinside_q").select();

        this._searchInsideArrow();
    },

    /**
     * Show info panel
     * @method      
     */       
    showInfo: function() {
        cleanWindow();
        divopen("#viewer>div.info");

        var metadataheight = $("#metadata").height();
        console.log("metadata height :"+metadataheight);

        var contextheight = $(".context").height();
        console.log("context height :"+contextheight);

        var titleheight = $("#title").height();
        var modelheight = $("#model").height();

        var nheight = metadataheight + 63 +contextheight+titleheight+modelheight ;

        $("#viewer .infobox").height(nheight);

        function triangle(nheight) {
                var v = $("#ctxmenu").height();     
                $("#viewer .actions").css("height",v + 55 +"px"); 
                var viewerWidth = $("#viewer").width();
                var offset = $("#vwr_ctx_metadata").offset();
                var actionsWidth = $("#vwr_ctx_metadata").width();
                var headerHeight = $("#header").height();
                var toffset = $("#mtd_footer_triangle").offset();
                var tw = $("#mtd_footer_triangle").width();
                var left = offset.left - (8) +(actionsWidth/2);
                var toff = {
                        "top":toffset.top,
                        "left":left
                };

                $("#mtd_footer_triangle").offset({"top":toff.top, "left":toff.left});                        
                return toff;
        } 

        triangle(nheight);
    },

    toggleHits: function() {
        $("li.ishit").toggleClass('hit');
        $("li.containhit").toggleClass('chit');
    },

    
    hidePanels: function(whenready) {
        if($("#viewer>div.infobox:visible").length===0){
            if (whenready) whenready.apply(null);
        }else{
            this.hidePanel("#viewer>div.infobox:visible", 290, -500, 200, whenready);
        }
    },

    _searchInsideArrow:function() {

        var offset = $("#vwr_ctx_searchinside").offset();
        var w = $("#vwr_ctx_searchinside").width();
        var toffset = $("#searchinside_triangle");
        var tw = $("#searchinside_triangle").width();
        var left = offset.left - (8) +(w/2);

        $("#searchinside_triangle").offset({"top":toffset.top, "left":left});                        

    },
   
    _actionsArrow:function() {
        function triangle() {
                var v = $("#ctxmenu").height();     
                
                $("#viewer .actions").css("height",v + 55 +"px"); 

                var offset = $("#vwr_ctx_actions").offset();
                var actionsWidth = $("#vwr_ctx_actions").width();


                var toffset = $("#acts_footer_triangle").offset();
                var tw = $("#acts_footer_triangle").width();
                var left = offset.left - (8) +(actionsWidth/2);

                var toff = {
                        "top":toffset.top,
                        "left":left
                };

                $("#acts_footer_triangle").offset({"top":toff.top, "left":toff.left});                        
                return toff;
        } 
        triangle();
    },    
    
    
    /**
     * Show menu actions
     * @method      
     */       
    showActions: function() {
        cleanWindow();
        divopen("#viewer>div.actions");
        K5.gui.nmenu.refreshActions();
        this._actionsArrow();
    },

    hidePanel: function(panel, l, t, speed, whenready) {
        $(panel).animate({'opacity': '0.5', 'left': l, 'top': t}, speed, function() {
            $(panel).removeClass("showing");
            $(panel).hide();
            if (whenready) whenready.apply(null);
        });
    },

        
        
    showPanel: function(panel, l, t, speed) {
        if (!$(panel).hasClass("showing")) {
            $(panel).addClass("showing");
            $(panel).show();
            $(panel).animate({'opacity': '1.0', 'left': l, 'top': t}, speed);

            $(panel).animate({'opacity': '1.0', 'left': l, 'top': t}, speed);
        }
    },
    
    
    /** 
     * toggle actions 
     * @method
     */
    toggleInfo: function() {
        if (visible("#viewer>div.infobox")) { cleanWindow(); } 
        else { this.showInfo();  }
    }

};





