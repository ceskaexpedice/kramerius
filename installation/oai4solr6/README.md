# OAI

OAI provider implementovaný jako plugin pro index SOLR, nahrazuje defaultní implementaci OAI-PMH úložiště fedora (ta je nadále dostupná v adresáři oaiprovider).

Implementace vychází z projektu oai4solr (https://github.com/IISH/oai4solr), kde můžete najít i další detailní dokumentaci.

----

## Instalace 

1. Adresář oai zkopírujte jako podadresář do adresáře solr_home (např. ~/fedora/solr).

2. Adresář lib, obsahující knihovnu pluginu (oai2-plugin-6.1.jar) zkopírujte jako podadresář do adresáře solr_home

3. Ze souboru solrconfig.xml v adresáři conf překopírujte do svého souboru solrconfig.xml elementy requestHandler a queryResponseWriter pro oai  (rozsah řádků 1435 - 1533). Pokud jste v původním souboru solrconfig.xml neprováděli žádné změny oproti defaultní konfiguraci SOLR Krameria, můžete jej jednoduše celý nahradit tímto novým konfiguračním souborem.

4. Upravte konfigurační soubory podle popisu v následujícím odstavci "Detaily konfigurace".

5. Restartujte SOLR. OAI bude dostupné na url solr_server/solr/oai

## Detaily konfigurace

### solrconfig.xml
Mezi dvojtečky v následujícím elementu vyplňte doménu vašeho Krameria.
```
<str name="prefix">oai:kramerius.example.com:</str>
```

Následující element definuje filtr, který je přidán do SOLR query a omezuje seznam vrácených výsledků podle modelu.
```
<str name="static_query">
    fedora.model:monograph OR fedora.model:periodical OR fedora.model:periodicalitem OR fedora.model:periodicalvolume OR fedora.model:manuscript
    OR fedora.model:graphic OR fedora.model:map OR fedora.model:sheetmusic OR fedora.model:article OR fedora.model:supplement
</str>
```

Ostatní elementy standardně není potřeba upravovat. 

### oai/Identify.xml

Statický dokument obsahující odezvu na OAI příkaz Identify. Upravte jej podle potřeb vaší organizace.

### oai/ListMetadataFormats.xml a oai/ListSets.xml

Statické dokumenty obsahující odezvu na odpovídající OAI příkazy, standardně je není potřeba upravovat.

### oai/oai.xsl

Sdílená část XSLT šablon. Obsahuje parametry nutné pro komunikaci s API Krameria. Pokud je Kramerius nainstalovaný na jiném serveru, než SOLR, případně pokud používá jiný port než 8080, je třeba změnit parametr kramerius_url. Kramerius musí být dostupný ze serveru, na kterém běží SOLR. Mezi dvojtečky elemntu oai_prefix nastavte doménu vašeho Krameria.

```
  <!-- Setup Kramerius location variables -->
  <xsl:variable name="kramerius_url">http://localhost:8080/search/</xsl:variable>
  <xsl:variable name="api_point">api/v5.0/</xsl:variable>a
  <xsl:variable name="oai_prefix">oai:kramerius.example.com:</xsl:variable>
```
### oai/drkramerius4.xsl, oai/ese.xsl a oai/oai_dc.xsl
XSLT šablony pro jednotlivé metadatové formáty, standardně je není potřeba upravovat.
