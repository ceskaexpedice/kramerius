/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var YearRows = function(elem, options) {
    this.elem = elem;
    this.$elem = $(elem);
    this.maxYear = options.maxYear;
    this.minYear = options.minYear;
    console.log(this.maxYear);
    this.init();
    var th = this;
    $(".years").bind("yearChanged", function(event, params) {
        var y = parseInt(params.year);
        if(y + th.maxBands > th.maxYear){
            y = th.maxYear - th.maxBands + 1;
        }
        th.scroll.find(".row").remove();
        th.activeYear = y;
        for (var i = 0; i < th.maxBands; i++) {
            //if(parseInt(params.year) + i <= th.maxYear){
                var div = $('<div>', {class: 'row'});
                th.scroll.append(div);
                th.addYear(y + i, div);
            //}
        }
        th.checkArrows();
        th.onResize();
    });

}

YearRows.prototype = {
    background: "silver",
    maxBands: 4,
    init: function() {
        this.years = [];
        var th = this;
        this.scroll = $('<div/>', {class: 'scroll'});
        this.$elem.append(this.scroll);
        //this.topArrow = $('<div>', {class: 'icon-arrow icon-arrow-up'});
        this.topArrow = $('<div>', {class: 'medium button'});
        this.topArrow.css({left: 3});
        this.topArrow.load("svg.vm?svg=arrowtop");
        this.$elem.append(this.topArrow);

        //this.bottomArrow = $('<div>', {class: 'icon-arrow icon-arrow-bottom'});
        this.bottomArrow = $('<div>', {class: 'medium button'});
        this.bottomArrow.css({"top": this.$elem.height() - 50, left: 3});
        this.bottomArrow.load("svg.vm?svg=arrowbottom");
        this.$elem.append(this.bottomArrow);


        this.topArrow.click(function() {
            var y = th.activeYear - 1;
            if (!(y.toString() in th.years)) {
                var div = $('<div>', {class: 'row'});
                th.scroll.prepend(div);
                th.addYear(y, div);
                th.activeYear = y;
                th.checkArrows();
            } else {
                th.activeYear = y;
                th.doScroll();
            }
        });
        this.bottomArrow.click(function() {
            var y = th.activeYear + th.maxBands;
            if (!(y.toString() in th.years) && y <= th.maxYear) {

                var div = $('<div>', {class: 'row'});
                th.scroll.append(div);
                th.addYear(y, div);
            }
            //if (y <= th.maxYear) {
            th.activeYear = th.activeYear + 1;
            //}
            th.doScroll();
            th.checkArrows();
        });

    },
    onResize: function() {
        var h = $(document).height();
        if (h < 1) {
            h = $(document).height();
        }
        $('#yearRows').css('height', h - $('#header').height() - $('#canvasda').height());
        this.bottomArrow.css({"top": this.$elem.height() - 50, left: 3});

    },
    addYear: function(year, div) {
        var yearContainer = new YearRow(div, {year: year});
        //this.bands.push(div);
        this.years[year.toString()] = div;
        yearContainer.setSizes();
        return div;
    },
    doScroll: function() {
        var speed = 500;
        var finalPos = this.years[this.activeYear.toString()].position().top + this.scroll.scrollTop();
        var th = this;
        this.scroll.animate({scrollTop: finalPos}, speed, function() {
            th.checkArrows();
        });

    },
    checkArrows: function() {
        var y = this.activeYear + this.maxBands - 1;
        if (this.scroll.scrollTop() < ($(this.scroll)[0].scrollHeight - this.scroll.height())) {
            //if (y < this.maxYear) {
            this.bottomArrow.show();
        } else {
            this.bottomArrow.hide();
        }


        if (this.activeYear === this.minYear) {
            this.topArrow.hide();
        } else {
            this.topArrow.show();
        }
    }
}

var YearRow = function(elem, options) {
    this.elem = elem;
    this.$elem = $(elem);
    this.year = options.year;
    this.init();
}

