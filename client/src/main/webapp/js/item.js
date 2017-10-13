
/* global K5, _ */

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
            var pid = hashParser().pid;
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
      if($("#viewer>div.searchinside").is(":visible") || $("#q").is(":focus")){
        return;
      } else {
        configuration[0].preventDefault(); // prevent the default action (scroll / move caret)
        K5.gui["selected"].prev();
      }
    }

    if (type === "application/keys/right") {
      if($("#viewer>div.searchinside").is(":visible") || $("#q").is(":focus")){
        return;
      } else {
        configuration[0].preventDefault(); // prevent the default action (scroll / move caret)
        K5.gui["selected"].next();
      }
    }

    if (type === "window/resized") {
        if (K5.gui["selected"]) {
            K5.gui["selected"].wresized();
        }
    }

    // changes in context buttons
    if (type === "application/menu/ctxchanged") {
        K5.gui["selected"].addContextButtons();
    }

});

//var phash = location.hash;
//var pid = phash.startsWith("#!") ? phash.substring(2) : phash.substring(1);
var pid = hashParser().pid;
if (pid) K5.api.askForItem(pid);

var maxwidth = $('html').css('max-width');

var w = (window.innerWidth > 0) ? window.innerWidth : screen.width;

function _eventProcess(pid) {

    var data = K5.api.ctx["item"][pid];
    var viewer = K5.gui["viewers"].select(data);

    
    
    K5.api.ctx["item"]["selected"] = pid;
    if (K5.gui.hasOwnProperty('selected') && K5.gui.selected !== null) {
        K5.gui.selected.clearContainer();
        if (K5.gui.selected.download) {
            K5.gui.selected.download.cleanDialog();
        }
        $("#viewer>div.container>div.loading").show();
    }

    var okfunc = _.bind(function() {
        
        $("#viewer>div.container>div.loading").hide();

        var instance = K5.gui["viewers"].instantiate(viewer.object);        
        K5.gui["selected"] = mixInto(new ItemSupport(K5), instance);
        K5.gui["selected"].initItemSupport();
        K5.gui["selected"].open();

        K5.gui["selected"]["ctx"] = {};    

        if (K5.gui["selected"].containsLeftStructure && K5.gui["selected"].containsLeftStructure()) {
                        
            if(typeof K5.gui["selected-left"] != 'undefined'){
                if (K5.gui["selected"].leftStructureSettings) {
                    K5.gui["selected-left"].setSettings( K5.gui["selected"].leftStructureSettings());
                }
                K5.gui["selected-left"].process();
            }else{
                if (K5.gui["selected"].leftStructureSettings) {
                    K5.gui["selected-left"] = new LeftThumbs(K5, '#viewer>div.container>div.thumbs',K5.gui["selected"].leftStructureSettings());
                } else {
                    K5.gui["selected-left"] = new LeftThumbs(K5, '#viewer>div.container>div.thumbs');
                }
            }
                 
            //K5.gui["selected-left"].init();
        }

        K5.gui.selected["disabledDisplay"] = false;
    });
    var failfunc = _.bind(function() {

        $("#viewer>div.container>div.loading").hide();

        var nviewer = K5.gui["viewers"].findByName('forbidden');
        var instance = K5.gui["viewers"].instantiate(nviewer.object);        

        K5.api.ctx["item"][pid]['forbidden'] = true;

        K5.gui["selected"] = mixInto(new ItemSupport(K5), instance);
        K5.gui["selected"].initItemSupport();
        K5.gui["selected"].open();
        
        if (K5.gui["selected"].containsLeftStructure && K5.gui["selected"].containsLeftStructure()) {
            if(typeof K5.gui["selected-left"] != 'undefined'){
                K5.gui["selected-left"].process();   
            }else{
                K5.gui["selected-left"] =  new LeftThumbs();   
            }
            //K5.gui["selected-left"].init();
        }
        
        K5.gui.selected["disabledDisplay"] = true;
    });

    K5.gui["viewers"].forbiddenCheck(viewer.object,okfunc,failfunc); 
    


    
    
    
    //thumbViewer.open();
    
    function _metadatainit() {
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
                if (type === "i18n/dictionary") {
                    this._initInfo();

                    this.download = new DownloadItem();
                    this.download.init();

                    this.messages = new Messages();
                    this.messages.init();

                    this.shares = new ShareItem();
                    this.shares.init();
                }
            }, this));


        }
    },

    _initInfo: function() {
        var pid = K5.api.ctx["item"]["selected"];
        var root_title = K5.api.ctx["item"][pid].root_title;
        $(document).prop('title', K5.i18n.ctx.dictionary['application.title'] + ". " + root_title);
        this.renderContext();
    },
    
    maximize:function(){

        if(K5.gui["maximized"]){
            this.restore();
        }else{
        	$("#metadata").hide();
            $(".thumbs").hide();
            $("#viewer>div.breadcrumbs").hide();
            $("#viewer>div.container").css("width", "100%");
            $("#viewer div.ol").css("width", "100%");
            K5.gui["maximized"] = true;
            this.clearContainer();
            this.open();
        }
    },
    
    restore:function(){
        //$("#header").show();
        $("#metadata").show();
        $(".thumbs").show();
        $("#viewer>div.breadcrumbs").show();
        $("#viewer>div.container").css("width", "calc(100% - 340px)");
        $("#viewer div.ol").css("width", "calc(100% - 350px)");
        K5.gui["maximized"] = false;
        this.clearContainer();
        this.open();
    },
    
    addContextButtons: function() {
        _ctxbuttonsrefresh();
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
        $("#metadata>div.full").empty();
        var pid = K5.api.ctx["item"]["selected"];
        var data = K5.api.ctx["item"][pid];

        this.itemContext = data.context.sort(function (a, b) { return b.length - a.length; })[0];

        var contextDiv = $("<div/>", {class: "context"});
        var titleH = $('<h2>' + K5.api.ctx["item"][pid]['root_title'] + '</h2>');
        
        var model = K5.api.ctx["item"][pid]['model'];
        model = K5.i18n.ctx.dictionary["fedora.model." + model];
        $('.mtd_footer .prev').attr('title', K5.i18n.ctx.dictionary["buttons.prev"] + " " + model);
        $('.mtd_footer .prev').data('key', "buttons.prev");
        $('.mtd_footer .next').attr('title', K5.i18n.ctx.dictionary["buttons.next"] + " " + model);
        $('.mtd_footer .next').data('key', "buttons.next");
        
        //contextDiv.append('<h2>' + K5.api.ctx["item"][pid]['root_title'] + '</h2>');
        for (var i = 0; i < this.itemContext.length; i++) {
            var p = this.itemContext[i].pid;
            var div = $('<div/>');
            
            this.biblioModsXml(div, p, this.itemContext[i].model);
            contextDiv.append(div);
            
        }
        $("#metadata>div.full").append(titleH);
        $("#metadata>div.full").append(contextDiv);
        this.renderDonator();
        contextDiv.height($("#metadata>div.full").height() - titleH.height() - 10);
    },

    renderDonator: function(){
        var pid = K5.api.ctx["item"]["selected"];
        var data = K5.api.ctx["item"][pid];
        if(data.hasOwnProperty("donator")){
            var donatorDiv = $("<div/>", {class: "donator"});
            donatorDiv.append('<img src="api/item/'+data.donator+'/streams/LOGO"/>');
            $(".mtd_footer dialogs_footer").prepend(donatorDiv);
        }
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
     * Process resize event
     * @method      
     */       
    wresized: function(){
        var contextDiv = $("#metadata>div.full>div.context");
        var titleH = $("#metadata>div.full>h2");
        contextDiv.height($("#metadata>div.full").height() - titleH.height() - 10);
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
            var hash = hashParser();
            hash.pid = parentPid;
            var histDeep = getHistoryDeep() + 1;
            hash.hist = histDeep;
            K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
            
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
                var hash = hashParser();
                hash.pid = nextPid;
                var histDeep = getHistoryDeep() + 1;
                hash.hist = histDeep;
                K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
            }
        } else {
            K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                    return (value.selected) ? index : memo;
                }, -1);
                if (index < arr.length - 2) {
                    var nextPid = arr[index + 1].pid;
                    var hash = hashParser();
                    hash.pid = nextPid;
                    var histDeep = getHistoryDeep() + 1;
                    hash.hist = histDeep;
                    K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
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
                var hash = hashParser();
                hash.pid = prevPid;
                var histDeep = getHistoryDeep() + 1;
                hash.hist = histDeep;
                K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
            }

        } else {
            K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                    return (value.selected) ? index : memo;
                }, -1);
                if (index > 0) {
                    var prevPid = arr[index - 1].pid;
                    var hash = hashParser();
                    hash.pid = prevPid;
                    var histDeep = getHistoryDeep() + 1;
                    hash.hist = histDeep;
                    K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
                }
            });
        }
    },
    toggleView: function(){
        $("#viewer .thumb img").toggle();
        $("svg.toggle .line").toggle();
        $("svg.toggle .img").toggle();
    },
    toogleModsView: function(){
        $("div.modsxml li.node").toggleClass("fullmods");
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
    dosearch: function() {
        var q = $("#searchinside_q").val();
        K5.eventsHandler.trigger("app/searchInside", q);
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





