
function Selections(appl) {
        this.application = appl;
}

Selections.prototype =  {

        thumb:function(thumb) {

                var selitem = $('<div/>');
                selitem.data("pid", thumb.pid);

                var imgdiv = $('<div/>');
                
                var ahref = $('<a/>', {'href': 'javascript:K5.api.gotoItemPage("'+thumb.pid+'");'});

                var imgsrc = 'api/item/' + thumb.pid + '/thumb';
                var img = $('<img/>', {src: imgsrc});
                ahref.append(img);

                imgdiv.append(ahref);
                selitem.append(imgdiv);

                var thumb = $('<li/>', {class: 'selitem', 'data-pid': thumb.pid});
                thumb.attr("title", thumb.title);
                thumb.append(selitem);
                
                return thumb;
        },

        open:function(data, fnc) {
                var ul = _.reduce(data, _.bind(function(memo, value){ 
                        memo.append(this.thumb(value));                        
                       return memo; },this), $("<ul/>"));
                $("#selections_thumbs").append(ul);
                this.refreshButtons();
        },


        
        refreshButtons:function() {
                // enable buttons                
                $(".selection_footer .button").each(function() {
                    if ($(this).data("ctx")) {
                        var attr = $(this).data("ctx").split(";");
                        if (jQuery.inArray('selection', attr) > -1) {
                           if (K5.gui.clipboard.ctx.selected.length > 0) {
                               $(this).show();
                           } else {
                               $(this).hide();
                           }                        
                        }
                    }
                });
        },        
        

        // TODO: move !! 
        print:function() {
                var selected = K5.gui.clipboard.ctx.selected;
                var ll = selected.length;          
                
                var v = _.reduce(selected, function(memo, value, key){ 
                        memo = memo + value;
                        if (key >= 0 && key < ll - 1) memo = memo +",";
                                return memo; 
                        }, "");  
                //TODO: change it          
                window.open('print.vm?pid='+selected[0].pid+
                        "&full="+true, '_blank');

                cleanWindow();

        },
        pdf:function() {
                var selected = K5.gui.clipboard.ctx.selected;
                var ll = selected.length;          
                var v = _.reduce(selected, function(memo, value, key){ 
                                memo = memo + value.pid;
                                if ((key >= 0) && (key < ll-1)) {
                                        memo = memo +",";
                                }
                                return memo; 
                }, "");  
                window.open("api/pdf/selection?pids="+ v,"_blank");
                cleanWindow();
        }
}


