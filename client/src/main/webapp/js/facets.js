K5.eventsHandler.addHandler(function(type, configuration) {
    if (type === "results/loaded") {
        if(!K5.gui["facets"]){
             K5.gui["facets"] = new Facets(configuration);
        }
    }
});


var Facets = function(data) {
    this.data = data;
    this._init();
};

Facets.prototype = {
    _init: function() {
        this.addContextButtons();
        var facets = this.data.facet_counts.facet_fields;
        this.render($("#facets>div.unused"), facets);
        $("#facets div.used").click(_.partial(function(facets, event) {
            event.preventDefault();
            var val = $(this).data("key");
            var facet = $(this).data("facet");
            facets.removeFilter(facet, val);
        }, this));
        
        K5.i18n.k5translate("div.used");
        
        this.show(1000);
    },
    addContextButtons: function() {
        var text = $("#facets_menu").html();
        $("#contextbuttons").append(text);
    },
    addFilter: function(facet, val) {
        //window.location.href = window.location.search + "&" + facet + "=" + val;
        var input = $("<input>", {type: "hidden", value: val, name: facet, class: "facet"});
        $("#search_form").append(input);
        $("#start").val("0");
        $("#search_form").submit();
    },
    removeFilter: function(facet, val) {
        $("input[name='" + facet + "']").remove();
        $("#search_form").submit();
    },
    removeAllFilters: function() {
        $("#search_form input.facet").remove();
        $("#search_form").submit();
    },
    hide: function() {
        var l = -55 - $("#facets").width();
        $("#facets").animate({'opacity': '0.5', 'right': l}, 200, function() {
            $("#facets").removeClass("showing");
            $("#facets svg.filter").show();
            $("#facets svg.pin").hide();
        });
    },
    show: function(speed) {
        if (!$("#facets").hasClass("showing")) {
            $("#facets").addClass("showing");
            $("#facets svg.filter").hide();
            $("#facets svg.pin").show();
            $("#facets").animate({'opacity': '1.0', 'right': '1px'}, speed);
        }
    },
    addFacetValue: function(div, key, val, count, more){
        var div2 = $('<div/>', {class: 'res'});
        var a = $('<a/>', {class: 'res'});

        a.data("key", val);
        a.data("facet", key);
        a.attr('href', '');
        a.click(_.partial(function(facets, event) {
            event.preventDefault();
            facets.addFilter($(this).data('facet'), $(this).data('key'));
        }, this));
        if (more) {
            div2.addClass("more");
        }
        if (key === "collection") {
            //a.addClass("vc");
            a.html(K5.i18n.translatable(val));
            //a.text(arr[i]);
        } else if (key === "typ_titulu" || key === "fedora.model" || key === "model_path") {
            a.html(K5.i18n.translatable("fedora.model." + val));
        } else if (key === "dostupnost") {
            a.html(K5.i18n.translatable("dostupnost." + val));
        } else {
            a.text(val);
        }
        var span = $('<span/>', {class: 'count'});
        span.text(" (" + count + ")");
        div2.append(a);
        div2.append(span);
        div.append(div2);
    },
    render: function(obj, json) {
        var facets = this;
        var root_models = {};
        $.each(json, function(key, arr) {
            if (arr.length > 2) {
                var div = $('<div/>', {class: 'facet'});
                if(key !== "model_path"){
                    div.html("<h3>" + K5.i18n.translatable('facet.' + key) + "</h3>");
                    obj.append(div);
                }
                for (var i = 0; i < arr.length; i++) {
                    var val = arr[i];
                    var count = parseInt(arr[++i]);
                    if(key === "model_path"){
                        val = val.split("/")[0];
                        if(root_models[val]){
                            root_models[val] = count + root_models[val];
                        }else{
                            root_models[val] = count;
                        }
                    }else{
                        facets.addFacetValue(div, key, val, count, i > 10);
                    }
                }
            }
        });
        if(!jQuery.isEmptyObject(root_models)){
            var div = $('<div/>', {class: 'facet'});
            div.html("<h3>" + K5.i18n.translatable('facet.model_path') + "</h3>");
            obj.prepend(div);
            $.each(root_models, function(val, count){
                facets.addFacetValue(div, "typ_titulu", val, count, false);
            });
        }
        K5.gui.vc.translate(K5.i18n.ctx.language);
    }
};



function togglePin() {
    $("#facets").toggleClass("pin");
}

