/*
 Based on xmlEditor by
 Copyright (c) 2010 Aleksandar Kolundzija <ak@subchild.com>
 Version 1.5
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 
 */




var ModsXml = function (elem, options) {

    this.elem = $(elem);
    this.dict = modslocale[K5.i18n.ctx['language']];
    this._nodeRefs = [], // will hold references to XML nodes   
            this._initNodeState = "expandable",
            this._$event = $({}), // beforeHtmlRendered, afterHtmlRendered, beforeToggleNode, afterToggleNode
            this._message = {
                "renderingHtml": "Rendering XML structure...",
                "readyToEdit": "Ready to edit.",
                "removeAttrConfirm": "Are you sure want to delete this attribute and its value?",
                "invalidAttrName": "The attribute name you entered is invalid.\nPlease try again.",
                "invalidNodeName": "The node name you entered is invalid.\nPlease try again.",
                "noTextValue": "(No text value. Click to edit.)",
                "removeNodeSuccess": "Removed node.",
                "removeNodeConfirm": "Are you sure you want to remove this node?",
                "xmlLoadSuccess": "XML file was loaded successfully.",
                "xmlLoadProblem": "There was a problem loading XML file."
            };

    this.parentRefs = []; // hash of references to previous sibling's parents. used for appending next siblings
    this._nodeRefs = []; // initialize node references (clear cache)
    this.parentRefIndex = 0;
    this.$trueParent = false;

};

