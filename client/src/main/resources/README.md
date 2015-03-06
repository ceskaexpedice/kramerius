#Kramerius 5 - client
Kramerius 5 přichází s možností využití API. Zároveň pro procházení a čtení dokumentů byl vyvinut nový klient. 
Jedná se o čistě webovou aplikaci složenou na straně serveru pomocí šablonovacího systému Velocity template, na straně klienta se jedná o čistý javascript. 

##Popis řešení klienta
Základním bodem na straně klienta je aplikační singleton, který zaštiťuje přístup k jednotlivým částem aplikace. 

 - `K5`  - Aplikace {@link Application}
 - `K5.api`  - Přístup k api {@link ClientAPIDev}
 - `K5.i18n` - Internacionalizace {@link I18N}
 - `K5.gui`  - Informace o zobrazeni, vybrana komponeta k zobrazeni atd..  {@link ItemSupport}, {@link Zoomify}, {@link AudioView} 
 - `K5.eventsHandler`  - Přístup k událostem   {@link ApplicationEvents}

##Výčet událostí

 Jednoltivé Části aplikace pro komunikaci využívají. Zde je přehled základních: 

 * `application/init/start` - Spouští se při inicializaci aplikačního sigletonu. Jedná se start aplikace. 
 * `application/init/end` - Aplikace inicializována 
 * `api/item/<pid>` - Přišla data o objektu. 
 * `application/menu/ctxchanged` - Přišel požadavek na přeuspořádání menu. 
 * `i18n/dictionary` - Přišel požadavek na změnu lokalizace. 

## Extensibilita

### Vlastní skript
Rozšíření chování, případně změnu, je možné docílit přidáním vlastního skritpu a editace deskriptoru `jsfiles.def`. Deskritor je ve formátu json pole. Vlastní skript je pak nutno umístit na cestu `~/.m2/k5client/ext/js/<name.js>`.

Příklad `~/.m2/k5client/ext/nfile.js`:
```
[

        "resources/js/nfile.js"
]
```

Příklad `~/.m2/k5client/ext/js/nfile.js`:
```
K5.eventsHandler.addHandler(function(type,data) {
    if (type === "application/init/end") {
        alert("application initialized");
    } 
});

```




### Vlastní CCS styl
Stejným způsobem je možno měnit css styly. Vlastní styl může ležet na cestě  `~/.m2/k5client/ext/css/<name.css>`. Zároveň je nutno editovat deskriptor `cssfiles.def`.  

Původní 
Příklad změny barvy. Vlastní soubor  `~/.m2/k5client/ext/css/styles.css` s tímto obsahem:
```
/*
#set( 
    $params = {
        "inactiveColor"         : "red",
        "activeColor"           : "green"
    })
*/
```


### Změna loga
Logo je možné měnit uživatelskou proměnnou k5.logo. Musí ukazovat na url loga. 

### Změna textů a 

##Vlastní zobrazení konkrétního typu 

##Přidání položky pro download 


##Vlastní styly
Instalační balík je k dispozici v sekci Releases na adrese https://github.com/ceskaexpedice/kramerius/releases/latest . Starší verze Krameria 4 vydané před přesunutím vývoje na GitHub jsou stále k dispozici na původním umístění na Google Code (https://code.google.com/p/kramerius/downloads/list)

##Změna loga

##Změna textů  


Kompletní dokumentace k aktuální verzi je v sekci Wiki.

