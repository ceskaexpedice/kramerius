function ShareItem() {}

ShareItem.prototype.selectAction = function(ident) {
    var soptions = K5.gui.shareoptions.ctx.actions;
    var reduced = _.reduce(soptions, function(memo, itm){ 
        if (memo  == null) {
            if (itm.name === ident) return itm; 
            else return null;
        } else return memo;
    }, null);
    return reduced;
}

ShareItem.prototype.doShare = function(ident) {
    cleanWindow();
    var act = this.selectAction(ident);
    if (act != null) {
        act.object.doAction();
    }
}

ShareItem.prototype.init = function() {

    function _dialog(container, footer) {
        var shareDiv = $("<div/>",{"id":"share","class":"share"});
        shareDiv.css("position","absolute");
        shareDiv.css("display","none");
        shareDiv.append(container);
        shareDiv.append(footer);
        return shareDiv;
    }

    function _container(options, message) {
        var container = $("<div/>",{"id":"share_container"});
        container.append(options);
        if (message) {
            container.append(message);
        }
        return container;
    }
    
    function _message() {
        var optionsdiv = $("<div/>");
        var span = $("<span/>",{"id":"share_action_message"});
        optionsdiv.append(span);
        return optionsdiv;
    }
    
    function _options() {
        var optsCont = $("<div/>");
        var optionsdiv = $("<div/>",{"id":"share_options"});
        optsCont.append(optionsdiv);
        return optsCont;
    }
    
    function _footer(rdiv) {
        var footer = $("<div/>",{"class":"share_footer dialogs_footer"});
        footer.append(rdiv);
        return footer;
    }


    function _rightbutton() {
        var rdiv = $("<div/>",{"class":"right"});
        var buttons = $("<div/>",{"class":"buttons"});
        rdiv.append(buttons);

        var closeButton = $("<div/>",{"class":"button"});
        closeButton.attr('onclick',"cleanWindow();");
        //closeButton.attr("data-ctx","selection;pdflimit");

        closeButton.append(K5.i18n.translatable('common.close'));

        buttons.append(closeButton);
        return rdiv;
    }

    $('#viewer>div.container').append(_dialog(_container( _options(),_message()), _footer(_rightbutton())));
}

ShareItem.prototype.open = function() {
    cleanWindow();
    divopen("#share");

    var vacts = K5.gui.shareoptions.ctx.actions;
    
    var shareCont = $("<ul/>");
    var options = _.map(vacts, function(a) {
        if (a.object.enabled()) {
            var liHtml =$('<li/>');
            var div  =$('<div/>',{"class":"sharesicon"});
            div.attr("title",K5.i18n.translate(a.i18nkey));
            div.click(function() {
                K5.gui.selected.shares.doShare(a.name);
            });
            
            div.load("svg.vm?svg="+a.icon);

            var textdiv  =$('<div/>');
            textdiv.html(K5.i18n.translatable(a.i18nkey));
            
            liHtml.append(div);
            liHtml.append(textdiv);
            
            
            return liHtml;
        } else return null;
    });
    
    _.each(options, function(opt) {
        if (opt != null) {
            shareCont.append(opt);
        }
    });
    
    $("#share_container").html(shareCont);

    /*
    var doptions = K5.gui.downloadoptions.ctx.actions;
    var select = $('<select/>');
    select.change(function() {
        var message = K5.gui.selected.download.selectedMessage();
        if (message != null) {
            $("#download_action_message").text(message);
        } else {
            $("#download_action_message").text("");
        }
    });
    var options = _.map(doptions, function(a) {
        if (a.object.enabled()) {
            var optHtml =$('<option/>', {'value': a.name,'data-key': a.i18nkey});
            optHtml.html(K5.i18n.translatable(a.i18nkey));
            var option = {
                    "elem":optHtml
            };
            if (a.object["message"]) {
                option["message"] = a.object["message"];
            }
            return option;
        } else return null;
    });

    _.each(options, function(opt) {
        if (opt != null) {
            select.append(opt.elem);
        }
    });

    var first = _.reduce(options, function(memo, value, index) {
        if (memo == null) {
            memo = value;
        }
        return memo;
    }, null);

    var message = first["message"];
    if ((message) && (message != null)) {
        $("#download_action_message").text(message);
    } else {
        $("#download_action_message").text("");
    }

    $("#download_options").html(select);
    */
}

ShareItem.prototype.cleanDialog = function() {
    $("#share_container").empty();
}