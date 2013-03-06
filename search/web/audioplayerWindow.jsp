<%-- 
    Document   : audioplayerWindow
    Created on : 18.2.2013, 13:09:29
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


<html>
    <%@ include file="inc/html_header.jsp" %>
    <body>

        <div id="audio.player" class="player"> 

            <div class="playerBody">

                <!-- buttons PREVIOUS, PLAY/PAUSE, NEXT -->
                <span class="audioButtons" >
                    <span id ="audio.playPreviousBtn" ></span>
                    <span id ="audio.playPauseBtn" ></span>
                    <span id ="audio.playNextBtn" ></span>
                </span>

                <!-- central display with informations and button to show/hide playlist -->
                <div class="audioDisplayPanel">
                    <div id="audio.playingTrackInfo" class="trackInfoContainer unselectable"></div>
                    <div>
                        <span id ="audio.progressInfo" class="progressInfoContainer unselectable" ></span>
                        <span id ="audio.playlistButton"></span>
                    </div>
                </div>

                <!-- volume control -->
                <div class="volumePanel">
                    <div class ="volumeTitle unselectable" >Hlasitost</div>
                    <div>
                        <span id ="audio.muteBtn" ></span>
                        <span id ="audio.volumeSlider" class="volumeSliderContainer"></span>
                    </div>
                </div>

                <!--TODO: remove br-->
                <br>
                <span id ="audio.timeline" class="timelineContainer"></span>
            </div>

            <div id="audio.playlist" class="playlistArea"></div>
            <img src="img/audioplayer/Record-icon.png" id="record_pic" hidden="hidden"></img>
            <div id="audio.rotatingRecord" class="rotatingRecordDiv"></div>
            <div id="audio.html5Error" class="html5ErrorDiv"></div>
        </div>

        <script>
            $.ajaxSetup({
                cache: true
            });
    
            if (window.audioScriptsLoaded){
                console.log("audio scripts already loaded");
                //var renderer = renderPlayer(false);
                var renderer = renderPlayer(true);
                loadTracks(renderer, 0);
            } else{
                console.log("audio scripts not loaded yet");
                //opet kolize s minimalizaci
                //tentokrat soundmanager vs closure
                //z nejakeho duvodu se to ale neprojevuje, kdyz nasadim buildnuty K4 na localhost
                //ale na krameriustest.mzk.cz se to uz projevilo (i kdyz opravdu ne hned)
                //$.getScript('js/audioplayer/soundmanager/script/soundmanager2.js', function(data, textStatus, jqxhr) {
                $.getScript('js/audioplayer/soundmanager/script/soundmanager2-jsmin.js', function(data, textStatus, jqxhr) {
                    console.log('soundmanager.js: ' + textStatus + ' (' + jqxhr.status + ')');    
                    $.getScript('js/audioplayer/audioplayer-compiled.js', function(data, textStatus, jqxhr) {
                        console.log('audioplayer.js: ' + textStatus + ' (' + jqxhr.status + ')');    
                        var renderer = renderPlayer(true);
                        loadTracks(renderer, 0);
                    });
                });
            }
    
    
            function renderPlayer(appendCss){
                var audioProxyUrl = 'audioProxy';
                console.log('window:');
                console.log(window);
                //var renderer = new AudioRenderer(audioProxyUrl,'js/audioplayer/soundmanager/swf', false);
                var renderer = new AudioRenderer(audioProxyUrl,'js/audioplayer/soundmanager/swf', true);
                //console.log("renderer:");
                //console.log(renderer);
                if (appendCss){
                    renderer.appendCss('css/audioplayer.css', 'audioplayer');
                    console.log(document);
                }
                return renderer;
            }
            
            function getQueryParamValue(name){
                var url = window.location.search.substring(1);
                var params = url.split('&');
                for (var i = 0; i < params.length; i++) {
                    var param = params[i].split('=');
                    if (param[0] == name) {
                        return param[1];
                    }
                }
            }
            
                
            function loadTracks(renderer){
                var pid_path = getQueryParamValue('pid_path');
                console.log("loadTracks:pid_path: '" + pid_path + "'");
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
                                    console.log("rendering track " + pid);
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
                                    console.log("not allowed to play " + pid);
                                }
                            } else if (req.status==404){
                                console.log("cannot determine rights for playing track")
                            }
                            //continue rendering other tracks
                            renderReadableTracks(renderer, tracks.slice(1));
                        }
                    });
                }
            }
        </script>
    </body>

</html>
