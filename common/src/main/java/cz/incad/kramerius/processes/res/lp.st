<processes>
	<process>
		<!-- identifikator  -->
		<id>mock</id>
		<!-- jenom popis -->
		<description>mock</description>
		<!-- spoustena trida -->
		<mainClass>cz.incad.kramerius.processes.mock.MockLPProcess</mainClass>
		<!-- standardni vystup -->
		<standardOs>$user_home$/lrOut</standardOs>
		<!-- err vystup -->
		<errOs>$user_home$/lrErr</errOs>
		<!--  Cesta ke kninovnam, ktere process potrebuje -->
		<lib>$user_home$/default-lib</lib>
	</process>

	<process>
		<id>static_export_CD</id>
		<description>Statický export do PDF na CD</description>
		<mainClass>cz.incad.kramerius.lp.PDFExport</mainClass>
		<standardOs>$user_home$/lrOut</standardOs>
		<errOs>$user_home$/lrErr</errOs>
		<lib>$user_home$/default-lib</lib>
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
		<standardOs>$user_home$/lrOut</standardOs>
		<errOs>$user_home$/lrErr</errOs>
		<lib>$user_home$/default-lib</lib>
		<parameters>
			<parameter>pdfs_out</parameter>		
			<parameter>DVD</parameter>		
		</parameters>
	</process>

</processes>