ModsXml.prototype = {
    /**
     * Visits every node in the DOM and runs the passed function on it.
     * 
     * @param node {Object} DOM node
     * @param func callback function
     */
    _traverseDOM: function (node) {
        this.appendNode(node);
        node = node.firstChild;
        while (node) {
            this._traverseDOM(node);
            node = node.nextSibling;
        }
    },
    /**
     * @param  node {Object}
     * @return {Boolean}
     */
    _isCommentNode: function (node) {
        return (node.nodeType === 8);
    },
    /**
     * Retrieves XML node using nodeIndex attribute of passed $elem
     * @param $elem {Object} jQuery DOM element
     * @return XML node
     */
    _getNodeFromElemAttr: function ($elem) {
        var nodeRefIndex = $elem.closest("li.node").attr("nodeIndex"); // $elem.attr("nodeIndex");
        return this._nodeRefs[nodeRefIndex];
    },
    /**
     * Returns a string representing path to passed node. The path is not unique 
     * (same path is returned for all sibling nodes of same type).
     * @param node {Object} DOM node
     */
    _getNodePath: function (node) {
        var pathArray = [];
        var parent = node;
        do {
            var s = parent.nodeName;
            for (var i = 0; i < parent.attributes.length; i++) {
                var attr = parent.attributes.item(i);
                if (attr.nodeName.indexOf("xmlns") < 0 && attr.nodeName !== "this.parentRefIndex") {
                    s += "[@" + attr.nodeName + " = " + attr.value + "]";
                }
            }
            pathArray.push(s);
            parent = parent.parentNode;
        }
        while ((parent) && (typeof parent !== 'undefined') && (parent.nodeName !== "#document"));
        var full = (pathArray.reverse()).join(" > ");

        return full;
    },
    /**
     * Binds custom event to private _$event object
     */
    _bind: function (eventName, dataOrFn, fnOrUndefined) {
        this._$event.bind(eventName, dataOrFn, fnOrUndefined);
    },
    /**
     * Unbinds custom event from private _$event object
     */
    _unbind: function (eventName, fn) {
        this._$event.unbind(eventName, fn);
    },
    /**
     * Returns an HTML string representing node attributes
     * @param  node {Object} DOM object
     * @return {String}
     */
    _getEditableAttributesHtml: function (node) {
        if (!node.attributes) {
            return "";
        }
        var attrsHtml = "<span class='nodeAttrs'>",
                totalAttrs = node.attributes.length;
        for (var i = 0; i < totalAttrs; i++) {
            attrsHtml += "<span class='singleAttr'>" + node.attributes[i].name +
                    "=\"<span class='attrValue' name='" + node.attributes[i].name + "'>" +
                    ((node.attributes[i].value === "") ? "&nbsp;" : node.attributes[i].value) +
                    "</span>\"</span>";
        }
        attrsHtml += "</span>";
        return attrsHtml;
    },
    /**
     * Retrieves non-empty text nodes which are children of passed XML node. 
     * Ignores child nodes and comments. Strings which contain only blank spaces 
     * or only newline characters are ignored as well.
     * @param  node {Object} XML DOM object
     * @return jQuery collection of text nodes
     */
    _getTextNodes: function (node) {
        return $(node).contents().filter(function () {
            return (
                    ((this.nodeName == "#text" && this.nodeType == "3") || this.nodeType == "4") && // text node, or CDATA node
                    ($.trim(this.nodeValue.replace("\n", "")) !== "") // not empty
                    );
        });
    },
    /**
     * Retrieves (text) node value
     * @param node {Object}
     * @return {String}
     */
    _getNodeValue: function (node) {
        var $textNodes = this._getTextNodes(node),
                textValue = (node && this._isCommentNode(node)) ? node.nodeValue : ($textNodes[0]) ? $.trim($textNodes[0].textContent) : "";
        return textValue;
    },
    /**
     * Detects if passed node has next sibling which is not a text node
     * @param  node {Object} XML DOM object
     * @return node or false
     */
    _getRealNextSibling: function (node) {
        do {
            node = node.nextSibling;
        }
        while (node && node.nodeType !== 1);
        return node;
    },
    /**
     * Toggles display by swapping class name (collapsed/expanded) and toggling
     * visibility of child ULs.
     * @TODO make use of setTimeouts to address delay when many children
     * @TODO if only allowing single expanded node at a time, will need to collapse others
     */
    _toggleNode: function () {
        this._$event.trigger("beforeToggleNode");
        var $thisLi = $(this);
        $thisLi.find(">ul").toggle("normal"); // animate({height:"toggle"});		
        if ($thisLi.hasClass("collapsable")) {
            $thisLi.removeClass("collapsable").addClass("expandable");
        }
        else {
            $thisLi.removeClass("expandable").addClass("collapsable");
        }
        this._$event.trigger("afterToggleNode");
    },
    /**
     * Returns number of XML nodes
     * @TODO Includes text nodes.  Should it?
     */
    _getXmlNodeCount: function () {
        return $('*', this.xml).length;
    },
    xml: {}, // variable will hold the XML DOM object		
    $container: $(document.body), // initialize as body, but should override with specific container


    /**
     * Assigns handlers for editing nodes and attributes. Happens only once, during renderAsHTML()
     */
    assignEditHandlers: function () {
        K5.eventsHandler.addHandler(_.bind(function(type, configuration) {
            if (type === "i18n/dictionary") {
                this.translateAll();
            } 
        },this));
        this.$container.find("li.node").mouseover(_.partial(function (modsxml, e) {
            var $this = $(this);
            var node = modsxml._getNodeFromElemAttr($this);
            var path = modsxml._getNodePath(node);
            //var tr = modsxml.translate(path);
            
            modsxml.$container.find("div.nodePath").text(modsxml.cropPath(path));


            var tr = $(this).data("tr");
            modsxml.nodeTooltip.text(tr);
            if (node.firstElementChild === null && node.firstChild !== null) {
                modsxml.nodeTooltip.append(": <span>" + node.firstChild.data + "</span>");
            }

            modsxml.nodeTooltip.css({
                left: e.pageX - e.offsetX + 20,
                top: e.pageY - e.offsetY - 30
            });
            e.stopPropagation();
        }, this));
        this.$container.find("li.node").mouseout(_.partial(function (modsxml, e) {
            $("div.nodePath").empty();
            modsxml.nodeTooltip.empty();
            modsxml.nodeTooltip.css({
                left: -2000,
                top: -30
            });
        }, this));
    },
    /**
     * Returns HTML representation of passed node.
     * Used during initial render, as well as when creating new child nodes.
     * @param node   {Object}
     * @param state  {String}  Ex: "expandable"
     * @param isLast {Boolean} Indicates whether there are additional node siblings
     * @returns {String}
     * @TODO replace anchor with button
     */
    getNewNodeHTML: function (node, state, isLast) {

        var nodeIndex = this._nodeRefs.length - 1,
                nodeValue = this._getNodeValue(node),
                nodeAttrs = this._getEditableAttributesHtml(node),
                //nodeValueStr = (nodeValue) ? nodeValue : "<span class='noValue'>" + _message["noTextValue"] + "</span>";
                nodeValueStr = (nodeValue) ? '<li class="last"><p class="nodeValue">' + nodeValue + '</p></li>' : '';
        var nodeHtml = "";
        if (this._isCommentNode(node)) { // display comment node
            nodeHtml = '<li class="node comment ' + state + (isLast ? ' last' : '') + '" nodeIndex="' + nodeIndex + '">' +
                    '<div class="hitarea' + (isLast ? ' last' : '') + '"/>' +
                    '<span class="nodeName">comment</span>' +
                    '<ul class="nodeCore">' +
                    (nodeValue) ? '<li class="last"><p class="nodeValue">' + nodeValue + '</p></li>' : '<li></li>' +
                    //	'<li class="last"><p class="nodeValue">'+ nodeValueStr +'</p></li>' +
                    '</ul>' +
                    '</li>';
        }
        else { // display regular node
            nodeHtml = '<li class="node ' + node.nodeName + ' ' + state + (isLast ? ' last' : '') + '" nodeIndex="' + nodeIndex + '">' +
                    '<div class="hitarea' + (isLast ? ' last' : '') + '"/>' +
                    '<span class="nodeName">' + node.nodeName + '</span>' + nodeAttrs +
                    '<ul class="nodeCore">' +
                    nodeValueStr +
                    //'<li class="last"><p class="nodeValue">'+ nodeValueStr +'</p></li>' +
                    '</ul>' +
                    '</li>';
        }
        return nodeHtml;
    },
    /**
     * local utility method for appending a single node
     * @param node {Object}
     */
    appendNode: function (node) {
        
        if (node.nodeType !== 1 && !this._isCommentNode(node)) { // exit unless regular node or comment
            return;
        }
        this._nodeRefs.push(node); // add node to hash for future reference (cache)
        var $xmlPrevSib = $(node).prev(),
                realNextSib = this._getRealNextSibling(node),
                nodeHtml = this.getNewNodeHTML(node, this._initNodeState, !realNextSib),
                $li = $(nodeHtml),
                $ul;


        if ($xmlPrevSib.length) { // appending node to previous sibling's parent

            this.$parent = this.parentRefs[$xmlPrevSib.attr("this.parentRefIndex")];
            $xmlPrevSib.removeAttr("this.parentRefIndex");
            $(node).attr("this.parentRefIndex", this.parentRefIndex);
            this.parentRefs[this.parentRefIndex] = this.$parent;
            this.parentRefIndex++;
            this.$trueParent = $li;
            this.$parent.append($li);
        }
        else { // appending a new child

            if (this.$trueParent) {
                this.$parent = this.$trueParent;
                this.$trueParent = false;
            }
            /*
             @TODO: move ul.children into getNewNodeHTML(). 
             here's how: check if $parent.find("ul.children"), if so use it, if not make root UL
             // $ul = ($parent.find(">ul.children").length) ? $parent.find(">ul.children:first") : $("<ul class='root'></ul>");
             */
            $ul = $("<ul class='children'></ul>").append($li);
            this.$parent.append($ul);
            if (!this._isCommentNode(node)) {
                this.$parent = $li;
                $(node).attr("this.parentRefIndex", this.parentRefIndex);
                this.parentRefs[this.parentRefIndex] = $ul;
                this.parentRefIndex++;
            }
        }
        
        

        var path = this._getNodePath(node);
        var tr = this.translate(path);
        $li.data("tr", tr);
        var nodename = $li.children("span.nodeName").text();
        $li.data("nodename", nodename);
        $li.data("nodepath", path);
    }, // end of appendNode()

    /**
     * Renders XML as an HTML structure.  Uses _traverseDOM() to render each node.
     * @TODO Explore use of documentFragment to optimize DOM manipulation
     */
    renderAsHTML: function () {
        this._$event.trigger("beforeHtmlRendered");
        this.$container.empty();
        this.nodeTooltip = $('<div class="nodePathTr"></div>');
        this.$container.append(this.nodeTooltip);
        this.$parent = this.$container;
        this._traverseDOM(this.xml);
        $("*", this.xml).removeAttr("this.parentRefIndex"); // clean up remaining this.parentRefIndex-es
        this.assignEditHandlers(); // bind in core app afterHtmlRendered

        this._$event.trigger("afterHtmlRendered");
    },
    /**
     * Sets value of node to the passed text. Existing value is overwritten,
     * otherwise new value is set.
     * @param node  {Object}
     * @param value {String}
     */
    setNodeValue: function (node, value) {
        var $textNodes = this._getTextNodes(node);
        if ($textNodes.get(0))
            $textNodes.get(0).nodeValue = value;
        else
            node["textContent"] = value;
    },
    /**
     * Returns string representation of private XML object
     */
    getXmlAsString: function () {
        return (typeof XMLSerializer !== "undefined") ?
                (new window.XMLSerializer()).serializeToString(this.xml) :
                this.xml.xml;
    },
    /**
     * Converts passed XML string into a DOM element.
     * @param xmlStr {String}
     * @TODO Should use this instead of loading XML into DOM via $.ajax()
     */
    getXmlDOMFromString: function (xmlStr) {
        if (window.ActiveXObject && window.GetObject) {
            var dom = new ActiveXObject('Microsoft.XMLDOM');
            dom.loadXML(xmlStr);
            return dom;
        }
        if (window.DOMParser) {
            return new DOMParser().parseFromString(xmlStr, 'text/xml');
        }
        throw new Error('No XML parser available');
    },
    /**
     * Loads file path from the first argument via Ajax and makes it available as XML DOM object.
     * Sets the $container which will hold the HTML tree representation of the XML.
     * @param xmlPath           {String} Path to XML file
     * @param containerSelector {String} CSS query selector for creating jQuery reference to container
     * @param callback          {Function}
     */
    loadXmlFromFile: function (xmlPath, containerSelector, callback) {
        this.$container = $(containerSelector);
        $.ajax({
            type: "GET",
            async: false,
            url: xmlPath,
            dataType: "xml",
            error: function () {
                //GLR.messenger.show({msg:_message["xmlLoadProblem"], mode:"error"}); 
            },
            success: _.bind(function (xml) {
                //GLR.messenger.show({msg:_message["xmlLoadSuccess"], mode:"success"});
                //console.dir(xml);
                this.xml = xml;
                callback();
            }, this)
        });
    },
    /**
     * Creates a DOM representation of passed xmlString and stores it in the .xml property
     * @param xmlPath           {String} Path to XML file
     * @param containerSelector {String} CSS query selector for creating jQuery reference to container
     * @param callback          {Function}
     */
    loadXmlFromString: function (xmlString, containerSelector, callback) {
        this.$container = $(containerSelector);
        this.xml = this.getXmlDOMFromString(xmlString);
        callback();
    },
    /**
     * Creates a DOM representation of passed xmlString and stores it in the .xml property
     * @param xmlPath           {String} Path to XML file
     * @param containerSelector {String} CSS query selector for creating jQuery reference to container
     * @param callback          {Function}
     */
    loadXmlFromDocument: function (doc, containerSelector, callback) {
        this.$container = $(containerSelector);
        this.xml = doc;

    },
    compact: function () {
        this.$container.find('li').each(function () {
            if ($(this).find('ul.children').length === 0) {
                var value = $(this).find('p.nodeValue');
                if (value && value.text().trim() === "") {
                    if ($(this).siblings().length === 0) {
                        $(this).parent().parent().hide();
                    } else {
                        $(this).hide();
                    }

                }
            }
        });
        this.$container.find('li.node:visible').each(function () {
            if ($(this).find('ul.children>li:visible').length === 0 && $(this).find('p.nodeValue').length === 0) {
                $(this).hide();
            }
        });
        this.$container.find('li.node[nodeIndex=0]>.nodeName').hide();
        this.$container.find('li.node[nodeIndex=1]>.nodeName').hide();
        this.isCompacted = true;

    },
    expand: function () {
        this.isCompacted = false;
        this.$container.find('li').show();
        this.$container.find('li.node[nodeIndex=0]>.nodeName').show();
        this.$container.find('li.node[nodeIndex=1]>.nodeName').show();
    },
    toggle: function(){
        if(this.isCompacted){
            this.expand();
            this.showNodeNames();
        }else{
            this.compact();
            this.showTranslated();
        }
    },
    translateNodes: function () {
        this.$container.find('li span.nodeName').each(function () {
            $(this).attr("data-before", "<");
        });

        this.$container.find('li span.nodeAttrs').each(function () {
            $(this).attr("data-after", ">");
            
        });
    },
    showTranslated: function () {

        this.$container.find('li span.nodeName').each(function () {
            $(this).attr("data-before", "");
        });

        this.$container.find('li span.nodeAttrs').each(function () {
            $(this).attr("data-after", "");
            $(this).css("display", "none");
        })
        this.$container.find('li>span.nodeName').each(function () {
            $(this).text($(this).parent().data('tr'));
        });
        this.$container.find('ul.nodeCore').css("display", "inline-block");
    },
    showNodeNames: function () {

        this.$container.find('li span.nodeName').each(function () {
            $(this).attr("data-before", "<");
        });

        this.$container.find('li span.nodeAttrs').each(function () {
            $(this).attr("data-after", ">");
            $(this).css("display", "inline");
        });

        this.$container.find('li>span.nodeName').each(function () {
            $(this).text($(this).parent().data('nodename'));
        });
        this.$container.find('ul.nodeCore').css("display", "block");
    },
    cropPath: function (path) {
        //var s = orig.toLowerCase().replace(/a\:/g, "");
        var s = path.toLowerCase().replace(/>[^\:]+\:/g, "> ");
        var p = s.indexOf(" > mods ");
        if (p > -1) {
            s = s.substring(s.indexOf("> ", p + 3) + 2);
        } else {
            p = s.indexOf(" > mods[");
            if (p > -1) {
                s = s.substring(s.indexOf("> ", p + 3) + 2);
            }
        }
        return s;
    },
    translateAll: function(){
        this.dict = modslocale[K5.i18n.ctx['language']];
        this.$container.find('li.node').each(_.partial(function (modsxml) {
            var $li = $(this);
            var node = modsxml._getNodeFromElemAttr($li);
            var path = modsxml._getNodePath(node);
            var tr = modsxml.translate(path);
            $li.data("tr", tr);
            var nodename = $li.children("span.nodeName").text();
            $li.data("nodename", nodename);
            $li.data("nodepath", path);
        }, this));
        this.showTranslated();
        
    },
    translate: function (orig) {
        var s = this.cropPath(orig);
        if (this.dict.hasOwnProperty(s)) {
            return this.dict[s];
        } else {
            //posledni tag
            var p = s.lastIndexOf("> ");
            if (p === -1) {
                p = -2;
            }
            var s1 = s.substring(p + 2);
            if (this.dict.hasOwnProperty(s1)) {
                return this.dict[s1];
            }

            //bez attributu
            s1 = s.replace(/\[[^\]]+\]/g, "");
            if (this.dict.hasOwnProperty(s1)) {
                return this.dict[s1];
            }

            //posledni tag
            p = s1.lastIndexOf("> ");
            if (p === -1) {
                p = -2;
            }
            s1 = s1.substring(p + 2);
            if (this.dict.hasOwnProperty(s1)) {
                return this.dict[s1];
            } else {
                return s1;
            }
        }
        return orig;
    },
    /**
     * Calls methods for generating HTML representation of XML, then makes it collapsible/expandable
     */
    renderTree: function () {
        this.renderAsHTML();
        this.$container.find("ul:first").addClass("treeview");
    }



};


