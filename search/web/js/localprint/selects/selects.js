function SelectObject() {
        //this.height
}

SelectObject.prototype = {
        
    x1:30, y1:40, // top left
    x2:140, y2:130, //bottom right

    width:100, height:100, // width & height of the selection

    pointsize:4, // size of selection point
    iconsize:30,    
    
    center:function() {
        var rs = [$('#imagepart img').position().left, $('#imagepart img').position().top];
        $('#overlay').css('top',rs[0]);
        $('#overlay').css('left',rs[1]);

        var imw = $('#imagepart img').width();
        var imh = $('#imagepart img').height();


        var x1 = imw *0.1;   
        var y1 = imh *0.1;   
        var x2 = imw-(0.1*imw);
        var y2 = imh -(0.1*imh);

        this._setSelectBoxPosition(x1,y1,x2,y2);
        this._recalculateSelectionDiv();        
        // all points
        this._recalculatePoints(15);        
        this._recalculateIcons();
        
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
        $( "#selectbox" ).css("left", this.x1);
        $( "#selectbox" ).css("top", this.y1);

        $( "#selectbox" ).css("width", this.width);
        $( "#selectbox" ).css("height", this.height);
    },

    _setSelectBoxPosition:function(x1,y1,x2,y2) {
        this.x1 = x1;
        this.y1 = y1;

        this.x2 = x2;
        this.y2 = y2;

        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;

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
    },
    dragRightBottom:function(position) {
        var y2 =  position.top+this.pointsize;
        var x2 =  position.left+this.pointsize;

        this._setSelectBoxPosition(this.x1,this.y1,x2,y2);
        this._recalculateSelectionDiv();        
        // topleft, topright, bottomleft
        this._recalculatePoints(7);        
        this._recalculateIcons();
    },
        
    relativePositions:function() {
        var imgWidth =  $("#imagepart img").width()
        var imgHeight = $("#imagepart img").height()
        var retVals  = [];
        retVals.push(this.x1/imgWidth);
        retVals.push(this.y1/imgHeight);
        retVals.push(this.x2/imgWidth);
        retVals.push(this.y2/imgHeight);
        return retVals;
    }
}


    $( "#left-top" ).draggable({
        start:function( event, ui ) {
            window.selObjects.startLeftTop(ui.position);
        },
        drag: function( event, ui ) {
            window.selObjects.dragLeftTop(ui.position);
        }
    });

    $( "#right-top" ).draggable({
        start:function( event, ui ) {
            window.selObjects.startRightTop(ui.position);
        },
        drag: function( event, ui ) {
            window.selObjects.dragRightTop(ui.position);
        }
    });


    $( "#left-bottom" ).draggable({
        start:function( event, ui ) {
            window.selObjects.startLeftBottom(ui.position);
        },
        drag: function( event, ui ) {
            window.selObjects.dragLeftBottom(ui.position);
        }
    });

    $( "#right-bottom" ).draggable({
        start:function( event, ui ) {
            window.selObjects.startRightBottom(ui.position);
        },
        drag: function( event, ui ) {
            window.selObjects.dragRightBottom(ui.position);
        }
    });


