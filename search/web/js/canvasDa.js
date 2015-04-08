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


var Da = function(elem, data, options) {
    this.div = elem;
    this.$div = $(elem);
    this.canvas = elem + ">canvas";
    this.$canvas = $(elem + ">canvas");
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
    if(options.isXml){
        var jarray = [];
        var xmlDoc = $.parseXML( data );
        $xml = $( xmlDoc );
        $xml.find( 'lst[name="rok"]' ).find('int').each(function(){
            
            jarray.push($(this).attr("name"));
            jarray.push($(this).text());
          });
        this.jarray = jarray;
    }else{
        this.jarray = data;
    }
    
    for (var i = 0; i < this.jarray.length; i++) {
        year = parseInt(this.jarray[i]);
        val = parseInt(this.jarray[++i]);
        if (year > 1000 && year<2015) {
            this.years[year] = {"count": val, "accumulated": accumulated};
            accumulated += val;
            this.maxYear = Math.max(this.maxYear, year);
            this.minYear = Math.min(this.minYear, year);
            if(val > this.maxCount){
                this.maxCount = val;
                this.yearWithMaxCount = year;
                this.indexWithMax = i;
            }
            
            this.minCount = Math.min(this.minCount, val);
        }
        
    }
    
    var pred = this.minYear;
    while(pred%10 !== 0){
        pred--;
    }
    this.minYear = pred;
    
    this.period = this.maxYear - this.minYear;
    var da = this;
    this.init();
    
    $(this.canvas).bind("yearWanted", function(event, params) {
        var year = params.year;
        var count = 0;
        if(da.years[year]){
            count = da.years[year].count;
        }
        var cy = parseInt(year);
        
        while(!da.years[cy.toString()] && cy>da.minYear){
            cy--;
        }
        
        $("canvas").trigger('yearAdding', {"year": year, "count": count, "offset": 0, "accumulated": da.years[cy.toString()].accumulated});
    });
    

};

