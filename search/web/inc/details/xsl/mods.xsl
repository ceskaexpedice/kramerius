<xsl:stylesheet  version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:mods="http://www.loc.gov/mods/v3"
    exclude-result-prefixes="mods" >
    <xsl:output method="html" indent="yes" encoding="UTF-8"  omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="model" select="model" />
    <xsl:param name="level" select="level" />
<xsl:template match="/">
	<xsl:apply-templates mode="info"/>
</xsl:template>

<xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        <xsl:variable name="uuid" ><xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:variable>
        <div id="details">
	<br>--------------------<xsl:value-of select="$bundle/value[@key=$model]"/>--------------------</br>


<h2><xsl:value-of select="mods:titleInfo[not(@type)]/mods:title"/></h2>
        


<!-- nazev a podnazev  -->
	<li>
	<xsl:for-each select="mods:titleInfo[not(@type)]">   
		<xsl:if test=".[not(@type)]/mods:title/text()">
			<span class="label"><xsl:value-of select="$bundle/value[@key='filter.maintitle']"/>:&#160;</span>
			<span class="value"><xsl:value-of select="./mods:title" /></span>
		</xsl:if>

		<xsl:if test="./mods:subTitle/text()">
			<br/><span class="label"><xsl:value-of select="$bundle/value[@key='mods.subtitle']"/>:&#160;</span>
                	<span class="value"><xsl:value-of select="./mods:subTitle" /></span>
		</xsl:if>
	</xsl:for-each>
	
	<xsl:for-each select="mods:titleInfo[@type='alternative']">
		<xsl:if test=".[@type='alternative']/mods:title/text()">
			<br/><span class="label">Alternativní název:&#160;</span>
                	<span class="value"><xsl:value-of select=".[@type='alternative']/mods:title/text()"/></span>
		</xsl:if>
	</xsl:for-each>
        </li>

<!-- ISSN  -->
        <xsl:if test="./mods:identifier[@type='issn']/text()">
        <li>
            <span class="label">ISSN: </span>
            <span class="value"><xsl:value-of select="./mods:identifier[@type='issn']" /></span>
        </li>
        </xsl:if>

<!--   -->        
        <xsl:if test="$level &gt; 0">
        </xsl:if>

<!-- rocnik -->
        <xsl:if test="mods:part/mods:detail[@type = 'volume']/mods:number">
        <li>
        	<span class="label">
        		<xsl:value-of select="$bundle/value[@key='Datum vydání']"/>&#160;
        	</span>
        	<span class="value">
        		<xsl:value-of select="mods:part/mods:date" />&#160;
        	</span>
        	<span class="label">
        		<xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;
       		</span>
        	<span class="value">
        		<xsl:value-of select="mods:part/mods:detail[@type = 'volume']/mods:number" />
        	</span>
        </li>
        </xsl:if>

<!-- cislo  -->
        <xsl:if test="mods:part[@type = 'PeriodicalIssue']">
        <li>
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>&#160;
        </span>
        <span class="value">
        <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:date" />&#160;
        </span>
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;
        </span>
        <span class="value">
        <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:detail[@type = 'issue']/mods:number" />
        </span>
        </li>
        </xsl:if>

        <xsl:variable name="partType"><xsl:value-of select="mods:part/@type" /></xsl:variable>
<!-- page -->
        <span class="value">
            <xsl:choose>
                <xsl:when test="$partType='Blank'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='Blank']"/>)</xsl:when>
                <xsl:when test="$partType='TitlePage'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='TitlePage']"/>)</xsl:when>
                <xsl:when test="$partType='TableOfContents'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='TableOfContents']"/>)</xsl:when>
                <xsl:when test="$partType='Index'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='TableOfContents']"/>)</xsl:when>
                <xsl:when test="$partType='NormalPage'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /></xsl:when>
            </xsl:choose>
        </span>
<!-- end page -->

<!-- monograph unit -->
        <xsl:if test="mods:part[@type = 'Volume']/mods:detail">
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Volume']"/>
        </span>
        <span class="value">
        <xsl:if test="mods:part[@type = 'Volume']/mods:detail/mods:title != ''" >
            &#160;(<xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:title" />)
        </xsl:if> - <xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:number" />
        </span>
        </xsl:if>
<!-- end monograph unit -->

<!-- internal part -->
            <span class="label">
            <xsl:choose>
                <xsl:when test="$partType='Chapter'">
                    (<xsl:value-of select="$bundle/value[@key='Chapter']"/>) - </xsl:when>
                <xsl:when test="$partType='Table'">
                    (<xsl:value-of select="$bundle/value[@key='Table']"/>) - </xsl:when>
                <xsl:when test="$partType='Introduction'">
                    (<xsl:value-of select="$bundle/value[@key='Introduction']"/>) - </xsl:when>
            </xsl:choose>
            </span>
            <span class="value">
	    <xsl:value-of select="mods:part/mods:extent/mods:list" />
		    </span>
<!-- end internal part -->

