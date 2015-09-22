/* 
 * Copyright (C) 2014 alberto
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/* global K5 */

K5.eventsHandler.addHandler(function(type, configuration) {
    if (type === "widow/url/hash") {
        if (K5.gui["da"]) {


            var show = window.location.hash;
            if (show.length > 1) {
                show = parseInt(show.substring(1));
            } else {
                show = K5.gui["da"].yearWithMaxCount - 1;
            }
            $(".years").trigger('yearChanged', {
                "year": show,
                "count": K5.gui["da"].years[show.toString()].count,
                "offset": 0,
                "accumulated": K5.gui["da"].years[show.toString()].accumulated
            });
        }

    }
    if (type === "i18n/dictionary") {
        $(document).prop('title', K5.i18n.ctx.dictionary['application.title'] + ". " + K5.i18n.ctx.dictionary['common.timeline']);
        if (!K5.gui["da"]) {
            var rowsImpl = true;
            if (rowsImpl) {
                //pouzivame rows.js
                var maxYear = 0;
                var minYear = 3000;
                for (var i = 0; i < ja.length; i++) {
                    var year = parseInt(ja[i]);
                    var val = parseInt(ja[++i]);
                    var curDate = new Date();
                    var curYear = curDate.getFullYear();
                    if (year > 1000 && year <= curYear) {
                        maxYear = Math.max(maxYear, year);
                        minYear = Math.min(minYear, year);
                    }

                }
                var th = new YearRows('#yearRows', {maxYear: maxYear, minYear: minYear});
                var da = new Da('#canvasda', ja);
                $("#yearRows").bind("wresize", function() {
                    th.onResize();
                });
                da.render();
                //setTimeout('th.onResize()', 100);

            } else {
                //pouzivame canvasThumbs.js

                $('#canvasthumbs').css('height', $(window).height() - $('#canvasda').height());
                var th = new Thumbs('#canvasthumbs');
                var th = null;
                var da = new Da('#canvasda', ja, th);
                $(window).resize(function() {
                    $('#canvasthumbs').css('height', $(window).height() - $('#canvasda').height());
                    th.onResize(th);
                });
                da.render();
            }
            K5.gui["da"] = da;
        }
    }
});




var Da = function(elem, jarray, th, options) {
    this.div = elem;
    this.$div = $(elem);
    this.canvas = elem + ">canvas";
    this.$canvas = $(elem + ">canvas");
    this.thumbs = th;
    if (options) {
        this.options = options;
    }
    this.maxYear = 0;
    this.minYear = 3000;
    this.yearWithMaxCount = 0;
    this.maxCount = 0;
    this.minCount = 0;
    this.years = [];
    var val;
    var year;
    var accumulated = 0;
    this.jarray = [];
    var curDate = new Date();
    var curYear = curDate.getFullYear();
    for (var i = 0; i < jarray.length; i++) {
        year = parseInt(jarray[i]);
        val = parseInt(jarray[++i]);
        if (year > 1000 && year <= curYear) {
            this.years[year] = {"count": val, "accumulated": accumulated};
            accumulated += val;
            this.maxYear = Math.max(this.maxYear, year);
            this.minYear = Math.min(this.minYear, year);
            if (val > this.maxCount) {
                this.maxCount = val;
                this.yearWithMaxCount = year;
                this.indexWithMax = i;
            }

            this.minCount = Math.min(this.minCount, val);
        }

    }
    this.period = this.maxYear - this.minYear + 1;
    var da = this;
    this.init();
    $(this.canvas).click(function(event) {
        da.onClick(event, da);
    });
    $(this.canvas).mousemove(function(event) {
        da.onMouseMove(event, da);
    });
    $(this.canvas).mouseout(function(event) {
        da.toggleBar(-1);
        da.selBar = -1;

    });

    this.scrolling = false;
    if (isTouchDevice()) {
        $(this.canvas).swipe({
            swipeLeft: function(event, direction, distance, duration, fingerCount) {
                if (!da.scrolling) {
                    da.doScroll(1);
                }
                event.stopPropagation();
            },
            swipeRight: function(event, direction, distance, duration, fingerCount) {
                if (!da.scrolling) {
                    da.doScroll(-1);
                }
                event.stopPropagation();
            },
            threshold: 20
        });
    }else{
        $(this.canvas).mousewheel(function(event) {
            if (!da.scrolling) {
                if (event.deltaX !== 0) {
                    da.doScroll(event.deltaX);
                } else {
                    da.doScroll(-event.deltaY);
                }
            }
        });
    }

    $(this.canvas).bind("yearWanted", function(event, params) {
        var year = params.year;
        var count = 0;
        if (da.years[year]) {
            count = da.years[year].count
        }
        var cy = parseInt(year);

        while (!da.years[cy.toString()] && cy > da.minYear) {
            cy--;
        }

        $("canvas").trigger('yearAdding', {"year": year, "count": count, "offset": 0, "accumulated": da.years[cy.toString()].accumulated});
    });


};