var modslocale = {
    "en":{
        "titleinfo": "Title info",
        "titleinfo > title": "Main title",
        "titleinfo > partnumber": "Part number",
        "titleinfo > partname": "Part name",
        "title": "Title",
        "name[@type = personal]": "Author",
        "namepart": "Author",
        "namepart[@type = family]": "Given name",
        "namepart[@type = given]": "Surname",
        "namepart[@type = date]": "Date",
        "dateissued": "Issued date",
        "identifier[@type = issn]": "ISSN",
        "identifier[@type = isbn]": "ISBN",
        "identifier[@type = ccnb]": "čČNB",
        "subtitle": "Subtitle",
        "part > detail[@type = regularsupplement]": "Supplement",
        "part > detail[@type = specialsupplement]": "Special supplement",
        "part > date": "Date",
        "part": "Part",
        "number": "Number",
        "detail[@type = pagenumber]": "Page number",
        "detail[@type = pageindex]": "Page index",
        "part > detail[@type = volume] > number": "Number",
        "language": "Language info",
        "languageterm": "Language",
        "publisher": "Publisher",
        "languageofcataloging": "Language of cataloging",
        "origininfo[@transliteration = publisher]": "Publisher",
        "origininfo[@transliteration = publisher] > publisher": "Publisher name",
        "origininfo[@transliteration = publisher] > place > placeterm": "Place of publication",
        "place": "Place",
        "origininfo > place > placeterm": "Place of publication",
        "origininfo[@transliteration = printer] > publisher": "Printer name",
        "origininfo[@transliteration = printer] > place > placeterm": "Place of print",
        "physicaldescription": "Physical description",
        "physicaldescription > extent": "Physical description",
        "physicaldescription > form": "Form",
        "role": "Role",
        "roleterm": "Role name",
        "origininfo": "Origin info",
        "typeofresource": "Type of resource",
        "physicallocation": "Physical location",
        "location": "Location",
        "shelflocator": "Shelf locator",
        "recordinfo": "Record info",
        "recordorigin": "Record origin",
        "recordcontentsource": "Record content source",
        "recordcreationdate": "Record creation date",
        "recordchangedate": "Record change date",
        "recordidentifier": "Record identifier",
        "identifier": "Identifier",
        "issuance": "Issuance",
        "note":"Note",
        "detail": "Detail",
        "subject": "Subject",
        "geographic": "Geographic",
        "geographiccode": "Geographic code",
        "cartographics":"Cartographics",
        "scale":"Scale",
        "coordinates": "Coordinates",
        "descriptionstandard":"Description standard",
        "classification":"classification",
        "genre":"Genre",
        "extent": "Extent",
        "start": "Start",
        "end":"End",
        "frequency":"Frequency",
        "topic":"Topic",
        "abstract":"Abstract",
        "relateditem":"Related item"
    },
    "cs": {
        "titleinfo": "Názvová informace",
        "titleinfo > title": "Hlavní název",
        "titleinfo > partnumber": "Číslo svazku",
        "titleinfo > partname": "Název svazku",
        "title": "Název",
        "name[@type = personal]": "Autor",
        "namepart": "Autor",
        "namepart[@type = family]": "Příjmení",
        "namepart[@type = given]": "Jméno",
        "namepart[@type = date]": "Datum",
        "dateissued": "Datum vydání",
        "identifier[@type = issn]": "ISSN",
        "identifier[@type = isbn]": "ISBN",
        "identifier[@type = ccnb]": "čČNB",
        "subtitle": "Podnázev",
        "part > detail[@type = regularsupplement]": "Příloha",
        "part > detail[@type = specialsupplement]": "Speciální příloha",
        "part > date": "Datum vydání",
        "part": "Část",
        "number": "Číslo",
        "detail[@type = pagenumber]": "Číslo stránky",
        "detail[@type = pageindex]": "Index stránky",
        "part > detail[@type = volume] > number": "Číslo",
        "language": "Jazykové údaje",
        "languageterm": "Jazyk",
        "languageofcataloging": "Jazyk katalogového záznamu",
        "publisher": "Vydavatel",
        "origininfo[@transliteration = publisher]": "Vydavatel",
        "origininfo[@transliteration = publisher] > publisher": "Název vydavatele",
        "origininfo[@transliteration = publisher] > place > placeterm": "Místo vydání",
        "place": "Místo",
        "origininfo > place > placeterm": "Místo vydání",
        "origininfo[@transliteration = printer] > publisher": "Název tiskaře",
        "origininfo[@transliteration = printer] > place > placeterm": "Místo tisku",
        "physicaldescription": "Fyzický popis",
        "physicaldescription > extent": "Fyzický popis",
        "physicaldescription > form": "Podoba",
        "role": "Role",
        "roleterm": "Název role",
        "origininfo": "Informace o původu",
        "typeofresource": "Popis charakteristiky typu",
        "physicallocation": "Fyzické uložení",
        "location": "Uložení",
        "shelflocator": "Lokační údaje",
        "recordinfo": "Údaje o metadatovém záznamu",
        "recordorigin": "Údaje o vzniku záznamu",
        "recordcontentsource": "Kód nebo jméno instituce",
        "recordcreationdate": "Datum vytvoření",
        "recordchangedate": "Datum změny",
        "recordidentifier": "Identifikátor záznamu",
        "identifier": "Identifikátor",
        "issuance": "Údaje o vydávání",
        "note": "Poznámka",
        "detail": "Detail",
        "subject": "Údaje o věcném třídění",
        "geographic": "Geografické",
        "geographiccode": "Geografický kod",
        "cartographics":"Kartografické údaje",
        "scale":"Měřítko",
        "coordinates": "Souřadnice",
        "descriptionstandard":"Popis standardu",
        "classification":"Klasifikační údaje",
        "genre":"Žánr",
        "extent": "Rozsah",
        "start": "První stránka",
        "end":"Poslední stránka",
        "frequency":"Pravidelnost",
        "topic":"Výraz",
        "abstract":"Shrnutí obsahu",
        "relateditem":"Související dokument"

    }
};
    