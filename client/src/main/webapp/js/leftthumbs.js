/* global K5, _ */

/**
 * Left panel thumbs 
 */



function LeftThumbs(appl, elem, settings) {
    this.application = (appl || K5);

    var jqSel = (elem || '#viewer>div.container>div.thumbs');
    this.elem = $(jqSel);

    this.settings = ( settings || null );

    this.init();
    
    this.application.eventsHandler.addHandler(_.bind(function(type, data) {
        if (type === "app/searchInside") {
            this.dosearch(data);
        }
    }, this));
    
    this.contentGenerated = false;
}

LeftThumbs.prototype = {
    relation: 1.3, // height/width
    thumbMargin: 4,
    thumbBorder: 2,
    thumbMinWidth: 90,
    thumbMinHeight: 128,
    imgMargin: 2,
    containerMargin: 1,
    maxInfoLength: 100,
    init: function() {

        $("table.container").remove();

        this.container = $('<table/>');
        this.container.addClass('container');
        this.elem.append(this.container);
        this.elem.css("width", "350px");
        this.width = this.elem.width() - this.containerMargin * 2;
        this.height = this.elem.height() - this.containerMargin * 2;
        this.hits = {};
        // i18n must be ready
        if (this.application.i18n.isKeyReady("dictionary")) {
            this.getThumbs();
        } else {
            this.application.eventsHandler.addHandler(_.bind(function(type, data) {
                if (type === "i18n/dictionary") {
                    if (!this.contentGenerated) {
                        this.getThumbs();
                    }
                }
            }, this));
        }
        $("#searchinside_q").keypress(_.bind(function(e) {
            if (e.keyCode === $.ui.keyCode.ENTER) {
                this.dosearch();
            }
        }, this));
    },
    process: function(){
        var pid = K5.api.ctx["item"]["selected"].split(";")[0];
        if(this.currentPidSelected !== pid){
            this.currentPidSelected = pid;
            var hash = hashParser();
            var elem = $("#viewer .thumb[data-pid='" + pid + "']");
            if(this.currentParentModel !== hash.pmodel && hash.hasOwnProperty("pmodel")){
                this.currentParentModel = hash.pmodel;
                this.init();
            }else if(elem.length > 0){
                $("#viewer .thumb").removeClass("selected");
                $("#viewer .tt_text").removeClass("selected");
                var idx = elem.index();
                elem.addClass('selected');
                var tt = elem.parent().next().find("td:eq("+idx+ ")");
                tt.addClass('selected');
                this.scrollToSelected();
            }else{
                this.init();
            }
        }
    },
    addContextButtons: function() {
        _ctxbuttonsrefresh("thumbs");
    },
    doScroll: function(dx) {
        var speed = 500;
        var finalPos = this.elem.scrollTop() + this.elem.height() * 0.6 * dx;
        var th = this;
        th.scrolling = true;
        
        this.elem.animate({scrollTop: finalPos}, speed, function() {
            //th.checkArrows();
        });
    },
    getThumbs: function() {
        this.thumbs = [];
        this.container.empty();
        this.thloaded = -1;
        this.setLoading(true);
        $("#viewer>div.loading").show();
        
        this.currentPidSelected = K5.api.ctx["item"]["selected"].split(";")[0];
        var hash = hashParser();
        
        
        if(hash.hasOwnProperty("pmodel")){
            this.currentParentModel = hash.pmodel;
        }else{
            this.currentParentModel = null;
        }
        
        K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], _.bind(function(data) {
            var nomodel = this.currentParentModel === null;
            var dd = [];
            _.each(data, _.bind(function(objectForPath) { 
                var path = objectForPath.path;
                var lastModel = path[path.length - 2].model;
                
                _.each(objectForPath.siblings, _.bind(function(thumb) {
                    if (this.settings && this.settings.selector) {
                        if (this.settings.selector.call(null, thumb)) {
                            dd.push(thumb);
                        }
                    } else {
                        // no selector defined
                        if(lastModel === hash.pmodel || nomodel){
                            dd.push(thumb);
                        }
                    }
                }, this));
            },this));
            
            this.thumbs = dd;
            
            this.thloaded = 0;
            this.setDimensions();
            var rowImg;
            var rowText;
            for (var i = 0; i < this.thumbs.length; i++) {
                if(i%3 === 0){
                    rowImg = $('<tr/>', {class: 'img'});
                    rowText = $('<tr/>', {class: 'txt'});
                    this.container.append(rowImg);
                    this.container.append(rowText);
                }
                this.addThumb(rowImg, rowText, i);
            }
            if (this.thumbs.length === 0) {
                this.setLoading(false);
            }
            $("#viewer>div.loading").hide();
            this.getHits();
            
        }, this));

        this.contentGenerated = true;

    },
    scrollToSelected: function(){
        
        var sel = $($(".thumbs .selected")[0]).parent();
        if(!sel){
            return;
        }
        var currentPos = this.container.parent().scrollTop();
        this.container.parent().animate({
            scrollTop: sel.position().top + currentPos
        }, 1000);
    },
    dosearch: function(q) {
        var q = $("#searchinside_q").val();
        console.log("query is "+q);
        if (q !== null && q !== $("#q").val()) {
            this.setLoading(true);
            $("#q").val(q);
            this.hits = {};
            var data  = this.textOcr;
            $('td.hit').removeClass("hit");
            $('td.hit').each(function() {
                var pid = $(this).data('pid');
                if (data[pid]) {
                    $(this).tooltip("option", "content", $(this).data("tt"));
                }
            });
            $('td.chit').each(function() {
                var pid = $(this).data('pid');
                if (data[pid]) {
                    $(this).tooltip("option", "content", $(this).data("tt"));
                }
            });
            $('td.thumb').removeClass("hit chit");
            this.getHits(true);
        }
        cleanWindow();
    },
    getHits: function(showalert) {
        if ($("#q").val() === "") {
            return;
        }
        if (jQuery.isEmptyObject(this.hits)) {
            var pid = K5.api.ctx["item"]["selected"];
            var root_pid = K5.api.ctx["item"][pid].root_pid;
            var pid_path = "";
            var context = K5.api.ctx["item"][pid].context[0];
            for (var i = 0; i < context.length-1; i++) {
                pid_path += context[i].pid + "/";
            }
            var q = "q=" + $("#q").val() + "&rows=5000&fq=pid_path:" + pid_path.replace(/:/g, "\\:") + "*";
            var hl = "&hl=true&hl.fl=text_ocr&hl.mergeContiguous=true&hl.snippets=2";
            K5.api.askForSolr(q + hl, _.bind(function(data) {
                var numFound = data.response.numFound;
                console.log("Hits: " + data.response.numFound);
                //console.log(JSON.stringify(data));
                this.hits = data.response.docs;
                this.highlighting = data.highlighting;
                this.setHitClass();
                
                if(showalert){
                    var key = 'common.page.plural_2';
                    if (numFound > 4) {
                        key = 'common.page.plural_2';
                    } else if (numFound > 1) {
                        key = 'common.page.plural_1';
                    } else {
                        key = 'common.page.singural';
                    }
                    alert(K5.i18n.ctx.dictionary["common.found"] + " " + numFound + " " + K5.i18n.ctx.dictionary[key]);
                }
            }, this));
        } else {
            this.setHitClass();
        }
    },
    setHitClass: function() {
        var hits = this.hits;
        var hl = this.highlighting;
        var data = {};
        $('td.thumb').each(function() {
            for (var i = 0; i < hits.length; i++) {
                var pid = hits[i].pid ? hits[i].pid : hits[i].PID;
                var pid_path = hits[i].pid_path[0];

                var lipid = $(this).data("pid").toString();
                if ($(this).data("pid") === pid) {
                    $(this).addClass('hit');
                    var tt = $(this).parent().next().find("td:eq("+$(this).index()+ ")");
                    tt.addClass('hit');

                    var hltext = "";
                    if (hl[pid].text_ocr) {
                        for (var j = 0; j < hl[pid].text_ocr.length; j++) {
                            hltext += '<div class="hl">' + hl[pid].text_ocr[j] + '</div>';
                        }
                        data[pid] = true;
                    } else {
                        data[pid] = false;
                    }
                    break;
                } else if (pid_path.indexOf(lipid) > -1) {
                    $(this).addClass('chit');
                    break;
                }
            }
        });
        this.textOcr = data;
        this.setLoading(false);
    },
    resized: function() {

        setTimeout(function() {
            this.width = this.elem.width();
            this.height = this.elem.height() - this.containerTop - this.containerMargin * 2;
            //this.setDimensions();
            $("#viewer li.thumb").css('width', this.thumbWidth + "px");
            $("#viewer li.thumb").css('height', this.thumbHeight + "px");
            //$("#viewer li.thumb img").attr("height", this.imgHeight);
            this.checkScroll();
        }.bind(this), 200);
    },
    
    setLoading: function(loading) {
        if (loading) {
            $("#viewer>div.loading").show();
            //$("#viewer").css("cursor", "progress");
        } else {
            $("#viewer>div.loading").hide();
            //$("#viewer").css("cursor", "default");
        }
    },
    checkLoading: function() {
        this.thloaded = this.thloaded + 1;
        if (this.thloaded >= this.thumbs.length) {
            this.setLoading(false);
            this.checkScroll();
            this.scrollToSelected();
        }
    },
    checkScroll: function(){
        var fit = this.elem.height() < this.elem.parent().height - 40;
        
            this.elem.css("height", "100%");
        
        if (fit) {
            this.elem.css("overflow", "hidden");
        } else {
            this.elem.css("overflow", "auto");
        }
    },
    
    
    thumbMaxWidth: 96,
    thumbMaxHeight: 128,
    thumbCurWidth: 0,
    thumbCurHeight: 0,
    setCurThumbSize: function(w, h) {
        return;
        if(w > this.thumbCurWidth || (h > this.thumbCurHeight && this.thumbCurHeight !== this.thumbMaxHeight)){
            //this.thumbCurWidth = Math.min(w, this.thumbMaxWidth);
            this.thumbCurWidth = w;
            this.thumbCurHeight = Math.min(h, this.thumbMaxHeight);
            this.thumbWidth = this.thumbCurWidth + 8;
            this.thumbHeight = this.thumbCurHeight + 8;
            this.imgWidth = this.thumbWidth - this.imgMargin * 2 - this.thumbBorder * 2;
            this.imgHeight = this.thumbHeight - this.imgMargin * 2 - this.thumbBorder * 2;
            
            this.resized();
        }
    },
    setDimensions: function() {
        return;
        $('#viewer li.thumb').css('width', this.thumbMaxWidth).css('height', this.thumbMaxHeight);
        
    },
    addThumb: function(rowImg, rowText, index) {
        var pid = this.thumbs[index].pid ? this.thumbs[index].pid : this.thumbs[index].PID;
        var model = this.thumbs[index].model;
        var datanode = this.thumbs[index].datanode;

        var imgsrc = 'api/item/' + pid + '/thumb';
        var img = $('<img/>', {src: "images/empty.gif"});
        var image = new Image();
        var itemths = this;
        image.onload = function() {
            $(img).attr("src", imgsrc);
            var w = this.naturalWidth;
            var h = this.naturalHeight;
            itemths.setCurThumbSize(w, h);
            itemths.checkLoading();
        };
        image.onerror = function() {
            $(img).attr("src", "");
            itemths.checkLoading();
        };
        image.src = imgsrc;
        
        var thumb = $('<td/>', {class: 'thumb', 'data-pid': pid});
        thumb.attr("title", this.thumbs[index].title);

        thumb.css('width', this.thumbWidth + "px");
        thumb.css('height', this.thumbHeight + "px");

        var modelName = this.thumbs[index].model === 'article' ? '' : K5.i18n.translatable('fedora.model.' + this.thumbs[index].model);
        var title = '<span class="title">' + modelName + " " + this.thumbs[index].title + '</span>';

        var info = {short: "", full: ""};
        this.getDetails(this.thumbs[index], info);
        thumb.data("pid", pid);
        thumb.data("model", model);
        
        var tt = $('<td class="tt_text">' + title + info.full + '</div>');
        
        thumb.data("tt", tt);

        thumb.append(img);
        
        thumb.addClass('policy');
        if (this.thumbs[index].policy) {
            thumb.addClass(this.thumbs[index].policy);
        }
        
        if(pid === K5.api.ctx["item"]["selected"].split(";")[0]){
            thumb.addClass('selected');
            tt.addClass('selected');
        }
        
        thumb.click(function() {
            itemths.navigate(pid, datanode, model);
        });
        tt.click(function() {
            itemths.navigate(pid, datanode, model);
        });

        rowImg.append(thumb);
        rowText.append(tt);
    },
    navigate: function(pid, datanode, model){
        if(this.currentPidSelected !== pid){
            var hash = hashParser();
            hash.pid = pid;
            var histDeep = getHistoryDeep() + 1;
            hash.hist = histDeep;
            if(datanode){
                window.location.hash = jsonToHash(hash);
            }else{
                hash.pmodel = model;
                K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
            }
        }
    },
    getDetails: function(json, info) {
        var model = json["model"];
        var details = json["details"];
        var root_title = json["root_title"];
        var detFull = "";
        var detShort = "";
        if (details) {

            if (model === "periodicalvolume") {

                detShort = "<div>" + root_title.substring(0, this.maxInfoLength) + "</div>" +
                        K5.i18n.translatable('field.datum') + ": " + details.year + " ";
                if (details.volumeNumber) {
                    detShort += K5.i18n.translatable('mods.periodicalvolumenumber') + " " + details.volumeNumber;
                }

                detFull = "<div>" + root_title + "</div>" +
                        K5.i18n.translatable('field.datum') + ": " + details.year + " ";
                if (details.volumeNumber) {
                    detFull += K5.i18n.translatable('mods.periodicalvolumenumber') + " " + details.volumeNumber;
                }

            } else if (model === "internalpart") {
                var dArr = details.split("##");
                detFull = dArr[0] + " " + dArr[1] + " " + dArr[2] + " " + dArr[3];
                detShort = dArr[0] + " " + dArr[1] + " " + dArr[2] + " " + dArr[3];
            } else if (model === "periodicalitem") {
                if (details.issueNumber !== root_title) {
                    detFull = details.issueNumber + " " + details.date + " " + details.partNumber;
                    detShort = details.issueNumber + " " + details.date + " " + details.partNumber;
                } else {
                    detFull = details.date + " " + details.partNumber;
                    detShort = details.date + " " + details.partNumber;
                }
            } else if (model === "monographunit") {
                detFull = details.title + " " + details.partNumber;
                detShort = details.title + " " + details.partNumber;
            } else if (model === "page") {
                // dateils type muze byt frontPage nebo FrontPage
                if(details.hasOwnProperty('type')){
                  var loc = details.type.substring(0,1).toUpperCase() + details.type.substring(1);
                  detFull = K5.i18n.translatable('mods.page.partType.' + loc);
                  detShort = K5.i18n.translatable('mods.page.partType.' + loc);
                }
            } else {
                detFull = details;
                detShort = details;
            }
        } else {
            return "";
        }

        info.short += '<div class="details">' +  detShort + '</div>';
        info.full += '<div class="details">' + detFull + '</div>';

    },

    setSettings: function(settinssObject) {
        this.settings = settinssObject;
    },

    getSettings: function (settingsObject) {
        return this.settings;
    }
};



