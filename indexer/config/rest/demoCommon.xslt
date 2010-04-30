<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
<!-- This xslt stylesheet is common to 
     demoGfindObjectsToHtml, demoGetIndexInfoToHtml, and demoUpdateIndexToHtml.
-->
	
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	
	<xsl:param name="TIMEUSEDMS" select="''"/>

	<xsl:template match="/resultPage">
		<html>
			<head>
				<title>REST Client Demo of Fedora Generic Search Service (configDemoOnSolr)</title>
				<link rel="stylesheet" type="text/css" href="css/demo.css"/>
				<style type="text/css">
					.highlight {
						background: yellow;
					}
				</style>
				<script language="javascript">
				</script>
			</head>
			<body>
				<div id="header">
					<a href="" id="logo"></a>
					<div id="title">
						<h1>REST Client Demo of Fedora Generic Search Service (configDemoOnSolr)</h1>
					</div>
				</div>
				<table cellspacing="10" cellpadding="10">
					<tr>
					<th><a href="?operation=updateIndex">updateIndex</a></th>
					<th><a href="?operation=gfindObjects">gfindObjects</a></th>
					<th><a href="?operation=browseIndex">browseIndex</a></th>
					<th><a href="?operation=getRepositoryInfo">getRepositoryInfo</a></th>
					<th><a href="?operation=getIndexInfo">getIndexInfo</a></th>
					<td>(<xsl:value-of select="$TIMEUSEDMS"/>)</td>
					</tr>
				</table>
				<p/>
				<!-- 
				<xsl:if test="$ERRORMESSAGE">
					<xsl:call-template name="error"/>
	 			</xsl:if>
	 			 -->
        		<xsl:apply-templates select="error"/>
				<xsl:call-template name="opSpecifics"/>
				<div id="footer">
   					<div id="copyright">
						Copyright &#xA9; 2008 Technical University of Denmark, Fedora Project
					</div>
					<div id="lastModified">
						Last Modified
						<script type="text/javascript">
							//<![CDATA[
							var cvsDate = "$Date: 2006-10-13 15:17:53 +0100 (Fri, 13 Oct 2006) $";
							var parts = cvsDate.split(" ");
							var modifiedDate = parts[1];
							document.write(modifiedDate);
							//]]>
						</script>
						by Gert Schmeltz Pedersen 
					</div>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="error">
		<p>
			<font color="red">
				<xsl:value-of select="$ERRORMESSAGE"/>
			</font>
		</p>			
	</xsl:template>
	
	<xsl:template match="message">
		<p>
			<font color="red">
				<xsl:value-of select="./text()"/>
			</font>
		</p>			
	</xsl:template>
	
</xsl:stylesheet>	





				




