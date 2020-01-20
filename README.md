###Novinky


Aktuální distribuční verze je k dispozici v sekci Releases na projektovém serveru GitHub (https://github.com/ceskaexpedice/kramerius/releases/latest)

Zapojení do České digitální knihovny je možné od verze Kramerius 5.

----

#Systém Kramerius 

je softwarové řešení pro zpřístupnění digitálních dokumentů. Primárně je určen pro digitalizované knihovní sbírky, monografie a periodika. Využit může být ke zpřístupnění dalších typů dokumentů např. map, hudebnin a starých tisků, případně částí dokumentů jako jsou články a kapitoly. Systém je vhodný také pro tzv. digital born dokumenty, tedy dokumenty, které vznikly v elektronické podobě. Kramerius je průběžně upravován tak, aby struktura metadat odpovídala standardům vyhlašovaným Národní knihovnou České republiky. Systém poskytuje rozhraní pro přístup koncových uživatelů, zajišťující vyhledávání v metadatech a v plných textech, generování vícestránkových PDF dokumentů z vybraných stran, vytváření virtuálních sbírek a další operace nad uloženou sbírkou digitálních dokumentů.

Aktuální ostrá verze 5 vychází koncepčně z verze 4, která byla vyvíjena a průběžně publikována od roku 2009. Navazuje funkčností na předchozí verzi systému Kramerius končící označením 4.8.6. Od verze 4 je jako jádro systému použit open source repozitář [Fedora](http://www.fedora-commons.org). Při vývoji jsou využívány další volně dostupné technologie třetích stran - Apache, Apache Tomcat, Apache Solr, Postgres SQL. Systém je založen na technologii Java a lze ho provozovat jako samostatnou webovou aplikaci v libovolném J2EE kontejneru (např. Apache Tomcat).

V testování je verze 6, ve které došlo k významnému vývojové mu kroku zejména z pohledu využitého repozitáře. Nové řešení jádra by mělo zajistit rychlejší práci s velkým množstvím objektů. Ostré nasazení je předpokládáno v průběhu roku 2019.

Uživatelské rozhraní je přístupné ve většině současných webových prohlížečů, vývoj a testování probíhá na aktuálních verzích prohlížečů Google Chrome, Firefox a Safari, uživatelská část rozhraní je funkční i v současných verzích prohlížeče Internet Explorer, který však není doporučován, vzhledem k tomu, že nepodporuje standardy.

##Licence

Kramerius je open source systém, který je vyvíjen pod licencí GNU GPL v3. http://www.gnu.org/licenses/gpl-3.0.en.html

##Vývojový tým

Vývojový tým tvoří zaměstnanci Knihovny AV ČR, Národní knihovny ČR, Moravské zemské knihovny v Brně, Národní technické knihovny, Národní lékařské knihovny, Městské knihovny v Praze a Severočeské vědecké knihovny v Ústí nad Labem. Technologickým partnerem jsou společnosti Galderon a INOVATIKA.

Členové vývojového týmu:
KNAV - M. Lhoták, M. Duda, J. Křížová; 
NK ČR – T. Foltýn, Z. Vozár, V. Jiroušek, K. Košťálová; 
MZK – M. Smetánková, P. Žabička, M. Indrák; 
NTK – J. Kolátor, J. Dobiášovský; 
NLK – F. Kříž;
SVKUL - A. Brožek;
MKP - M. Světlý;

Zástupci programátorského týmu – P. Kocourek, P. Šťastný, V. Lahoda;

Koordinátorem vývoje je Knihovna Akademie věd ČR zastoupená Ing. Martinem Lhotákem.

Kontakt:
Ing. Martin Lhoták,
Knihovna AV ČR, v. v. i.,
Národní 3, 115 22 Praha 1

lhotak@knav.cz



##Financování

V současné době je financování vývoje zajištěno zejména z grantů MK ČR prostřednictvím dotačních programů NAKI (projekt RightLib 2018-2022) a VISK (individuální roční projekty). Od roku 2019 je vývoj provázán i s rozvojem výzkumné infrastruktury LINDAT/CLARIAH-CZ umístěné na národní cestovní mapě velkých výzkumných infrastruktur. 

V letech 2012 - 2015 bylo financování vývoje zajištěno díky projektu "Česká digitální knihovna a nástroje pro zajištění komplexních digitalizačních procesů" - DF12P01OVV002 z Programu aplikovaného výzkumu a vývoje národní a kulturní identity (NAKI) Ministerstva kultury ČR

V předchozích letech byl vývoj systému Kramerius průběžně financován z různých dotačních programů Akademie věd ČR a Ministerstva kultury ČR. Počátek vývoje se datuje do roku 2003, kdy byl vývoj iniciován Národní knihovnou ČR ve spolupráci s Knihovnou AV ČR. 


##Instalace
Instalační balík je k dispozici v sekci Releases na adrese https://github.com/ceskaexpedice/kramerius/releases/latest . 

Kompletní dokumentace k aktuální verzi je v sekci [Wiki](https://github.com/ceskaexpedice/kramerius/wiki).

[Instalační postup a konfigurace systému](https://github.com/ceskaexpedice/kramerius/wiki/Instalace) jsou popsány na Wiki.

Službu instalace lze také objednat na http://www.unidata.cz/system-kramerius

Distribuovanou instalaci u společnosti Incad, která zajišťuje analytické a programátorské práce http://www.incad.cz

##Komunikace
Hlášení o chybách a požadavky na novou funkcionalitu zadávejte pomocí formuláře New Issue v sekci Issues. 

Při požadavku na přidání vlastní funkcionality do standardní distribuce systému Kramerius prosím kontaktujte administrátory projektu. Jednodušší změny v rámci existujících modulů bude možné řešit připravením pull requestu, složitější úpravy bude třeba řešit individuálně.


Mailová konference pro administrátory systému Kramerius: kramerius@lib.cas.cz

První příhlášení do konference: https://mailman.lib.cas.cz/mailman/listinfo/kramerius

Do mailové konference je možné posílat hlášení chyb, návrhy na vylepšení a vývoj i na přispění ke zdrojovému kódu Krameria.


##Podpora systému Kramerius

Systém Kramerius je open source řešení. SW podporu poskytuje vývojový tým. Pro garantované termíny musí mít instituce uzavřenu samostatnou smlouvu o podpoře.

SPRÁVA SYSTÉMU

Správa a dohled instalovaného systému může být zajištěna na základě individuální dohody např. od http://www.unidata.cz/ nebo http://www.inovatika.cz

Instalaci a správu systému mohou, vzhledem k licenci pod kterou je systém vyvíjen, zajišťovat i jiné subjekty.

[![Join the chat at https://gitter.im/ceskaexpedice/kramerius](https://badges.gitter.im/ceskaexpedice/kramerius.svg)](https://gitter.im/ceskaexpedice/kramerius?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

