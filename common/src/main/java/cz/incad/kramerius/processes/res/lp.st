
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

        <checkErrorStream>false</checkErrorStream>

    </process>

    <process>
        <!-- wiz...  -->
        <id>wmock</id>
        <!-- jenom popis -->
        <description>Mocked process</description>
        <!-- spoustena trida -->
        <mainClass>cz.incad.kramerius.processes.mock.wizard.WizardMockLPProcess</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <parameters>/home/pavels/tmp two</parameters>

        <templates>
            <input class="cz.incad.kramerius.processes.mock.wizard.FolderTemplates"></input>
            <output class="cz.incad.kramerius.processes.mock.logs.LogsOutputTemplate"></output>
        </templates>
        
        <securedaction>mock</securedaction>
    </process>

    <process>
        <id>defaultmock</id>
        <description>Mocked process</description>
        <!-- spoustena trida -->
        <mainClass>cz.incad.kramerius.processes.mock.wizard.DefaultWizardMockLPProcess</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <parameters>lp-name lp-value</parameters>

        <templates>
            <input class="cz.incad.kramerius.processes.def.DefaultTemplate"></input>
            <output class="cz.incad.kramerius.processes.mock.logs.LogsOutputTemplate"></output>
        </templates>
        
    </process>


    <process>
        <id>parametrized_static_export</id>
        <description>Parametrized static export</description>
        <mainClass>cz.incad.kramerius.lp.ParametrizedPDFExport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <templates>
            <input class="cz.incad.kramerius.lp.ParametrizedPDFExportInputTemplate"></input>
        </templates>
        <securedaction>export</securedaction>
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
        <securedaction>export</securedaction>
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
        <securedaction>export</securedaction>
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
        <securedaction>virtualcollection_manage</securedaction>
    </process>

    <process>
        <id>k3_replication</id>
        <description>Parametrizovany replikator</description>
        <mainClass>org.kramerius.k3replications.ParametrizedReplication</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
        <parameters></parameters>
        <templates>
            <input class="org.kramerius.k3replications.input.InputTemplate"></input>
        </templates>
        <securedaction>replikator_k3</securedaction>
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
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
        <parameters>monographs</parameters>
        <!--
        <parameters>
            <parameter>monographs</parameter>
        </parameters>
        <actions>
            <action>
                <name>imported</name>
                <resourceBundleKey>administrator.processes.imported</resourceBundleKey>
                <actionURL>processes/imported/_imported.jsp</actionURL>
            </action>
        </actions>
        -->

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
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
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
        <id>sort</id>
        <description>Sort relations</description>
        <mainClass>cz.incad.kramerius.service.impl.SortingServiceImpl</mainClass>
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
        <id>parametrizedconvert</id>
        <description>Convert and import</description>
        <mainClass>org.kramerius.convert.ParametrizedConvert</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
        
        <templates>
            <input class="org.kramerius.convert.input.ParametrizedConvertInputTemplate"></input>
        </templates>
        
        <securedaction>convert</securedaction>
        
    </process>

    <process>
        <id>ndkmets</id>
        <description>Import NDK METs</description>
        <mainClass>org.kramerius.importmets.parametrized.ParametrizedMetsNKDImport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
        
        <templates>
            <input class="org.kramerius.importmets.parametrized.input.MetsImportInputTemplate"></input>
        </templates>

        <securedaction>ndk_mets_import</securedaction>
        
    </process>

    <process>
        <id>convert</id>
        <description>Convert and import</description>
        <mainClass>org.kramerius.Convert</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
    </process>


    <!-- parametrizovany process importu -->
    <process>
        <id>parametrizedimport</id>
        <description>Import FOXML</description>
        <mainClass>org.kramerius.imports.ParametrizedImport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <templates>
            <input class="org.kramerius.imports.input.ParametrizedImportInputTemplate"></input>
        </templates>
        
        <securedaction>import</securedaction>
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
        <securedaction>aggregate</securedaction>
        
    </process>


    <!-- k4 replication process -->
    <process>
        <id>k4_replication</id>
        <description>K4 Replication process</description>
        <mainClass>org.kramerius.replications.K4ReplicationProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>
        
        <templates>	
            <input class="org.kramerius.replications.inputs.InputTemplate"></input>
            <output class="org.kramerius.replications.outputs.OutputTemplate"></output>
        </templates>

        <securedaction>import_k4_replications</securedaction>
        
    </process>


    <!-- delete old processes -->
    <process>
        <id>delete_processes</id>
        <description>Delete old processes</description>
        <mainClass>cz.incad.kramerius.processes.manages.DeleteProcesses</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>
        
        <templates>
            <input class="cz.incad.kramerius.processes.manages.input.DeleteProcessesInput"></input>
        </templates>
        
    </process>


    <!-- Apply moving wall to subtree -->
    <process>
        <id>applymw</id>
        <description>Apply moving wall</description>
        <mainClass>cz.cas.lib.knav.ApplyMovingWall</mainClass>
        <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
    </process>
    
    <!-- Parametrized apply moving wall to subtree -->
    <process>
        <id>parametrizedapplymw</id>
        <description>parametrized apply moving wall</description>
        <!-- spoustena trida -->
        <mainClass>cz.cas.lib.knav.ParametrizedApplyMovingWall</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>

        <templates>
            <input class="cz.cas.lib.knav.templates.ParametrizedApplyMWTemplate"></input>
        </templates>
        
    </process>
    

    <!-- iterate over models -->
    <process>
        <id>iterated_appliedmw</id>
        <description>Iterate over models and starts subprocesses</description>
        <mainClass>cz.incad.kramerius.processes.impl.IterateNonParametrized</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>applymw ["$pid$"]</parameters>
        <securedaction>aggregate</securedaction>
    </process>

    <!-- iterate over models -->
    <process>
        <id>parametrized_iterated_appliedmw</id>
        <description>Iterate over models and starts subprocesses</description>
        <mainClass>cz.cas.lib.knav.ParametrizedIterateRepoMovingWall</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>aggregate</securedaction>
        <templates>
            <input class="cz.cas.lib.knav.templates.ParametrizedIterateRepoApplyMWTemplate"></input>
        </templates>
    </process>

    <!-- iterate over models -->
    <process>
        <id>iterated_mock</id>
        <description>Iterate over models and starts subprocesses</description>
        <mainClass>cz.incad.kramerius.processes.impl.IterateNonParametrized</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>mock ["$pid$","$model$","$index$"]</parameters>
        <securedaction>aggregate</securedaction>
    </process>
    
    <process>
        <id>batchindexer</id>
        <description>Batch indexer </description>
        <mainClass>cz.cas.lib.knav.indexer.BatchIndexerSupport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>reindex</securedaction>
    </process>


    <process>
        <id>migrationindex</id>
        <description>Index migrations</description>
        <mainClass>cz.incad.kramerius.services.ParallelMigrateSolrIndexImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>reindex</securedaction>
    </process>


</processes>
