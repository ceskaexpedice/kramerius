<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="audio" clz="cz.incad.Kramerius.views.inc.details.tabs.AudioViewObject"></view:object>

  <div id="audioSecurityError" class="ui-state-error-text" style="display: none;">
       ${audio.notAllowedMessageText}          
  </div>
  
  <div id="audio.player" class="player" style="display: none;"> 
	<audio controls>
		<div class="ui-state-error-text">${audio.notSupportedMessageText}</div>
	</audio>
  </div>

<script type="text/javascript">
	var audioDiv = "#audio\\.player";
	var secErrorDiv = "#audioSecurityError";
	
	$(document).ready(function() {
		$.get('audioTracks?action=isTrack&pid_path=' + viewerOptions.pid, function(data){
    		if (data.isTrack){ 
    			$('#audioPlayer_li').show();
    			renderPlayerTab(viewerOptions);	
	        } else { 
    	        // TODO: canContainTracks -> playlist
    	        hidePlayerTab();       			      			           		
    	    }
		});
	});

	function renderPlayerTab(viewerOptions) {
		if(!viewerOptions) return;
		if (viewerOptions.rights["read"][viewerOptions.pid]) {
			display(audioDiv);
            loadTrack(viewerOptions.pid);
        } else {           
        	display(secErrorDiv);            
        }
	}
		
	function loadTrack(pid){
        $.get('audioTracks?action=getTracks&pid_path=' + pid, function(data){
        	if (data.tracks[0].mp3) {
    			var mp3 = "http://krameriustest.mzk.cz/search/audioProxy/" + pid + "/MP3";
    			$('<source src="' + mp3 + '" type="audio/mpeg">').appendTo("audio");
            }
        	if (data.tracks[0].ogg) {
        		var ogg = "http://krameriustest.mzk.cz/search/audioProxy/" + pid + "/OGG";
        		$('<source src="' + ogg + '" type="audio/ogg">').appendTo("audio");
            }            
            if (data.tracks[0].wav) {
    			var wav = "http://krameriustest.mzk.cz/search/audioProxy/" + pid + "/WAV";
    			$('<source src="' + wav + '" type="audio/wav">').appendTo("audio");
            }
        });
	}

	function display(contentToShow) {        
        $.each([
            audioDiv,
            secErrorDiv],
        function(index,item) {
            if (item==contentToShow) {
                $(item).show();
                if (console) console.log("showing '"+item+"'");
            } else {
                $(item).hide();
            }
        });        
    }

    function hidePlayerTab() {
    	$('#audioPlayer_li').hide();
		$(audioDiv).hide();
		$("#audioSecurityError").hide(); bigThumbZone
		if (!$('#itemtab_audioPlayer').hasClass('ui-tabs-hide')) {
			$('#itemtab_audioPlayer').addClass('ui-tabs-hide');
			$('#bigThumbZone').removeClass('ui-tabs-hide');
			$("div[id='centralContent'] ul li:first").addClass('ui-tabs-selected');
			$("div[id='centralContent'] ul li:first").addClass('ui-state-active');		
			$('#audioPlayer_li').removeClass('ui-tabs-selected');
			$('#audioPlayer_li').removeClass('ui-state-active');
		} 
     }
	
</script>