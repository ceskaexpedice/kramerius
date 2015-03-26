
function Footer(app) {
    this.application = app || K5;
    this.application.eventsHandler.addHandler(_.bind(function(type, configuration) {
        var splitted = type.split("/");
        if (type === "widow/url/hash") {
            this.footer((K5.gui.page && K5.gui.page ==="doc"))
        }
        if (type === "application/init/end") {
            this.footer((K5.gui.page && K5.gui.page ==="doc"))
        }
    },this));
}

Footer.prototype = {
        "footer":function(item) {
            if (item) {
                $("#footer_noitem").hide();
                $("#footer_item").show();
            } else {
                $("#footer_noitem").show();
                $("#footer_item").hide();
            }
        },

        "feedback" : function() {
            var email = $("#feedback_field").val();
            var text = $("#feedback_area").val();
            var pid = K5.api.ctx.item.selected;
            K5.api.feedback(text, pid, email, function() {
                //cleanWindow();
            });
            cleanWindow();
        },

        "feedbackDialog" : function() {
            $("#feedback").remove();
            var sel = K5.api.ctx.item.selected;
            function _dialog(container, footer) {
                    var downloadDiv = $("<div/>",{"id":"feedback","class":"feedback"});
                    downloadDiv.css("position","absolute");
                    downloadDiv.css("display","none");
                    downloadDiv.append(container);
                    downloadDiv.append(footer);
                    return downloadDiv;
                }

                function _container(header, text,field) {
                    var container = $("<div/>",{"id":"feedback_container"});
                    container.append(header);
                    container.append(field);
                    container.append(text);
                    return container;
                }
                
                function _field() {
                    var c = $("<div/>");
                    var l = $("<label/>");
                    l.text(K5.i18n.ctx.dictionary['feedback.email.title']);
                    c.append(l);
                    
                    
                    var t = $("<input/>",{"id":"feedback_field","type":"text"});
                    if (K5.authentication.ctx.user) {
                        var text = K5.authentication.ctx.user.firstname+" "+K5.authentication.ctx.user.surname;
                        t.val(text);
                    }
                    c.append(t);
                    
                    return c;
                }
                
                function _area() {
                    var c = $("<div/>");
                    
                    var ldiv =  $("<div/>");
                    
                    var l = $("<label/>");
                    l.text(K5.i18n.ctx.dictionary['feedback.message.title']);
                    ldiv.append(l);

                    
                    c.append(ldiv);

                    var sel = K5.api.ctx.item.selected;
                    var title = K5.api.ctx.item[sel]["title"];
                    var rootTitle = K5.api.ctx.item[sel]["root_title"];
                    var model = K5.api.ctx.item[sel]["model"];

                    var t = $("<textarea/>",{"id":"feedback_area"});
                    
                    var message = K5.i18n.ctx.dictionary['feedback.message.placeholder'];
                    var postfix = (title === rootTitle) ? "("+title +") "+sel : "("+title +","+ rootTitle+") "+sel
                    t.attr("placeholder",message + postfix);
                    
                    t.val(message+postfix);
                    
                    c.append(t);
                    return c;
                }

                function _footer(rdiv) {
                    var footer = $("<div/>",{"class":"feedback_footer dialogs_footer"});
                    footer.append(rdiv);
                    return footer;
                }

                function _header() {
                    function _div(v) { var retval = $("<div/>"); retval.append(v); return retval;  }

                    var head = $("<div/>",{"class":"feedback_header"});
                    var h2  = $("<h2/>");

                    var sel = K5.api.ctx.item.selected;
                    var title = K5.api.ctx.item[sel]["title"];
                    var rootTitle = K5.api.ctx.item[sel]["root_title"];
                    var model = K5.api.ctx.item[sel]["model"];
                    h2.html(K5.i18n.translatable('feedback.title'));

                    head.append(h2);
                    return head;
                }

                function _rightbutton() {
                    var rdiv = $("<div/>",{"class":"right"});
                    var buttons = $("<div/>",{"class":"buttons"});
                    rdiv.append(buttons);

                    var okButton = $("<div/>",{"class":"button"});
                    okButton.attr('onclick',"K5.gui.footer.feedback();");

                    okButton.append(K5.i18n.translatable('common.ok'));

                    var closeButton = $("<div/>",{"class":"button"});
                    closeButton.attr('onclick',"cleanWindow();");

                    closeButton.append(K5.i18n.translatable('common.close'));

                    buttons.append(okButton);
                    buttons.append(closeButton);

                    return rdiv;
                }
                $('#viewer>div.container').append(_dialog(_container(_header(), _area(),_field()), _footer(_rightbutton())));
                divopen('#feedback');
        }
}