/**
 * Manipulating with Remote API v5.0
 * 
 * K5 client api stub
 * 
 * @constructor
 * @param {Application}
 *            application - The application instance {@link Application}.
 */
function ClientAPIDev(application) {
    this.application = application;
}

ClientAPIDev.prototype = {

    /**
     * Contains context informations
     * 
     * @member
     */
    ctx : {},

    /**
     * Tests if given key is present in the context
     * 
     * @method
     */
    isKeyReady : function(keys) {
        return lookUpKey(keys, this.ctx);
    },

    askForCool : function(whenready) {
        $.getJSON("api/feed/custom", _.bind(function(data) {
            if (!this.isKeyReady("feed")) {
                this.ctx["feed"] = {};
            }
            this.ctx["feed"]["cool"] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/feed/cool", data);
        }, this));
    },

    /**
     * Requests for latest
     * 
     * @method
     */
    askForLatest : function(whenready) {
        $.getJSON("api/feed/newest", _.bind(function(data) {
            if (!this.isKeyReady("feed")) {
                this.ctx["feed"] = {};
            }
            this.ctx["feed"]["newest"] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/feed/newest", data);
        }, this));
    },

    /**
     * Request for mostdesirable
     * 
     * @method
     */
    askForPopular : function(whenready) {
        $.getJSON("api/feed/mostdesirable", _.bind(function(data) {
            if (!this.isKeyReady("feed")) {
                this.ctx["feed"] = {};
            }
            this.ctx["feed"]["mostdesirable"] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/feed/mostdesirable",
                    data);
        }, this));
    },

    /**
     * Request for virtual collections
     * 
     * @method
     */
    askForCollections : function(whenready) {
        $.getJSON("api/vc", _.bind(function(data) {
            var collections = {};
            for (var i = 0; i < data.length; i++) {
                var pid = data[i].pid;
                collections[pid] = {
                    "cs" : data[i].descs.cs,
                    "en" : data[i].descs.en
                };
            }
            this.ctx["vc"] = collections;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/vc", data);
        }, this));
    },

    /**
     * Search request
     * 
     * @method
     */
    askForSolr : function(query, whenready) {
        // "?fl=dc.creator,dc.title,PID,dostupnost&fq=" + URLEncoder.encode(fq,
        // "UTF-8") + "&q=rok:" + year + "&start=" + offset + "&rows=" + rows;
        $.getJSON("api/search?" + query, _.bind(function(data) {
            this.ctx["solr"] = {};
            this.ctx["solr"][query] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/solr?" + query, data);
        }, this));
    },

    /**
     * Search terms request
     * 
     * @method
     */
    askForTerms : function(query, whenready) {
        // "?fl=dc.creator,dc.title,PID,dostupnost&fq=" + URLEncoder.encode(fq,
        // "UTF-8") + "&q=rok:" + year + "&start=" + offset + "&rows=" + rows;
        $.getJSON("api/search/terms?" + query, _.bind(function(data) {
            this.ctx["solr"] = {};
            this.ctx["solr"][query] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/terms?" + query, data);
        }, this));
    },

    /**
     * Requests for item
     * 
     * @method
     */
    askForItem : function(pid, whenready) {
        $.getJSON("api/item/" + pid, _.bind(function(data) {
            if (!this.isKeyReady("item")) {
                this.ctx["item"] = {};
            }
            if (!this.isKeyReady("callhistory")) {
                this.ctx["callhistory"] = [];
            }
            this.ctx["item"][pid] = data;

            // combine data with streams
            $.getJSON("api/item/" + pid + "/streams", _.bind(
                    function(sdata) {
                        this.ctx["item"][pid]["streams"] = sdata;
                        this.ctx.callhistory.push(pid);
                        if (whenready)
                            whenready.apply(null, [ data ]);
                        this.application.eventsHandler.trigger("api/item/"
                                + pid, data);
                    }, this));

        }, this));
    },

    /**
     * Requests for item streams
     * 
     * @method
     */
    askForItemStreams : function(pid, whenready) {
        $.getJSON("api/item/" + pid + "/streams", _.bind(function(data) {
            if (!this.isKeyReady("item")) {
                this.ctx["item"] = {};
            }
            if (!this.isKeyReady("item/" + pid)) {
                this.ctx["item"][pid] = {};
            }

            if (!this.isKeyReady("item/" + pid + "/streams")) {
                this.ctx["item"][pid]["streams"] = {};
            }
            this.ctx["item"][pid]["streams"] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/item/" + pid
                    + "/streams", data);
        }, this));
    },

    askForItemConcreteStream : function(pid, stream, whenready) {
        $.get("api/item/" + pid + "/streams/" + stream, _.bind(function(
                data) {
            if (!this.isKeyReady("item")) {
                this.ctx["item"] = {};
            }
            if (!this.isKeyReady("item/" + pid)) {
                this.ctx["item"][pid] = {};
            }

            if (!this.isKeyReady("item/" + pid + "/streams")) {
                this.ctx["item"][pid]["streams"] = {};
            }

            if (!this.isKeyReady("item/" + pid + "/streams/" + stream)) {
                this.ctx["item"][pid]["streams"][stream] = {};
            }
            this.ctx["item"][pid]["streams"][stream]["data"] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/item/" + pid
                    + "/streams/" + stream, data);
        }, this));
    },

    /**
     * Requests for item
     * 
     * @method
     */
    askForItemContextData : function(pid, whenready) {
        $.getJSON("api/item/" + pid, _.bind(function(data) {
            if (!this.isKeyReady("item")) {
                this.ctx["item"] = {};
            }
            if (!this.isKeyReady("callhistory")) {
                this.ctx["callhistory"] = [];
            }
            this.ctx["item"][pid] = data;
            this.ctx.callhistory.push(pid);
            if (whenready)
                whenready.apply(null, [ data ]);
        }, this));
    },

    /**
     * Requests for siblings
     * 
     * @method
     */
    askForItemSiblings : function(pid, whenready) {
        $.getJSON("api/item/" + pid + "/siblings", _.bind(function(data) {
            if (!this.isKeyReady("item")) {
                this.ctx["item"] = {};
            }
            if (!this.isKeyReady("item/" + pid)) {
                this.ctx["item"][pid] = {};
            }
            this.ctx["item"][pid]['siblings'] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/item/" + pid
                    + "/siblings", data);
        }, this));
    },

    /**
     * Requests for children
     * 
     * @method
     */
    askForItemChildren : function(pid, whenready) {
        $.getJSON("api/item/" + pid + "/children", _.bind(function(data) {
            if (!this.isKeyReady("item")) {
                this.ctx["item"] = {};
            }
            if (!this.isKeyReady("item/" + pid)) {
                this.ctx["item"][pid] = {};
            }
            this.ctx["item"][pid]['children'] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/item/" + pid
                    + "/children", data);
        }, this));
    },

    /** Requests for content in cache */
    askForCache : function(filename, whenready, onerror) {
        if (!this.isKeyReady("cache")) {
            this.ctx["cache"] = {};
        }
        $.get(
                "cache?action=get&f=" + filename,
                _.bind(function(data) {
                    if (!this.isKeyReady("cache/" + filename)) {
                        this.ctx["cache"][filename] = {};
                    }
                    this.ctx["cache"][filename] = data;
                    if (whenready)
                        whenready.apply(null, [ data ]);
                    this.application.eventsHandler.trigger("api/cache/"
                            + filename, data);
                }, this)).error(function(data) {
            console.log("error asking for cache: " + data);
            if (onerror)
                onerror.apply(null, [ data ]);
        });
    },

    /** Requests for save content into cache */
    saveToCache : function(filename, content, whenready) {
        $.post("cache", {
            action : "save",
            f : filename,
            c : content
        }, _.bind(function(data) {
            if (!this.isKeyReady("cache")) {
                this.ctx["cache"] = {};
            }
            if (!this.isKeyReady("cache/" + filename)) {
                this.ctx["cache"][filename] = {};
            }
            this.ctx["cache"][filename] = content;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/cache/" + filename,
                    data);
        }, this));
    },

    // ??
    translateAll : function() {
        $('.translate').each(function() {
            var key = $(this).data("key");
            $(this).text(dictionary[key]);
        });
        translateCollections();
    },
    // ??
    translate : function(obj) {
        // alert($(obj).find('.translate').length);
        $(obj).find('.translate').each(function() {
            var key = $(this).data("key");
            $(this).text(dictionary[key]);
        });
    },
    // ??
    translatable : function(key) {
        return '<span class="translate" data-key="' + key + '">'
                + dictionary[key] + '</span>';
    },

    vctranslatable : function(key) {
        return '<span class="vc" data-key="' + key + '">'
                + collections[key][language] + '</span>';
    },

    /**
     * Search given pid and find first component to display ommit every
     */
    searchItemAndExploreChildren : function(pid, whenready) {
        $.getJSON("api/item/" + pid + "/children", _.bind(function(data) {
            if (!this.isKeyReady("item")) {
                this.ctx["item"] = {};
            }
            if (!this.isKeyReady("item/" + pid)) {
                this.ctx["item"][pid] = {};
            }
            this.ctx["item"][pid]['children'] = data;
            if (data.length == 1) {
                this.searchItemAndExploreChildren(data[0].pid, whenready);
            } else {
                if (whenready)
                    whenready.apply(null, [ pid ]);
            }
        }, this));
    },

    gotoDisplayingItemPage : function(pid, q) {
        this.searchItemAndExploreChildren(pid, _.bind(function(data) {
            this.gotoItemPage(data, q);
        }, this));
    },

    /**
     * Go to item page
     * 
     * @method
     */
    gotoItemPage : function(pid, q) {
        var href = "";
        if (q !== undefined) {
            href += "?q=" + q + "&";
        } else {
            href += "?";
        }
        href += "page=doc#" + pid;
        window.location.assign(href);
    },

    /**
     * Go to results page
     * 
     * @method
     */
    gotoResultsPage : function(q) {
        var href = "?page=search" + q;
        window.location.assign(href);
    },

    /**
     * Get paramater from current url
     * 
     * @method
     */
    getParameterByName : function(name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex
                .exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(
                /\+/g, " "));
    },

    /**
     * Method is suitable for storing objects into http session
     * @param key Session key
     * @param object Storing object
     * @method
     */
    storeToSession:function(key,object) {
        console.log(JSON.stringify(object));
        var encodedData = Base64.encode(JSON.stringify(object));
        $.ajax({
                dataType: "json",
                type: "POST",
                //contentType:'application/json',
                'url': 'session?name='+key,
                //'beforeSend': function(xhr) { xhr.setRequestHeader("Authorization","Basic " + Base64.encode(uname + ":" + pass)); },
                data: {'encodedfield':encodedData}
                //success: postok
        });
    }
};
