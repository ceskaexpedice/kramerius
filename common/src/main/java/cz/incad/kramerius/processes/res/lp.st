<processes>
	<process>
		<!-- identifikator  -->
		<id>mock</id>
		<!-- jenom popis -->
		<description>mock</description>
		<!-- spoustena trida -->
		<mainClass>cz.incad.kramerius.processes.mock.MockLPProcess</mainClass>
		<!-- standardni vystup -->
		<standardOs>$default_lp_work_dir$/lrOut</standardOs>
		<!-- err vystup -->
		<errOs>$default_lp_work_dir$/lrErr</errOs>
		<!--  Cesta ke kninovnam, ktere process potrebuje -->
		<lib>$default_lp_work_dir$/default-lib</lib>
	</process>

	<process>
		<id>static_export_CD</id>
		<description>Statický export do PDF na CD</description>
		<mainClass>cz.incad.kramerius.lp.PDFExport</mainClass>
		<standardOs>$default_lp_work_dir$/lrOut</standardOs>
		<errOs>$default_lp_work_dir$/lrErr</errOs>
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
		<standardOs>$default_lp_work_dir$/lrOut</standardOs>
		<errOs>$default_lp_work_dir$/lrErr</errOs>
		<parameters>
			<parameter>pdfs_out</parameter>		
			<parameter>DVD</parameter>		
		</parameters>
	</process>

	<process>
		<id>reindex</id>
		<description>Reindexace</description>
		<mainClass>cz.incad.kramerius.indexer.Main</mainClass>
		<standardOs>$default_lp_work_dir$/lrOut</standardOs>
		<errOs>$default_lp_work_dir$/lrErr</errOs>
		<parameters>
			<parameter>-cfgFile</parameter>
			<parameter>$default_lp_work_dir$/reindexer/config.properties</parameter>
			<parameter>-log4jFile</parameter>
			<parameter>$default_lp_work_dir$/reindexer/log4j.properties</parameter>
		</parameters>
	</process>

</processes>
