function _ctxbuttonsrefresh(viewer) {
    $("#contextbuttons").html("");
    $(".mtd_footer .buttons>div").each(function (i, obj) { 
        if ($(this).data("ctx")) {
            
            var display = false;
            $(obj).hide();
            
            var a = $(this).data("ctx").split(";");
            if (viewer) {
                if (jQuery.inArray(viewer, a) > -1) {
                    display = true;
                }
            }

            // all context
            if (jQuery.inArray('all', a) > -1) {
                display = true;
            }


            // all context
            if (jQuery.inArray('share', a) > -1) {
                display = true;
            }

            // only selected
            if (jQuery.inArray('selected', a) > -1) {
                if (K5.gui.clipboard.isCurrentSelected()) {
                    display = true;
                }
            }

            // only notselected
            if (jQuery.inArray('notselected', a) > -1) {
                if (!K5.gui.clipboard.isCurrentSelected()) {
                    display = true;
                }
            }
            
            // only clipboard
            if (jQuery.inArray('clipboardnotempty', a) > -1) {
                if (K5.gui.clipboard.getSelected().length > 0) {
                    display = true;
                }
            }

            // add to favorites
            if (jQuery.inArray('favorite', a) > -1 ) {
                var addf = false;
                if (K5.authentication.profileDisplay != null) {
                    addf = !K5.authentication.profileDisplay.isCurrentPidInFavorites();
                } else {
                    addf = true;
                }
                if (addf) {
                    display = true;
                }
            }
            
            // remove from favorites
            if (jQuery.inArray('notfavorite', a) > -1) {
                var addf = false;
                if (K5.authentication.profileDisplay != null) {
                    addf = K5.authentication.profileDisplay.isCurrentPidInFavorites();
                }
                if (addf) {
                    display = true;
                }
            }
            
            // download icon
            if (jQuery.inArray('download', a) > -1) {
                var downenabled = false;
                if (K5.gui.downloadoptions.ctx.actions) {
                    downenabled = _.reduce(
                            K5.gui.downloadoptions.ctx.actions,
                            function(memo, a) {
                                if (!memo) {
                                    if (a.object.enabled()) {
                                        memo = true;
                                        return memo;
                                    }
                                }
                                return memo;
                            }, false);

                }
                if (downenabled) {
                    display = true;
                }
                    
            }

            if (jQuery.inArray('structurebuttons', a) > -1) {
                if (K5.gui.selected && K5.gui.selected.containsLeftStructure) {
                    display = K5.gui.selected.containsLeftStructure();
                } else {
                    display = true;
                }
            }

            if (jQuery.inArray('nextpage', a) > -1) {
                if (K5.gui.selected && K5.gui.selected.nextPageEnabled) {
                    display = K5.gui.selected.nextPageEnabled();
                } else {
                    display = true;
                }
            }

            if (jQuery.inArray('prevpage', a) > -1) {
                if (K5.gui.selected && K5.gui.selected.prevPageEnabled) {
                    display = K5.gui.selected.prevPageEnabled();
                } else {
                    display = true;
                }
            }

            // next context -- TODO: kick out
            if (jQuery.inArray('next', a) > -1) {

                if (K5.api.ctx["item"][selected]["siblings"]) {
                    var data = K5.api.ctx["item"][selected]["siblings"];
                    var arr = data[0]['siblings'];
                    var index = _.reduce(arr, function(memo,
                            value, index) {
                        return (value.selected) ? index : memo;
                    }, -1);
                    if (index < arr.length - 1) {
                        if (K5.gui.selected.nextPageEnabled) {
                            display = K5.gui.selected.nextPageEnabled();
                        } else {
                            display = true;
                        }
                    }
                }
            }

            // prev context -- TODO: kick out
            if (jQuery.inArray('prev', a) > -1) {
                // contains siblings and must be enabled by viewer

                if (K5.api.ctx["item"][selected]["siblings"]) {
                    var data = K5.api.ctx["item"][selected]["siblings"];
                    var arr = data[0]['siblings'];
                    var index = _.reduce(arr, function(memo,
                            value, index) {
                        return (value.selected) ? index : memo;
                    }, -1);
                    if (index > 0) {
                        if (K5.gui.selected.prevPageEnabled) {
                            display = K5.gui.selected.prevPageEnabled();
                        } else {
                            display = true;
                        }
                    }
                }
            }

            if (jQuery.inArray('parent', a) > -1) {
                var pid = K5.api.ctx["item"]["selected"];
                var data = K5.api.ctx["item"][pid];
                var itemContext = data.context[0]; // jinak?
                if (itemContext.length > 1) {
                    display = true;
                }
            }
            
            if (display) {
                $(obj).show();
            } else {
                $(obj).hide();
            }
            
        }

    });
    // TODO: Moved buttons
    /*
    $("#item_menu>div")
            .each(
                    function() {
                        if ($(this).data("ctx")) {
                            var a = $(this).data("ctx").split(";");
                            if (viewer) {
                                if (jQuery.inArray(viewer, a) > -1) {
                                    $("#contextbuttons").append($(this).clone());
                                }
                            }

                            // all context
                            if (jQuery.inArray('all', a) > -1) {
                                $("#contextbuttons").append($(this).clone());
                            }


                            // all context
                            if (jQuery.inArray('share', a) > -1) {
                                $("#contextbuttons").append($(this).clone());
                            }

                            // only selected
                            if (jQuery.inArray('selected', a) > -1) {
                                if (K5.gui.clipboard.isCurrentSelected()) {
                                    $("#contextbuttons")
                                            .append($(this).clone());
                                }
                            }

                            // only notselected
                            if (jQuery.inArray('notselected', a) > -1) {
                                if (!K5.gui.clipboard.isCurrentSelected()) {
                                    $("#contextbuttons")
                                            .append($(this).clone());
                                }
                            }
                            
                            // only clipboard
                            if (jQuery.inArray('clipboardnotempty', a) > -1) {
                                if (K5.gui.clipboard.getSelected().length > 0) {
                                    $("#contextbuttons")
                                            .append($(this).clone());
                                }
                            }

                            // add to favorites
                            if (jQuery.inArray('favorite', a) > -1 ) {
                                var addf = false;
                                if (K5.authentication.profileDisplay != null) {
                                    addf = !K5.authentication.profileDisplay.isCurrentPidInFavorites();
                                } else {
                                    addf = true;
                                }
                                if (addf) {
                                    $("#contextbuttons")
                                        .append($(this).clone());
                                }
                            }
                            
                            // remove from favorites
                            if (jQuery.inArray('notfavorite', a) > -1) {
                                var addf = false;
                                if (K5.authentication.profileDisplay != null) {
                                    addf = K5.authentication.profileDisplay.isCurrentPidInFavorites();
                                }
                                if (addf) {
                                    $("#contextbuttons")
                                        .append($(this).clone());
                                }
                            }
                            
                            // download icon
                            if (jQuery.inArray('download', a) > -1) {
                                var downenabled = false;
                                if (K5.gui.downloadoptions.ctx.actions) {
                                    downenabled = _.reduce(
                                            K5.gui.downloadoptions.ctx.actions,
                                            function(memo, a) {
                                                if (!memo) {
                                                    if (a.object.enabled()) {
                                                        memo = true;
                                                        return memo;
                                                    }
                                                }
                                                return memo;
                                            }, false);

                                }
                                if (downenabled)
                                    $("#contextbuttons")
                                            .append($(this).clone());
                            }

                            // next context
                            if (jQuery.inArray('next', a) > -1) {
                                if (K5.api.ctx["item"][selected]["siblings"]) {
                                    var data = K5.api.ctx["item"][selected]["siblings"];
                                    var arr = data[0]['siblings'];
                                    var index = _.reduce(arr, function(memo,
                                            value, index) {
                                        return (value.selected) ? index : memo;
                                    }, -1);
                                    if (index < arr.length - 1) {
                                        $("#contextbuttons").append(
                                                $(this).clone());
                                    }
                                }
                            }

                            // prev context
                            if (jQuery.inArray('prev', a) > -1) {
                                if (K5.api.ctx["item"][selected]["siblings"]) {
                                    var data = K5.api.ctx["item"][selected]["siblings"];
                                    var arr = data[0]['siblings'];
                                    var index = _.reduce(arr, function(memo,
                                            value, index) {
                                        return (value.selected) ? index : memo;
                                    }, -1);
                                    if (index > 0) {
                                        $("#contextbuttons").append(
                                                $(this).clone());
                                    }
                                }
                            }

                            if (jQuery.inArray('parent', a) > -1) {
                                var pid = K5.api.ctx["item"]["selected"];
                                var data = K5.api.ctx["item"][pid];
                                var itemContext = data.context[0]; // jinak?
                                if (itemContext.length > 1) {
                                    $("#contextbuttons").append($(this).clone());
                                }
                            }
                        }
                    });*/
    
}
