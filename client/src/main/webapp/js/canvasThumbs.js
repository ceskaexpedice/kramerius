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
var Thumbs = function(elem, options) {
    this.div = elem;
    this.$div = $(elem);
    this.canvas = elem + ">canvas";
    this.$canvas = $(elem + ">canvas");
    if (options) {
        this.options = options;
    }
    var th = this;
    this.init();
    $(this.canvas).click(function(event) {
        th.onClick(event,th);
    });
    $(this.canvas).mousemove(function(event) {
        th.onMouseMove(event, th);
    });
    $(this.canvas).mouseout(function(event) {
        th.thumbIndex = -1;
        th.$canvas.hideThumbInfo({th: th});
    });
    this.$div.addClass("listener");
    $(this.canvas).bind("yearChanged", function(event, params) {
        th.$canvas.clearCanvas();
        th.offset = params.accumulated;
        th.startYear = params.year;
        th.getYear(params);
    });
    $(this.canvas).bind("yearAdding", function(event, params) {
        th.getYear(params);
    });
};


Thumbs.prototype = {
    panelHeight: 400,
    panelWidth: 400,
    thumbHeight: 128,
    margin: 20,
    colWidth: 100,
    rowHeight: 100,
    rowsPerRequest: 100, 
    selBandHeight: 40,
    selBandBg: "#999999",
    selBandColor: "#010101",
    selBandStrokeWidth: 1,
    
    infoDataCache: null,
    shadowBlur: 5,
    shadowOffset: 5,
    shadowColor: "rgba(0,0,0,0.2)",
    
    infoFont: "12px sans-serif",
    lineWidth: 1.0,
    infoRect: new Rectangle(0, 0, 0, 0),
    infoBg: "#FFFFFF",
    infoColor: "#000000",
    infoPadding: 10,
    infoArrowW: 10,
    infoMaxWidth: 350,
    
    yearOddColor: "#ededed",
    yearEvenColor: "#cdcdcd",
    init: function() {
        this.docs = {};
        this.thumbIndex = -1;
        this.setSizes();
    },
    setSizes: function(){
        this.panelHeight = $(this.div).height();
        $(this.canvas).attr("height", this.panelHeight);
        var totalWidth = $(this.div).width();
        this.panelWidth = totalWidth;
        $(this.canvas).css("width", totalWidth);
        $(this.canvas).attr("width", totalWidth);
        this.thumbsPerCol = Math.floor((this.panelHeight - this.margin - this.selBandHeight) / (this.thumbHeight + this.margin));
        this.visibleCols = Math.floor(totalWidth / this.colWidth);
        this.visibleThumbs = this.visibleCols * this.thumbsPerCol;
        this.rowHeight = this.margin + this.thumbHeight;
    },
    onResize: function(th) {
        th.setSizes();
        th.renderYear(th);
    },
    onClick: function(event, th) {
        
    },
    onMouseMove: function(event, th) {
        var col = Math.floor((event.offsetX -th.margin) / th.colWidth);
        var row = Math.floor((event.offsetY -th.margin) / (th.thumbHeight + th.margin));
            var index = col * this.thumbsPerCol + row;
        if(row >= th.thumbsPerCol || index>=th.docs.length || col >= th.visibleCols){
            th.thumbIndex = -1;
            th.$canvas.hideThumbInfo({th: th});
            
        }else{
            if(th.thumbIndex !== index){
                th.thumbIndex = index;
                var year = th.startYear;
                
                if(th.docs[year]){
                    th.drawInfo({
                        col: col,
                        row: row
                    });
                }
            }
        }
    },
    getYear: function(params){
  	var rows = Math.min(params.count, this.rowsPerRequest);
	var query = "fl=dc.creator,dc.title,PID,dostupnost&fq=fedora.model:monograph OR fedora.model:map&q=rok:"+ params.year+"q=rok:" + params.year+"&start="+params.offset+"rows="+rows;
	K5.api.askForSolr(query, _.bind(function(data) {
            this.docs[params.year.toString()] = {"docs": data.response.docs, "accumulated": params.accumulated};
            this.renderYear(th, params.year);
            if(params.accumulated - th.offset < th.visibleThumbs){
                $("canvas").trigger('yearWanted', {"year": params.year+1});
            }
	},this));	
    },
    renderYear: function(th, year){
        th.loading = true;
        th.loaded = 0;
        var acc = th.docs[year.toString()].accumulated;
        th.drawSelBand(year, th.docs[year.toString()].docs.length, acc - th.offset);
        for(var i=0; i<th.docs[year.toString()].docs.length; i++){
            var imgsrc = "img?pid=" + th.docs[year.toString()].docs[i]["PID"] + "&h=" + th.thumbHeight;
            th.addThumb(i + acc - th.offset, imgsrc, year);
        }
    },
    colorByYear: function(year){
        var color;
        if(year % 2 == 0){
            color = this.yearOddColor;
        }else{
            
            color = this.yearEvenColor;
        }
        return color
    },
    drawSelBand: function(year, count, i){
        var color = this.colorByYear(year);
        var w = Math.floor(count / this.thumbsPerCol) * this.colWidth;
        var left = (Math.floor(i / this.thumbsPerCol) * this.colWidth) + this.margin;
        var rect = new Rectangle(left, this.panelHeight - this.selBandHeight, w, this.selBandHeight);
        
        this.$canvas.drawRect({
                fillStyle: color,
                x: rect.getCenter().getX(),
                y: rect.getCenter().getY(),
                width: rect.getWidth(),
                height: rect.getHeight()
        });
        var th = this;
        this.$canvas.draw({
            fn: function(ctx) {
              ctx.save();
              th.setFont(ctx);
              var tw = ctx.measureText(year.toString()).width;
              var l = rect.getCenter().getX() - tw/2;
              ctx.fillStyle = th.infoColor;
              ctx.fillText(year.toString(), l, rect.getCenter().getY());
              ctx.restore();
            }
          });
    },
    addThumb:function(i, img, year){
        var top = this.margin + (i % this.thumbsPerCol) * (this.thumbHeight + this.margin);
        var left = (Math.floor(i / this.thumbsPerCol) * this.colWidth) + this.margin;
        
        var color = this.colorByYear(year);
        var rect = new Rectangle(left, top, this.colWidth, this.rowHeight);
        this.$canvas.drawRect({
            fillStyle: color,
            x: rect.getX(),
            y: rect.getY(),
            width: rect.getWidth(),
            height: rect.getHeight(),
            fromCenter: false
        });
        this.$canvas.drawImage({
            x: rect.getCenter().getX(),
            y: rect.getCenter().getY(),
            source: img
        });
    },
    drawInfo:function(params){
        if(this.thumbIndex === -1) return;        
        var year = 0;
        var posInYear =0;
        var th = this;
        $.each(th.docs, function(key, item){
            if(item){
                var count = item.docs.length;
                var yearMin = item.accumulated - th.offset;
                var yearMax = item.accumulated - th.offset + count;
                if(th.thumbIndex>=yearMin && th.thumbIndex<yearMax ){
                    year = key;
                    posInYear = th.thumbIndex - yearMin;
                    return false;
                }
            }
        });
        if (year == 0) return;
        var title = this.docs[year].docs[posInYear]["dc.title"];
        var top = this.margin + (this.thumbIndex % this.thumbsPerCol) * (this.thumbHeight + this.margin);
        var left = (Math.floor(this.thumbIndex / this.thumbsPerCol) * this.colWidth) + this.margin;
        var center = new Point(left + this.colWidth/2, top + this.thumbHeight/2);
        this.$canvas.drawThumbInfo({
            th: this,
            y: top,
            x: left,
            center: center,
            text: title
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
    }
    
}




// Create a drawThumb() method
$.jCanvas.extend({
    name: 'drawThumb',
    type: 'drawThumb',
    props: {},
    fn: function(ctx, params) {
        var th = params.th;
        var rect = params.rect;
        var image = new Image();
        // When the image has loaded, draw it to the canvas
        image.onload = function(){
            ctx.drawImage(image, params.x, params.y);
            th.loaded++;
        }
        
        image.onerror = function(){
            th.loaded++;
        }

        // Now set the source of the image that we want to load
        image.src = params.url;
        
        
    }
});

// Create a hideThumbInfo() method
$.jCanvas.extend({
    name: 'hideThumbInfo',
    type: 'hideThumbInfo',
    props: {},
    fn: function(ctx, params) {
        var th = params.th;
        if (th.infoDataCache !== null) {
            ctx.putImageData(th.infoDataCache, th.infoRect.getX(), th.infoRect.getY());
            th.infoDataCache = null;
        }
    }
});

// Create a drawThumbInfo() method
$.jCanvas.extend({
    name: 'drawThumbInfo',
    type: 'drawThumbInfo',
    props: {},
    fn: function(ctx, params) {
        var th = params.th;
        var y = params.y;
        var x = params.x;
        var text = params.text;

        var thumbCenter = params.center;
        //scrollBarsPanel.getHorizontalScrollPosition()
        var scrollPos = $(th.div).scrollLeft();

        if (th.infoDataCache !== null) {
            ctx.putImageData(th.infoDataCache, th.infoRect.getX(), th.infoRect.getY());
        }

        ctx.save();
        
        
        //calculate rect
        var lineHeight = 20;
        var words = text.split(" ");
        var line = "";
        var tdy = 0;
        var MAX_TEXT_WIDTH = th.infoMaxWidth - th.infoPadding;
        var textWidth = 0;
        var lines = [];

        th.setFont(ctx);
        for (var n = 0; n < words.length; n++) {
            var testLine = line + words[n] + " ";
            var testWidth = ctx.measureText(testLine).width;
            if (testWidth > MAX_TEXT_WIDTH) {
                lines.push(line);
                line = words[n] + " ";
                tdy += lineHeight;
                textWidth = Math.max(textWidth, testWidth);
            } else {
                line = testLine;
                textWidth = Math.max(textWidth, testWidth);
            }
        }
        lines.push(line);
        tdy += lineHeight;
        var textSize = new Size(textWidth, tdy);
        
        
        var w = Math.min(th.infoMaxWidth, textSize.getWidth() + th.infoPadding * 2);
        var h = textSize.getHeight() + th.infoPadding * 2;
        ctx.restore();
        var points = [];
        
        var topleft = new Point(thumbCenter.getX() + th.colWidth/2, y);
        

        if (topleft.getX() + w > th.panelWidth) {
            // thumb near right
            // 
            topleft.setX(thumbCenter.getX() - th.colWidth/2 - w);

            if (topleft.getY() + textSize.getHeight() > th.panelHeight) {
                topleft.setY(th.panelHeight - textSize.getHeight() - 10);
            }
            
            th.infoRect = new Rectangle(Math.min(topleft.getX(), thumbCenter.getX()) - 2 - th.shadowBlur, 
                    topleft.getY() - 2 - th.shadowBlur ,
                    th.colWidth/2 + w + th.shadowBlur*3 + 4, 
                    h + th.shadowBlur*3 + 4);
            points.push(new Point(topleft.getX(), topleft.getY()));
            points.push(new Point(topleft.getX() + w, topleft.getY()));
            points.push(new Point(thumbCenter.getX(), thumbCenter.getY()));
            points.push(new Point(topleft.getX() + w, topleft.getY() + th.infoArrowW ));
            
            points.push(new Point(topleft.getX() + w, topleft.getY() + h));
            points.push(new Point(topleft.getX(), topleft.getY() + h));
            points.push(new Point(topleft.getX(), topleft.getY()));

        } else {


            if (topleft.getY() + textSize.getHeight() > th.panelHeight) {
                topleft.setY(th.panelHeight - textSize.getHeight() - 10);
            }
            
            th.infoRect = new Rectangle(Math.min(topleft.getX(), thumbCenter.getX()) - 2 - th.shadowBlur, 
                    topleft.getY() - 2 - th.shadowBlur ,
                    th.colWidth/2 + w + th.shadowBlur*3 + 4, 
                    h + th.shadowBlur*3 + 4);
            points.push(new Point(topleft.getX(), topleft.getY()));
            points.push(new Point(topleft.getX() + w, topleft.getY()));
            points.push(new Point(topleft.getX() + w, topleft.getY() + h));
            points.push(new Point(topleft.getX(), topleft.getY() + h));
            points.push(new Point(topleft.getX(), topleft.getY() + th.infoArrowW ));
            points.push(new Point(thumbCenter.getX(), thumbCenter.getY()));
            points.push(new Point(topleft.getX(), topleft.getY()));
        }
        th.infoDataCache = ctx.getImageData(th.infoRect.getX(), th.infoRect.getY(), th.infoRect.getWidth(), th.infoRect.getHeight());

        ctx.save();
        ctx.fillStyle = th.infoBg;
        
        var stopPos = 1 - 8.0 / h;
        var grd=ctx.createLinearGradient(0,topleft.getY(),0,topleft.getY()+h);
        grd.addColorStop(0,"#eeeeee");
        grd.addColorStop(stopPos,"#eeeeee");
        grd.addColorStop(1,"#656565");

        ctx.fillStyle=grd;

        ctx.beginPath();
        ctx.moveTo(points[0].getX(), points[0].getY());
        for (var i = 1; i < points.length; i++) {
            ctx.lineTo(points[i].getX(), points[i].getY());
        }
        th.setShadow(ctx);
        ctx.fill();
        ctx.restore();
        
        ctx.save();
        ctx.strokeStyle = th.infoColor;
        ctx.stroke();
        ctx.closePath();
        ctx.restore();
        
        ctx.save();
        ctx.fillStyle = th.infoColor;
        ctx.strokeStyle = th.infoColor;
        th.setFont(ctx);
        var lineHeight = 20;
        var words = text.split(" ");
        var line = "";
        var tx = points[0].getX() + th.infoPadding;
        var ty = points[0].getY() + th.infoPadding + 15;
        var tdy = 0;

        for (var n = 0; n < lines.length; n++) {
                ctx.fillText(lines[n], tx, ty + tdy);
                tdy += lineHeight;
        }
        ctx.restore();




        // Call the detectEvents() function to enable jCanvas events
        // Be sure to pass it these arguments, too!
        $.jCanvas.detectEvents(this, ctx, params);
        // Call the closePath() functions to fill, stroke, and close the path
        // This function also enables masking support and events
        // It accepts the same arguments as detectEvents()    
        $.jCanvas.closePath(this, ctx, params);
    }
});


