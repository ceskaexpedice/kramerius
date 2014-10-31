function MorePagesPDF(application) {
   application.eventsHandler.addHandler(_.bind(function(type, data) {
        var selected = K5.api.ctx.item.selected; 
        var itm = K5.api.ctx.item[selected];

        if (type == "application/gui/selection/from") {
            var found = _.reduce(itm.children, function(memo, value, index) {
                if (memo === null && value.pid === data) {
                        memo = value;
                }
                return memo;
            }, null);
                
            var idiv = this._imagecontent(data, found);
                                
            $("#cellthumbfrom").empty();
            $("#cellthumbfrom").append(idiv);
        }
        if (type == "application/gui/selection/to") {

            var found = _.reduce(itm.children, function(memo, value, index) {
                if (memo === null && value.pid === data) {
                        memo = value;
                }
                return memo;
            }, null);

            var idiv = this._imagecontent(data, found);

            $("#cellthumbto").empty();
            $("#cellthumbto").append(idiv);
        }
    },this));                

}
MorePagesPDF.prototype =  {
         
       _selectcontent:function() {

                function select(id,nullval, children) {
                        
                        var sel = $('<select/>',{'id':id});
                        sel.css('color','white');

                        var nullOption = $('<option/>',{'value':'not-selected'});
                        nullOption.text(nullval);
                        nullOption.css('color','black');
                        sel.append(nullOption);

                        var itms = _.map(children, function(a){ 
                                if (a.model == 'page') {
                                        var option = $('<option/>', {'value':a.pid});
                                        option.css('color','black');
                                        option.text(a.details.pagenumber);
                                        return option; 
                                } else return null;
                        });

                        _.each(itms, function(itm) {
                                if (itm != null) sel.append(itm);
                        });
                        return sel;
                }                


                var tableDiv = $("<table/>",{});
                tableDiv.css('border-collapse','collapse');
                tableDiv.css('width','80%');
                
                var rowDiv = $("<tr/>",{});
                var cellFrom = $("<td/>",{'id':'cellselectfrom'});
                var cellTo = $("<td/>",{'id':'cellselectto'});

                rowDiv.append(cellFrom);
                rowDiv.append(cellTo);

                tableDiv.append(rowDiv);

                var cellThumbFrom = $("<td/>",{'id':'cellthumbfrom'});
                var cellThumbTo = $("<td/>",{'id':'cellthumbto'});
                var rowDiv = $("<tr/>",{});


                rowDiv.append(cellThumbFrom);
                rowDiv.append(cellThumbTo);
        
                tableDiv.append(rowDiv);

                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];

                var selFrom = select('pdf-from','Od',itm.children);
                selFrom.change(_.bind(function() {
                        var v= $('#pdf-from').val();
                        K5.eventsHandler.trigger("application/gui/selection/from",v);
                },this));
               
                cellFrom.append(selFrom);

                var selTo = select('pdf-to','Do',itm.children);
                selTo.change(_.bind(function() {
                        var v= $('#pdf-to').val();
                        K5.eventsHandler.trigger("application/gui/selection/to",v);
                },this));
                cellTo.append(selTo);



                return tableDiv;
        },



        pdf:function() {

                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                var from = $("#pdf-from").val();
                var to = $("#pdf-to").val();
                
                var reduced = _.reduce(itm.children, function(memo, value, index) {
                        console.log(JSON.stringify(memo));
                        if (value.pid === $("#pdf-from").val()) {
                                memo.selected = true;    
                        }

                        if (memo.selected) {
                                if (memo.pidlist.length>0) memo.pidlist += ",";   
                                memo.pidlist += value.pid;
                        }
                        if (value.pid === $("#pdf-to").val()) {
                                memo.selected = false;    
                        }
                        return memo;
                }, {'pidlist':"",'selected':false});

                window.open("api/pdf/selection?pids="+ reduced.pidlist);

        }, 
        clear:function() {
                $("#pdf-from").val('not-selected');
                $("#pdf-to").val('not-selected');

                $("#cellthumbfrom").empty();                
                $("#cellthumbto").empty();
        },        

        _imagecontent:function(pid, itm) {
                var idiv = $('<div/>');

                var titlediv = $('<div/>',{'class':'title', "id":"titlediv_"+pid.replace(':','\\:')});
                titlediv.css('color','black')
                        .css('text-align','center')
                        .css('font-size','0.7em')
                        .css('background-color','white');    

                if (itm != null) {
                        titlediv.text(itm.details.pagenumber + itm.details.type);
                }

                var img = $('<img/>', {'src':"api/item/"+ pid+"/thumb","id":"img_"+pid.replace(':','\\:')});
                img.load(_.bind(function() {
                        var width = img.width();
                        titlediv.css('width',""+width+"px");

                },this));   
                idiv.append(img);    


                idiv.append(titlediv);
                return idiv;                               
        
        }
}

function PDFDialogSupport(application) {}

PDFDialogSupport.prototype = {
       _container:function() {
                var dialDiv = $("<div/>", {'class':'actionbox','id':'dialog'});
                var thmbs = $("<div/>", {'class':'pdfthumbs'});

                dialDiv.append(this._icons())
                        .append(this._selectcontent());

                var w = $("#viewer").width();
                var h = $("#viewer").height();
        
                
                dialDiv.css("width",w*0.43);                
                dialDiv.css("height",h*0.3);                
                
                dialDiv.css("left",(w*0.3)/2);                
                dialDiv.css("top",(h*0.3)/2);                


                return dialDiv;      
        },
        
        _icons:function() {

                var iconsDiv = $("<div/>",{});

                var downloadDiv = $("<div/>",{'class':'small', 'title':'download'});
                downloadDiv.css('display','inline-block');        
                downloadDiv.bind( "click", _.bind(function() {
                        this.pdf();
                        this.close();
                },this));

                iconsDiv.append(downloadDiv);                

                $.get("svg.vm?svg=download",function(data) {
                        downloadDiv.html(data);            
                });


                var closeDiv = $("<div/>",{'class':'small', 'title':'close'});
                closeDiv.css('display','inline-block');        
                closeDiv.bind( "click", _.bind(function() {
                        this.close();
                },this));

                iconsDiv.append(closeDiv);                
                $.get("svg.vm?svg=close",function(data) {
                        closeDiv.html(data);            
                });

                 
                iconsDiv.css('position','absolute');
                iconsDiv.css('right','10px');
                iconsDiv.css('top','10px');
        
                var clearDiv = $("<div/>",{});
                clearDiv.css('clear','both');
                iconsDiv.append(clearDiv);

                return iconsDiv;        
        },



        _details:function(pids) {
                return "";
        },
        _selectcontent:function() {
                return "";
        }, 
        open:function() {
                this.clear(); 
                $("#dialog").css('width','20%');
                $("#dialog").show();
        },
        close: function() {
                $("#dialog").hide();
        },        
        build:function() {
                var dialDiv = this._container();
                $("#viewer").append(dialDiv);
                K5.gui.selected.hideInfo();
        }
}

function _PDFDialogBuild() {
        return mixInto(new PDFDialogSupport(K5), new MorePagesPDF(K5),"_selectcontent","clear","pdf");
}