YearRow.prototype = {
    background: "silver",
    rowsPerRequest: 50,
    thumbHeight: 128,
    panelWidth: 500,
    panelHeight: 138,
    init: function() {


        this.scroll = $('<div/>', {class: 'scroll'});
        this.container = $('<ul/>');
        this.container.addClass('container');
        this.scroll.append(this.container);
        this.$elem.append(this.scroll);

        //this.rightArrow = $('<div>', {class: 'arrow arrowRight'});
        this.rightArrow = $('<div>', {class: 'medium button'});
        this.rightArrow.css({right: "25px", top: 50});
        this.rightArrow.load("svg.vm?svg=arrowright");

        //this.leftArrow = $('<div>', {class: 'arrow arrowLeft'});
        this.leftArrow = $('<div>', {class: 'medium button'});
        this.leftArrow.css({left: "24px", top: "50px"});
        this.leftArrow.load("svg.vm?svg=arrowleft");
        this.$elem.append(this.leftArrow);
        this.$elem.append(this.rightArrow);

        this.scrolling = false;

        this.titleBand = $('<div>', {class: 'rowtitle', title: 'filter'});
        this.titleBand.click(_.bind(function() {
            K5.api.gotoResultsPage("&rok=" + this.year);
        }, this));
        this.$elem.append(this.titleBand);
        this.titleBand.html(this.year.toString());

        this.totalBand = $('<div>', {class: 'total'});
        this.totalBand.click(_.bind(function() {
            K5.api.gotoResultsPage("&rok=" + this.year);
        }, this));
        this.$elem.append(this.totalBand);


        this.getThumbs({offset: 0});
        var obj = this;
        this.rightArrow.click(function() {
            obj.doScroll(1);
        });
        this.leftArrow.click(function() {
            obj.doScroll(-1);
        });
        if (isTouchDevice()) {
            this.$elem.swipe({
                swipeLeft: function(event, direction, distance, duration, fingerCount) {
                    if (!obj.scrolling) {
                        obj.doScroll(1);
                    }
                },
                swipeRight: function(event, direction, distance, duration, fingerCount) {
                    if (!obj.scrolling) {
                        obj.doScroll(-1);
                    }
                },
                threshold: 2
            });
        }else{
            this.$elem.mousewheel(function(event) {
                if (!obj.scrolling) {
                    if (event.deltaX !== 0) {
                        obj.doScroll(event.deltaX);
                    } else {
                        obj.doScroll(-event.deltaY);
                    }
                }
            });
        }


        this.leftArrow.hide();
    },
    setSizes: function() {

        this.panelHeight = this.$elem.height();
        this.panelWidth = this.$elem.width();
    },
    doScroll: function(dx) {
        var speed = 500;
        var finalPos = this.scroll.scrollLeft() + this.panelWidth * 0.8 * dx;
        var th = this;
        th.scrolling = true;
        this.scroll.animate({scrollLeft: finalPos}, speed, function() {
            th.checkArrows();
            th.scrolling = false;
        });
        //this.checkArrows();

    },
    checkArrows: function() {
        if (this.scroll.scrollLeft() <= 0) {
            this.leftArrow.hide();
        } else {
            this.leftArrow.show();
        }

        if (this.scroll.scrollLeft() >= ($(this.scroll)[0].scrollWidth - this.scroll.width())) {
            this.rightArrow.hide();
        } else {
            this.rightArrow.show();
        }
    },
    getThumbs: function(params) {
        //var query = "fl=dc.creator,dc.title,PID,dostupnost,model_path&fq=fedora.model:monograph OR fedora.model:map&q=rok:" + this.year + "&start=" + params.offset + "&rows=" + this.rowsPerRequest;
        var query = "fl=root_pid,dc.creator,root_title,dc.title,PID,dostupnost,model_path,fedora.model"+
                "&group=true&group.ngroups=true&group.field=root_pid&group.format=simple&group.sort=level asc"+
                "&q=rok:" + this.year + "&start=" + params.offset + "&rows=" + this.rowsPerRequest;
                //"&defType=edismax&bq=(level:0)^4.5&&bq=(level:1)^3.5&bq=(level:2)^2.5";
        K5.api.askForSolr(query, _.bind(function(data) {
            //this.docs = {"docs": data.response.docs, "count": data.response.numFound};
            //this.totalBand.html(data.response.numFound);
            this.docs = {"docs": data.grouped.root_pid.doclist.docs, "count": data.grouped.root_pid.ngroups};
            this.totalBand.html(data.grouped.root_pid.ngroups);
            this.render();
        }, this), "application/json");

    },
    render: function() {
        var docs = this.docs.docs;
        for (var i = 0; i < docs.length; i++) {
            this.addThumb(docs[i]);
        }

    },
    addThumb: function(doc) {
        var pid = doc["PID"];
        var root_pid = doc["root_pid"];
        var model = doc["fedora.model"];
        var imgsrc = "api/item/" + pid + "/thumb";
        var thumb = $('<li/>', {class: 'thumb'});
        thumb.data("metadata", doc["root_title"]);
        var title = doc["root_title"];
        var dctitle = doc["dc.title"];
        var typtitulu = doc["model_path"][0].split("/")[0];
        var shortTitle = title;
        var creator = "";
        var maxLength = 90;
        var showToolTip = false;
        if (shortTitle.length > maxLength) {
            shortTitle = shortTitle.substring(0, maxLength) + "...";
            showToolTip = true;
        }
        shortTitle = '<div class="title">' + shortTitle + '</div>';
        if (doc["dc.creator"]) {
            creator = '<div class="autor">' + doc["dc.creator"] + '</div>';
        }
        var titletag = '<div class="title">' + title + '</div>';
        if(title !== dctitle){
            titletag = titletag + '<div class="dctitle">' + dctitle + '</div>';
            shortTitle = shortTitle + '<div class="dctitle">' + dctitle.substring(Math.min(30, dctitle.length)) + '</div>';
        }
        var modeltag = '<div class="title">' + K5.i18n.translatable('fedora.model.' + model) + '</div>';
        thumb.data("pid", pid);
        thumb.data("root_pid", root_pid);
        this.container.append(thumb);
        var policy = $('<div/>', {class: 'policy'});
        if (doc['dostupnost']) {
            policy.addClass(doc['dostupnost']);
        }
        thumb.append(policy);
        if (showToolTip) {
            thumb.attr("title", titletag + creator + modeltag);
            thumb.tooltip({
                content: titletag + creator,
                position: {my: "left bottom-1", at: "right-1 bottom"}
            });
        }
        thumb.click(function() {
            K5.api.gotoDisplayingItemPage($(this).data('pid'));
        });

        var divimg = $('<div/>', {class: 'img'});
        var img = $('<img/>', {src: imgsrc});
        $(thumb).append(divimg);
        $(divimg).append(img);

        var ithumb = $('<div/>', {class: 'info'});
        ithumb.html(shortTitle + creator + modeltag);
        thumb.append(ithumb);
    }
}
