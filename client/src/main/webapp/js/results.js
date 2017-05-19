/*
 * Copyright (C) 2013-2014 Alberto Hernandez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

K5.eventsHandler.addHandler(function(type, configuration) {
    if (type === "i18n/dictionary") {
        $(document).prop('title', K5.i18n.ctx.dictionary['application.title'] +
                ". " + K5.i18n.ctx.dictionary['common.results.for'] +
                " \"" + $("#q").val() + "\"");
        if (!K5.gui["results"])
            K5.gui["results"] = new Results();
    }
});

var Results = function(elem) {
    
    this.elem = elem;
    this.displayStyle = "display";
    this._init();
};

Results.prototype = {
    _init: function() {
        if (this.resultsLoaded)
            return;
        this.addContextButtons();
        
        K5.eventsHandler.addHandler(_.bind(function(type, data) {
            if (type === "window/resized") {
                this.srResize();
            }
        }, this));

        this.getDocs(false);
        var hash = hashParser();
        if (hash.hasOwnProperty(this.displayStyle) > 1) {
            if (hash[this.displayStyle] === "asrow")
                this.setRowStyle();
        }
        this.touchStart = 0;
        var obj = this;
        if (isTouchDevice()) {
            $("#search_results_docs").swipe({
                swipeStatus: function(event, phase, direction, distance, duration, fingers) {

                    if (phase === "start") {
                        this.touchStart = $("#search_results_docs").scrollTop();
                        this.startDir = null;
                    }


                    if (direction === "down") {
                        this.startDir = -1;
                    } else if (direction === "up") {
                        this.startDir = 1;
                    }

                    var idist = this.touchStart + distance * this.startDir;
                    $("#search_results_docs").scrollTop(idist);

                },
                //Default is 75px, set to 0 for demo so any distance triggers swipe
                threshold: 2
            });
        }
        
        $("#search_results_docs").scroll(_.bind(function() {
            this.onScroll();
        }, this));

    },
    onScroll: function() {
        if ($('#search_results_docs .more_docs').length > 0 && !this.loadingDocs) {
            var el = $('#search_results_docs .more_docs');
            if (isScrolledIntoView($(el), $('#search_results_docs'))) {
              this.loadingDocs = true;
                var start = $('#search_results_docs .more_docs').data('start');
                $("#start").val(start);
                this.getDocs(true);
            }
        }
    },
    getDocs: function(isMore) {
        $('.opacityloading').show();
        this.srResize();
        var start = isMore ? $("#start").val() : 0 ; 
        
        $.get("raw_results.vm?" + $("#search_form").serialize() + "&start=" + start, _.bind(function(data) {
            //console.log(data);
            $('#search_results_docs .more_docs').remove();
            var json = jQuery.parseJSON(data);
            K5.eventsHandler.trigger("results/loaded", json);
            this.loadDocs(json);
            if (!this.resultsLoaded) {
                this.srResize();
            }
            $('.opacityloading').hide();
            this.loadingDocs = false;
            this.resultsLoaded = true;
        }, this));
    },
    loadDocs: function(res) {
        var numFound = 0;
        var addMore = false;
        var docs;
        if (res.grouped) {
            numFound = res.grouped.root_pid.ngroups;
            var groups = res.grouped.root_pid.groups;

            for (var i = 0; i < groups.length; i++) {
                var doc = groups[i].doclist.docs[0];
                var pid = doc.PID;
                var r = new Result("#search_results_docs",
                        {"json": doc,
                            "collapsed": groups[i].doclist.numFound,
                            "hl": res.highlighting[pid]});
            }
            if (numFound > parseInt(res.responseHeader.params.rows) + parseInt(res.responseHeader.params.start)) {
                addMore = true;
            }
        } else {
            numFound = res.response.numFound;
            docs = res.response.docs;
            for (var i = 0; i < docs.length; i++) {
                var pid = docs[i].PID;
                var r = new Result("#search_results_docs", {
                    "json": docs[i],
                    "hl": res.highlighting[pid]
                });
            }
        }
        if (addMore) {
            var start = parseInt(res.responseHeader.params.rows) + parseInt(res.responseHeader.params.start);
            $("#search_results_docs").append('<div class="more_docs" data-start="' + start + '">more...</div>');
        }
        
        
            
        $("div.collections").mouseenter(function(){
            $(this).children("div.cols").show();
        });
        $("div.collections").mouseleave(function(){
            $(this).children("div.cols").hide();
        });

        this.setHeader(numFound);
    },
    addContextButtons: function() {
        var text = $("#results_menu").html();
        $("#contextbuttons").append(text);
    },
    srResize: function() {
        var h = window.innerHeight - $('#header').height() - $('#footer').height();
        $('#search_results').css('height', h);
        var h2 = h - $('#search_results_header').height() - 5;
        $('#search_results_docs').css('height', h2);
        $('#facets').css('height', h2 - 30); //30 = 2x15 padding 
    },
    setRowStyle: function() {
        $('#search_results').addClass('as_row');
        $('.as_row>.search_result>div.thumb>div.info').each(function() {
            var w = $(this).parent().width() - $(this).prev().width() - 20;
            $(this).css('width', w);
        });
        var hash = hashParser();
        hash[this.displayStyle] = "asrow";
        window.location.hash = jsonToHash(hash);
    },
    setHeader: function(numFound) {
        var key = 'common.title.plural_2';
        if (numFound > 4) {
            key = 'common.title.plural_2';
        } else if (numFound > 1) {
            key = 'common.title.plural_1';
        } else {
            key = 'common.title.singural';
        }
        $("#search_results_header>div.totals>span.totaltext").data('key', key);
        $("#search_results_header>div.totals>span.totaltext").text(K5.i18n.ctx.dictionary[key]);
        $("#search_results_header>div.totals>span.total").text(numFound);
    },
    setThumbsStyle: function() {
        $('#search_results').removeClass('as_row');
        $('.search_result>div.thumb>div.info').css('width', '');
        
        var hash = hashParser();
        hash[this.displayStyle] = "asthumb";
        window.location.hash = jsonToHash(hash);
    }
};



var Result = function(parent, options) {
    this.parent = parent;
    this.$parent = $(parent);
    this.json = options.json;
    this.hl = options.hl;
    this.collapsed = options.collapsed;
    this.init();

    return this.elem;
};

Result.prototype = {
    background: "silver",
    thumbHeight: 128,
    maxInfoLength: 80,
    init: function() {

        this.elem = $('<div/>', {class: 'search_result'});

        this.$parent.append(this.elem);

        this.render();

    },
    setSizes: function() {
        this.panelHeight = this.$elem.height();
        this.panelWidth = this.$elem.width();
    },
    render: function() {
        var doc = this.json;
        var pid = doc.PID;
        var imgsrc = "api/item/" + pid + "/thumb";
        var thumb = $('<div/>', {class: 'thumb'});

        var title = doc[fieldMappings.title];
        var rootTitle = doc["root_title"];
        var info = {short: "", full: ""};
        info.full = '<div class="title">' + rootTitle + '</div>';
        info.short = "";

        var showtooltip = false;

        if (rootTitle.length > this.maxInfoLength) {
            showtooltip = true;
            info.short += '<div class="title">' + rootTitle.substring(0, this.maxInfoLength) + '...</div>';
        } else {
            info.short += '<div class="title">' + rootTitle + '</div>';
        }
        this.getDetails(info);

        if (doc[fieldMappings.autor]) {
            var cre = doc[fieldMappings.autor].toString();
            info.full += '<div class="autor">' + cre + '</div>';

            if (cre.length > 50) {
                cre = cre.substring(0, 50) + "...";
                showtooltip = true;
            }
            info.short += '<div class="autor">' + cre + '</div>';
        }

        if (doc["datum_str"]) {
            info.full += '<div class="datum">' + doc["datum_str"] + '</div>';
            info.short += '<div class="datum">' + doc["datum_str"] + '</div>';
        }

        var linkpid = pid;
//        if(this.collapsed && this.collapsed > 1){
//            var collapsed = $('<div class="collapsed">'+this.collapsed+'</div>');
//            thumb.append(collapsed);
//        }
        var fedora_model = doc[fieldMappings.fedora_model];
        var typtitulu = doc["model_path"][0].split("/")[0];
        var modeltag = $('<div class="collapsed">');
        if ((this.collapsed && this.collapsed > 1)) {
            linkpid = doc['root_pid'];
            var key = 'common.hits.plural_1';
            if (this.collapsed > 4) {
                key = 'common.hits.plural_2';
            }
            var tx = K5.i18n.translatable(key);
            modeltag.append(this.collapsed + ' ' + tx + ' ' + K5.i18n.translatable('model.locativ.' + typtitulu));
        } else if (fedora_model === typtitulu) {
            modeltag.append(K5.i18n.translatable('fedora.model.' + typtitulu));
        } else {
            modeltag.append(K5.i18n.translatable('fedora.model.' + fedora_model) + ' ' +
                    K5.i18n.translatable('model.locativ.' + typtitulu));
        }

//        info.short += modeltag;
//        info.full += modeltag;
        

        if (this.hl && this.hl["text_ocr"]) {
            for (var j = 0; j < this.hl.text_ocr.length; j++) {
                showtooltip = true;
                info.full += '<div class="hl">' + this.hl.text_ocr[j] + '</div>';
            }
        }

        thumb.data("pid", pid);
        thumb.data("root_pid", doc["root_pid"]);
        thumb.data("info", info);
        this.elem.append(thumb);
        var policy = $('<div/>', {class: 'policy'});
        if (doc['dostupnost']) {
            policy.addClass(doc['dostupnost']);
            policy.addClass("translate_title");
            policy.attr("data-key","dostupnost."+doc['dostupnost']);
            //policy.attr("title", doc['dostupnost']);
            policy.attr("title", K5.i18n.translate("dostupnost."+doc['dostupnost']));
			
        }

        var divimg = $('<div/>', {class: 'img'});
        var img = $('<img/>', {src: imgsrc});
        $(thumb).append(divimg);
        $(divimg).append(img);
        img.load(function() {
            if ($(this).parent().parent().parent().hasClass('as_row')) {
                var w = $(this).parent().parent().width() - $(this).width() - 20;
                $(this).parent().next().css('width', w);
            }
        });


        var ithumb = $('<div/>', {class: 'info'});
        var ifull = $('<div/>', {class: 'full'});
        var ishort = $('<div/>', {class: 'short'});

        ishort.html(info.short);
        ifull.html(info.full);

        ithumb.append(ishort);
        ithumb.append(ifull);
        thumb.append(ithumb);
        if (showtooltip) {
            thumb.attr("title", title);
            thumb.tooltip({
                content: info.full,
                open: function(event, ui) {
                    K5.i18n.k5translate(ui.tooltip);
                    //ui.tooltip.animate({ top: ui.tooltip.position().top + 10 }, "fast" );
                },
                position: {my: "left top", at: "right top"}
            });
        }
        
        if(doc.hasOwnProperty("collection") && doc.collection.length>0){
            var collTag  = $("<div/>", {class: "collections"});
            collTag.append('<span class="ui-icon ui-icon-folder-open">collections</span>');
            var collDiv = $('<div class="cols shadow-bottom ui-widget ui-widget-content">');
            for(var i=0; i< doc.collection.length; i++){
                collDiv.append('<div class="collection">' + K5.i18n.translatable(doc.collection[i]) + '</div>');
            }

            collTag.append(collDiv);
            modeltag.prepend(collTag);
        }

        thumb.append(modeltag);
        this.elem.append(policy);


        thumb.click(function() {
            var hash = hashParser();
            hash.pid = linkpid;
            //hash.pmodel = typtitulu;
            K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
        });

    },
    getDetails: function(info) {
        //var title = this.json["dc.title"];
        var model = this.json["fedora.model"];
        var details = this.json["details"];
        var root_title = this.json["root_title"];
        var detFull = "";
        var detShort = "";
        if (details && details.length > 0) {
            var dArr = details[0].split("##");
            if (model === "periodicalvolume") {
                detShort = "<div>" + root_title.substring(0, this.maxInfoLength) + "</div>" +
                        K5.i18n.translatable('field.datum') + ": " + dArr[0] + " " +
                        K5.i18n.translatable('mods.periodicalvolumenumber') + " " + dArr[1];
                detFull = "<div>" + root_title + "</div>" +
                        K5.i18n.translatable('field.datum') + ": " + dArr[0] + " " +
                        K5.i18n.translatable('mods.periodicalvolumenumber') + " " + dArr[1];
            } else if (model === "internalpart") {
                detFull = dArr[0] + " " + dArr[1] + " " + dArr[2] + " " + dArr[3];
                detShort = dArr[0] + " " + dArr[1] + " " + dArr[2] + " " + dArr[3];
            } else if (model === "periodicalitem") {
                if (dArr[0] !== root_title) {
                    detFull = dArr[0] + " " + dArr[1] + " " + dArr[2];
                    detShort = dArr[0] + " " + dArr[1] + " " + dArr[2];
                } else {
                    detFull = dArr[1] + " " + dArr[2];
                    detShort = dArr[1] + " " + dArr[2];
                }
            } else if (model === "monographunit") {
                detFull = dArr[0] + " " + dArr[1];
                detShort = dArr[0] + " " + dArr[1];
            } else if (model === "page") {
                detFull = dArr[0] + " " + K5.i18n.translatable('mods.page.partType.' + dArr[1]);
                detShort = dArr[0] + " " + K5.i18n.translatable('mods.page.partType.' + dArr[1]);

            } else {
                detFull = details;
                detShort = details;
            }
        } else {
            return "";
        }

        info.short += '<div class="details">' + detShort + '</div>';
        info.full += '<div class="details">' + detFull + '</div>';

    }
};
