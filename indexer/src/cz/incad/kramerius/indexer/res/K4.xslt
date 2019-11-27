<?xml version="1.1" encoding="UTF-8"?> 
<!-- $Id: demoFoxmlToLucene.xslt 5734 2006-11-28 11:20:15Z gertsp $ -->
<xsl:stylesheet version="1.1"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
        xmlns:fn="http://www.w3.org/2005/xpath-functions"
    	xmlns:exts="java://cz.incad.kramerius.indexer.FedoraOperations"
        xmlns:java="http://xml.apache.org/xslt/java"
        xmlns:zs="http://www.loc.gov/zing/srw/"
        xmlns:foxml="info:fedora/fedora-system:def/foxml#"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
        xmlns:uvalibdesc="http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd"
        xmlns:uvalibadmin="http://dl.lib.virginia.edu/bin/admin/admin.dtd/"
        xmlns:fedora-model="info:fedora/fedora-system:def/model#" 
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
        xmlns:mods="http://www.loc.gov/mods/v3" 
        xmlns:kramerius="http://www.nsdl.org/ontologies/relationships#" 
        exclude-result-prefixes="exts java fedora-model uvalibadmin fn zs foxml dc oai_dc uvalibdesc rdf mods kramerius"

    >
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" encoding="UTF-8" />

    <xsl:include href="biblio_location.xslt"/>

<!--
	 This xslt stylesheet generates the Solr doc element consisting of field elements
     from a FOXML record. The PID field is mandatory.
