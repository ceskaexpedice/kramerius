sfHover = function() {
	var sfEls = document.getElementById("serviceMenu").getElementsByTagName("li");
	for (var i=0; i<sfEls.length; i++) {
		sfEls[i].onmouseover=function() {
			this.className+="smhover";
		}
		sfEls[i].onmouseout=function() {
			this.className=this.className.replace(new RegExp("smhover\\b"), "");
		}
	}
}
if (window.attachEvent) window.attachEvent("onload", sfHover);