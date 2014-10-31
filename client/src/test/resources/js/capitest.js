module( "group api-tests" );

asyncTest("Collections", 1, function() {

	expect( 4 );

	window.collectionsloaded = false;	
	
	$.mockjax({
	  url: 'api/vc',
	  contentType: 'application/json',
	  dataType:'json',	
	  status:200,
	  responseTime: 750,
	  responseText: [{"pid":"vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea","label":"label","canLeave":true,"descs":{"en":"NDK-Test","cs":"NDK-Test"}}]
	});
		
	K5.eventsHandler.addHandler(function(type, data) {
		if (type === "api/vc") {
			window.collectionsloaded = true;
			console.log("collectionsloaded");
		}
	});
	K5.api.askForCollections(function() {});
	
	setTimeout(function() {
	    // test trigger
	    ok( collectionsloaded === true , "Trigger called" );

	    ok( K5.api.ctx.vc !==null , "Collections is not null" );
	    ok((typeof K5.api.ctx.vc === "object"), "Collections is type of object");
	    ok(K5.api.ctx.vc["vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea"] !== null, "Contains vc with concrete pid");
	    	
    	    start();
	}, 1000);
	
});


asyncTest("Newest",1,function() {

	expect(4);

	window.newestloaded = false;	

	$.mockjax({
	  url: 'api/feed/newest',
	  contentType: 'application/json',
	  dataType:'json',	
	  status:200,
	  responseTime: 750,
	  responseText: {"rss":"http://cdk-test.lib.cas.cz/search/inc/home/newest-rss.jsp","data":[{"pid":"uuid:1c0b81d0-d57d-11e1-9b23-0800200c9a66","model":"monograph","issn":"3-033-00220-X","datumstr":"2004","root_pid":"uuid:1c0b81d0-d57d-11e1-9b23-0800200c9a66","title":"Cross language applications on the Web","root_title":"Cross language applications on the Web"},{"pid":"uuid:0dcec060-d57c-11e1-9b23-0800200c9a66","model":"monograph","issn":"","datumstr":"2003","root_pid":"uuid:0dcec060-d57c-11e1-9b23-0800200c9a66","title":"Semantic Web and Libraries","root_title":"Semantic Web and Libraries"},{"pid":"uuid:4485d220-d57b-11e1-9b23-0800200c9a66","model":"monograph","issn":"80-7050-413-7","datumstr":"2002","root_pid":"uuid:4485d220-d57b-11e1-9b23-0800200c9a66","title":"Integrating heterogenous resources","root_title":"Integrating heterogenous resources"},{"pid":"uuid:01dc60f0-d577-11e1-9b23-0800200c9a66","model":"monograph","issn":"2-86842-146-6","datumstr":"2001","root_pid":"uuid:01dc60f0-d577-11e1-9b23-0800200c9a66","title":"Archives, Libraries and Museums Convergence","root_title":"Archives, Libraries and Museums Convergence"},{"pid":"uuid:1b06c030-d576-11e1-9b23-0800200c9a66","model":"monograph","issn":"961-6162-50-0","datumstr":"1999","root_pid":"uuid:1b06c030-d576-11e1-9b23-0800200c9a66","title":"Managing multimedia collections","root_title":"Managing multimedia collections"},{"pid":"uuid:51b175e0-d575-11e1-9b23-0800200c9a66","model":"monograph","issn":"906259143-4","datumstr":"2000","root_pid":"uuid:51b175e0-d575-11e1-9b23-0800200c9a66","title":"Object oriented approaches","root_title":"Object oriented approaches"},{"pid":"uuid:37f63330-d574-11e1-9b23-0800200c9a66","model":"monograph","issn":"83-86230-35-5","datumstr":"1998","root_pid":"uuid:37f63330-d574-11e1-9b23-0800200c9a66","title":"Document publishing and delivery","root_title":"Document publishing and delivery"},{"pid":"uuid:562d24e0-d573-11e1-9b23-0800200c9a66","model":"monograph","issn":"3-88053-068-8","datumstr":"1998","root_pid":"uuid:562d24e0-d573-11e1-9b23-0800200c9a66","title":"Quality of electronic services","root_title":"Quality of electronic services"},{"pid":"uuid:5b21df00-d572-11e1-9b23-0800200c9a66","model":"monograph","issn":"","datumstr":"1995","root_pid":"uuid:5b21df00-d572-11e1-9b23-0800200c9a66","title":"Organising the electronic library","root_title":"Organising the electronic library"},{"pid":"uuid:6709ecf0-d571-11e1-9b23-0800200c9a66","model":"monograph","issn":"963-200-345-4","datumstr":"1994","root_pid":"uuid:6709ecf0-d571-11e1-9b23-0800200c9a66","title":"Library services in an electronic environment","root_title":"Library services in an electronic environment"},{"pid":"uuid:706c1350-d570-11e1-9b23-0800200c9a66","model":"monograph","issn":"3-901379-00-2","datumstr":"1993","root_pid":"uuid:706c1350-d570-11e1-9b23-0800200c9a66","title":"The Virtual library","root_title":"The Virtual library"},{"pid":"uuid:9c8f51f0-d56f-11e1-9b23-0800200c9a66","model":"monograph","issn":"","datumstr":"1993","root_pid":"uuid:9c8f51f0-d56f-11e1-9b23-0800200c9a66","title":"ILL in network","root_title":"ILL in network"},{"pid":"uuid:8e4c9d10-d56e-11e1-9b23-0800200c9a66","model":"monograph","issn":"951-45-5882-0","datumstr":"1991","root_pid":"uuid:8e4c9d10-d56e-11e1-9b23-0800200c9a66","title":"Database management systems","root_title":"Database management systems"},{"pid":"uuid:839754b0-d56d-11e1-9b23-0800200c9a66","model":"monograph","issn":"90-6637-065-3","datumstr":"","root_pid":"uuid:839754b0-d56d-11e1-9b23-0800200c9a66","title":"14th Library systems seminar Brussels","root_title":"14th Library systems seminar Brussels"},{"pid":"uuid:7fbea3d0-d56c-11e1-9b23-0800200c9a66","model":"monograph","issn":"86-7237-019-8","datumstr":"1989","root_pid":"uuid:7fbea3d0-d56c-11e1-9b23-0800200c9a66","title":"13th Library systems seminar Zagreb","root_title":"13th Library systems seminar Zagreb"},{"pid":"uuid:312560b0-d562-11e1-9b23-0800200c9a66","model":"monograph","issn":"91-7000-130-8","datumstr":"1988","root_pid":"uuid:312560b0-d562-11e1-9b23-0800200c9a66","title":"Local Systems","root_title":"Local Systems"},{"pid":"uuid:15b4a5d0-d561-11e1-9b23-0800200c9a66","model":"monograph","issn":"3-922051-19-7","datumstr":"1987","root_pid":"uuid:15b4a5d0-d561-11e1-9b23-0800200c9a66","title":"The library of the future","root_title":"The library of the future"},{"pid":"uuid:2d660e00-d4ce-11e1-9b23-0800200c9a66","model":"monograph","issn":"90-6637-001-7","datumstr":"","root_pid":"uuid:2d660e00-d4ce-11e1-9b23-0800200c9a66","title":"Database management systems","root_title":"Database management systems"}]}
	});


	K5.eventsHandler.addHandler(function(type, data) {
		if (type === "api/feed/newest") {
			window.newestloaded = true;
			console.log("newestloaded");
		}
	});
	K5.api.askForLatest(function() {});

	setTimeout(function() {

	    // test trigger
	    ok( newestloaded === true , "Trigger newest called" );

	    ok( K5.api.ctx.feed.newest !== null , "Newest is not null" );
	    ok((typeof K5.api.ctx.feed.newest === "object"), "Newest is type of object");
	    ok(K5.api.ctx.feed.newest["data"] !== null, "Newest contains data");
		
    	    start();
	}, 1000);
	
});