Da.prototype = {
    barColor: "orange",
    barColorSel: "red",
    barsPanelHeight: 300,
    barsPanelWidth: "100%",
    barMargin: 2,
    barsLeftOffset: 80,
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
        var labelcss = $(this.div).find("div.yearLabel");
        this.yearLabelBg = $(labelcss).css("background-color");
        this.yearLabelColor = $(labelcss).css("color");
        
        var barcss = $(this.div).find("div.bar");
        this.barColor = $(barcss).css("background-color");
        this.barWidth = $(barcss).height();
        
        var barSelcss = $(this.div).find("div.bar>div.sel");
        this.barColorSel = $(barSelcss).css("background-color");

        this.barsPanelWidth = this.$div.width() - 25;
        $(this.canvas).attr("width", this.barsPanelWidth);
        $(this.canvas).css("width", this.barsPanelWidth);
        var totalHeight = this.period * (this.barMargin + this.barWidth);
        this.barsPanelHeight = totalHeight;
        $(this.canvas).css("height", totalHeight);
        $(this.canvas).attr("height", totalHeight);
        this.scale = (1.0 * this.barsPanelWidth - this.barsLeftOffset) / this.maxCount;
        
        this.infoCanvas = $('<canvas/>');
        $(this.infoCanvas).css({left: 0, top: 0, "z-index": 20, "position": "absolute"});
        $(this.infoCanvas).css("height", totalHeight);
        $(this.infoCanvas).attr("height", totalHeight);
        $(this.infoCanvas).css("width", $(this.canvas).width());
        $(this.infoCanvas).attr("width", $(this.canvas).width());
        this.$div.append(this.infoCanvas);
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
      
    },
    resize: function(){
        this.$canvas.clearCanvas();
        $(this.infoCanvas).remove();
        this.init();
        this.render();
    },
    render: function() {
        for (var i = 0; i <= this.period; i++) {
            if((i + this.minYear) % 10 === 0){
                this.drawYearLabel(i);
            }
            this.addBar(i);
        }
        
        var show = window.location.hash;
        if(show.length > 1){
            show = parseInt(show.substring(1));
        }else{
            show = this.yearWithMaxCount;
        }
        
        return this;
    },
    drawYearLabel: function(i){
        var h = i * (this.barMargin + this.barWidth);
        this.$canvas.drawLine({
            strokeStyle: this.yearLabelBg,
            strokeWidth: 0.5,
            x1: 0, y1: h,
            x2: this.barsPanelWidth, y2: h
        });
        var text = (i + this.minYear).toString()+" - " + (i + this.minYear+10).toString();
        var da = this;
        this.$canvas.draw({
            fn: function(ctx) {
              ctx.save();
              da.setFont(ctx);
              ctx.fillStyle = da.yearLabelColor;
              ctx.fillText(text , 4, h+13);
              ctx.restore();
            }
          });
    },
    drawBar: function(i, color) {
        
        var bl = 0;
        var x = this.barsLeftOffset;
        if (this.years[i + this.minYear]) {
            bl = Math.floor(this.years[i + this.minYear].count * this.scale);
        }
        if (bl > 0) {
            var t = i * (this.barMargin + this.barWidth);
            var h = this.barWidth;
            this.$canvas.drawRect({
                fillStyle: color,
                x: x, 
                y: t,
                width: bl ,
                height: h,
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
            var texto = "" + (i + this.minYear) + ": " + h;
            var l = i * (this.barMargin + this.barWidth);

            this.infoCanvas.drawBarInfo({
                da: this,
                barTop: l,
                barLeft: h * this.scale + this.barsLeftOffset,
                y: l,
                x: this.barsPanelWidth - 20 + this.barsLeftOffset,
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
        var i = Math.floor(event.offsetY / (da.barMargin + da.barWidth));
        da.toggleBar(i);
    },
    doScroll: function(dx){
        var finalPos = this.$div.scrollLeft() + this.panelWidth * 0.5 * dx;
        this.scrollTo(finalPos);
    },
    scrollToMax: function(){
        this.scrollTo(this.indexWithMax * (this.barMargin + this.barWidth) + this.$div.height()/2);
    },
    scrollTo: function(to){
        var speed = 500;
        this.$div.animate({scrollTop:to}, speed);
        if(to <=0){
            //this.leftArrow.hide();
        }else{
            //this.leftArrow.show();
        }
    },
    onClick: function(event, da) {
        var i = Math.floor(event.offsetY / (da.barMargin + da.barWidth));
        var year = i + da.minYear;
        var val = 0;
        if (da.years[year]) {
            val = da.years[year].count;
        }
        var cy = parseInt(year);
        
        while(!da.years[cy.toString()] && cy>da.minYear){
            cy--;
        }
        $(".years").trigger('yearChanged', {"year": year, "count": val, "offset": 0, "accumulated": da.years[cy].accumulated});
        //window.location.hash = year.toString();
    },
    setDatePicker: function(){
        var minY = this.minYear;
        var maxY = this.maxYear;
        var tod = "01.01."+this.minYear;
        var tdo = "31.12."+this.maxYear;
        $( "#f1" ).val(tod);
        $( "#f2" ).val(tdo);
        var dates = $( "#f1, #f2" ).datepicker({
            changeMonth: true,
            changeYear: true,
            numberOfMonths: 1,
            dateFormat: "dd.mm.yy",
            minDate: tod,
            maxDate: tdo,
            constrainInput: true,
            yearRange: minY + ":" + maxY,
            onSelect: function( selectedDate ) {
                var option = this.id === "f1" ? "minDate" : "maxDate",
                    instance = $( this ).data( "datepicker" ),
                    date = $.datepicker.parseDate(
                        instance.settings.dateFormat ||
                        $.datepicker._defaults.dateFormat,
                        selectedDate, instance.settings );
                dates.not( this ).datepicker( "option", option, date );
            },
            onChangeMonthYear:function(year,month,instance){
                console.log(year);
                $(instance).datepicker("refresh"); 
                //$(inst)._selectDate(a,$(inst)._formatDate(f,f.currentDay,f.currentMonth,f.currentYear));
            }
        });
        $("#ui-datepicker-div").css("z-index", 100);
        $('#ui-datepicker-div').hide();
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
        var da = params.da;
        var barTop = params.barTop;
        var barLeft = params.barLeft;
        var y = params.y;
        var x = barLeft + da.infoArrowW;
        var text = params.text;
        var scrollPos = $(da.div).scrollTop();

        //content.getOffsetWidth()
        var bottomBorder = da.barsPanelHeight;

        if (da.barInfoDataCache !== null) {
            ctx.putImageData(da.barInfoDataCache, da.infoRect.getX(), da.infoRect.getY());
        }
        
        ctx.save();
        da.setFont(ctx);
        //
        var w = ctx.measureText(text).width + da.infoPadding * 2;
        var h = 5 + da.infoPadding * 2;
        ctx.restore();
        var points = [];
        var belowBar = false;
        var atRight = true;

        if (barLeft + da.infoArrowW + w > da.barsPanelWidth) {
            // big bar
            x = da.barsPanelWidth - w - 8;
            atRight = false;
        }
        
        var fy = barTop - h*2;

        if (fy < scrollPos) {
            fy = barTop + h;
            belowBar = true;
        }
        
        da.infoRect = new Rectangle(0, 
                Math.min(fy - 2 - da.shadowBlur, barTop - 2 - da.shadowBlur), 
                da.barsPanelWidth,
                Math.max(barTop + da.shadowBlur * 2 + da.shadowOffset, fy + h*2 + da.shadowBlur * 2 + da.shadowOffset));
        points.push(new Point(x, fy));
        if(belowBar){
            points.push(new Point(x + w/2 - da.infoArrowW, fy));
            points.push(new Point(barLeft, barTop));
            points.push(new Point(x + w/2 + da.infoArrowW, fy));
        }
        points.push(new Point(x + w, fy));
        points.push(new Point(x + w, fy + h));
        if(!belowBar){
            points.push(new Point(x + w/2 + da.infoArrowW, fy + h));
            points.push(new Point(barLeft, barTop));
            points.push(new Point(x + w/2 - da.infoArrowW, fy + h));
        }
        points.push(new Point(x, fy + h));
        points.push(new Point(x, fy));
        
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

        // Call the detectEvents() function to enable jCanvas events
        // Be sure to pass it these arguments, too!
        $.jCanvas.detectEvents(this, ctx, params);
        // Call the closePath() functions to fill, stroke, and close the path
        // This function also enables masking support and events
        // It accepts the same arguments as detectEvents()    
        $.jCanvas.closePath(this, ctx, params);
    }
});



    function daYearClicked(params){
        var rok = params.year;
        $("#" + fromField).val("01.01."+rok);
        $("#" + toField).val("31.12."+rok);
        if(!isValidDate($("#" + fromField).val()) || !isValidDate($("#" + toField).val())){
            alert(dictionary['filter.invalid.date'] );
            return;
        }

        var page = new PageQuery(window.location.search);
        page.setValue("offset", "0");
        page.setValue("forProfile", "dateaxis");
        //page.setValue(fromField, decodeDate($("#" + fromField).val()));
        //page.setValue(toField, decodeDate($("#" + toField).val()));

        page.setValue("da_od", decodeDate($("#" + fromField).val()));
        page.setValue("da_do", decodeDate($("#" + toField).val()));
        var newurl = "r.jsp?" + page.toString() + dateAxisAdditionalParams;

        document.location.href = newurl;

    }