<!-- jazyk  -->
	<xsl:if test="mods:language/mods:languageTerm">
	<li>
		<span class="label"><xsl:value-of select="$bundle/value[@key='common.language']"/>:&#160;</span>
		<span class="value"><xsl:value-of select="mods:language/mods:languageTerm" /></span>
	</li>
	</xsl:if>

<!-- autor puvodne

        <xsl:if test="mods:name[@type='personal']/mods:role/mods:roleTerm = 'Author'">
	<h3><xsl:value-of select="$bundle/value[@key='common.author']"/></h3>
	<xsl:for-each select="mods:name[@type='personal']"><li>
		<span class="value">
			<xsl:if test="./mods:role/mods:roleTerm = 'Author'">
			<div>
				<xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
				<xsl:value-of select="./mods:namePart[@type='given']" />
			</div>
			</xsl:if>
		</span>
	</li></xsl:for-each>
	</xsl:if> 

autor konec -->
<!-- neresi se zde hodnota <roleTerm type="code" authority="marcrelator"> -->

<!-- autor  -->
	<xsl:if test="count(mods:name)=1">
	<h3><xsl:value-of select="$bundle/value[@key='common.author']"/></h3>
	</xsl:if>
	<xsl:if test="count(mods:name)>1">
        <h3>Autoři</h3>
        </xsl:if>
		
	<xsl:for-each select="mods:name">
	<xsl:if test="./mods:role/mods:roleTerm[@type='code']='aut' or ./mods:role/mods:roleTerm[@type='code']='cre' or ./mods:role/mods:roleTerm[@type='text']='Author'">
	<li>
	<span class="value">
		<xsl:if test="./mods:namePart[@type='family'] and ./mods:namePart[@type='given']">
			<xsl:value-of select="./mods:namePart[@type='family']"/>,&#160;<xsl:value-of select="./mods:namePart[@type='given']"/>
		</xsl:if>
		<xsl:if test="./mods:namePart[@type='family'] and not(./mods:namePart[@type='given'])">
			<xsl:value-of select="./mods:namePart[@type='family']" />
		</xsl:if>
		<xsl:if test="not(./mods:namePart[@type='family']) and ./mods:namePart[@type='given']">
			<xsl:value-of select="./mods:namePart[@type='given']" />
		</xsl:if>
		<xsl:if test="./mods:namePart[not(@type)]">
			<xsl:value-of select="./mods:namePart[not(@type)]" />
		</xsl:if>
		<xsl:if test="./mods:namePart[@type='date']">,&#160;<xsl:value-of select="./mods:namePart[@type='date']" /></xsl:if>
		<xsl:if test="./mods:role/mods:roleTerm[@type='code']='aut' or ./mods:role/mods:roleTerm[@type='code']='cre' or ./mods:role/mods:roleTerm[@type='text']='Author'">&#160;<i>(autor)</i></xsl:if>
	</span>
        </li>
	</xsl:if>
	</xsl:for-each>
	<xsl:for-each select="mods:name">
	<xsl:if test="not(./mods:role/mods:roleTerm[@type='code']='aut') and not(./mods:role/mods:roleTerm[@type='code']='cre') and not(./mods:role/mods:roleTerm[@type='text']='Author')">
        <li>
        <span class="value">
                <xsl:if test="./mods:namePart[@type='family'] and ./mods:namePart[@type='given']">
                        <xsl:value-of select="./mods:namePart[@type='family']"/>,&#160;<xsl:value-of select="./mods:namePart[@type='given']"/>
                </xsl:if>
                <xsl:if test="./mods:namePart[@type='family'] and not(./mods:namePart[@type='given'])">
                        <xsl:value-of select="./mods:namePart[@type='family']" />
                </xsl:if>
                <xsl:if test="not(./mods:namePart[@type='family']) and ./mods:namePart[@type='given']">
                        <xsl:value-of select="./mods:namePart[@type='given']" />
                </xsl:if>
                <xsl:if test="./mods:namePart[not(@type)]">
                        <xsl:value-of select="./mods:namePart[not(@type)]" />
                </xsl:if>
                <xsl:if test="./mods:namePart[@type='date']">,&#160;<xsl:value-of select="./mods:namePart[@type='date']" /></xsl:if>
                <xsl:if test="./mods:role/mods:roleTerm[@type='code']='aut'">&#160;<i>(autor)</i></xsl:if>
		<xsl:if test="./mods:role/mods:roleTerm[@type='code']='ill'">&#160;<i>(ilustrátor)</i></xsl:if>
		<xsl:if test="./mods:role/mods:roleTerm[@type='code']='trl'">&#160;<i>(překladatel)</i></xsl:if>
		<xsl:if test="./mods:role/mods:roleTerm[@type='code']='edt'">&#160;<i>(editor)</i></xsl:if>
		<xsl:if test="./mods:role/mods:roleTerm[@type='code']='ilu'">&#160;<i>(iluminátor)</i></xsl:if>
		<xsl:if test="./mods:role/mods:roleTerm[@type='code']='aut'">&#160;<i>(autor)</i></xsl:if>
        </span>
        </li>
	</xsl:if>
        </xsl:for-each>
	

