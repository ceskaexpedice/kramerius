function SelectObject(appl,initobj) {
        this.application = appl;        
        this.application.eventsHandler.trigger("gui/item/crop/start",null);
}

SelectObject.prototype = {
        
    x1:30, y1:40, // top left
    x2:140, y2:130, //bottom right

    width:400, height:400, // width & height of the selection

    pointsize:32, // size of selection point
    iconsize:30,    

    
    overlay:function() {
        // margin top header plus breadcrum
        var headerHeight = $("#header").height();
        var breadcrumHeight = $("#viewer div.breadcrumbs").height();
        
        var relativePos = this.application.gui.selected.relativePosition();
        $('#overlay').css('top',relativePos.top);
        $('#overlay').css('left',relativePos.left);
        //$('#overlay').css('margin-top',(headerHeight+ breadcrumHeight)+"px");
    },
    
    page:function() {

        this.overlay();
        
        var sel = K5.api.ctx.item.selected;
        var streams = K5.api.ctx.item[sel].streams;
        if (streams["ALTO"]) {

            K5.api.askForItemConcreteStream(sel,"ALTO",_.bind(function(data) {
                var v = $(data).find("Layout>Page>PrintSpace");
                
                var height = parseInt(v.attr("HEIGHT"));
                var width = parseInt(v.attr("WIDTH"));
                var vpos = parseInt(v.attr("VPOS"));
                var hpos = parseInt(v.attr("HPOS"));

                var page = this.application.gui.selected.translateCurrent(hpos, vpos, width , height);
                
                var x1 = page[0];
                var y1 = page[1];    
                var x2 = page[2]; 
                var y2 = page[3]; 
                
                this._setSelectBoxPosition(x1,y1,x2,y2);
                this._recalculateSelectionDiv();        
                
                // all points
                this._recalculatePoints(15);        
                this._recalculateIcons();
                
            },this));
        } else {
            var page = this.application.gui.selected.currentPage();

            var x1 = page[0];
            var y1 = page[1];    
            var x2 = page[2]; 
            var y2 = page[3]; 
            
            this._setSelectBoxPosition(x1,y1,x2,y2);
            this._recalculateSelectionDiv();        
            
            // all points
            this._recalculatePoints(15);        
            this._recalculateIcons();
        }
        
        

    },    
             
    _recalculateIcons:function() {
        $( "#okButton" ).css("top", this.y1-3-this.iconsize);
        $( "#cancelButton" ).css("top", this.y1-3-this.iconsize);

        $( "#okButton" ).css("left", this.x1+this.width-3-(2*this.iconsize));
        $( "#cancelButton" ).css("left",this.x1+this.width-3-this.iconsize);
    },  

    _recalculatePoints:function(pointsflag) {
        //top left
        if ((pointsflag & 1) == 1) {
                $("#left-top").css("left", this.x1);
                $("#left-top").css("top", this.y1);
        }
        //top right           
        if ((pointsflag & 2) == 2) {
                $("#right-top").css("left", this.x1+(this.width - this.pointsize));
                $("#right-top").css("top", this.y1);
        }
        //bottom left
        if ((pointsflag & 4) == 4) {
                $("#left-bottom").css("left", this.x1);
                $("#left-bottom").css("top", this.y1+(this.height - this.pointsize));
        }
        //bottom right
        if ((pointsflag & 8) == 8) {
                $("#right-bottom").css("left", this.x1+(this.width - this.pointsize));
                $("#right-bottom").css("top", this.y1+(this.height - this.pointsize));
        }
    },    

    _recalculateSelectionDiv:function(pointsflag) {
        $( "#selectbox" ).css("left", this.x1-2);
        $( "#selectbox" ).css("top", this.y1-2);

        $( "#selectbox" ).css("width", this.width+2);
        $( "#selectbox" ).css("height", this.height+2);
    },

    _setSelectBoxPosition:function(x1,y1,x2,y2) {
        this.x1 = x1;
        this.y1 = y1;

        this.x2 = x2;
        this.y2 = y2;

        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;

    },

    getSelectionBoxPosition:function() {
        var box = {
                "x1":this.x1,
                "y1":this.y1,

                "x2":this.x2,
                "y2":this.y2
        };

        return box;
    },    
    
    startLeftTop:function(position) {
        /*
        this.x1 =  $( "#left-top" ).position().left;
        this.y1 =  $( "#left-top" ).position().top;
        
        this.x2 =  $( "#selectbox" ).width() + this.x1;
        this.y2 =  $( "#selectbox" ).height() + this.y1;

        this._setSelectBoxPosition(x1,y1,this.x2,this.y2);
        */
    },
    
    dragLeftTop:function(position) {
        var x1 =  position.left;
        var y1 =  position.top;
        
        
        this._setSelectBoxPosition(x1,y1,this.x2,this.y2);
        this._recalculateSelectionDiv();        

        // topright, bottomleft, bottomright
        this._recalculatePoints(14);        
        this._recalculateIcons();
    },
    
    startRightTop:function(position) {
        /*
        this.x1 =  $( "#left-top" ).position().left;
        this.y1 =  $( "#left-top" ).position().top;
        
        this.x2 =  $( "#selectbox" ).width() + this.x1;
        this.y2 =  $( "#selectbox" ).height() + this.y1;

        this._setSelectBoxPosition(x1,y1,this.x2,this.y2);
        */
    },
    dragRightTop:function(position) {
        var y1 =  position.top;
        var x2 =  position.left+this.pointsize;

        this._setSelectBoxPosition(this.x1,y1,x2,this.y2);
        this._recalculateSelectionDiv();        
        // topleft, bottomleft, bottomright
        this._recalculatePoints(13);        
        this._recalculateIcons();
    },
    startLeftBottom:function(position) {
        /*                
        this.x1 =  $( "#left-top" ).position().left;
        this.y1 =  $( "#left-top" ).position().top;
        
        this.x2 =  $( "#selectbox" ).width() + this.x1;
        this.y2 =  $( "#selectbox" ).height() + this.y1;

        this._setSelectBoxPosition(x1,y1,this.x2,this.y2);
        */
    },
    dragLeftBottom:function(position) {
        //this.x1 =  position.left;
        var y2 =  position.top+this.pointsize;
        var x1 =  position.left;
                
        this._setSelectBoxPosition(x1,this.y1,this.x2,y2);
        this._recalculateSelectionDiv();        
        // topleft, topright, bottomright
        this._recalculatePoints(11);        
        this._recalculateIcons();
    },
    startRightBottom:function(position) {
        /*         
       this.x1 =  $( "#left-top" ).position().left;
        this.y1 =  $( "#left-top" ).position().top;
        
        this.x2 =  $( "#selectbox" ).width() + this.x1;
        this.y2 =  $( "#selectbox" ).height() + this.y1;

        this._setSelectBoxPosition(x1,y1,this.x2,this.y2);
        */
    },
    dragRightBottom:function(position) {
        var y2 =  position.top+this.pointsize;
        var x2 =  position.left+this.pointsize;

        this._setSelectBoxPosition(this.x1,this.y1,x2,y2);
        this._recalculateSelectionDiv();        
        // topleft, topright, bottomleft
        this._recalculatePoints(7);        
        this._recalculateIcons();
    }
}




