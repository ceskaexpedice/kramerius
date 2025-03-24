/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.utils;

import cz.incad.kramerius.utils.conf.KConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FedoraUtils {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FedoraUtils.class.getName());
    public static final String RELS_EXT_STREAM = "RELS-EXT";
    public static final String IMG_THUMB_STREAM = "IMG_THUMB";
    public static final String IMG_FULL_STREAM = "IMG_FULL";
    public static final String IMG_PREVIEW_STREAM = "IMG_PREVIEW";
    public static final String ALTO_STREAM = "ALTO";
    public static final String DC_STREAM = "DC";
    public static final String BIBLIO_MODS_STREAM = "BIBLIO_MODS";
    public static final String TEXT_OCR_STREAM = "TEXT_OCR";
    public static final String MP3_STREAM = "MP3";
    public static final String OGG_STREAM = "OGG";
    public static final String WAV_STREAM = "WAV";

    public static final String MIGRATION_STREAM = "MIGRATION";
    
    public static final String POLICY_STREAM="POLICY";
    
    
    public static List<String> DEFAULT_SECURED_STREAM = new ArrayList<String>(){{
        
        add(IMG_FULL_STREAM);
        add(IMG_PREVIEW_STREAM);

        add(TEXT_OCR_STREAM);
        add(ALTO_STREAM);

        add(MP3_STREAM);
        add(WAV_STREAM);
        add(OGG_STREAM);
        
        add(ALTO_STREAM);
        
    }};

    public static List<String> getSecuredStreams() {
        String[] securedStreamsExtension = KConfiguration.getInstance().getSecuredAditionalStreams();
        List<String> retvals = new ArrayList<>(DEFAULT_SECURED_STREAM);
        Arrays.stream(securedStreamsExtension).forEach(retvals::add);
        return retvals;
    }

    public static List<String> INTERNAL_STREAM = new ArrayList<String>(){{
       add(RELS_EXT_STREAM);
       add(IMG_THUMB_STREAM);
       add(IMG_FULL_STREAM);
       add(IMG_PREVIEW_STREAM);
       add(ALTO_STREAM);
       add(DC_STREAM);
       add(BIBLIO_MODS_STREAM);
    }};

    public static List<String> AUDIO_STREAMS = new ArrayList<String>(){{
        add(OGG_STREAM);
        add(MP3_STREAM);
        add(WAV_STREAM);
     }};

    /** Stream for fedora internal use */
    public static List<String> FEDORA_INTERNAL_STREAMS = new ArrayList<String>(){{
        //add(RELS_EXT_STREAM);
        add(POLICY_STREAM);
     }};


    public static final String RELS_EXT_FORMAT_URI = "info:fedora/fedora-system:FedoraRELSExt-1.0";
    public static final String BIBLIO_MODS_FORMAT_URI = "http://www.loc.gov/mods/v3";
    public static final String DC_FORMAT_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";

    public static final int THUMBNAIL_HEIGHT = 128;
    public static final int PREVIEW_HEIGHT = 700;

}