asyncTest("Most desirable",1,function() {

	expect(5);

	window.mostdesirableloaded = false;	

	$.mockjax({
	  url: 'api/feed/mostdesirable',
	  contentType: 'application/json',
	  dataType:'json',	
	  status:200,
	  responseTime: 750,
	  responseText: {"rss":"http://cdk-test.lib.cas.cz/search/inc/home/mostDesirables-rss.jsp","data":[{"pid":"uuid:b4d5a20a-148b-11e1-9c33-005056a60003","model":"periodical","issn":"0001-5601","datumstr":"1965-1992","root_pid":"uuid:b4d5a20a-148b-11e1-9c33-005056a60003","title":"Acta entomologica bohemoslovaca","root_title":"Acta entomologica bohemoslovaca"},{"pid":"uuid:bdccaf08-687a-4058-ae06-fb9f13bc72a8","model":"monograph","issn":"80-200-0391-6","datumstr":[1991],"root_pid":"uuid:bdccaf08-687a-4058-ae06-fb9f13bc72a8","title":"100 let České akademie věd a umění","root_title":"100 let České akademie věd a umění"},{"pid":"uuid:0a596f82-3c59-11e1-a824-005056a60003","model":"periodical","issn":"1212-1576","datumstr":"1994-2003","root_pid":"uuid:0a596f82-3c59-11e1-a824-005056a60003","title":"Acta Montana","root_title":"Acta Montana"},{"pid":"uuid:6034dd46-acd8-4a12-859a-3c291e7f6010","model":"periodical","issn":"1212-1428","datumstr":"1994-","root_pid":"uuid:6034dd46-acd8-4a12-859a-3c291e7f6010","title":"Acta Universitatis Carolinae: Kinantropologica","root_title":"Acta Universitatis Carolinae: Kinantropologica"},{"pid":"uuid:9f3ed0d4-d4e8-11e0-9681-0050569d679d","model":"page","issn":"","datumstr":"1868","root_pid":"uuid:8cf8786d-d4e8-11e0-9681-0050569d679d","title":"{1]","root_title":"Kniha Tovačovská, aneb, Pana Ctibora z Cimburka a\n                z Tovačova Paměť obyčejů, řádů, zvyklostí starodávných a\n                řízení práva zemského v Markrabství Moravském"},{"pid":"uuid:6430d7ae-0ea5-4587-9aab-9d7d9c42a791","model":"monograph","issn":"","datumstr":"","root_pid":"uuid:6430d7ae-0ea5-4587-9aab-9d7d9c42a791","title":"--kde slunce vyjde zítra","root_title":"--kde slunce vyjde zítra"},{"pid":"uuid:0174f891-62d3-11e1-8115-0050569d679d","model":"monograph","issn":"","datumstr":"1893","root_pid":"uuid:0174f891-62d3-11e1-8115-0050569d679d","title":"12. březen 1893","root_title":"12. březen 1893"},{"pid":"uuid:ad9bfd5f-e91f-482a-8d94-c54662e9df9d","model":"monograph","issn":"978-80-85015-56-0","datumstr":"2010","root_pid":"uuid:ad9bfd5f-e91f-482a-8d94-c54662e9df9d","title":"50 let hodonínské galerie : [Galerie výtvarného umění v Hodoníně 22.9.2010-2.1.2011","root_title":"50 let hodonínské galerie : [Galerie výtvarného umění v Hodoníně 22.9.2010-2.1.2011"},{"pid":"uuid:e7373dec-307c-11e0-9e26-0050569d679d","model":"monograph","issn":"","datumstr":"1986","root_pid":"uuid:e7373dec-307c-11e0-9e26-0050569d679d","title":"500 let knihtisku v Brně","root_title":"500 let knihtisku v Brně"},{"pid":"uuid:81c15d83-21bf-11e3-a6ac-001b63bd97ba","model":"page","issn":"","datumstr":"1997","root_pid":"uuid:81c10f62-21bf-11e3-a6ac-001b63bd97ba","title":"","root_title":"Optimální rekonstrukce výtokového traktu pravé a levé komory u dětí s vrozenou srdeční vadou"},{"pid":"uuid:4ff703f0-66f6-11de-abff-000d606f5dc6","model":"monograph","issn":"","datumstr":"18--","root_pid":"uuid:4ff703f0-66f6-11de-abff-000d606f5dc6","title":"24 Cadenzen und Versetten für die Orgel nebst 24 vorangehenden kurzen Uebungen für beide Hände","root_title":"24 Cadenzen und Versetten für die Orgel nebst 24\n                vorangehenden kurzen Uebungen für beide Hände"},{"pid":"uuid:aa593a6b-867b-4783-a15e-d3e3d97c32ba","model":"monograph","issn":"","datumstr":"1661","root_pid":"uuid:aa593a6b-867b-4783-a15e-d3e3d97c32ba","title":"(: Oculus Fidei :) Theologia Naturalis; sive Liber Creaturarum :specialiter De Homine & Natura ej...","root_title":"(: Oculus Fidei :) Theologia Naturalis; sive Liber Creaturarum :specialiter De Homine & Natura ej..."},{"pid":"uuid:8b93a4e0-d7bb-11e0-a7c1-0050569d679d","model":"monograph","issn":"80-85282-28-3 (Brož.)","datumstr":"1992","root_pid":"uuid:8b93a4e0-d7bb-11e0-a7c1-0050569d679d","title":"100 her k rozvoji tvořivosti v předškolním a mladším školním věku","root_title":"100 her k rozvoji tvořivosti v předškolním a\n                mladším školním věku"},{"pid":"uuid:9d68eb70-394a-43ed-9d21-d533a80ec431","model":"monograph","issn":"80-901850-6-1","datumstr":"1997","root_pid":"uuid:9d68eb70-394a-43ed-9d21-d533a80ec431","title":"1. česko-polské geomechanické sympozium","root_title":"1. česko-polské geomechanické sympozium"},{"pid":"uuid:17f29aa8-72ef-4bdc-8584-739db24b4e30","model":"periodical","issn":"1211-8796","datumstr":"1997-","root_pid":"uuid:17f29aa8-72ef-4bdc-8584-739db24b4e30","title":"Acta Musei Moraviae. Scientiae Geologicae","root_title":"Acta Musei Moraviae. Scientiae Geologicae"},{"pid":"uuid:1f910b93-0d55-4a66-bebc-fb03845b35d2","model":"periodical","issn":"0374-1036","datumstr":"[1967]-","root_pid":"uuid:1f910b93-0d55-4a66-bebc-fb03845b35d2","title":"Acta entomologica Musei Nationalis Pragae","root_title":"Acta entomologica Musei Nationalis Pragae"},{"pid":"uuid:7ee27dd9-bf5b-11e1-8a5c-005056a60003","model":"periodical","issn":"1803-5795","datumstr":"1824 - 1837","root_pid":"uuid:7ee27dd9-bf5b-11e1-8a5c-005056a60003","title":"Abhandlungen der Königlichen böhmischen Gesellschaft der Wissenschaften. Neuere Folge","root_title":"Abhandlungen der Königlichen böhmischen Gesellschaft der Wissenschaften. Neuere Folge"},{"pid":"uuid:188ba578-8c45-4bbe-b315-0644d225c9ea","model":"page","issn":"","datumstr":"1993","root_pid":"uuid:b8719ff5-4cee-4244-8f18-455b09758634","title":"","root_title":"Revolution for whom?: Analysis of selected patterns of intragenerational mobility in the Czech Republic, 1989-1992"}]}
	});


	K5.eventsHandler.addHandler(function(type, data) {
		if (type === "api/feed/mostdesirable") {
			window.mostdesirableloaded = true;
		}
	});

	K5.api.askForPopular(function() {});
	
	setTimeout(function() {
	    // test triggercalled
	    ok( mostdesirableloaded === true , "Trigger newest called" );

	    ok( K5.api.isKeyReady("feed/mostdesirable") , "Most desirable key is ready" );

	    ok( K5.api.ctx.feed.mostdesirable !== null , "Mostdesirable is not null" );
	    ok((typeof K5.api.ctx.feed.mostdesirable === "object"), "Mostdesirable is type of object");
	    ok(K5.api.ctx.feed.mostdesirable["data"] !== null, "Mostdesirable contains data");

    	    start();
	}, 1000);
	
});



