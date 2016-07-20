###OAI

OAI provider postaven jako plugin do SOLRu

----

##Instalace 

Adresar oai kopirovat do adresare solr_home (napr ~/fedora/solr)
Adresar lib, obsahujici knihovnu pluginu (oai2-plugin-3.1.jar) musi byt dostupny SOLRu. Pri defaultni instalaci kopirovat do solr_home 

Soubor solrconfig.xml v adresari conf nahradi aktualni solrconfig.xml pokud mate vychozi konfigurace.

Restartovat SOLR

##Konfigurace

Do konfiguraci SOLRu pribude nove requestHandler a queryResponseWriter: oai (v defaultni konfiguraci uplne dole)
solrconfig.xml


Toto je filter pridan do SOLR query, ktery omezuje seznam vracenych vysledku podle modelu
```
<str name="static_query">
    fedora.model:monograph OR fedora.model:periodical OR fedora.model:periodicalitem OR fedora.model:periodicalvolume OR fedora.model:manuscript
    OR fedora.model:graphic OR fedora.model:map OR fedora.model:sheetmusic OR fedora.model:article OR fedora.model:supplement
</str>
```

Parametry nutne pro komunikaci s Kramerius. Kramerius musi byt dostupny pro server, v kterem bezi SOLR.
```
<lst name="xslt_parameters">
  <str name="kramerius_url">http://localhost:8080/search/</str>
  <str name="api_point">api/v5.0/</str>
</lst>
```