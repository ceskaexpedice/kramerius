package cz.kramerius.searchIndex.indexer.conversions;

import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Deprecated
public class DateParser {

    DateFormat df;

    // TODO: 2019-08-21 tohle udělat jinak, vůbec se mi nelíbí flow parsování

    //iso date, tj. i pro rocnik, periodikum
    //pro razeni
    private Date datum;

    //originalni str
    //k zobrazeni v karte u vysledku
    private String datum_str;


    //jen rocniky pres jeden cely rok, pro vicelete ne 0 a null
    private String rok;//integer

    //iso, obecne pro vsechny typy
    //Date date_start
    //Date date_end


    //jen pro periodika, rocniky (dulezite pro ty napric roky)
    private String datum_begin;//int year_begin
    private String datum_end;//int year_end


    public DateParser(Element biblioMods) {
        //df = new SimpleDateFormat(config.getProperty("mods.date.format", "dd.MM.yyyy"));
        df = new SimpleDateFormat("dd.MM.yyyy");
        process(biblioMods);
    }

    public Date getDatum() {
        return datum;
    }

    public String getDatum_str() {
        return datum_str;
    }

    public String getRok() {
        return rok;
    }

    public String getDatum_begin() {
        return datum_begin;
    }

    public String getDatum_end() {
        return datum_end;
    }

    /*
    @see cz.incad.kramerius.indexer.ExtendedFields.setDate()
     */
    private void process(Element biblioMods) /*throws Exception*/ {
        String[] xpaths = {
                "mods/part/date/text()",
                //"mods:mods/mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()",
                "mods/originInfo/dateIssued/text()"};//posledni xpath obsahne i veci, na ktere se chytne predposledni
        //podle DMF Monografie:
        //https://www.ndk.cz/standardy-digitalizace/dmf_monografie_1-3-2
        //titul vicesvazkove monografie
        //nic

        //ignorovat ?  a []


        //svazek monografie
        //mods:mods/mods:originInfo/mods:dateIssued (datum vydani predlohy)
        //nepovinne atributy @encoding, @point(start, end), @qualifier(approximate)
        // a
        //mods:mods/mods:originInfo/mods:dateCreated (datum vydani predlohy pro rukopisy)
        // a
        //mods:mods/mods:originInfo/mods:dateOther (datum vytvoreni, distribuce, vyroby predlohy)
        //nepovinny atribut @type (productin, distribution, manufacture)
        //a mozna
        //mods:mods/mods:originInfo/mods:copyrightDate

        //kartograficke dokumenty, hudebniny,
        //mods:mods/mods:originInfo/mods:dateIssued (datum vydani predlohy)
        //nepovinne atributy @encoding, @point(start, end), @qualifier(approximate)
        // a
        //mods:mods/mods:originInfo/mods:dateOther (datum vytvoreni, distribuce, vyroby predlohy)
        //nepovinny atribut @type (productin, distribution, manufacture)
        //a mozna
        //mods:mods/mods:originInfo/mods:copyrightDate

        //vnitrni cast (kapitola, obraz, mapa)
        //nic

        //priloha mohografie
        //mods:mods/mods:originInfo/mods:dateIssued (datum vydani)
        //format podle katalogizacniho zaznamu? nebo DD.MM.RRRR, RRRR, MM.RRRR, DD.-DD.MM.RRRR, MM.-MM.RRRR
        //nepovinny atribut @qualifier (approximate)
        //a
        //mods:mods/mods:originInfo/mods:dateOther (datum vytvoreni, distribuce, vyroby)
        //povinny atribut @type (productin, distribution, manufacture)
        //nepovinny atribut @qualifier(approximate)
        //a
        //mods:mods/mods:originInfo/mods:dateCreated (datum vytvoreni, bude pouzito pri popisu tiskare?)
        //a mozna
        //mods:mods/mods:originInfo/mods:copyrightDate

        //strana
        //nic

        //nejake dalsi datumy byvaji v recordInfo, ale to se tyka metadatoveho zaznamu, tady irelevantni


        //podle DMF Periodika
        //https://www.ndk.cz/standardy-digitalizace/dmf_periodika_1-7-1_opravena_verze_rijen2018

        //titul periodika
        //mods:mods/mods:originInfo/mods:dateIssued (datum vydani)
        //1920-1921, 19uu-198x apod.,  odpovídá hodnotě z katalogizačního záznamu, pole 260 $c a polí 008/07-10 a 008/11-14
        //povinny atribut @encoding (marc), @point(start, end), @qualifier(approximate)
        //a mozna
        //mods:mods/mods:originInfo/mods:dateOther (datum vytvoreni, distribuce, vyroby predlohy)
        //a
        //mods:mods/mods:originInfo/mods:dateCreated (datum vydani pro rukopisy)

        //rocnik periodika
        //mods:mods/mods:originInfo/mods:dateIssued (datum vydani)
        //rok, nebo rozsah let
        //nepovinne atributy @point(start, end), @qualifier(approximate)

        //cislo periodika
        //mods:mods/mods:originInfo/mods:dateIssued (datum vydani)
        //format obecne whatever "vyplňuje se ručně, dle předlohy"
        //nepovinne atributy @point(start, end), @qualifier(approximate)

        //vnitrni cast periodika (clanek, obraz, mapa, apod)
        //nic

        //priloha periodika
        //mods:mods/mods:originInfo/mods:dateIssued (datum vydani)
        //format obecne whatever
        //nepovinny atribut @qualifier(approximate)
        //a
        //mods:mods/mods:originInfo/mods:dateCreated (datum vytvoreni prilohy)
        //format obecne whatever
        //nepovinny atribut @qualifier(approximate)

        //strana periodika
        //nic

        for (String xpath : xpaths) {
            List<Node> nodes = Dom4jUtils.buildXpath(xpath).selectNodes(biblioMods);
            for (Node node : nodes) {
                datum_str = node.getStringValue().trim();
                parseDatum(datum_str);
            }
        }
    }

