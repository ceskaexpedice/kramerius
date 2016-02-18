function ItemThumbs(appl, elem) {
    this.application = (appl || K5);

    var jqSel = (elem || '#viewer>div.container>div.thumbs');        
    this.elem = $(jqSel);

    this.init();
    this.application.eventsHandler.addHandler(_.bind(function(type, data) {
        if (type === "window/resized") {
            this.resized();
        }
    }, this));

    this.contentGenerated = false;
}

ItemThumbs.prototype = {
    relation: 1.3, // height/width
    thumbMargin: 4,
    thumbBorder: 2,
    thumbMinWidth: 90,
    thumbMinHeight: 128,
    imgMargin: 2,
    containerMargin: 1,
    maxInfoLength: 100,
    init: function() {

        this.elem.empty();

        this.container = $('<ul/>');
        this.container.addClass('container');
        this.elem.append(this.container);
        this.elem.css("width", "100%");
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

        this.elem.mousewheel(_.bind(function(event) {
            if (event.deltaX !== 0) {
                this.doScroll(event.deltaX);
            } else {
                this.doScroll(-event.deltaY);
            }
        }, this));
        
        
    

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
            th.checkArrows();
        });
    },
    checkArrows: function() {
        /*
        if (this.elem.scrollTop() < ($(this.elem)[0].scrollHeight - this.elem.height())) {
            this.bottomArrow.show();
        } else {
            this.bottomArrow.hide();
        }*/


        if (this.elem.scrollTop() <= 0) {
            this.topArrow.hide();
        } else {
            this.topArrow.show();
        }
    },
    getThumbs: function() {
        this.thumbs = [];
        this.thloaded = -1;
        this.setLoading(true);
        $("#viewer>div.loading").show();
        K5.api.askForItemChildren(K5.api.ctx["item"]["selected"], _.bind(function(data) {
            this.thumbs = data;
            this.thloaded = 0;
            this.setDimensions();
            for (var i = 0; i < this.thumbs.length; i++) {
                this.addThumb(i);
            }
            if (this.thumbs.length === 0) {
                this.setLoading(false);
            }
            $("#viewer>div.loading").hide();
            this.getHits();

            K5.eventsHandler.trigger("application/menu/ctxchanged", null);

        }, this));
        this.contentGenerated = true;

    },
    dosearch: function(q) {
        var q = $("#searchinside_q").val();
        console.log("query is "+q);
        if (q !== null && q !== $("#q").val()) {
            console.log("searching");
            this.setLoading(true);
            $("#q").val(q);
            this.hits = {};
//            $('li.hit').each(function() {
//                $(this).tooltip("option", "content", $(this).data("tt"));
//            });
//            $('li.chit').each(function() {
//                $(this).tooltip("option", "content", $(this).data("tt"));
//            });
            $('li.thumb').removeClass("hit chit");
            this.getHits();
        }
        cleanWindow();
    },
    search: function() {
        $("#searchinside_q").val($("#q").val());
        var th = this;
        $("#searchinside").dialog({
            resizable: false,
            modal: true,
            height: 75,
            position: {of: $("#contextbuttons>div.search"), my: "left top", at: "left bottom"},

//            buttons: {
//                "OK": function() {
//                    th.dosearch();
//                    //$(this).dialog("close");
//                },
//                Cancel: function() {
//                    $(this).dialog("close");
//                }
//            },

            focus: function() {
                $("#searchinside_q").focus();
                $("#searchinside_q").select();
            }
        });
    },
    getHits: function() {
        if ($("#q").val() === "" && !isAdvancedSearch()) {
            return;
        }
        if (jQuery.isEmptyObject(this.hits)) {
            var pid = K5.api.ctx["item"]["selected"];
            var root_pid = K5.api.ctx["item"][pid].root_pid;
            var pid_path = "";
            var context = K5.api.ctx["item"][pid].context[0];
            for (var i = 0; i < context.length; i++) {
                pid_path += context[i].pid + "/";
            }
            var fq = setAdvSearch();
            var q = "";
            if($("#q").val() !== ""){
                q += "q=" + $("#q").val();
            }else if(fq !== ""){
                q = "q=*:*";
            }
            q += "&rows=5000&fq=pid_path:" + pid_path.replace(/:/g, "\\:") + "*";
            var hl = "&hl=true&hl.fl=text_ocr&hl.mergeContiguous=true&hl.snippets=2";
            K5.api.askForSolr(q + hl, _.bind(function(data) {
                console.log("Hits: " + data.response.numFound);
                //console.log(JSON.stringify(data));
                this.hits = data.response.docs;
                this.highlighting = data.highlighting;
                this.setHitClass();
            }, this));
        } else {
            this.setHitClass();
        }
    },
    setHitClass: function() {
        var hits = this.hits;
        var hl = this.highlighting;
        $('li.thumb').each(function() {
            for (var i = 0; i < hits.length; i++) {
                var pid = hits[i].pid ? hits[i].pid : hits[i].PID;
                var pid_path = hits[i].pid_path[0];

                var lipid = $(this).data("pid").toString();
                if ($(this).data("pid") === pid) {
                    $(this).addClass('hit');
                    var tt = $(this).data("tt");

                    var hltext = "";
                    for (var j = 0; j < hl[pid].text_ocr.length; j++) {
                        hltext += '<div class="hl">' + hl[pid].text_ocr[j] + '</div>';
                    }
//                    $(this).tooltip("option", "content", tt + hltext);
                    break;
                } else if (pid_path.indexOf(lipid) > -1) {
                    $(this).addClass('chit');
                    break;
                }
            }
        });
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
            //$("#viewer>div.loading").show();
            $("#viewer").css("cursor", "progress");
        } else {
            //$("#viewer>div.loading").hide();
            $("#viewer").css("cursor", "default");
        }
    },
    checkLoading: function() {
        this.thloaded = this.thloaded + 1;
        if (this.thloaded >= this.thumbs.length) {
            this.setLoading(false);
            this.checkScroll();
            var max = Math.max($("#viewer li.thumb img").width());
            $("#viewer li.thumb").css("width", max + 40);
        }
    },
    checkScroll: function(){
        var fit = this.elem.height() < this.elem.parent().height - 40;
        
        if (fit) {
            this.elem.css("overflow", "hidden");
        } else {
            this.elem.css("overflow", "auto");
        }
    },
    setDimensions2: function() {
        this.width = this.elem.width() - this.containerMargin * 2;
        var marginTop = 40;
        this.height = this.elem.height() - this.containerMargin * 2 - marginTop;
        this.relation = 128.0 / 96.0;
        this.relation = 96.0 / 128.0;
        var minGridCols = Math.floor(this.width / this.thumbMinWidth);
        var fit = true;
        var numThumbs = this.thumbs.length;

        var gridCols = Math.round(Math.sqrt((numThumbs * this.relation * this.width) / (this.height)));
        if (gridCols > minGridCols) {
            gridCols = minGridCols;
            fit = false;
        }
        if (gridCols === 0) {
            gridCols = 1;
        }
        var gridRows = Math.round(numThumbs / gridCols);
        if (gridRows * gridCols < numThumbs) {
            gridRows = gridRows + 1;
        }

        this.cellWidth = Math.floor((this.width - this.thumbMargin * 2 - this.thumbBorder * 2) / gridCols);
        if (fit) {
            this.cellHeight = (this.height) / gridRows;
            this.elem.css("overflow", "hidden");
        } else {
            this.cellHeight = this.cellWidth * this.relation;
            this.elem.css("overflow", "auto");
        }
        this.thumbWidth = this.cellWidth - this.thumbMargin * 2 - this.thumbBorder * 2;
        this.thumbHeight = Math.max(this.thumbMinHeight, this.cellHeight - this.thumbMargin * 2 - this.thumbBorder * 2);
        
        this.imgWidth = this.thumbWidth - this.imgMargin * 2 - this.thumbBorder * 2;
        this.imgHeight = this.thumbHeight - this.imgMargin * 2 - this.thumbBorder * 2;

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
    addThumb: function(index) {
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
            var relation = w / h;
            if (itemths.imgHeight * relation > itemths.imgWidth) {
                $(img).css("top", (itemths.thumbHeight - itemths.imgHeight)/2);
                if(w > itemths.imgWidth){
                    //$(img).css("width", "calc(100% - 8px)");
                    //$(img).attr("width", Math.min(w, itemths.imgWidth));
                }else{
//                    $(img).css("width", w);
                }
            } else {
                var finalh = Math.min(h, itemths.imgHeight);
//                    $(img).attr("height", finalh);
                    //$(img).css("height", "calc(100% - 8px)");
                    $(img).css("top", (itemths.thumbHeight - finalh)/2);
//                if(h > itemths.imgHeight){
//                    
//                }else{
//                    $(img).css("top", (finalh - h)/2);
//                }
            }

        
            itemths.checkLoading();
        };
        image.onerror = function() {
            $(img).attr("src", "");
            itemths.checkLoading();
        };
        image.src = imgsrc;
        var thumb = $('<li/>', {class: 'thumb', 'data-pid': pid});
        thumb.attr("title", this.thumbs[index].title);

        thumb.css('width', this.thumbWidth + "px");
        thumb.css('height', this.thumbHeight + "px");
        img.click(function() {
            var hash = hashParser();
            hash.pid = pid;
            var histDeep = getHistoryDeep() + 1;
            hash.hist = histDeep;
            if(datanode){
                //hash.pmodel = model;
            }else{
                hash.pmodel = model;
            }
            K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
            
        });

        var title = '<span class="title">' + K5.i18n.translatable('fedora.model.' + this.thumbs[index].model) + " " + this.thumbs[index].title + '</span>';
        var info = {short: "", full: ""};
        this.getDetails(this.thumbs[index], info);
        thumb.data("pid", pid);
        //var tt = '<img src="' + imgsrc + '" style="float:left;margin-right:4px;" /><div class="tt_text">' + title + info.full + '</div>';
        var tt = '<div class="tt_text">' + title + info.full + '</div>';
        
        thumb.data("tt", tt);
        this.container.append(thumb);

        thumb.append(img);
//        var infoDiv = $('<div/>', {class: "thumb_info"});
//        infoDiv.append(title);
//        thumb.append(infoDiv);
        thumb.append(tt);
        
        thumb.addClass('policy');
        if (this.thumbs[index].policy) {
            thumb.addClass(this.thumbs[index].policy);
        }

//        thumb.tooltip({
//            content: tt,
//            //position: {my: "left top", at: "left bottom+6"},
//            position: {my: "left top", at: "left top"},
//            open: function(event, ui) {
//                K5.i18n.k5translate($(ui.tooltip[0]));
//            }
//        });
        this.container.append(thumb);
    },
    getDetails: function(json, info) {
        var model = json["model"];
        var details = json["details"];
        var root_title = json["root_title"];
        if(root_title == null){
            root_title = json.title;
        }
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
                detFull = K5.i18n.translatable('mods.page.partType.' + details.type);
                detShort = K5.i18n.translatable('mods.page.partType.' + details.type);
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
    '_markerRight': function() {
        var mark = $('<li/>', {class: 'thumbmarker', id: 'right-marker'});
        var div = $('<div/>', {class: 'separator'});
        mark.append(div);
        $.get("svg.vm?svg=markline-right", function(data) {
            div.html(data);
        });

        return mark;
    },
    '_markerLeft': function() {
        var mark = $('<li/>', {class: 'thumbmarker', id: 'left-marker'});
        var div = $('<div/>', {class: 'separator'});
        mark.append(div);
        $.get("svg.vm?svg=markline-left", function(data) {
            div.html(data);
        });
        return mark;
    },
    'insertLeftMarker': function(pid) {
        $("#left-marker").remove();
        $(this._markerLeft()).insertBefore("li[data-pid='" + pid + "']");
    },
    'insertRightMarker': function(pid) {
        $("#right-marker").remove();
        $(this._markerRight()).insertAfter("li[data-pid='" + pid + "']");
    },
    clearContainer: function() {
        $("ul.container").remove();

        //this.topArrow.remove();
        //this.bottomArrow.remove();
    }
};

ItemThumbs.prototype.open=function() {
        K5.eventsHandler.trigger("application/menu/ctxchanged", null);
}


ItemThumbs.prototype.isEnabled=function(data) {
        var datanode = data["datanode"];
        return  (!datanode);
}
