/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var Carousel = function(elem, options, ajaxCall) {
    this.curname = 'unnamed';    
    this.elem = elem;
    this.$elem = $(elem);
    this.json = options.json;
    this.ajaxCall = ajaxCall || false;    
    
    this.init();
    
    this.scrolling = false;    
}


Carousel.prototype = {
    background : "silver",
    rowsPerRequest:50,
    thumbHeight:128,
    panelWidth: 500,
    panelHeight: 138,
    init: function(){
        
        this.rightArrow = $('<div>', {class: 'arrow arrowRight'});
        this.leftArrow = $('<div>', {class: 'arrow arrowLeft'});
        this.$elem.append(this.leftArrow);
        this.$elem.append(this.rightArrow);
        
        
        this.scroll = $('<div/>', {class: 'scroll'});
        this.container = $('<ul/>');
        
        this.container.addClass('container');
        this.scroll.append(this.container);
        this.$elem.append(this.scroll);
        
        try {
            this.render();
        } catch(apperror) {
            console.log("cannot render due:"+apperror);
        }
        var obj = this;
        this.rightArrow.click(function(){
            obj.doScroll(1);
        });
        this.leftArrow.click(function(){
            obj.doScroll(-1);
        });
        this.leftArrow.hide();

        if (isTouchDevice()) {
            this.$elem.swipe({
                swipeLeft: _.bind(function(event, direction, distance, duration, fingerCount) {
                    if (!this.scrolling) {
                        this.doScroll(1);
                    }
                },this),
                swipeRight: _.bind(function(event, direction, distance, duration, fingerCount) {
                    if (!this.scrolling) {
                        this.doScroll(-1);
                    }
                },this),
                threshold: 2
            });
        }
    },
    setSizes: function(){
        
        this.panelHeight = this.$elem.height();
        this.panelWidth = this.$elem.width();
    },
    doScroll: function(dx){
        var speed = 500;
        var finalPos = this.scroll.scrollLeft() + this.panelWidth * 0.8 * dx;
        this.scrolling = true;
        this.scroll.animate({scrollLeft:finalPos}, speed, _.bind(function() {
                this.scrolling = false;
        },this));
        if(finalPos <=0){
            this.leftArrow.hide();
        }else{
            this.leftArrow.show();
        }
        
    },
    enableTooltip: function(){
        this.container.find('li.thumb').tooltip({
            disabled: false,
        });
    },
    
    retriveTitle:function(pid) {

        K5.api.askForItem(pid, function(data) {
                var profile = $("#rows>div.profilefavorites li.thumb[data-pid='"+pid+"']");                
                var title = '<div class="tool_title">' + data.title + ' </div>';
                profile.attr("title", title);
                profile.tooltip({
                        content: title,
                        //disabled: true,
                        position: { my: "center bottom-7", at: "center top" }
                });
        }); 
    },
    
    render: function(){
        var docs = this.json.data;
        for(var i=0; i<docs.length; i++){
            var pid = docs[i].pid;
            var imgsrc = "api/item/" + pid + "/thumb";
            var thumb = $('<li/>', {class: 'thumb','data-pid':pid});
            var title = docs[i].title;
            title = '<div class="tool_title">' + title + '</div>';
                
            thumb.attr("title", title);
            thumb.data("pid", pid);

            if (this.ajaxCall) this.retriveTitle(pid);     
               
            this.container.append(thumb);
            if (!this.ajaxCall) {
                    thumb.tooltip({
                        content: title,
                        //disabled: true,
                        position: { my: "center bottom-7", at: "center top" }
                    });
            }
            thumb.click(function(){
                K5.api.gotoDisplayingItemPage($(this).data('pid'));
            });
            this.addThumb(thumb, imgsrc);
        }
    },

    addThumb: function(div, imgurl){

        var divimg = $('<div/>', {class: 'img'});
        var img = $('<img/>', {src: imgurl});
        $(div).append(divimg);
        $(divimg).append(img);
    },
    setName:function(nm) {
        this.curname=nm; 
   }    
}
