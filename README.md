![Build](https://img.shields.io/github/actions/workflow/status/ceskaexpedice/kramerius/gradle-push.yml?branch=master)



## Novinky 

Aktuální distribuční verze je k dispozici v sekci Releases na projektovém serveru GitHub (https://github.com/ceskaexpedice/kramerius/releases/latest) 

# Kramerius system

is a digital library open source software solution (GNU GPL v3) primarily designated for digitized library collections, monographs and periodicals. It can be used also for other types of documents such as maps, music sheets and old prints or parts of documents such as articles and chapters. The system is also suitable for digital born documents, i.e. documents that were created in electronic form. Kramerius is continuously developed so that the metadata structure corresponds to the standards announced by the National Library of the Czech Republic.

Main development coordinator: Martin Lhoták, Library of the Czech Academy of Sciences lhotak@knav.cz

# Systém Kramerius 

je softwarové řešení pro zpřístupnění digitálních dokumentů. Primárně je určen pro digitalizované knihovní sbírky, monografie a periodika. Využit může být ke zpřístupnění dalších typů dokumentů např. map, hudebnin a starých tisků, případně částí dokumentů jako jsou články a kapitoly. Systém je vhodný také pro tzv. digital born dokumenty, tedy dokumenty, které vznikly v elektronické podobě. Kramerius je průběžně upravován tak, aby struktura metadat odpovídala standardům vyhlašovaným Národní knihovnou České republiky. Systém poskytuje rozhraní pro přístup koncových uživatelů, zajišťující vyhledávání v metadatech a v plných textech, generování vícestránkových PDF dokumentů z vybraných stran, vytváření virtuálních sbírek a další operace nad uloženou sbírkou digitálních dokumentů.

Aktuální verze 7 vychází koncepčně z předchozích verzí 4 a 5, které byly vyvíjeny a zveřejňovány od roku 2009. Při vývoji jsou využívány další volně dostupné technologie třetích stran - Apache, Apache Tomcat, Apache Solr, Postgres SQL. Pro práci s JPEG 2000 je využíván software Kakadu. Systém je založen na technologii Java a lze ho provozovat jako samostatnou webovou aplikaci v libovolném J2EE kontejneru (např. Apache Tomcat).

Uživatelské rozhraní je přístupné ve většině současných webových prohlížečů, vývoj a testování probíhá na aktuálních verzích prohlížečů Google Chrome, Firefox a Safari, uživatelská část rozhraní je funkční i v současných verzích prohlížeče Internet Explorer, který však není doporučován, vzhledem k tomu, že nepodporuje standardy. Rozhranní je vícejazyčné.

## Související moduly
* [Kramerius Journals](https://github.com/ceskaexpedice/K5Journals) Prostředí pro správu a publikování vědeckých časopisů.
* [Klient pro zvukové dokumenty](https://github.com/ceskaexpedice/kramerius-music-frontend) Univerzální přehrávací modul, který prostřednictvím API systému Kramerius.
* [Mobilní klient pro iOS](https://github.com/ceskaexpedice/kramerius/wiki/Aplikace-pro-iOS) Klient pro mobilní zařízení s iOS, který zpřístupňuje obsah digitálních knihoven prostřednictvím portálu Digitalniknihovna.cz.
* [Mobilní klient pro Android](https://github.com/ceskaexpedice/kramerius/wiki/Aplikace-pro-Android) Klient pro mobilní zařízení s OS Android, který zpřístupňuje obsah digitálních knihoven prostřednictvím portálu Digitalniknihovna.cz.

## Licence

Kramerius je open source systém, který je vyvíjen pod licencí GNU GPL v3. http://www.gnu.org/licenses/gpl-3.0.en.html

## Vývojový tým

Vývojový tým tvoří zaměstnanci Knihovny AV ČR, Národní knihovny ČR, Moravské zemské knihovny v Brně, Národní technické knihovny, Národní lékařské knihovny a Městské knihovny v Praze. Technologickými partnery jsou společnosti Galderon, INOVATIKA a Trinera.

Členové vývojového týmu:
KNAV - M. Lhoták, M. Duda, F. Kersch; 
NK ČR – P. Burdová, J. Hrzinová, V. Jiroušek, K. Košťálová; 
MZK – M. Dvořáková, P. Žabička, F. Jebavý; 
NTK - T. Lysoněk
NLK – F. Kříž;
KUK - A. Brožek;
MKP - M. Světlý;
Programátorský tým: P.Kocourek, V.Lahoda, P.Šťastný, J.Rychtář

Koordinátorem vývoje je Knihovna Akademie věd ČR zastoupená Ing. Martinem Lhotákem.

Kontakt:
Ing. Martin Lhoták,
Knihovna AV ČR, v. v. i.,
Národní 3, 115 22 Praha 1

lhotak@knav.cz


## Financování

Financování vývoje je zajištěno zejména z grantů MK ČR prostřednictvím dotačních programů NAKI, VISK a NPO. Od roku 2019 je vývoj provázán i s rozvojem výzkumné infrastruktury LINDAT/CLARIAH-CZ umístěné na národní cestovní mapě velkých výzkumných infrastruktur. 


## Komunikace
Hlášení o chybách a požadavky na novou funkcionalitu zadávejte pomocí formuláře New Issue v sekci Issues. 

Při požadavku na přidání vlastní funkcionality do standardní distribuce systému Kramerius prosím kontaktujte administrátory projektu. Jednodušší změny v rámci existujících modulů bude možné řešit připravením pull requestu, složitější úpravy bude třeba řešit individuálně.

Mailová konference pro administrátory systému Kramerius: kramerius@lib.cas.cz

První příhlášení do konference: https://mailman.lib.cas.cz/mailman/listinfo/kramerius

Do mailové konference je možné posílat hlášení chyb, návrhy na vylepšení a vývoj i na přispění ke zdrojovému kódu Krameria.

##Instalace
Instalační balík je k dispozici v sekci Releases na adrese https://github.com/ceskaexpedice/kramerius/releases/latest . 

Kompletní dokumentace k aktuální verzi je v sekci [Wiki](https://github.com/ceskaexpedice/kramerius/wiki).

[Instalační postup a konfigurace systému](https://github.com/ceskaexpedice/kramerius/wiki/Instalace) jsou popsány na Wiki.

## Podpora systému Kramerius

Systém Kramerius je open source řešení. SW podporu poskytuje vývojový tým. 
Pro garantované termíny musí mít instituce uzavřenu samostatnou smlouvu o podpoře.

INSTALACE A SPRÁVA SYSTÉMU

Instalace a správa instalovaného systému může být zajištěna na základě individuální dohody.
Instalaci a správu systému mohou, vzhledem k licenci pod kterou je systém vyvíjen, zajišťovat i jiné subjekty.

Technologickými partnery jsou společnosti [Galderon](mailto:kontakt@galderon.cz), [INOVATIKA](https://inovatika.cz/) a [Trinera](https://www.trinera.cz/)





[![Join the chat at https://gitter.im/ceskaexpedice/kramerius](https://badges.gitter.im/ceskaexpedice/kramerius.svg)](https://gitter.im/ceskaexpedice/kramerius?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

