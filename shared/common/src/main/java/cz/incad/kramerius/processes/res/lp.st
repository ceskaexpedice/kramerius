<processes>

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

    <process>
        <id>add_license</id>
        <description>přidání licence objektu, nebo seznamu objektů, včetně aktualizace vyhledávacího indexu</description>
        <mainClass>cz.incad.kramerius.SetLicenseProcess.addLicense</mainClass>
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

    <!--delete object and own tree with this object as a root-->
    <process>
        <id>delete_tree</id>
        <description>Smazání objektu a všech vlastních potomků. Smazání odkazujících vazeb; synchronizace inforamcí o licencích; náležitosti do sbírek u
            přeživších vlastních/nevlastních předků a nevlastních potomků.
        </description>
        <mainClass>cz.incad.kramerius.DeleteTreeProcess - dat jinam z dnntflag</mainClass>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_delete</securedaction>
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

    <process>
        <id>solr-migration</id>
        <description>(re)indexace modelu v novém vyhledávacím indexu</description>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_migration</securedaction>
    </process>

    <process>
        <id>update-logs</id>
        <description>Update logs index with authors and langs</description>
        <javaProcessParameters>-Xmx4g -Xms1g</javaProcessParameters>
        <securedaction>a_statistics_edit</securedaction>
    </process>

</processes>
