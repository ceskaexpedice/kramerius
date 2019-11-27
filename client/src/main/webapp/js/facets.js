/* global K5, _ */

K5.eventsHandler.addHandler(function (type, configuration) {
    if (type === "results/loaded") {
        if (!K5.gui["facets"]) {
            K5.gui["facets"] = new Facets(configuration);
        }
    }
});


var Facets = function (data) {
    this.data = data;
    this._init();
};

Facets.prototype = {
    _init: function () {
        this.addContextButtons();
        var facets = this.data.facet_counts.facet_fields;
        this.render($("#facets>div.unused"), facets);
        $("#facets div.used").click(_.partial(function (facets, event) {
            event.preventDefault();
            var val = $(this).data("key");
            var facet = $(this).data("facet");
            facets.removeFilter(facet, val);
        }, this));

        K5.i18n.k5translate("div.used");

        this.show(1000);
    },
    addContextButtons: function () {
        var text = $("#facets_menu").html();
        $("#contextbuttons").append(text);
    },
    addFilter: function (facet, val) {
        if (val === "") {
            val = "none";
        }
        var input = $("<input>", {type: "hidden", value: val, name: facet, class: "facet"});
        $("#search_form").append(input);
        $("#start").val("0");
        $("#search_form").submit();
    },
    addRokFilter: function () {
        var selid = "#sel_rok";
        var min = $(selid).data("from");
        var max = $(selid).data("to");
        if (parseInt(min) && parseInt(max) && min <= max) {
            var val = '[' + min + ' TO ' + max + ']';

            var input = $("<input>", {type: "hidden", value: val, name: 'rok', class: "facet"});
            $("#search_form").append(input);
            $("#start").val("0");
            $("#search_form").submit();
        } else {
            alert("Invalid values");
        }

    },
    isUsed: function(facet, val){
        var ret = false;
        $("input[name='" + facet + "'][type='hidden']").each(function () {
            if ($(this).val() === val) {
                ret = true;
                return;
            }
        });
        
        return ret;
    },
    removeFilter: function (facet, val) {

        $("input[name='" + facet + "']").each(function () {
            if ($(this).val() === val) {
                $(this).remove();
            }
        });
        $("#search_form").submit();
    },
    removeAllFilters: function () {
        $("#search_form input.facet").remove();
        $("#search_form").submit();
    },
    hide: function () {
        var l = -55 - $("#facets").width();
        $("#facets").animate({'opacity': '0.5', 'right': l}, 200, function () {
            $("#facets").removeClass("showing");
            $("#facets svg.filter").show();
            $("#facets svg.pin").hide();
        });
    },
    show: function (speed) {
        if (!$("#facets").hasClass("showing")) {
            $("#facets").addClass("showing");
            $("#facets svg.filter").hide();
            $("#facets svg.pin").show();
            $("#facets").animate({'opacity': '1.0', 'right': '1px'}, speed);
        }
    },
    addFacetValue: function (div, key, val, count, more) {
        
        if(this.isUsed(key, val)){
            return;
        }
        
        if(count === 0){
          return;
        }
        
        var div2 = $('<div/>', {class: 'res'});
        var a = $('<a/>', {class: 'res'});

        a.data("key", val);
        a.data("facet", key);
        a.attr('href', '');
        a.click(_.partial(function (facets, event) {
            event.preventDefault();
            facets.addFilter($(this).data('facet'), $(this).data('key'));
        }, this));
        if (more) {
            div2.addClass("more");
        }
        if (key === "collection") {
            if(!K5.i18n.hasKey(val)){
                return;
            }
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
    rokFacet: function (div, minv, maxv) {
        var facetName = 'rok';
        var sel = $("<div/>", {class: 'sel'});
        var selid = "sel_" + facetName;
        sel.attr('id', selid);
        div.append(sel);

        var span = $("<span/>", {class: 'label'});
        span.text('od ' + minv + ' do ' + maxv);
        sel.append(span);

        var spango = $("<span/>", {class: 'go'});
        spango.text('go');
        sel.append(spango);

        var id = facetName + "_range";
        var range = $("<div/>", {class: "slid"});
        range.attr('id', id);
        range.data("min", minv);
        range.data("max", maxv);
        div.append(range);

        $(range).slider({
            range: true,
            min: minv,
            max: maxv,
            values: [minv, maxv],
            slide: function (event, ui) {
                $(sel).find("span.label").html("od " + ui.values[ 0 ] + " - do " + ui.values[ 1 ]);
                $(sel).data("from", ui.values[ 0 ]);
                $(sel).data("to", ui.values[ 1 ]);
            }
        });
        $(sel).find("span.go").button({
            icons: {
                primary: "ui-icon-arrowthick-1-e"
            },
            text: false
        });
        $(sel).find("span.go").click(_.bind(function () {
            this.addRokFilter();
        }, this));
    },
    render: function (obj, json) {
        var facets = this;
        var moreCount = 10;
        var root_models = {};
        $.each(json, function (key, arr) {
            if (arr.length > 2) {
                var div = $('<div/>', {class: 'facet'});
                if (key === "rok") {
                    div.addClass("range");
                    div.html("<h3>" + K5.i18n.translatable('facet.' + key) + "</h3>");
                    obj.prepend(div);
                    var min = parseInt(arr[0]);
                    if (min === 0 && arr.length > 2) {
                        min = parseInt(arr[2]);
                    }
                    var max = parseInt(arr[arr.length - 2]);
                    facets.rokFacet(div, min, max);
                } else {
                    if (key !== "model_path") {
                        div.html("<h3>" + K5.i18n.translatable('facet.' + key) + "</h3>");
                        obj.append(div);
                    }
                    for (var i = 0; i < arr.length; i++) {
                        var val = arr[i];
                        var count = parseInt(arr[++i]);
                        if (key === "model_path") {
                            val = val.split("/")[0];
                            if (root_models[val]) {
                                root_models[val] = count + root_models[val];
                            } else {
                                root_models[val] = count;
                            }
                        } else {
                            facets.addFacetValue(div, key, val, count, i > moreCount);
                        }
                    }
                    if (arr.length > moreCount) {
                        var moreDiv = $('<div class="moreButton">...</div>');
                        moreDiv.click(function () {
                            $(this).parent().find(".more").toggle();
                        });
                        div.append(moreDiv);
                    }
                }
            }
        });
        if (!jQuery.isEmptyObject(root_models)) {
            var div = $('<div/>', {class: 'facet'});
            div.html("<h3>" + K5.i18n.translatable('facet.model_path') + "</h3>");
            obj.prepend(div);
            $.each(root_models, function (val, count) {
                facets.addFacetValue(div, "typ_titulu", val, count, false);
            });
        }
        K5.gui.vc.translate(K5.i18n.ctx.language);
    }
};



function togglePin() {
    $("#facets").toggleClass("pin");
}

