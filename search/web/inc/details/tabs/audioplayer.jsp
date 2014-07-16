<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false" %>

<view:object name="audio" clz="cz.incad.Kramerius.views.inc.details.tabs.AudioViewObject"></view:object>

<link rel="stylesheet" href="js/jplayer/skin/blue.monday/jplayer.blue.monday.css" type="text/css" />
<script type="text/javascript" src="js/jplayer/jquery.jplayer.min.js"></script>

<div id="jquery_jplayer_1" class="jp-jplayer"></div>
<div id="jp_container_1" class="jp-audio">
    <div class="jp-type-single">
        <div class="jp-gui jp-interface">
            <ul class="jp-controls">
                <li><a href="javascript:;" class="jp-play" tabindex="1">play</a></li>
                <li><a href="javascript:;" class="jp-pause" tabindex="1">pause</a></li>
                <li><a href="javascript:;" class="jp-stop" tabindex="1">stop</a></li>
                <li><a href="javascript:;" class="jp-mute" tabindex="1" title="mute">mute</a></li>
                <li><a href="javascript:;" class="jp-unmute" tabindex="1" title="unmute">unmute</a></li>
                <li><a href="javascript:;" class="jp-volume-max" tabindex="1" title="max volume">max volume</a></li>
            </ul>
            <div class="jp-progress">
                <div class="jp-seek-bar">
                    <div class="jp-play-bar"></div>
                </div>
            </div>
            <div class="jp-volume-bar">
                <div class="jp-volume-bar-value"></div>
            </div>
            <div class="jp-time-holder">
                <div class="jp-current-time"></div>
                <div class="jp-duration"></div>
                <ul class="jp-toggles">
                    <li><a href="javascript:;" class="jp-repeat" tabindex="1" title="repeat">repeat</a></li>
                    <li><a href="javascript:;" class="jp-repeat-off" tabindex="1" title="repeat off">repeat off</a></li>
                </ul>
            </div>
        </div>
        <div class="jp-title" />
        <div class="jp-no-solution">
            <span>Update Required</span>
            To play the media you will need to either update your browser to a recent version or update your <a
                href="http://get.adobe.com/flashplayer/" target="_blank">Flash plugin</a>.
        </div>
    </div>
</div>

<script type="text/javascript">

    var audioDiv = "#jquery_jplayer_1";
    var secErrorDiv = "#audioSecurityError";

    $(document).ready(function () {
        $.get('audioTracks?action=isTrack&pid_path=' + viewerOptions.pid, function (data) {
            if (data.isTrack) {
                $('#audioPlayer_li').show();
                renderPlayerTab(viewerOptions);
            } else {
                // TODO: canContainTracks -> playlist
                hidePlayerTab();
            }
        });
    });

    function renderPlayerTab(viewerOptions) {
        if (!viewerOptions) return;
        if (viewerOptions.rights["read"][viewerOptions.pid]) {
            display(audioDiv);
            loadTrack(viewerOptions.pid);
        } else {
            display(secErrorDiv);
        }
    }

    function loadTrack(pid) {
        $("#jquery_jplayer_1").jPlayer({
            ready: function () {
                $(this).jPlayer("setMedia", {
                    oga: "http://" + window.location.host + "/search/audioProxy/" + pid + "/OGG",
                    wav: "http://" + window.location.host + "/search/audioProxy/" + pid + "/WAV",
                    mp3: "http://" + window.location.host + "/search/audioProxy/" + pid + "/MP3"
                });
            },
            swfPath: "js/jplayer/swf",
            supplied: "oga, wav, mp3"
        });

        $.get('audioTracks?action=getTracks&pid_path=' + pid, function (data) {
            $(".jp-title").html('<ul><li>' + data.tracks[0].title + '</li></ul>');
        });
    }

    function display(contentToShow) {
        $.each([
            audioDiv,
            secErrorDiv],
                function (index, item) {
                    if (item == contentToShow) {
                        $(item).show();
                    } else {
                        $(item).hide();
                    }
                });
    }

    function hidePlayerTab() {
        $('#audioPlayer_li').hide();
        $(audioDiv).hide();
        $("#audioSecurityError").hide();
        bigThumbZone
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

