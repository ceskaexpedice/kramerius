<!DOCTYPE html>
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
                viewer.openDzi("deepZoom/4a7c2e50-af36-11dd-9643-000d606f5dc6/");
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