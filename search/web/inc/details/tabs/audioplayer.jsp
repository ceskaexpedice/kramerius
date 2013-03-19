<%-- 
    Document   : audioplayer
    Created on : 27.11.2012, 11:29:14
    Author     : Martin Řehánek <rehan at mzk.cz>
--%>

<%@page import="java.util.Collections"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="cz.incad.kramerius.utils.XMLUtils"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.UnicodeUtil"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.InputStream"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>

<div id="audio.player" class="player"> 
    <div id="audio.playingTrackInfo" class="trackInfoContainer"></div>
    <div id="audio.progress" style="overflow:hidden">
        <span id ="audio.progressBar" class="progressBarSpan"></span>
        <span id ="audio.progressInfo" class="progressInfoSpan" ></span>
    </div>
    <div id="audio.buttons" >
        <span id ="audio.playPauseBtn" ></span>
        <span id ="audio.stopBtn" ></span>
        <span id ="audio.playPreviousBtn" ></span>
        <span id ="audio.playNextBtn" ></span>
        <span id ="audio.playlistButton"></span>
        <span id ="audio.volumeSlider" class="volumeSliderSpan">
            <span id ="audio.volumeInfo" class="volumeInfoSpan"></span>
        </span>
        <span id ="audio.volumeBtn" ></span>
    </div>
    <div id="audio.playlist"></div>
    <img src="img/audioplayer/Record-icon.png" id="record_pic" hidden="hidden"></img>
    <div id="audio.rotatingRecord" class="rotatingRecordDiv"></div>
    <div id="audio.html5Error" class="html5ErrorDiv"></div>
</div>
<script>
    $.ajaxSetup({
        cache: true
    });
    
    //bohuzel to, jestli byly nacteny, musim resit, 
    //protoze pri preklikavani mezi obrazky v horni liste 
    //se vykresluji vsechny panely znovu 
    //a nove nacitani stejneho skriptu zpusobovalo nefunkcnost prehravace
    if (window.audioScriptsLoaded){
        if (console) console.log("audio scripts already loaded");
        var renderer = renderPlayer(false);
        loadTracks(renderer, 0);
    } else{
        if (console) console.log("audio scripts not loaded yet");
        $.getScript('js/audioplayer/soundmanager/script/soundmanager2-jsmin.js', function(data, textStatus, jqxhr) {
            if (console) console.log('soundmanager.js: ' + textStatus + ' (' + jqxhr.status + ')');    
            $.getScript('js/audioplayer/audioplayer-compiled.js', function(data, textStatus, jqxhr) {
                if (console) console.log('audioplayer.js: ' + textStatus + ' (' + jqxhr.status + ')');    
                var renderer = renderPlayer(true);
                loadTracks(renderer, 0);
            });
        });
    }
    
    
    function renderPlayer(appendCss){
        var audioProxyUrl = 'audioProxy';
        var renderer = new Renderer(audioProxyUrl,'js/audioplayer/soundmanager/swf', false);
        if (appendCss){
            renderer.appendCss('css/audioplayer/audioplayer.css', 'audioplayer');
            renderer.appendCss('css/audioplayer/playlist.css', 'playlist');
        }
        return renderer;
    }
    
    function loadTracks(renderer){
        $.get('audioTracks?action=getTracks&pid_path=' + pid_path, function(data){
            renderReadableTracks(renderer, data.tracks);
        });
    }
    
    function renderReadableTracks(renderer, tracks){
        if (tracks.length != 0){
            //console.log("processing first track of " + tracks.length);
            var track = tracks[0];
            var pid = track.pid;
            $.ajax({
                url:"viewInfo?uuid=" + pid,
                complete:function(req, textStatus) {
                    if ((req.status==200) || (req.status==304)) {
                        var viewerOptions = eval('(' + req.responseText + ')');
                        if ((viewerOptions.rights["read"][pid])) {
                            if (console) console.log("rendering track " + pid);
                            renderer.addNewTrack(
                            pid,
                            track.title,
                            track.length,
                            track.mp3,
                            track.ogg,
                            track.wav);
                        } else {
                            //TODO: mozna je taky nacist, ale deaktivovane 
                            //a pri prehravani takove preskakovat
                            if (console) console.log("not allowed to play " + pid);
                        }
                    } else if (req.status==404){
                        if (console) console.log("cannot determine rights for playing track")
                    }
                    //continue rendering other tracks
                    renderReadableTracks(renderer, tracks.slice(1));
                }
            });
        }
    }
</script>

