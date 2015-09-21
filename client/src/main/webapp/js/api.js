/**
 * @description
 * Manipulating with Remote API v5.0. Accessed via singleton <code>K5</code>
 * <pre><code>
 *  K5.api.askForCool(function(data) {
 *      alert("cool : "+data);
 *  });
 *  K5.api.askForLatest(function(data) {
 *      alert("cool : "+data);
 *  });
 * </code></pre>
 * @constructor
 * @param {Application} application - The application instance.
 */
function ClientAPIDev(application) {
    this.application = application;
}

ClientAPIDev.prototype = {

    /**
     * @description
     * Contains information received from API (items, children, siblings, etc...)
     * @example <caption>var array = K5.api.ctx.item;</caption> 
     * @example <caption>var current = K5.api.ctx.item[K5.api.ctx.item.selected];</caption> 
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

    /**
     * Request for cool data
     * @param {requestCallback} whenready  - Callback handling responses.
     */
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
     * @param {requestCallback} whenready  - Callback handling responses.
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
     * @param {requestCallback} whenready  - Callback handling responses.
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
     * @param {requestCallback} whenready  - Callback handling responses.
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
     * Request for sources
     * @param {requestCallback} whenready  - Callback handling responses.
     * @method
     */
    askForSources : function(whenready) {
        $.getJSON("api/sources", _.bind(function(data) {
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
            this.application.eventsHandler.trigger("api/sources", data);
        }, this));
    },

    /**
     * Solr search request
     * @param {string} query - Query.
     * @param {requestCallback} whenready  - Callback handling responses.
     * @method
     */
    askForSolr : function(query, whenready) {
        $.getJSON("api/search?" + query, _.bind(function(data) {
            this.ctx["solr"] = {};
            this.ctx["solr"][query] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/solr?" + query, data);
        }, this));
    },

    /**
     * Solr search terms request
     * @param {string} query - Query.
     * @param {requestCallback} whenready  - Callback handling responses.
     * @method
     */
    askForTerms : function(query, whenready) {
        $.getJSON("api/search/terms?" + query, _.bind(function(data) {
            this.ctx["solr"] = {};
            this.ctx["solr"][query] = data;
            if (whenready)
                whenready.apply(null, [ data ]);
            this.application.eventsHandler.trigger("api/terms?" + query, data);
        }, this));
    },

    /**
     * Requests for basic item information
     * @param {string} pid - Pid of object.
     * @param {requestCallback} whenready  - Callback handling responses.
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
     * @param {string} pid - Pid of object.
     * @param {requestCallback} whenready  - Callback handling responses.
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

    /**
     * Requesting data from concrete stream
     * @param {string} pid - Pid of object.
     * @param {string} pid - Name of the stream.
     * @param {requestCallback} whenready  - Callback handling responses.
     * @method
     */
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
     * Requesting information about context
     * @param {string} pid - Pid of object.
     * @param {requestCallback} whenready  - Callback handling responses.
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
     * Requesting siblings
     * @param {string} pid - Pid of object.
     * @param {requestCallback} whenready  - Callback handling responses.
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
     * Requesting children from concrete pid
     * @param {string} pid - Pid of object.
     * @param {requestCallback} whenready  - Callback handling responses.
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

    /**
     * Sending feedback
     * @param {string} mess - Message.
     * @param {string} pid - Pid.
     * @param {string} pid - From - user identification.
     * @param {requestCallback} okFunc  - Message has been sent callback.
     * @param {requestCallback} failFunc  - Something wrong callback.
     * @method
     */
    feedback:function(message, pid, from, okFunc, failFunc) {
        $.post("feedback", {
            "from" : from,
            "pid" : pid,
            "content" : message
        }, okFunc);
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
     * Searching first item to display. 
     * <p>
     * The algorithm finds first item wich doesn't contain only one child
     * </p>
     * @param {string} pid - Pid.
     * @param {string} pid - From - user identification.
     * @param {requestCallback} okFunc  - Message has been sent callback.
     * @param {requestCallback} failFunc  - Something wrong callback.
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
                if(data.length>0 && data[0].datanode){
                    this.searchItemAndExploreChildren(data[0].pid, whenready);
                }else{
                    if (whenready)
                        whenready.apply(null, [ pid ]);
                }
            }
        }, this));
    },

    /**
     * Search first pid to display and navigate browser to  this item.
     * @method
     */
    gotoDisplayingItemPage : function(newhash, q) {
        var hash = hashParser(newhash);
        var pid = hash.pid;
        
        this.searchItemAndExploreChildren(pid, _.bind(function(data) {
            hash.pid = data;
            this.gotoItemPage(jsonToHash(hash), q);
        }, this));
    },

    /**
     * Navigate browser to concrete item 
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
