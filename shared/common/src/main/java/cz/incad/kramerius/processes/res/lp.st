<processes>

    <process>
        <id>new_indexer</id>
        <description></description>
        <mainClass>cz.kramerius.searchIndex.NewIndexerProcessIndexObject</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_index</securedaction>
    </process>

    <process>
        <id>new_indexer_index_object</id>
        <description>(re)indexace objektu v novém vyhledávacím indexu</description>
        <mainClass>cz.kramerius.searchIndex.NewIndexerProcessIndexObject</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_index</securedaction>
    </process>

    <process>
        <id>new_indexer_index_model</id>
        <description>(re)indexace modelu v novém vyhledávacím indexu</description>
        <mainClass>cz.kramerius.searchIndex.NewIndexerProcessIndexModel</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_index</securedaction>
    </process>

    <!-- K3 replication process - deprecated - it will be removed in the future -->
    <process>
        <id>replikator_monographs</id>
        <description>Replikator (monografie)</description>
        <mainClass>org.kramerius.Replicate</mainClass>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
    </process>

    <!-- K3 replication process - deprecated - it will be removed in the future -->
    <process>
        <id>replikator_periodicals</id>
        <description>Replikator (periodika)</description>
        <mainClass>org.kramerius.Replicate</mainClass>
        <javaProcessParameters>-Xmx2048m -Xms512m</javaProcessParameters>
    </process>

    <!-- K3 replication process - deprecated - it will be removed in the future -->
    <processes>
        <id>enumerator</id>
        <description>Enumerator</description>
        <mainClass>org.kramerius.Enumerator</mainClass>
    </processes>

    <process>
        <id>replicationrights</id>
        <description>Replication rights</description>
        <mainClass>org.kramerius.ReplicationRights</mainClass>
    </process>

    <process>
        <id>add_license</id>
        <description>přidání licence objektu, nebo seznamu objektů, včetně aktualizace vyhledávacího indexu</description>
        <mainClass>cz.incad.kramerius.SetLicenseProcess</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_set_accessibility</securedaction>
    </process>

    <process>
        <id>remove_license</id>
        <description>odebrání licence objektu, nebo seznamu objektů, včetně aktualizace vyhledávacího indexu</description>
        <mainClass>cz.incad.kramerius.SetLicenseProcess</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_set_accessibility</securedaction>
    </process>

    <process>
        <id>flag_to_license</id>
        <description>Change flag (public| private) to license (public | onsite)</description>
        <mainClass>cz.inovatika.licenses.FlagToLicenseProcess</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_set_accessibility</securedaction>
    </process>

    <!--deprecated, was replaced with delete_tree-->
    <process>
        <id>delete</id>
        <description>Delete</description>
        <mainClass>cz.incad.kramerius.service.impl.DeleteServiceImpl</mainClass>
        <securedaction>a_delete</securedaction>
    </process>

    <!--delete object and own tree with this object as a root-->
    <process>
        <id>delete_tree</id>
        <description>Smazání objektu a všech vlastních potomků. Smazání odkazujících vazeb; synchronizace inforamcí o licencích; náležitosti do sbírek u
            přeživších vlastních/nevlastních předků a nevlastních potomků.
        </description>
        <mainClass>cz.incad.kramerius.DeleteTreeProcess</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_delete</securedaction>
    </process>

    <process>
        <id>sort</id>
        <description>Sort relations</description>
        <mainClass>cz.incad.kramerius.service.impl.SortingServiceImpl</mainClass>
    </process>

    <process>
        <id>export</id>
        <description>Export</description>
        <mainClass>cz.incad.kramerius.service.impl.ExportServiceImpl</mainClass>
    </process>

    <!-- deprecated: only nonparamtrized version (convert_and_import) is used now -->
    <!-- K3 replication process - deprecated - it will be removed in the future -->
    <process>
        <id>convert</id>
        <description>Convert and import</description>
        <mainClass>org.kramerius.Convert</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
    </process>

    <!-- Update FOXML process -->
    <process>
        <id>update</id>
        <description>Update FOXML Stream</description>
        <mainClass>org.kramerius.UpdateStreams</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_import</securedaction>
    </process>

    <!-- Import FOXML process -->
    <process>
        <id>import</id>
        <description>Import FOXML</description>
        <mainClass>org.kramerius.Import</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_import</securedaction>
    </process>

    <!-- Import NDK METS process -->
    <process>
        <id>convert_and_import</id>
        <description>Convert and import NDK METS</description>
        <mainClass>org.kramerius.importmets.MetsConvertor</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_import</securedaction>
    </process>

    <!-- Generating Deep Zoom tiles process -->
    <process>
        <id>generateDeepZoomTiles</id>
        <description>Generate cache for Deep Zoom</description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateDeepZoomCache</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g -Dcz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag=true
            -Dcz.incad.kramerius.imaging.lp.GenerateThumbnail=true
        </javaProcessParameters>
    </process>

    <!-- Set flag into RELS-EXT -->
    <process>
        <id>generateDeepZoomFlag</id>
        <description>Generate deep zoom flag to RELS-EXT</description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag</mainClass>
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
    </process>

    <!-- Deleting Deep Zoom tiles and flags -->
    <process>
        <id>deleteGeneratedDeepZoomTiles</id>
        <description>Delete generated deepZoomCache</description>
        <mainClass>cz.incad.kramerius.imaging.lp.DeleteGeneratedDeepZoomCache</mainClass>
        <javaProcessParameters>-Xmx1024m -Xms512m -Dcz.incad.kramerius.imaging.lp.GenerateDeepZoomFlag=true</javaProcessParameters>
    </process>

    <!-- Generate thumbnails -->
    <process>
        <id>generateFullThumb</id>
        <description>Generate full thumbs</description>
        <mainClass>cz.incad.kramerius.imaging.lp.GenerateThumbnail</mainClass>
        <javaProcessParameters>-Xmx3072m -Xms512m</javaProcessParameters>
    </process>

    <!-- K4 replication process -->
    <process>
        <id>k4_replication</id>
        <description>K4 Replication process</description>
        <mainClass>org.kramerius.replications.K4ReplicationProcess</mainClass>
        <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>
        <securedaction>a_import_replications</securedaction>
    </process>

    <!-- processing index check  itereate over processing index and check if object exists in fedora  -->
    <process>
        <id>processing_check</id>
        <description>Check processing index</description>
        <mainClass>cz.incad.kramerius.resourceindex.ProcessingIndexCheck</mainClass>
        <securedaction>a_rebuild_processing_index</securedaction>
    </process>

    <!-- walk through repository and rebuild processing index -->
    <process>
        <id>processing_rebuild</id>
        <description>Processing index rebuild</description>
        <mainClass>cz.incad.kramerius.resourceindex.ProcessingIndexRebuild</mainClass>
        <securedaction>a_rebuild_processing_index</securedaction>
    </process>

    <!--rebuild processing index for given object (from it's foxml), i.e. fix description and relations going FROM this object-->
    <process>
        <id>processing_rebuild_for_object</id>
        <description>Processing index - rebuild records from FOXML of given object (i.e. description and relations from this object)</description>
        <mainClass>cz.incad.kramerius.resourceindex.ProcessingIndexRebuildFromFoxmlByPid</mainClass>
        <securedaction>a_rebuild_processing_index</securedaction>
    </process>

    <!-- migrace dat z Krameria do Krameria -->
    <process>
        <id>data_migration</id>
        <description>Repository to repository migration</description>
        <mainClass>org.kramerius.replications.FedoraDataMigrationProcess</mainClass>
        <securedaction>import</securedaction>
    </process>

    <process>
        <id>nkplogs</id>
        <description>Generate access logs for NKP</description>
        <mainClass>cz.incad.kramerius.statistics.impl.nkp.NKPLogProcess</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_generate_nkplogs</securedaction>
    </process>

    <process>
        <id>sdnnt-sync</id>
        <description>SDNNT sync</description>
        <mainClass>cz.inovatika.sdnnt.SDNNTFetch</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_generate_nkplogs</securedaction>
    </process>

    <process>
        <id>backup-collections</id>
        <description>Collection backup</description>
        <mainClass>cz.inovatika.collections.Backup</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_collections_edit</securedaction>
    </process>

    <process>
        <id>restore-collections</id>
        <description>Restore collections</description>
        <mainClass>cz.inovatika.collections.Restore</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_collections_edit</securedaction>
    </process>

    <process>
        <id>migrate-collections-from-k5</id>
        <description>Collections from K5 instance</description>
        <mainClass>cz.inovatika.collections.migrations.FromK5Instance</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g -Dfile.encoding=UTF-8</javaProcessParameters>
        <securedaction>a_collections_edit</securedaction>
    </process>

</processes>
