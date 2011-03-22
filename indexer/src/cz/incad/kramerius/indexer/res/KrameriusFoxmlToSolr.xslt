<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: demoFoxmlToLucene.xslt 5734 2006-11-28 11:20:15Z gertsp $ -->
<xsl:stylesheet version="1.0"
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
        exclude-result-prefixes="exts java"
    >
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />

<!--
	 This xslt stylesheet generates the Solr doc element consisting of field elements
     from a FOXML record. The PID field is mandatory.
     Options for tailoring:
       - generation of fields from other XML metadata streams than DC
       - generation of fields from other datastream types than XML
         - from datastream by ID, text fetched, if mimetype can be handled
             currently the mimetypes text/plain, text/xml, text/html, application/pdf can be handled.
-->
    <xsl:param name="DOCCOUNT" select="0"/>
    
    <xsl:param name="PAGESCOUNT" select="1"/>
    <xsl:param name="DATUM" select="''"/>
    <xsl:param name="ROK" select="''"/>
    <xsl:param name="DATUM_BEGIN" select="''"/>
    <xsl:param name="DATUM_END" select="''"/>
    <xsl:param name="PARENT_TITLE" select="''"/>
    <xsl:param name="PARENT_PID" select="''"/>
    <xsl:param name="PARENT_MODEL" select="''"/>
    <xsl:param name="PATH" select="''"/>
    <xsl:param name="PID_PATH" select="''"/>
    <xsl:param name="ROOT_TITLE" select="''"/>
    <xsl:param name="ROOT_MODEL" select="''"/>
    <xsl:param name="ROOT_PID" select="''"/>
    <xsl:param name="LANGUAGE" select="''"/>
    <xsl:param name="LEVEL" select="''"/>
    <xsl:param name="RELS_EXT_INDEX" select="1"/>
    <xsl:param name="PARENTS" select="''"/>
    
    <xsl:variable name="generic" select="exts:new()" />

    <xsl:variable name="PID" select="/foxml:digitalObject/@PID"/>
    <xsl:variable name="title" select="/foxml:digitalObject/foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:title/text()"/>
    
    <xsl:variable name="MODEL" 
    select="substring(/foxml:digitalObject/foxml:datastream[@CONTROL_GROUP='X' and @ID='RELS-EXT']/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description/fedora-model:hasModel/@rdf:resource, 19)" />
    <xsl:variable name="docBoost" select="1.4*2.5"/> <!-- or any other calculation, default boost is 1.0 -->
    <xsl:variable name="HANDLE" 
    select="/foxml:digitalObject/foxml:datastream[@CONTROL_GROUP='X' and @ID='RELS-EXT']/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description/kramerius:handle/text()" />
    
    
    <xsl:template match="/">
        
      <add>
    <xsl:call-template name="for.loop">
     <xsl:with-param name="i">0</xsl:with-param>
    </xsl:call-template>
        </add>
    </xsl:template>
    
    <xsl:template name="for.loop">
        <xsl:param name="i" />
       <xsl:if test="$i &lt;= $DOCCOUNT">

                <doc>
                    <xsl:attribute name="boost">
                        <xsl:value-of select="$docBoost"/>
                    </xsl:attribute>

                    <!-- Indexujeme vsechny activa a ne activ. -->
                    <xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
                        <xsl:if test="not(foxml:digitalObject/foxml:datastream[@ID='METHODMAP'] or foxml:digitalObject/foxml:datastream[@ID='DS-COMPOSITE-MODEL'])">
                            <xsl:apply-templates mode="activeDemoFedoraObject" select="/foxml:digitalObject" >
                                <xsl:with-param name="pageNum">
                                      <xsl:value-of select="$i"/>
                                  </xsl:with-param>
                            </xsl:apply-templates>
                            <xsl:apply-templates mode="biblioMods" select="/foxml:digitalObject/foxml:datastream[@ID='BIBLIO_MODS']/foxml:datastreamVersion[last()]/foxml:xmlContent/mods:modsCollection/mods:mods" />
                            <xsl:apply-templates mode="imgFull" select="/foxml:digitalObject/foxml:datastream[@ID='IMG_FULL']/foxml:datastreamVersion[last()]" />
                        </xsl:if>
                    </xsl:if>

                </doc>

          <xsl:call-template name="for.loop">
              <xsl:with-param name="i">
                  <xsl:value-of select="$i + 1"/>
              </xsl:with-param>
          </xsl:call-template>
       </xsl:if>
  </xsl:template>
  
    <xsl:template match="/foxml:digitalObject" mode="activeDemoFedoraObject">
        <xsl:param name="pageNum" />
        <xsl:choose>
            <xsl:when test="$pageNum = 0">
                <field name="PID" boost="2.5">
                    <xsl:value-of select="substring($PID, 6)"/>
                </field>
                <field name="fedora.model">
                    <xsl:value-of select="$MODEL"/>
                </field>

                <xsl:for-each select="/foxml:digitalObject/foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:type">
                <field name="document_type">
                    <xsl:value-of select="substring(./text(), 7)"/>
                </field>
                </xsl:for-each>
                <field name="dc.title"><xsl:value-of select="normalize-space($title)"/></field>
                
                <xsl:if test="$RELS_EXT_INDEX and not($RELS_EXT_INDEX = '')" >
                    <field name="rels_ext_index">
                        <xsl:value-of select="$RELS_EXT_INDEX" />
                    </field>
                </xsl:if>
                
                <xsl:if test="not($LEVEL = '')" >
                    <field name="level">
                        <xsl:value-of select="$LEVEL" />
                    </field>
                </xsl:if>
                
                <xsl:if test="$PATH and not($PATH = '')" >
                    <field name="path">
                        <xsl:value-of select="$PATH" />
                    </field>
                </xsl:if>
                <xsl:if test="$PID_PATH and not($PID_PATH = '')" >
                    <field name="pid_path"><xsl:value-of select="$PID_PATH" /></field>
                </xsl:if>
                <xsl:if test="$PAGESCOUNT and not($PAGESCOUNT = '')" >
                    <field name="pages_count">
                        <xsl:value-of select="$PAGESCOUNT" />
                    </field>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <field name="PID" boost="2.5">
                    <xsl:value-of select="substring($PID, 6)"/>/@<xsl:value-of select="$pageNum"/>
                </field>
                <field name="fedora.model">page</field>
                <field name="document_type">page</field>
                <field name="dc.title"><xsl:value-of select="$pageNum"/></field>
                <xsl:if test="not($LEVEL = '')" >
                    <field name="level">
                        <xsl:value-of select="$LEVEL + 1" />
                    </field>
                </xsl:if>
                
                <xsl:if test="$PATH and not($PATH = '')" >
                    <field name="path"><xsl:value-of select="$PATH" />/page</field>
                </xsl:if>
                <xsl:if test="$PID_PATH and not($PID_PATH = '')" >
                    <field name="pid_path">
                        <xsl:value-of select="$PID_PATH" />/@<xsl:value-of select="$pageNum"/>
                    </field>
                </xsl:if>
                <field name="pages_count">1</field>
                <!--
                <xsl:if test="$PAGESCOUNT and not($PAGESCOUNT = '')" >
                    <field name="pages_count">
                        <xsl:value-of select="$PAGESCOUNT" />
                    </field>
                </xsl:if>
                -->
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
        <field name="dostupnost">
            <xsl:value-of select="substring(/foxml:digitalObject/foxml:datastream[@CONTROL_GROUP='X' and @ID='RELS-EXT']/foxml:datastreamVersion[last()]/foxml:xmlContent/rdf:RDF/rdf:Description/kramerius:policy, 8)"/>
        </field>
        
        <xsl:for-each select="foxml:datastream/foxml:datastreamVersion[last()]/foxml:xmlContent/oai_dc:dc/dc:creator">
            <field name="dc.creator" >
                <xsl:value-of select="text()"/>
            </field>
        </xsl:for-each>
        
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
        <xsl:if test="$PARENT_TITLE and not($PARENT_TITLE = '')" >
            <field name="parent_title">
                <xsl:value-of select="$PARENT_TITLE" />
            </field>
        </xsl:if>
        <xsl:if test="$MODEL = 'page'">
        <xsl:call-template name="parentTemplate">
            <!--
            <xsl:with-param name="parent"><xsl:value-of select="exts:getParents($generic, $PID)"/></xsl:with-param>
            -->
            <xsl:with-param name="parent"><xsl:value-of select="$PARENTS"/></xsl:with-param>
        </xsl:call-template>
        </xsl:if>
        <xsl:if test="$MODEL != 'page'">
            <xsl:choose>
            <xsl:when test="$pageNum != 0">
                <field name="parent_pid">
                    <xsl:value-of select="substring($PID, 6)"/>
                </field>
            </xsl:when>
            <xsl:when test="$PARENT_PID and not($PARENT_PID = '')" >
                <field name="parent_pid">
                    <xsl:value-of select="$PARENT_PID" />
                </field>
            </xsl:when>
            </xsl:choose>
        </xsl:if>
        
        <xsl:if test="$PARENT_MODEL and not($PARENT_MODEL = '')" >
            <field name="parent_model">
                <xsl:value-of select="$PARENT_MODEL" />
            </field>
        </xsl:if>
        <xsl:if test="not($ROOT_TITLE = '')" >
            <field name="root_title">
                <xsl:value-of select="$ROOT_TITLE" />
            </field>
        </xsl:if>
        <xsl:if test="not($ROOT_MODEL = '')" >
            <field name="root_model">
                <xsl:value-of select="$ROOT_MODEL" />
            </field>
        </xsl:if>
        <xsl:if test="not($ROOT_PID = '')" >
            <field name="root_pid">
                <xsl:value-of select="$ROOT_PID" />
            </field>
        </xsl:if>
        
        <xsl:if test="not($LANGUAGE = '')" >
            <field name="language">
                <xsl:value-of select="$LANGUAGE" />
            </field>
        </xsl:if>
        
        <!-- a managed datastream is fetched, if its mimetype 
             can be handled, the text becomes the value of the field. -->        
        <xsl:for-each select="foxml:datastream[@CONTROL_GROUP='M']">
            
            <xsl:if test="foxml:datastreamVersion/@MIMETYPE= 'text/plain' or 
            foxml:datastreamVersion/@MIMETYPE='text/xml' or
            foxml:datastreamVersion/@MIMETYPE='text/html' or
            foxml:datastreamVersion/@MIMETYPE='application/pdf' or
            foxml:datastreamVersion/@MIMETYPE='application/ps' or
            foxml:datastreamVersion/@MIMETYPE='application/msword'">
            
                <field name="text">
                    <xsl:value-of select="exts:getDatastreamText($generic, $PID, @ID, $pageNum)"/>
                </field>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="/foxml:digitalObject/foxml:datastream[@ID='IMG_FULL']/foxml:datastreamVersion[last()]" mode="imgFull">
        <field name="page_format"><xsl:value-of select="@MIMETYPE"/></field>
    </xsl:template>
    
    <xsl:template match="/foxml:digitalObject/foxml:datastream[@ID='BIBLIO_MODS']/foxml:datastreamVersion[last()]/foxml:xmlContent/mods:modsCollection/mods:mods" mode="biblioMods">
        <field name="issn">
            <xsl:value-of select="mods:identifier[@type='isbn']/text()"/>
            <xsl:value-of select="mods:identifier[@type='issn']/text()"/>
        </field>
        <field name="mdt">
            <xsl:value-of select="mods:classification[@authority='mdt']/text()"/>
        </field>
        <field name="ddt">
            <xsl:value-of select="mods:classification[@authority='ddt']/text()"/>
        </field>
        
        
        <xsl:if test="$MODEL = 'monographunit'">
            <field name="details">
                <xsl:value-of select="mods:part/mods:detail/mods:title" /><xsl:value-of select="'##'" />
               <xsl:value-of select="mods:part/mods:detail/mods:number" />
            </field>
        </xsl:if>
        <xsl:if test="$MODEL = 'page'">
            <field name="details">
                <xsl:if test="mods:part">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="mods:part/@type" />
                </xsl:if>
            </field>
        </xsl:if>
        <xsl:if test="$MODEL = 'periodicalitem'">
            <field name="details">
                <xsl:value-of select="mods:titleInfo/mods:title" /><xsl:value-of select="'##'" />
                <xsl:value-of select="/mods:titleInfo/mods:subTitle" /><xsl:value-of select="'##'" />
                <xsl:value-of select="mods:part/mods:date" /><xsl:value-of select="'##'" />
                <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:detail/mods:number" />
            </field>
        </xsl:if>
        <xsl:if test="$MODEL = 'periodicalvolume'">
            <field name="details">
                <xsl:value-of select="mods:part/mods:date" /><xsl:value-of select="'##'" />
                <xsl:value-of select="mods:part/mods:detail[@type = 'volume']/mods:number" />
            </field>
        </xsl:if>
        
        <xsl:if test="$MODEL = 'internalpart'">
            <field name="details">
                <xsl:value-of select="mods:part/@type" /><xsl:value-of select="'##'" />
                <xsl:value-of select="mods:titleInfo/mods:title" /><xsl:value-of select="'##'" />
                <xsl:value-of select="/mods:titleInfo/mods:subTitle" /><xsl:value-of select="'##'" />
                <xsl:value-of select="mods:part/mods:extent/mods:list" />
            </field>
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
</xsl:stylesheet>	
