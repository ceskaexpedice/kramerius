<processes>
    <process>
        <!-- identifikator  -->
        <id>mock</id>
        <!-- jenom popis -->
        <description>mock</description>
        <!-- spoustena trida -->
        <mainClass>cz.incad.kramerius.processes.mock.MockLPProcess</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <parameters>one two</parameters>
    </process>

    <process>
        <id>static_export_CD</id>
        <description>Statický export do PDF na CD</description>
        <mainClass>cz.incad.kramerius.lp.PDFExport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>pdfs_out CD</parameters>
        <!--
        <parameters>
            <parameter>pdfs_out</parameter>
            <parameter>CD</parameter>
        </parameters>
        -->
    </process>

    <process>
        <!-- identifikator  -->
        <id>static_export_DVD</id>
        <!-- jenom popis -->
        <description>Statický export do PDF na DVD</description>
        <!-- spoustena trida -->
        <mainClass>cz.incad.kramerius.lp.PDFExport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>pdfs_out DVD</parameters>
        <!--
        <parameters>
            <parameter>pdfs_out</parameter>
            <parameter>DVD</parameter>
        </parameters>
        -->
    </process>

    <process>
        <id>reindex</id>
        <description>Reindexace</description>
        <mainClass>cz.incad.kramerius.indexer.Main</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <!--
        <parameters>$default_lp_work_dir$/reindexer/config.properties $default_lp_work_dir$/reindexer/log4j.properties</parameters>
        <parameters>
            <parameter>$default_lp_work_dir$/reindexer/config.properties</parameter>
            <parameter>$default_lp_work_dir$/reindexer/log4j.properties</parameter>
        </parameters>
        -->
        <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>

    </process>

    <process>
        <id>virtualcollections</id>
        <description>Virtual Collections manager</description>
        <mainClass>cz.incad.kramerius.virtualcollections.VirtualCollectionsManager</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>

    </process>

    <process>
        <id>replikator_monographs</id>
        <description>Replikator (monografie)</description>
        <mainClass>org.kramerius.Replicate</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <!--
        <processOutputURL>processes/imported/_imported.jsp</processOutputURL>
        -->
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
        <parameters>monographs</parameters>
        <!--
        <parameters>
            <parameter>monographs</parameter>
        </parameters>
        -->
        <actions>
            <action>
                <name>imported</name>
                <resourceBundleKey>administrator.processes.imported</resourceBundleKey>
                <actionURL>processes/imported/_imported.jsp</actionURL>
            </action>
        </actions>

    </process>

    <process>
        <id>replikator_periodicals</id>
        <description>Replikator (periodika)</description>
        <mainClass>org.kramerius.Replicate</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <!--
        <processOutputURL>processes/imported/_imported.jsp</processOutputURL>
        -->
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
        <parameters>periodicals</parameters>
        <!--
        <parameters>
            <parameter>periodicals</parameter>
        </parameters>
        -->
        <actions>
            <action>
                <name>imported</name>
                <resourceBundleKey>administrator.processes.imported</resourceBundleKey>
                <actionURL>processes/imported/_imported.jsp</actionURL>
            </action>
        </actions>

    </process>

    <processes>
        <id>enumerator</id>
        <description>Enumerator</description>
        <mainClass>org.kramerius.Enumerator</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
    </processes>

    <process>
        <id>replicationrights</id>
        <description>Replication rights</description>
        <mainClass>org.kramerius.ReplicationRights</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
    </process>

    <process>
        <id>setpublic</id>
        <description>Set public</description>
        <mainClass>cz.incad.kramerius.service.impl.PolicyServiceImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>public</parameters>
        <!--
        <parameters>
            <parameter>public</parameter>
        </parameters>
        -->
    </process>

    <process>
        <id>setprivate</id>
        <description>Set private</description>
        <mainClass>cz.incad.kramerius.service.impl.PolicyServiceImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>private</parameters>
        <!--
        <parameters>
            <parameter>private</parameter>
        </parameters>
        -->
    </process>


    <process>
        <id>delete</id>
        <description>Delete</description>
        <mainClass>cz.incad.kramerius.service.impl.DeleteServiceImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
    </process>

    <process>
        <id>export</id>
        <description>Export</description>
        <mainClass>cz.incad.kramerius.service.impl.ExportServiceImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
    </process>


    <process>
        <id>convert</id>
        <description>Convert and import</description>
        <mainClass>org.kramerius.Convert</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
    </process>

    <process>
        <id>import</id>
        <description>Import FOXML</description>
        <mainClass>org.kramerius.Import</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
    </process>
    <!-- Proces  generovani deepZoomDlazdic -->
    <process>
        <id>generateDeepZoomTiles</id>
        <description>Generate cache for  Deep Zoom </description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateDeepZoomCache</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx3072m -Xms512m -Dcz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag=true -Dcz.incad.kramerius.imaging.lp.GenerateThumbnail=true</javaProcessParameters>
    </process>
    <!-- Proces generovani priznaku do RELS-EXT -->
    <process>
        <id>generateDeepZoomFlag</id>
        <description>Generate deep zoom flag to RELS-EXT</description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
    </process>
    <!-- smazani cache pro dlazdice -->
    <process>
        <id>deleteGeneratedDeepZoomTiles</id>
        <description>Delete generated deepZoomCache </description>
        <mainClass>cz.incad.kramerius.imaging.lp.DeleteGeneratedDeepZoomCache</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms512m -Dcz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag=true</javaProcessParameters>
    </process>

    <process>
        <id>generateFullThumb</id>
        <description>Generate full thumbs </description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateThumbnail</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
    </process>


    <process>
        <id>aggregate</id>
        <description>Process aggregator</description>
        <mainClass>cz.incad.kramerius.processes.impl.ProcessAggregator</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
    </process>

</processes>