-->
    
    <xsl:param name="PAGENUM" select="0"/>
    <xsl:param name="DATUM" select="''"/>
    <xsl:param name="ROK" select="''"/>
    <xsl:param name="DATUM_BEGIN" select="''"/>
    <xsl:param name="DATUM_END" select="''"/>
    <xsl:param name="BROWSEMODELS" select="''"/>
    
    <xsl:param name="RELS_EXT_INDEX" select="''"/>
    <xsl:variable name="generic" select="exts:new()" />
    
    <xsl:variable name="PID" select="/foxml:digitalObject/@PID"/>
    <xsl:variable name="title" 
    select="translate(normalize-space(/foxml:digitalObject/foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:title/text()),'&#xA;','')"/>

    <xsl:variable name="MODEL" 
    select="substring(/foxml:digitalObject/foxml:datastream[@CONTROL_GROUP='X' and @ID='RELS-EXT']/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description/fedora-model:hasModel/@rdf:resource, 19)" />
    
    <xsl:variable name="HANDLE" 
    select="/foxml:digitalObject/foxml:datastream[@CONTROL_GROUP='X' and @ID='RELS-EXT']/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description/kramerius:handle/text()" />
    
    
    <xsl:template match="/">
        <xsl:param name="i" />
        <!-- Indexujeme vsechny activa a ne activ. -->
        <xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
            <xsl:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
                <xsl:apply-templates mode="activeDemoFedoraObject" select="/foxml:digitalObject" >
                    <xsl:with-param name="pageNum">
                          <xsl:value-of select="$PAGENUM"/>
                      </xsl:with-param>
                </xsl:apply-templates>
                <xsl:apply-templates mode="biblioMods" select="/foxml:digitalObject/foxml:datastream[@ID='BIBLIO_MODS']/foxml:datastreamVersion[last()]/foxml:xmlContent/mods:modsCollection/mods:mods" />
                <xsl:call-template name="imgFull"  />
                <xsl:call-template name="browse" />
                <xsl:call-template name="collection">
                    <xsl:with-param name="rels" select="/foxml:digitalObject/foxml:datastream[@CONTROL_GROUP='X' and @ID='RELS-EXT']/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description" />
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
  </xsl:template>
  
    <xsl:template match="/foxml:digitalObject" mode="activeDemoFedoraObject">
        <xsl:param name="pageNum" />
        <xsl:choose>
            <xsl:when test="$pageNum = 0">
                <field name="PID">
                    <xsl:value-of select="$PID"/>
                </field>
                <field name="fedora.model">
                    <xsl:value-of select="$MODEL" />
                </field>
                <xsl:choose>
                    <xsl:when test="/foxml:digitalObject/foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:type">
                        <xsl:for-each select="/foxml:digitalObject/foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:type">
                        <xsl:choose>
                            <xsl:when test="starts-with(./text(), 'model:')">
                                <field name="document_type">
                                    <xsl:value-of select="substring(./text(), 7)"/>
                                </field>
                            </xsl:when>
                            <xsl:otherwise>
                                <field name="document_type">
                                    <xsl:value-of select="./text()"/>
                                </field>
                            </xsl:otherwise>
                        </xsl:choose>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <field name="document_type">
                            <xsl:value-of select="$MODEL" />
                        </field>
                    </xsl:otherwise>
                </xsl:choose>
                <field name="dc.title" boost="2.0"><xsl:value-of select="$title"/></field>
                <xsl:variable name="modsTitle"
                    select="/foxml:digitalObject/foxml:datastream[@ID='BIBLIO_MODS']/foxml:datastreamVersion[last()]/foxml:xmlContent/mods:modsCollection/mods:mods/mods:titleInfo[not(@*)]/mods:title" />
                <field name="title_sort" ><xsl:value-of select="exts:prepareCzech($generic, $modsTitle)"/></field>
        
                <xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:creator">
                    <field name="dc.creator" boost="1.5">
                        <xsl:value-of select="text()"/>
                    </field>
                    <field name="browse_autor" >
                        <xsl:value-of select="exts:prepareCzech($generic, text())"/>##<xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>
        
                <xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:contributor">
                    <field name="dc.creator" boost="1.5">
                        <xsl:value-of select="text()"/>
                    </field>
                    <field name="browse_autor" >
                        <xsl:value-of select="exts:prepareCzech($generic, text())"/>##<xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>
        
                <xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:identifier">
                    <field name="dc.identifier"><xsl:value-of select="text()"/></field>
                </xsl:for-each>
                
            </xsl:when>
            <xsl:otherwise>
                <field name="PID">
                    <xsl:value-of select="$PID"/>/@<xsl:value-of select="$pageNum"/>
                </field>
                <field name="fedora.model">page</field>
                <field name="document_type">page</field>
                <field name="virtual">true</field>
                <field name="dc.title" boost="2.0"><xsl:value-of select="$pageNum"/></field>
             </xsl:otherwise>   
        </xsl:choose>
        
        <field name="status" >
            <xsl:value-of select="foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state']/@VALUE"/>
        </field>
        <field name="handle">
            <xsl:value-of select="$HANDLE"/>
        </field>
        
        <field name="created_date">
            <xsl:value-of select="foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#createdDate']/@VALUE"/>
        </field>
        <field name="modified_date"><xsl:value-of select="foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/view#lastModifiedDate']/@VALUE"/></field>
        <field name="dostupnost">
            <xsl:value-of select="substring(/foxml:digitalObject/foxml:datastream[@CONTROL_GROUP='X' and @ID='RELS-EXT']/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description/kramerius:policy, 8)"/>
        </field>
        
        <!-- Check params -->
        <xsl:if test="$DATUM and not($DATUM = '')" >
            <field name="datum">
                <xsl:value-of select="$DATUM" />
            </field>
        </xsl:if>
        <xsl:if test="$ROK and $ROK != ''" >
            <field name="rok">
                <xsl:value-of select="$ROK" />
            </field>
        </xsl:if>
        <xsl:if test="$DATUM_BEGIN and not($DATUM_BEGIN = '')" >
            <field name="datum_begin">
                <xsl:value-of select="$DATUM_BEGIN" />
            </field>
        </xsl:if>
        <xsl:if test="$DATUM_END and not($DATUM_END = '')" >
            <field name="datum_end">
                <xsl:value-of select="$DATUM_END" />
            </field>
        </xsl:if>
        
        
        <!-- a managed datastream is fetched, if its mimetype 
             can be handled, the text becomes the value of the field.
             Excluding ALTO -->
        <xsl:for-each select="foxml:datastream[@STATE='A']">
            
            <xsl:if test="(foxml:datastreamVersion/@MIMETYPE= 'text/plain' or
            foxml:datastreamVersion/@MIMETYPE='text/xml' or
            foxml:datastreamVersion/@MIMETYPE='text/html' or
            foxml:datastreamVersion/@MIMETYPE='application/ps' or
            foxml:datastreamVersion/@MIMETYPE='application/msword') and
            (@ID='TEXT_OCR' or @ID='IMG_FULL')">
            
                <field name="text_ocr">
                    <xsl:value-of select="exts:getDatastreamText($generic, $PID, @ID, $pageNum)"/>
                </field>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="imgFull">
        <xsl:if test="/foxml:digitalObject/foxml:datastream[@ID='IMG_FULL']/foxml:datastreamVersion[last()]">
            <field name="img_full_mime"><xsl:value-of select="/foxml:digitalObject/foxml:datastream[@ID='IMG_FULL']/foxml:datastreamVersion[last()]/@MIMETYPE"/></field>
            <field name="viewable">true</field>
        </xsl:if>
    </xsl:template>

    
    <xsl:template match="/foxml:digitalObject/foxml:datastream[@ID='BIBLIO_MODS']/foxml:datastreamVersion[last()]/foxml:xmlContent/mods:modsCollection/mods:mods" mode="biblioMods">
        <xsl:for-each select="mods:language[not(@objectPart) or @objectPart != 'translation']/mods:languageTerm/text()">
        <field name="language">
            <xsl:value-of select="." />
        </field>
        </xsl:for-each>

        <!-- <mods:location  -->
        <xsl:apply-templates select="mods:location" />

        
        <xsl:if test="$PAGENUM=0">
            <xsl:for-each select="mods:subject/mods:topic/text()">
                <field name="keywords" >
                    <xsl:value-of select="."/>
                </field>
            </xsl:for-each>
            <xsl:for-each select="mods:subject/mods:geographic/text()">
                <field name="geographic_names" >
                    <xsl:value-of select="."/>
                </field>
            </xsl:for-each>
            <xsl:for-each select="mods:identifier">
                <field name="dc.identifier"><xsl:value-of select="."/></field>
            </xsl:for-each>
            <field name="issn">
                <xsl:value-of select="mods:identifier[@type='isbn']/text()"/>
                <xsl:value-of select="mods:identifier[@type='issn']/text()"/>
            </field>
            <field name="mdt">
                <xsl:value-of select="mods:classification[@authority='udc']/text()"/>
            </field>
            <field name="ddt">
                <xsl:value-of select="mods:classification[@authority='ddc']/text()"/>
            </field>
            <xsl:if test="$MODEL = 'monographunit'">
                <field name="details">
                    <xsl:choose>
                        <xsl:when test="mods:titleInfo/mods:partNumber">
                            <xsl:value-of select="mods:titleInfo/mods:partNumber" /><xsl:value-of select="'##'" />
                            <xsl:value-of select="mods:titleInfo/mods:partName" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="mods:part/mods:detail/mods:title" /><xsl:value-of select="'##'" />
                            <xsl:value-of select="mods:part/mods:detail/mods:number" />
                        </xsl:otherwise>
                    </xsl:choose>
                </field>
            </xsl:if>
            <xsl:if test="$MODEL = 'page'">
                <field name="details">
                    <xsl:if test="mods:part">
                        <xsl:for-each select="mods:part/mods:detail[@type = 'pageNumber']/mods:number">
                        <xsl:value-of select="." />&#160;
                        </xsl:for-each><xsl:value-of select="'##'" />
                        <xsl:value-of select="mods:part/@type" />
                    </xsl:if>
                </field>
            </xsl:if>
            <xsl:if test="$MODEL = 'periodicalitem'">
                <field name="details">
                    <xsl:value-of select="mods:titleInfo/mods:title" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="/mods:titleInfo/mods:subTitle" /><xsl:value-of select="'##'" />
                    <!-- Alberto's change -->
 		    <xsl:choose>
                        <xsl:when test="mods:part/mods:date">
                            <xsl:value-of select="mods:part/mods:date" /><xsl:value-of select="'##'" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="mods:originInfo/mods:dateIssued" /><xsl:value-of select="'##'" />
                        </xsl:otherwise>
                     </xsl:choose>

                    <xsl:choose>
                        <xsl:when test="mods:part/mods:detail[@type = 'issue']/mods:number">
                            <xsl:value-of select="mods:part/mods:detail[@type = 'issue']/mods:number" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="mods:titleInfo/mods:partNumber" />
                        </xsl:otherwise>
                    </xsl:choose>
			

                </field>
            </xsl:if>
            <xsl:if test="$MODEL = 'periodicalvolume'">
                <field name="details">


                    <xsl:variable name="volumeName"><xsl:choose>
                        <xsl:when test="mods:titleInfo/mods:partName">
                            <xsl:value-of select="mods:titleInfo/mods:partName" />
                        </xsl:when>
                    </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="volumeNumber"><xsl:choose>
                        <xsl:when test="mods:titleInfo/mods:partNumber">
                            <xsl:value-of select="mods:titleInfo/mods:partNumber" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="mods:part/mods:detail[@type = 'volume']/mods:number" />
                        </xsl:otherwise>
                     </xsl:choose></xsl:variable>
                    <xsl:choose>
                        <xsl:when test="mods:part/mods:date">
                            <xsl:value-of select="mods:part/mods:date" /><xsl:value-of select="'##'" />
                            <xsl:value-of select="$volumeNumber" />
                        </xsl:when>
                        <xsl:when test="mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()">
                            <xsl:value-of select="mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()" /><xsl:value-of select="'##'" />
                            <xsl:value-of select="$volumeNumber" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="mods:originInfo/mods:dateIssued" /><xsl:value-of select="'##'" />
                            <xsl:value-of select="$volumeNumber" />
                            <xsl:if test="$volumeName">
                                <xsl:value-of select="'##'" />
                                <xsl:value-of select="$volumeName" />
                            </xsl:if>

                        </xsl:otherwise>
                     </xsl:choose>
                </field>
            </xsl:if>

            <xsl:if test="$MODEL = 'internalpart'">
                <field name="details">
                    <xsl:value-of select="mods:part/@type" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="mods:titleInfo/mods:title" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="mods:titleInfo/mods:subTitle" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="mods:part/mods:extent/mods:list" />
                </field>
            </xsl:if>
        </xsl:if>
                
    </xsl:template>
    <xsl:template name="parentTemplate">
        <xsl:param name="parent" />
        <xsl:if test="$parent and not($parent = '')" >
            <xsl:choose>
                <xsl:when test="contains($parent, ';')">
                    <field name="parent_pid">
                        <xsl:value-of select="substring-before($parent, ';')" />
                    </field>
                    <xsl:call-template name="parentTemplate">
                    <xsl:with-param name="parent"><xsl:value-of select="substring-after($parent, ';')" /></xsl:with-param>
                   </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <field name="parent_pid">
                        <xsl:value-of select="$parent" />
                    </field>
                </xsl:otherwise>    
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    <xsl:template name="browse" >
        <!--
        <xsl:choose>
            <xsl:when test="$MODEL = 'internalpart'">
                <xsl:variable name="bt"><xsl:value-of select="/foxml:digitalObject/foxml:datastream[@ID='BIBLIO_MODS']/foxml:datastreamVersion[last()]/foxml:xmlContent/mods:modsCollection/mods:mods/mods:titleInfo/mods:title" /></xsl:variable>
                <field name="browse_title" >
                    <xsl:value-of select="exts:prepareCzech($generic, $bt)"/>##<xsl:value-of select="$bt"/>
                </field>
            </xsl:when>
            <xsl:when test="$MODEL = 'periodicalvolume'">
                
            </xsl:when>
            <xsl:when test="not($MODEL = 'page') and not(normalize-space($title)='')">
                <field name="browse_title" >
                    <xsl:value-of select="exts:prepareCzech($generic, $title)"/>##<xsl:value-of select="$title"/>
                </field>
            </xsl:when>
        </xsl:choose>
        -->
        <!--
        <xsl:if test=($MODEL = 'monograph' or $MODEL = 'periodical') and ($PAGENUM = 0)">
        -->
        <xsl:if test="contains($BROWSEMODELS, $MODEL) and ($PAGENUM = 0)">
            <field name="browse_title" >
                <xsl:value-of select="exts:prepareCzech($generic, $title)"/>##<xsl:value-of select="$title"/>
            </field>
        </xsl:if>
    </xsl:template>
    <xsl:template name="collection" >
        <xsl:param name="rels" />
        <xsl:for-each  select="$rels/rdf:isMemberOfCollection">
            <field name="collection" >
                <xsl:value-of select="substring-after(./@rdf:resource, 'info:fedora/')"/>
            </field>
        </xsl:for-each>    
    </xsl:template>
    
    
    
</xsl:stylesheet>