Da.prototype = {
    barColor: "orange",
    barColorSel: "red",
    barsPanelheight: 300,
    barsPanelWidth: "100%",
    barMargin: 2,
    barWidth: 6,
    scale: 1.0,
    selBar: -1,
    barInfoDataCache: null,
    shadowBlur: 6,
    shadowOffset: 4,
    shadowColor: "rgba(0,0,0,0.2)",
    infoFont: "12px Arial",
    lineWidth: 0.5,
    infoRect: new Rectangle(0, 0, 0, 0),
    infoBg: "#000000",
    infoColor: "#FFFF00",
    infoPadding: 10,
    infoArrowW: 10,
    init: function() {


        var infocss = $(this.div).find("div.info");
        this.infoFont = $(infocss).css("font");
        this.lineWidth = parseInt($(infocss).css("fontWeight")) / 1000.0;
        //alert(this.lineWidth);
        this.infoBg = $(infocss).css("background-color");
        this.infoColor = $(infocss).css("color");
        
        var labelcss = $(this.div).find("div.label");
        this.labelFont = $(labelcss).css("font");
        this.labelLineWidth = parseInt($(labelcss).css("fontWeight")) / 1000.0;
        this.labelBg = $(labelcss).css("background-color");
        this.labelColor = $(labelcss).css("color");

        var barcss = $(this.div).find("div.bar");
        this.barColor = $(barcss).css("background-color");
        this.barWidth = $(barcss).width();

        var barSelcss = $(this.div).find("div.bar>div.sel");
        this.barColorSel = $(barSelcss).css("background-color");

        this.barsPanelheight = $(this.canvas).height();
        $(this.canvas).attr("height", this.barsPanelheight);
        var totalWidth = this.period * (this.barMargin + this.barWidth);
        this.barsPanelWidth = totalWidth;
        $(this.canvas).css("width", totalWidth);
        $(this.canvas).attr("width", totalWidth);
        this.scale = (1.0 * this.barsPanelheight) / this.maxCount;

        this.infoCanvas = $('<canvas/>');
        $(this.infoCanvas).css({left: 0, top: 0, "z-index": 20, "position": "absolute", "cursor": "pointer"});
        $(this.infoCanvas).css("width", totalWidth);
        $(this.infoCanvas).attr("width", totalWidth);
        $(this.infoCanvas).css("height", $(this.canvas).height());
        $(this.infoCanvas).attr("height", $(this.canvas).height());
        this.$div.append(this.infoCanvas);


    },
    render: function() {
        for (var i = 0; i <= this.period; i++) {
            if ((i + this.minYear) % 10 === 0) {
                var l = i * (this.barMargin + this.barWidth);
                var c = this.labelBg;
                var h = 10;
                if ((i + this.minYear) % 100 === 0) {
                    h = this.barsPanelheight;
                    c = this.labelColor;
                }
                this.$canvas.drawLine({
                    strokeStyle: c,
                    strokeWidth: 1,
                    x1: l, y1: 0,
                    x2: l, y2: h
                });
                if ((i + this.minYear) % 100 === 0) {
                    var text = (i + this.minYear).toString();
                    var da = this;
                    this.$canvas.draw({
                        fn: function(ctx) {
                            ctx.save();
                            da.setFont(ctx);
                            ctx.fillStyle = da.labelColor;
                            ctx.fillText(text, l + 3, 10);
                            ctx.restore();
                        }
                    });
                }
            }
            this.addBar(i);
        }
        this.scrollTo(this.indexWithMax * (this.barMargin + this.barWidth));
        var show = window.location.hash;
        if (show.length > 1) {
            show = parseInt(show.substring(1));
        } else {
            show = this.yearWithMaxCount - 1;
        }
        $(".years").trigger('yearChanged', {
            "year": show,
            "count": this.years[show.toString()].count,
            "offset": 0,
            "accumulated": this.years[show.toString()].accumulated
        });

        return this;
    },
    drawBar: function(i, color) {

        var h = 0;
        if (this.years[i + this.minYear]) {
            h = this.years[i + this.minYear].count;
        }
        if (h > 0) {
            var l = i * (this.barMargin + this.barWidth);
            var w = this.barWidth;
            this.$canvas.drawRect({
                fillStyle: color,
                x: l,
                y: Math.min(this.barsPanelheight - 3, this.barsPanelheight - h * this.scale),
                width: w,
                height: Math.max(3, h * this.scale),
                fromCenter: false
            });
        }
    },
    drawBarInfo: function(i) {
        if (i > -1) {
            var h = 0;
            if (this.years[i + this.minYear]) {
                h = this.years[i + this.minYear].count;
            }
            var texto = "" + (i + this.minYear) + ": ~" + h;
            var l = i * (this.barMargin + this.barWidth);

            this.infoCanvas.drawBarInfo({
                da: this,
                barTop: this.barsPanelheight - h * this.scale,
                y: 2,
                x: l,
                text: texto,
                index: i
            });
        } else {
            this.infoCanvas.hideBarInfo({da: this});
        }
    },
    addBar: function(i) {
        this.drawBar(i, this.barColor);
    },
    toggleBar: function(i) {
        if (this.selBar > -1) {
            this.drawBar(this.selBar, this.barColor);
        }
        this.selBar = i;
        //this.drawBar(i, this.barColorSel);
        this.drawBarInfo(i);
    },
    addItem: function(left, top) {
        this.$canvas.drawArc({
            fillStyle: 'black',
            x: left, y: top,
            radius: 50
        });
    },
    setShadow: function(context) {
        context.shadowBlur = this.shadowBlur;
        context.shadowOffsetX = this.shadowOffset;
        context.shadowOffsetY = this.shadowOffset;
        context.shadowColor = this.shadowColor;
    },
    setFont: function(context) {
        context.lineWidth = this.lineWidth;
        context.font = this.infoFont;
    },
    onMouseMove: function(event, da) {
        var i = Math.floor(event.offsetX / (da.barMargin + da.barWidth));
        da.toggleBar(i);
    },
    doScroll: function(dx) {
        var finalPos = this.$div.scrollLeft() + this.$div.width() * 0.5 * dx;
        this.scrollTo(finalPos);
    },
    scrollTo: function(to) {
        var speed = 500;
        var obj = this;
        this.scrolling = true;
        this.$div.animate({scrollLeft: to}, speed, function() {
            obj.scrolling = false;
        });
        if (to <= 0) {
            //this.leftArrow.hide();
        } else {
            //this.leftArrow.show();
        }
    },
    onClick: function(event, da) {
        var i = Math.floor(event.offsetX / (da.barMargin + da.barWidth));
        var year = i + da.minYear;
        var val = 0;
        if (da.years[year]) {
            val = da.years[year].count;
        }
        var cy = parseInt(year);

        while (!da.years[cy.toString()] && cy > da.minYear) {
            cy--;
        }
        $(".years").trigger('yearChanged', {"year": year, "count": val, "offset": 0, "accumulated": da.years[cy].accumulated});
        window.location.hash = year.toString();
    }
};