<!-- nakladatel -->

	<xsl:if test="./mods:originInfo/mods:publisher or ./mods:originInfo/mods:dateIssued or ./mods:originInfo/mods:place">
		<h3><xsl:value-of select="$bundle/value[@key='Vydavatel']"/></h3>
	</xsl:if>
<!--
        <xsl:if test="count(mods:originInfo)=1">
            <h3><xsl:value-of select="$bundle/value[@key='Vydavatel']"/></h3>
        </xsl:if>

        <xsl:if test="count(mods:originInfo)>1">
            <h3><xsl:value-of select="$bundle/value[@key='Vydavatele']"/></h3>
        </xsl:if>
-->
        <xsl:for-each select="mods:originInfo">
        <li>
            <span class="value"><xsl:if test="./mods:publisher">
            <span class="label"><xsl:value-of select="$bundle/value[@key='Název vydavatele']"/>: </span>            
                 <xsl:value-of select="./mods:publisher" />
            </xsl:if></span>
        </li>
        <li>
            <span class="value"><xsl:if test="./mods:dateIssued">
            <span class="label"><xsl:value-of select="$bundle/value[@key='Datum vydání']"/>: </span>
                  <xsl:value-of select="./mods:dateIssued" />
            </xsl:if></span>
        </li>
        <li>
            <span class="value"><xsl:if test="./mods:place/mods:placeTerm">
            <span class="label"><xsl:value-of select="$bundle/value[@key='Místo vydání']"/>: </span>
                <xsl:value-of select="./mods:place/mods:placeTerm[@type='text']" />
                </xsl:if></span>
        </li>
        <h3></h3>
        </xsl:for-each>
        <xsl:if test="mods:originInfo[@transliteration='printer']">
        <li>
            <span class="label"><xsl:value-of select="$bundle/value[@key='Název tiskaře']"/>: </span>
            <span class="value"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" /></span>
        </li>
        <li>
            <span class="label"><xsl:value-of select="$bundle/value[@key='Místo tisku']"/>: </span>
            <span class="value"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" /></span>
        </li>
        </xsl:if>

<!-- fyzicky popis  -->
        <xsl:if test="mods:physicalDescription/mods:extent">
            <h3><xsl:value-of select="$bundle/value[@key='Fyzický popis']"/></h3>
            <xsl:choose>
            <xsl:when test="contains(mods:physicalDescription/mods:extent, ',')">
                <li>
                    <span class="label"><xsl:value-of select="$bundle/value[@key='Rozměry']"/>: </span>
                    <span class="value"><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" /></span>
                </li>
                <li>
                    <span class="label"><xsl:value-of select="$bundle/value[@key='Rozsah']"/>: </span>
                    <span class="value"><xsl:value-of select="substring-before(mods:physicalDescription/mods:extent, ',')" /></span>
                </li>
            </xsl:when>
            <xsl:otherwise>
                <li>
                    <span class="label"><xsl:value-of select="$bundle/value[@key='Rozsah']"/>: </span>
                    <span class="value"><xsl:value-of select="mods:physicalDescription/mods:extent" /></span>
                </li>
            </xsl:otherwise>
        </xsl:choose>
        </xsl:if>

        <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
        <h3><xsl:value-of select="$bundle/value[@key='Stav z hlediska ochrany fondů']"/></h3>
        <li>
            <span class="label">
                    <xsl:value-of select="$bundle/value[@key='Aktuální stav']"/>:
            </span>
            <span class="value">
                    <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
            </span>
        </li>
        </xsl:if>
        <xsl:if test="mods:location/mods:physicalLocation">
        <li>
            <span class="label"><xsl:value-of select="$bundle/value[@key='Místo uložení']"/>: </span>
            <span class="value"><xsl:value-of select="mods:location/mods:physicalLocation" /></span>
        </li>
        </xsl:if>
        <xsl:if test="mods:location/mods:shelfLocator">
        <li>
            <span class="label"><xsl:value-of select="$bundle/value[@key='common.signature']"/>: </span>
            <span class="value"><xsl:value-of select="mods:location/mods:shelfLocator" /></span>
        </li>
        </xsl:if>

        <xsl:if test="mods:originInfo/mods:frequency">
        <li>
            <span class="label"><xsl:value-of select="$bundle/value[@key='Periodicita']"/>: </span>
            <span class="value"><xsl:value-of select="mods:originInfo/mods:frequency" /></span>
        </li>
        </xsl:if>
        <xsl:if test="mods:part/mods:detail[@type='regularsupplement']">
        <li>
            <span class="label"><xsl:value-of select="$bundle/value[@key='Pravidelná příloha']"/>: </span>
            <span class="value"><xsl:value-of select="mods:part/mods:detail[@type='regularsupplement']" /></span>
        </li>
        </xsl:if>

        <xsl:if test="mods:physicalDescription/mods:note/text()">
        <h3><xsl:value-of select="$bundle/value[@key='Poznámky']"/></h3>
        <li>
            <span class="value"><xsl:value-of select="mods:physicalDescription/mods:note" /></span>
        </li>
        </xsl:if>
        
    </div>
    </xsl:template>
</xsl:stylesheet>