asyncTest("Solr -search",1,function() {
	expect(4);

	$.mockjax({
	  url: 'api/search?fl=dc.creator,dc.title,PID,dostupnost&fq=fedora.model%3Amonograph+OR+fedora.model%3Amap&q=rok:2001&start=0&rows=50',
	  contentType: 'application/json',
	  dataType:'json',	
	  status:200,
	  responseTime: 750,
	  responseText: {"responseHeader":{"status":0,"QTime":1,"params":{"fl":"dc.creator,dc.title,PID,dostupnost","start":"0","q":"rok:2002","wt":"json","fq":"fedora.model:monograph OR fedora.model:map","rows":"50"}},"response":{"numFound":196,"start":0,"docs":[{"PID":"uuid:7af19af3-a940-11e0-a3f2-0050569d679d","dc.title":"Funkce krajských knihoven","dostupnost":"private","dc.creator":["",""]},{"PID":"uuid:56f2df02-a975-11e0-a5e1-0050569d679d","dc.title":"Reiki","dostupnost":"private","dc.creator":["Glaser Brigitte"]},{"PID":"uuid:7a6936d1-3074-11e0-8e0a-0050569d679d","dc.title":"Osudy Němců vysídlených z České republiky po roce 1945","dostupnost":"public","dc.creator":["Kubíček Jaromír"]},{"PID":"uuid:b5fed44e-be92-4780-abc1-ea08710a16f9","dc.title":"Bibliografie okresu Šumperk","dostupnost":"private","dc.creator":["Dohnalová, Emilie"]},{"PID":"uuid:c807c9ca-885d-460f-8a8d-e8ad23d556ac","dc.title":"Alpha ; [z francouzštiny přeložil Tomáš Bicek].","dostupnost":"private","dc.creator":["Jigounov, Youri"]},{"PID":"uuid:cf35b628-18ac-4bb6-9999-d55ff17a068b","dc.title":"Diagnostika předškoláka : správný vývoj řeči dítěte","dostupnost":"private","dc.creator":["Klenková, Jiřina","Kolbábková, Helena"]},{"PID":"uuid:633921d2-82f4-4384-adf2-56830b9c47a9","dc.title":"Bibliografie (1976-2001)","dostupnost":"private","dc.creator":["Poláček, Jiří"]},{"PID":"uuid:f4082e99-a409-459c-a6a1-f5b786dc2a8b","dc.title":"Bibliografie okresu Břeclav","dostupnost":"private","dc.creator":["Papírník, Miloš","Šuláková, Ludislava"]},{"PID":"uuid:826653fc-0c9b-45ca-9e7e-73d7d2f4bc88","dc.title":"Bibliografie okresu Brno-venkov","dostupnost":"private","dc.creator":["Machová, Jitka","Muzejní a vlastivědná společnost"]},{"PID":"uuid:439371f3-20be-4c1d-a281-c7c648650fe1","dc.title":"Bibliografie okresu Jeseník","dostupnost":"private","dc.creator":["Dohnalová, Emilie","Musilová, Eva"]},{"PID":"uuid:992aeaa0-1cd3-11e2-bec6-005056827e51","dc.title":"Chotěšov a jeho dominanta","dostupnost":"public"},{"PID":"uuid:f8b383bc-a664-11e1-ac9a-0050569d679d","dc.title":"A žena náhle zmizí","dostupnost":"private","dc.creator":["Dokský Jiří"]},{"PID":"uuid:81964ead-712a-4fa9-8d28-4a594008073f","dc.title":"Biographies in the borderland: preliminary results of the reserach on the biographical identity of the borderland population","dostupnost":"private","dc.creator":["Zich, František"]},{"PID":"uuid:b811a058-25a3-4b09-8356-46575cb47420","dc.title":"Archeologie nenalézaného: sborník přátel, kolegů a žáků k životnímu jubileu Slavomila Vencla","dostupnost":"private","dc.creator":["Neústupný, Evžen"]},{"PID":"uuid:c9be4a9d-44d4-4f55-a326-696d9037b725","dc.title":"Železniční vozidla : dopravní prostředky I, Uspořádání a stavba","dostupnost":"private","dc.creator":["Pohl Rudolf"]},{"PID":"uuid:ea416b82-4f20-42e5-b915-d0c134fa57c8","dc.title":"Elektrické obvody a elektronika","dostupnost":"private","dc.creator":["Uhlíř Ivan"]},{"PID":"uuid:84349d84-2f13-4091-84d5-8a9f331a5bff","dc.title":"Pravděpodobnost a statistika","dostupnost":"private","dc.creator":["Jaroš František"]},{"PID":"uuid:f0e714ca-516a-4b84-8d3f-372a259d5f0d","dc.title":"Technologie potravin II","dostupnost":"private","dc.creator":["Kadlec Pavel"]},{"PID":"uuid:b1420e47-8bef-42e3-b276-c840b8442044","dc.title":"Ocelové konstrukce 20","dostupnost":"private","dc.creator":["Studnička Jiří"]},{"PID":"uuid:ec8dcfa5-e125-4ece-9289-7aa4ccd11c9e","dc.title":"Organická chemie pro posluchače nechemických oborů","dostupnost":"private","dc.creator":["Trnka Tomáš"]},{"PID":"uuid:20564d97-a417-4c46-9394-f83619738c1d","dc.title":"Odhadování a filtrace : doplňkové skriptum","dostupnost":"private","dc.creator":["Havlena Vladimír"]},{"PID":"uuid:750c8f67-944d-4c66-b297-d7c077c5577a","dc.title":"Matematická kartografie 10","dostupnost":"private","dc.creator":["Buchar Petr"]},{"PID":"uuid:32daae6d-3395-483b-99f2-44c1fe974f01","dc.title":"Konstrukce pozemních staveb 30 : kompletační konstrukce","dostupnost":"private","dc.creator":["Hájek Václav"]},{"PID":"uuid:550c913e-9938-4e53-a3e4-07be7f2bac04","dc.title":"Stavebně historický průzkum","dostupnost":"private","dc.creator":["Kašička Bohumil"]},{"PID":"uuid:aab34329-82cd-4576-a9ac-3b7c4bac8cc6","dc.title":"Matematické základy fenomenologické termodynamiky","dostupnost":"private","dc.creator":["Nožička Jiří"]},{"PID":"uuid:58a6a3da-44c3-4113-b97f-caa49ce43b37","dc.title":"Výpočty kotlů a spalinových výměníků","dostupnost":"private","dc.creator":["Dlouhý Tomáš"]},{"PID":"uuid:ff165a56-7956-417a-9848-91192e368f04","dc.title":"Technologie potravin I","dostupnost":"private","dc.creator":["Kadlec Pavel"]},{"PID":"uuid:a19b99ca-3213-45f3-8c8a-748b276ee87a","dc.title":"Sbírka příkladů z matematiky","dostupnost":"private","dc.creator":["Míčka Jiří"]},{"PID":"uuid:72a2e017-7563-4093-bf7b-bc1a0771e07a","dc.title":"Elektronika osobních mikropočítačů","dostupnost":"private","dc.creator":["Kopecký David"]},{"PID":"uuid:535f87d1-c54a-4e89-b6fc-f027c08fc53b","dc.title":"Ekologie průmyslu","dostupnost":"private","dc.creator":["Kudláček Ivan"]},{"PID":"uuid:0650dc91-9e40-4a8d-811a-f80ec0732ec5","dc.title":"Řízení dopravy","dostupnost":"private","dc.creator":["Štůsek Jaromír"]},{"PID":"uuid:1673b5ba-1db7-4d9e-bf5b-f718c304bb1e","dc.title":"Manažerské účetnictví : sbírka úloh","dostupnost":"private","dc.creator":["Zralý Martin"]},{"PID":"uuid:8f61a7ad-eda2-46b3-8415-db0482b1d3e6","dc.title":"Ekonomika dopravních energetických systémů","dostupnost":"private","dc.creator":["Vítek Miroslav"]},{"PID":"uuid:016336f7-c1cc-415d-83cf-1d5aed73d7b3","dc.title":"Programovací jazyk : cvičení","dostupnost":"private","dc.creator":["Nešvera Šimon"]},{"PID":"uuid:4f948e6a-e1a5-4b61-8abb-b9bd074d7022","dc.title":"Optoelektronické senzory a videometrie","dostupnost":"private","dc.creator":["Fischer Jan"]},{"PID":"uuid:284cc3c1-aaac-4dbb-8602-55f97efc666c","dc.title":"Německé odborné texty pro studenty Fakulty dopravní ČVUT","dostupnost":"private","dc.creator":["Kusák Alexej"]},{"PID":"uuid:7950dfed-39d9-461d-adb6-7e2bd75491dd","dc.title":"Letadlové systémy","dostupnost":"private","dc.creator":["Věk Vratislav"]},{"PID":"uuid:5f6fb86b-e540-46fd-96f7-49b18d7be625","dc.title":"Základy mikropočítačů a úvod do programování v Assembleru","dostupnost":"private","dc.creator":["Valášek Pavel"]},{"PID":"uuid:210cb146-51b0-489f-8fa1-c1e39439171d","dc.title":"Technologie obrábění s využitím CAD/CAM systémů","dostupnost":"private","dc.creator":["Bilík Oldřich"]},{"PID":"uuid:8de207d1-607c-4cea-8779-81fa55267c56","dc.title":"Úvod do tváření II","dostupnost":"private","dc.creator":["Březina Richard"]},{"PID":"uuid:e7980ce5-d038-479d-9cec-c3c3260d5222","dc.title":"Laboratorní cvičení z fyziky 1","dostupnost":"private","dc.creator":["Nováková Danuše"]},{"PID":"uuid:21c392b4-52bb-433f-a26e-478510290295","dc.title":"Počítače pro řízení : přednášky","dostupnost":"private","dc.creator":["Bayer Jiří"]},{"PID":"uuid:a42bfc78-8a7f-43bf-8622-d49f28f2e344","dc.title":"Konstrukce a realizace elektronických obvodů","dostupnost":"private","dc.creator":["Cetl Tomáš"]},{"PID":"uuid:27f518ab-c30b-4a2c-a00a-317cdd008e00","dc.title":"Programovací jazyky","dostupnost":"private","dc.creator":["Müller Karel"]},{"PID":"uuid:4169f308-05b7-442b-bf79-a8b6884fb069","dc.title":"Sbírka příkladů z matematiky","dostupnost":"private","dc.creator":["Samková Libuše"]},{"PID":"uuid:f6dac175-649e-4c46-a4bd-2176f531da25","dc.title":"Teorie dynamických systémů : přednášky","dostupnost":"private","dc.creator":["Štecha Jan"]},{"PID":"uuid:6ade013c-e6eb-4175-9e9a-fbff598c6716","dc.title":"Nauka o materiálu : kovy a kovové materiály. 2. část","dostupnost":"private","dc.creator":["Machek Václav"]},{"PID":"uuid:b639e4af-e244-42d2-86a1-6e8e4ff1b858","dc.title":"Fyzika 2 : semináře","dostupnost":"private","dc.creator":["Kulhánek Petr"]},{"PID":"uuid:535b8a7c-a61d-41fb-b83d-4adcefbc56d7","dc.title":"Elektromechanické systémy v dopravě a ve strojírenství","dostupnost":"private","dc.creator":["Novák Jaroslav"]},{"PID":"uuid:3d241dac-a2fa-4caa-8408-c098eff6b0d4","dc.title":"Ocelové konstrukce 10","dostupnost":"private","dc.creator":["Studnička Jiří"]}]}}
	});

	K5.api.askForSolr("fl=dc.creator,dc.title,PID,dostupnost&fq=fedora.model%3Amonograph+OR+fedora.model%3Amap&q=rok:2001&start=0&rows=50", function() {});

	K5.eventsHandler.addHandler(function(type, data) {});

	setTimeout(function() {
	    ok( K5.api.isKeyReady("solr/fl=dc.creator,dc.title,PID,dostupnost&fq=fedora.model%3Amonograph+OR+fedora.model%3Amap&q=rok:2001&start=0&rows=50") , "Search key is ready" );
	    ok( K5.api.ctx.solr !== null , "Search result is not null" );
	    ok((typeof K5.api.ctx.solr === "object"), "Searching results is object");
	    var key	 = "fl=dc.creator,dc.title,PID,dostupnost&fq=fedora.model%3Amonograph+OR+fedora.model%3Amap&q=rok:2001&start=0&rows=50";			
	    ok(K5.api.ctx.solr[key] !== null, "Searching results contains data");
	    start();
	}, 1000);
	
});

