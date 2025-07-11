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

    <!--deprecated - jen proto, aby bylo mozne zobrazit zaznamy starsich procesu-->
    <process>
        <id>process-api-test</id>
        <description>Proces pro testování nové správy procesů</description>
        <mainClass>cz.incad.kramerius.processes.mock.ProcessApiTestProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <checkErrorStream>false</checkErrorStream>
    </process>

    <process>
        <id>new_process_api_test</id>
        <description>Proces pro testování nové správy procesů</description>
        <mainClass>cz.incad.kramerius.processes.mock.ProcessApiTestProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <!--tohle neni definice ocekavanych parametru, ale hardcoded parametry pred temi predavanymi-->
        <!--<parameters>duration processesInBatch finalState</parameters>-->
        <checkErrorStream>false</checkErrorStream>
        <securedaction>a_index</securedaction>
    </process>

    <!--deprecated - jen proto, aby bylo mozne zobrazit zaznamy starsich procesu-->
    <process>
        <id>new_indexer</id>
        <description></description>
        <mainClass>cz.kramerius.searchIndex.NewIndexerProcessIndexObject</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <checkErrorStream>false</checkErrorStream>
        <securedaction>a_index</securedaction>
    </process>

    <process>
        <id>new_indexer_index_object</id>
        <description>(re)indexace objektu v novém vyhledávacím indexu</description>
        <mainClass>cz.kramerius.searchIndex.NewIndexerProcessIndexObject</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <!--tohle neni definice ocekavanych parametru, ale hardcoded parametry pred temi predavanymi-->
        <!--<parameters>duration processesInBatch finalState</parameters>-->
        <checkErrorStream>false</checkErrorStream>
        <securedaction>a_index</securedaction>
    </process>

    <process>
        <!-- identifikator  -->
        <id>new_indexer_index_model</id>
        <description>(re)indexace modelu v novém vyhledávacím indexu</description>
        <mainClass>cz.kramerius.searchIndex.NewIndexerProcessIndexModel</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <!--tohle neni definice ocekavanych parametru, ale hardcoded parametry pred temi predavanymi-->
        <!--<parameters>duration processesInBatch finalState</parameters>-->
        <checkErrorStream>false</checkErrorStream>
        <securedaction>a_index</securedaction>
    </process>



    <process>
        <id>parametrized_static_export</id>
        <description>Parametrized static export</description>
        <mainClass>cz.incad.kramerius.lp.ParametrizedPDFExport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
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



    <!-- Index & Reindex process -->
    <process>
        <id>virtualcollections</id>
        <description>Virtual Collections manager</description>
        <mainClass>cz.incad.kramerius.virtualcollections.VirtualCollectionsManager</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>virtualcollection_manage</securedaction>
    </process>

    <!-- K3 replication process - deprecated - it will be removed in the future -->
    <process>
        <id>k3_replication</id>
        <description>Parametrizovany replikator</description>
        <mainClass>org.kramerius.k3replications.ParametrizedReplication</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
        <parameters></parameters>
        <securedaction>replikator_k3</securedaction>
    </process>

    <!-- K3 replication process - deprecated - it will be removed in the future -->
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

    <!-- K3 replication process - deprecated - it will be removed in the future -->
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

    <!-- K3 replication process - deprecated - it will be removed in the future -->
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

    <!--deprecated - očekává datastream DC a spouští indexaci do starého indexu -->
    <process>
        <id>setpublic</id>
        <description>Set public</description>
        <mainClass>cz.incad.kramerius.service.impl.PolicyServiceImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>public</parameters>
        <securedaction>a_set_accessibility</securedaction>
        <!--
        <parameters>
            <parameter>public</parameter>
        </parameters>
        -->
    </process>

    <!--deprecated - očekává datastream DC a spouští indexaci do starého indexu -->
    <process>
        <id>setprivate</id>
        <description>Set private</description>
        <mainClass>cz.incad.kramerius.service.impl.PolicyServiceImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>private</parameters>
        <securedaction>a_set_accessibility</securedaction>
        <!--
        <parameters>
            <parameter>private</parameter>
        </parameters>
        -->
    </process>

    <process>
        <id>set_policy</id>
        <description>nastavení viditelnosti (POLICY) objektu, případně i potomků, na public/private</description>
        <mainClass>cz.incad.kramerius.processes.SetPolicyProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <checkErrorStream>false</checkErrorStream>
        <securedaction>a_set_accessibility</securedaction>
    </process>
    
    <!-- remove policy flag -->
    <process>
        <id>remove_policy</id>
        <description>Remove policy </description>
        <mainClass>cz.incad.kramerius.processes.RemovePolicyProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <checkErrorStream>false</checkErrorStream>
        <securedaction>a_set_accessibility</securedaction>
    </process>

    <process>
        <id>add_license</id>
        <description>přidání licence objektu, nebo seznamu objektů, včetně aktualizace vyhledávacího indexu</description>
        <mainClass>cz.incad.kramerius.SetLicenseProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <!--<checkErrorStream>false</checkErrorStream>-->
        <parameters>ADD</parameters>
        <securedaction>a_set_accessibility</securedaction>
    </process>

    <process>
        <id>remove_license</id>
        <description>odebrání licence objektu, nebo seznamu objektů, včetně aktualizace vyhledávacího indexu</description>
        <mainClass>cz.incad.kramerius.SetLicenseProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <!--<checkErrorStream>false</checkErrorStream>-->
        <parameters>REMOVE</parameters>
        <securedaction>a_set_accessibility</securedaction>
    </process>


    <process>
        <id>flag_to_license</id>
        <description>Change flag (public| private) to license (public | onsite)</description>
        <mainClass>cz.inovatika.licenses.FlagToLicenseProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
        <!--<checkErrorStream>false</checkErrorStream>-->
        <!--parameters>ADD</parameters-->
        <securedaction>a_set_accessibility</securedaction>
    </process>

    <!--deprecated, was replaced with delete_tree-->
    <process>
        <id>delete</id>
        <description>Delete</description>
        <mainClass>cz.incad.kramerius.service.impl.DeleteServiceImpl</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_delete</securedaction>
    </process>

    <!--delete object and own tree with this object as a root-->
    <process>
        <id>delete_tree</id>
        <description>Smazání objektu a všech vlastních potomků. Smazání odkazujících vazeb; synchronizace inforamcí o licencích; náležitosti do sbírek u
            přeživších vlastních/nevlastních předků a nevlastních potomků.
        </description>
        <mainClass>cz.incad.kramerius.DeleteTreeProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_delete</securedaction>
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

    <!-- K3 replication process - deprecated - it will be removed in the future -->
    <process>
        <id>parametrizedconvert</id>
        <description>Convert and import</description>
        <mainClass>org.kramerius.convert.ParametrizedConvert</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
        <securedaction>convert</securedaction>
    </process>

    <!-- Import NDK METS data  -->
    <process>
        <id>ndkmets</id>
        <description>Import NDK METs</description>
        <mainClass>org.kramerius.importmets.parametrized.ParametrizedMetsNKDImport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
        <securedaction>ndk_mets_import</securedaction>
    </process>

    <!-- deprecated: only nonparamtrized version (convert_and_import) is used now -->
    <!-- K3 replication process - deprecated - it will be removed in the future -->
    <process>
        <id>convert</id>
        <description>Convert and import</description>
        <mainClass>org.kramerius.Convert</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
    </process>

    <!-- deprecated: only nonparamtrized version (import) is used now -->
    <!-- Parametrized import FOXML - dedicated for use  in UI  -->
    <process>
        <id>parametrizedimport</id>
        <description>Import FOXML</description>
        <mainClass>org.kramerius.imports.ParametrizedImport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_import</securedaction>
    </process>

    <!-- Import FOXML process -->
    <process>
        <id>import</id>
        <description>Import FOXML</description>
        <mainClass>org.kramerius.Import</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_import</securedaction>
    </process>

    <!-- Import NDK METS process -->
    <process>
        <id>convert_and_import</id>
        <description>Convert and import NDK METS</description>
        <mainClass>org.kramerius.importmets.MetsConvertor</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_import</securedaction>
    </process>

    <!-- Generating Deep Zoom tiles process -->
    <process>
        <id>generateDeepZoomTiles</id>
        <description>Generate cache for Deep Zoom</description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateDeepZoomCache</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx3072m -Xms512m -Dcz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag=true
            -Dcz.incad.kramerius.imaging.lp.GenerateThumbnail=true
        </javaProcessParameters>
    </process>

    <!-- Set flag into RELS-EXT -->
    <process>
        <id>generateDeepZoomFlag</id>
        <description>Generate deep zoom flag to RELS-EXT</description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
    </process>

    <!-- Deleting Deep Zoom tiles and flags -->
    <process>
        <id>deleteGeneratedDeepZoomTiles</id>
        <description>Delete generated deepZoomCache</description>
        <mainClass>cz.incad.kramerius.imaging.lp.DeleteGeneratedDeepZoomCache</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms512m -Dcz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag=true</javaProcessParameters>
    </process>

    <!-- Generate thumbnails -->
    <process>
        <id>generateFullThumb</id>
        <description>Generate full thumbs</description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateThumbnail</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
    </process>

    <!-- Generic aggregate process - it could wrap an start several child subprocess -->
    <process>
        <id>aggregate</id>
        <description>Process aggregator</description>
        <mainClass>cz.incad.kramerius.processes.impl.ProcessAggregator</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>aggregate</securedaction>
    </process>

    <!-- K4 replication process -->
    <process>
        <id>k4_replication</id>
        <description>K4 Replication process</description>
        <mainClass>org.kramerius.replications.K4ReplicationProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>
        <securedaction>a_import_replications</securedaction>
    </process>

    <!-- Deleting ald process -->
    <process>
        <id>delete_processes</id>
        <description>Delete old processes</description>
        <mainClass>cz.incad.kramerius.processes.manages.DeleteProcesses</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>
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
        <mainClass>cz.cas.lib.knav.ParametrizedApplyMovingWall</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx1024m -Xms256m</javaProcessParameters>
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

    <!-- batch indexer -->
    <process>
        <id>batchindexer</id>
        <description>Batch indexer</description>
        <mainClass>cz.cas.lib.knav.indexer.BatchIndexerSupport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_index</securedaction>
    </process>


    <!-- Set/unset dnnt label from csv file -->
    <process>
        <id>dnntcsvlabeledflag</id>
        <description>DNNT SET/UNSET label from CSV</description>
        <mainClass>cz.incad.kramerius.csv.DDNTCSVLabeledFlag</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>dnnt_admin</securedaction>
    </process>

    <!-- processing index check  itereate over processing index and check if object exists in fedora  -->
    <process>
        <id>processing_check</id>
        <description>Check processing index</description>
        <mainClass>cz.incad.kramerius.resourceindex.ProcessingIndexCheck</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_rebuild_processing_index</securedaction>
    </process>

    <!-- walk through repository and rebuild processing index -->
    <process>
        <id>processing_rebuild</id>
        <description>Processing index rebuild</description>
        <mainClass>cz.incad.kramerius.resourceindex.ProcessingIndexRebuild</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_rebuild_processing_index</securedaction>
    </process>

    <!--rebuild processing index for given object (from it's foxml), i.e. fix description and relations going FROM this object-->
    <process>
        <id>processing_rebuild_for_object</id>
        <description>Processing index - rebuild records from FOXML of given object (i.e. description and relations from this object)</description>
        <mainClass>cz.incad.kramerius.resourceindex.ProcessingIndexRebuildFromFoxmlByPid</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_rebuild_processing_index</securedaction>
    </process>

    <!-- migrace dat z Krameria do Krameria -->
    <process>
        <id>data_migration</id>
        <description>Repository to repository migration</description>
        <mainClass>org.kramerius.replications.FedoraDataMigrationProcess</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>import</securedaction>
    </process>

    <process>
        <id>dnntexport</id>
        <description>Create dnnt csv export</description>
        <mainClass>cz.incad.kramerius.LicensesExport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>false</parameters>
        <securedaction>a_set_accessibility</securedaction>
    </process>


    <!-- Set/unset dnnt label -->
    <process>
        <id>dnntlabel</id>
        <description>DNNT SET/UNSET label</description>
        <mainClass>cz.incad.kramerius.plain.DNNTPidLabeledFlag</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_set_accessibility</securedaction>
    </process>


    <!-- parametrized label dnntset -->
    <process>
        <id>parametrizeddnntlabelset</id>
        <description>Set the DNNT flag</description>
        <mainClass>cz.incad.kramerius.csv.ParametrizedLabelSetDNNTFlag</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>true</parameters>
        <securedaction>a_set_accessibility</securedaction>
    </process>

    <!-- parametrized label dnntunset -->
    <process>
        <id>parametrizeddnntlabelunset</id>
        <description>Unset the DNNT flag</description>
        <mainClass>cz.incad.kramerius.csv.ParametrizedLabelUnsetDNNTFlag</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <parameters>true</parameters>
        <securedaction>dnnt_admin</securedaction>
    </process>


    <process>
        <id>dnntexport</id>
        <description>Create dnnt csv export</description>
        <mainClass>cz.incad.kramerius.LicensesExport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_set_accessibility</securedaction>
    </process>


    <process>
        <id>parametrizeddnntexport</id>
        <description>Parametrized dnnt export</description>
        <mainClass>cz.incad.kramerius.export.ParametrizedLicenseExport</mainClass>
        <standardOs>lrOut</standardOs>
        <errOs>lrErr</errOs>
        <securedaction>a_set_accessibility</securedaction>
    </process>


    <process>
        <id>nkplogs</id>
        <description>Generate access logs for NKP</description>
        <!-- spoustena trida -->
        <mainClass>cz.incad.kramerius.statistics.impl.nkp.NKPLogProcess</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx4096m -Xms256m -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_generate_nkplogs</securedaction>
    </process>


    <process>
        <id>sdnnt-sync</id>
        <description>SDNNT sync</description>
        <!-- spoustena trida -->
        <mainClass>cz.inovatika.sdnnt.SDNNTFetch</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx4096m -Xms256m -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_generate_nkplogs</securedaction>
    </process>

    <process>
        <id>backup-collections</id>
        <description>Collection backup</description>
        <mainClass>cz.inovatika.collections.Backup</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx4096m -Xms256m -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_collections_edit</securedaction>
    </process>

    <process>
        <id>restore-collections</id>
        <description>Restore collections</description>
        <mainClass>cz.inovatika.collections.Restore</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx4096m -Xms256m -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_collections_edit</securedaction>
    </process>

    <process>
        <id>migrate-collections-from-k5</id>
        <description>Collections from K5 instance</description>
        <mainClass>cz.inovatika.collections.migrations.FromK5Instance</mainClass>
        <!-- standardni vystup -->
        <standardOs>lrOut</standardOs>
        <!-- err vystup -->
        <errOs>lrErr</errOs>
        <javaProcessParameters>-Xmx4096m -Xms256m -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_collections_edit</securedaction>
    </process>

</processes>
