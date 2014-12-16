function Messages() {}


Messages.prototype.init = function() {

    function _dialog(container, footer) {
        var downloadDiv = $("<div/>",{"id":"message","class":"message"});
        downloadDiv.css("position","absolute");
        downloadDiv.css("display","none");
        downloadDiv.append(container);
        downloadDiv.append(footer);
        return downloadDiv;
    }

    function _container(header) {
        var container = $("<div/>",{"id":"message_container"});
        container.append(header);
        return container;
    }
    
    
    function _footer(rdiv) {
        var footer = $("<div/>",{"class":"message_footer dialogs_footer"});
        footer.append(rdiv);
        return footer;
    }

    function _rightbutton() {
        var rdiv = $("<div/>",{"class":"right"});
        var buttons = $("<div/>",{"class":"buttons"});
        rdiv.append(buttons);

        var closeButton = $("<div/>",{"class":"button"});
        closeButton.attr('onclick',"K5.gui.selected.messages.close();");

        closeButton.append(K5.i18n.translatable('common.close'));
        buttons.append(closeButton);

        return rdiv;
    }

    $('#viewer>div.container').append(_dialog(_container(), _footer(_rightbutton())));
}

Messages.prototype.open = function(messagefunc) {
    cleanWindow();
    divopen("#message");
    if (messagefunc != null) {
        messagefunc.apply(null, [ $("#message_container") ]);
    }
}

Messages.prototype.close = function() {
    $("#message_container").empty();
    cleanWindow();
}