<!DOCTYPE html>
<%@page import="java.net.URLEncoder"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<html>
    <head>
        
        <script type="text/javascript" 
              src="http://seadragon.com/ajax/0.8/seadragon-min.js">
        </script>
        <script type="text/javascript">
            var viewer = null;
            
            function init() {
                viewer = new Seadragon.Viewer("container");
                //viewer.openDzi("deepZoom/4a7c2e50-af36-11dd-9643-000d606f5dc6/");
                //viewer.openDzi("http://192.168.1.5/fcgi-bin/iipsrv.fcgi?DeepZoom=/var/www/test.jp2.dzi");
                //viewer.openDzi("copy?DeepZoom=/var/www/test.2.jp2.dzi");
                //viewer.openDzi("copy?DeepZoom=/mnt/datastreams/2010/0914/14/08/uuid_ffffeb80-b03b-11dd-a0f6-000d606f5dc6+IMG_FULL+IMG_FULL.2.jp2.dzi");
                <%
                	String encodedUUID = URLEncoder.encode("uuid_ffffeb80-b03b-11dd-a0f6-000d606f5dc6+IMG_FULL+IMG_FULL.2.jp2.dzi");
					System.out.println(encodedUUID);
                %>
                viewer.openDzi("copy?DeepZoom=/mnt/datastreams/2010/0914/14/08/"+"<%=encodedUUID%>");
            }
            Seadragon.Utils.addEvent(window, "load", init);
        </script>
        
        <style type="text/css">
            #container
            {
                width: 500px;
                height: 400px;
                background-color: black;
                border: 1px solid black;
                color: white;   /* for error messages, etc. */
            }
        </style>
        
    </head>
    <body>
    <h1><%=KConfiguration.getInstance().getImagingStorage()%></h1>
        <div id="container"></div>
    </body>
</html>            