// Create a hideBarInfo() method
$.jCanvas.extend({
    name: 'hideBarInfo',
    type: 'hideInfo',
    props: {},
    fn: function(ctx, params) {
        var da = params.da;
        if (da.barInfoDataCache !== null) {
            ctx.putImageData(da.barInfoDataCache, da.infoRect.getX(), da.infoRect.getY());
        }
    }
});

// Create a drawBarInfo() method
$.jCanvas.extend({
    name: 'drawBarInfo',
    type: 'barInfo',
    props: {},
    fn: function(ctx, params) {
        // Just to keep our lines short
        //var p = params;
        var da = params.da;
        var barTop = params.barTop;
        var y = params.y;
        var x = params.x;
        var text = params.text;
        //scrollBarsPanel.getHorizontalScrollPosition()
        var scrollPos = $(da.div).scrollLeft();

        //content.getOffsetWidth()
        var rightBorder = da.barsPanelWidth;

        if (da.barInfoDataCache !== null) {
            ctx.putImageData(da.barInfoDataCache, da.infoRect.getX(), da.infoRect.getY());
        }

        var barCenter = x + (da.barWidth + da.barMargin) / 2;

        ctx.save();
        da.setFont(ctx);
        //
        var w = ctx.measureText(text).width + da.infoPadding * 2;
        var h = 5 + da.infoPadding * 2;
        ctx.restore();
        var points = [];

        if (barTop - y - h < 5) {
            // big bar
            var dx = 15;
            var dy = 10;
            var fx = barCenter - w - dx;

            if (fx < scrollPos) {
                fx = barCenter + w + dx;
            }
            da.infoRect = new Rectangle(fx - 2 - da.shadowBlur, 0,
                    barCenter + da.barWidth - fx + 8 + da.shadowBlur * 2 + da.shadowOffset, da.barsPanelheight);
            points.push(new Point(fx, barTop + dy));
            points.push(new Point(fx + w - da.infoArrowW, barTop + dy));
            points.push(new Point(barCenter, barTop));
            points.push(new Point(fx + w, barTop + dy));
            points.push(new Point(fx + w, barTop + dy + h));
            points.push(new Point(fx, barTop + dy + h));
            points.push(new Point(fx, barTop + dy));


        } else {

            var fx = x - w / 2;
            if (fx + w > rightBorder) {
                fx = rightBorder - w;
            }

            if (fx < scrollPos) {
                fx = scrollPos;
            }
            da.infoRect = new Rectangle(fx - 2 - da.shadowBlur, 0, w + 8 + da.shadowBlur * 2 + da.shadowOffset, da.barsPanelheight);
            points.push(new Point(fx, y));
            points.push(new Point(fx + w, y));
            points.push(new Point(fx + w, y + h));
            points.push(new Point(barCenter, barTop));
            points.push(new Point(fx + w - da.infoArrowW, y + h));
            points.push(new Point(fx, y + h));
            points.push(new Point(fx, y));
        }
        da.barInfoDataCache = ctx.getImageData(da.infoRect.getX(), da.infoRect.getY(), da.infoRect.getWidth(), da.infoRect.getHeight());

        ctx.save();
        ctx.fillStyle = da.infoBg;
        ctx.strokeStyle = da.infoColor;
        da.setShadow(ctx);
        ctx.beginPath();
        ctx.moveTo(points[0].getX(), points[0].getY());
        for (var i = 1; i < points.length; i++) {
            ctx.lineTo(points[i].getX(), points[i].getY());
        }
        ctx.fill();
        ctx.stroke();
        ctx.closePath();
        ctx.restore();

        ctx.save();
        ctx.fillStyle = da.infoColor;
        ctx.strokeStyle = da.infoColor;
        da.setFont(ctx);
        ctx.fillText(text, points[0].getX() + da.infoPadding, points[0].getY() + da.infoPadding + 7);
        ctx.restore();

        da.drawBar(params.index, da.barColorSel);
        /*
         ctx.save();
         ctx.fillStyle = da.barColorSel;
         ctx.fillRect(x, barTop, da.barWidth, da.barsPanelheight - barTop);
         ctx.restore();
         */

        // Call the detectEvents() function to enable jCanvas events
        // Be sure to pass it these arguments, too!
        $.jCanvas.detectEvents(this, ctx, params);
        // Call the closePath() functions to fill, stroke, and close the path
        // This function also enables masking support and events
        // It accepts the same arguments as detectEvents()    
        $.jCanvas.closePath(this, ctx, params);
    }
});


