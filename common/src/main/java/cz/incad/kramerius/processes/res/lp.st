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
	</process>

	<process>
		<id>static_export_CD</id>
		<description>Statický export do PDF na CD</description>
		<mainClass>cz.incad.kramerius.lp.PDFExport</mainClass>
		<standardOs>lrOut</standardOs>
		<errOs>lrErr</errOs>
		<parameters>
			<parameter>pdfs_out</parameter>		
			<parameter>CD</parameter>		
		</parameters>
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
		<parameters>
			<parameter>pdfs_out</parameter>		
			<parameter>DVD</parameter>		
		</parameters>
	</process>

	<process>
		<id>reindex</id>
		<description>Reindexace</description>
		<mainClass>cz.incad.kramerius.indexer.Main</mainClass>
		<standardOs>lrOut</standardOs>
		<errOs>lrErr</errOs>
		<parameters>
			<parameter>-cfgFile</parameter>
			<parameter>$default_lp_work_dir$/reindexer/config.properties</parameter>
			<parameter>-log4jFile</parameter>
			<parameter>$default_lp_work_dir$/reindexer/log4j.properties</parameter>
		</parameters>
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
		<parameters>
			<parameter>periodicals</parameter>
		</parameters>
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

</processes>