    // TODO: 2019-08-21 prejmenovat, opravit parsovani, idealne bez vedlejsich efektu
    private void parseDatum(String datumStr) {
        DateFormat outformatter = new SimpleDateFormat("yyyy");
        try {
            Date dateValue = df.parse(datumStr);
            rok = outformatter.format(dateValue);
            datum = dateValue;
        } catch (Exception e) {
            if (datumStr.matches("\\d\\d\\d\\d")) { //rok
                rok = datumStr;
                datum_begin = rok;
                datum_end = rok;
            } else if (datumStr.matches("\\d\\d--")) {  //Datum muze byt typu 18--
                datum_begin = datumStr.substring(0, 2) + "00";
                datum_end = datumStr.substring(0, 2) + "99";
            } else if (datumStr.matches("\\d\\d-\\d\\d\\.\\d\\d\\d\\d")) {  //Datum muze byt typu 11-12.1946
                rok = datumStr.split("\\.")[1].trim();
            } else if (datumStr.matches("\\d\\d\\.-\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d")) {  //Datum muze byt typu 19.-20.03.1890

                String end = datumStr.split("-")[1].trim();
                try {
                    Date dateValue = df.parse(end);
                    rok = outformatter.format(dateValue);
                    datum = dateValue;
                } catch (Exception ex) {
                    System.err.println("Cant parse date " + datumStr);
                    //logger.log(Level.FINE, "Cant parse date " + datumStr);
                }
            } else if (datumStr.matches("\\d---")) {  //Datum muze byt typu 187-
                datum_begin = datumStr.substring(0, 3) + "0";
                datum_end = datumStr.substring(0, 3) + "9";
            } else if (datumStr.matches("\\d\\d\\d\\d[\\s]*-[\\s]*\\d\\d\\d\\d")) {  //Datum muze byt typu 1906 - 1945
                String begin = datumStr.split("-")[0].trim();
                String end = datumStr.split("-")[1].trim();
                datum_begin = begin;
                datum_end = end;
            } else {
                System.err.println("Cant parse date " + datumStr);
                //logger.log(Level.FINE, "Cant parse date " + datumStr);
                /*try {
                    DatesParser p = new DatesParser(new DateLexer(new StringReader(datumStr)));
                    Date parsed = p.dates();
                    rok = outformatter.format(parsed);
                    datum = parsed;
                } catch (RecognitionException ex) {
                    logger.log(Level.FINE, "Cant parse date " + datumStr);
                } catch (TokenStreamException ex) {
                    logger.log(Level.FINE, "Cant parse date " + datumStr);
                }*/
            }
        }
    }


}
