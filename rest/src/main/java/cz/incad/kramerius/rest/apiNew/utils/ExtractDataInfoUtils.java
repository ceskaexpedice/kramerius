package cz.incad.kramerius.rest.apiNew.utils;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.json.JSONObject;

public class ExtractDataInfoUtils {

    private ExtractDataInfoUtils() {}

    public static JSONObject extractAvailableDataInfo(AkubraRepository akubraRepository, String pid) {
        JSONObject dataAvailable = new JSONObject();
        //metadata
        JSONObject metadata = new JSONObject();
        metadata.put("mods", akubraRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_MODS));
        metadata.put("dc", akubraRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_DC));
        dataAvailable.put("metadata", metadata);
        JSONObject ocr = new JSONObject();
        //ocr
        ocr.put("text", akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_TEXT));
        ocr.put("alto", akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_ALTO));
        dataAvailable.put("ocr", ocr);
        //images
        JSONObject image = new JSONObject();
        image.put("full", akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL));
        image.put("thumb", akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_THUMB));
        image.put("preview", akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW));
        dataAvailable.put("image", image);
        //audio
        JSONObject audio = new JSONObject();
        audio.put("mp3", akubraRepository.datastreamExists(pid, KnownDatastreams.AUDIO_MP3));
        audio.put("ogg", akubraRepository.datastreamExists(pid, KnownDatastreams.AUDIO_OGG));
        audio.put("wav", akubraRepository.datastreamExists(pid, KnownDatastreams.AUDIO_WAV));
        dataAvailable.put("audio", audio);
        return dataAvailable;
    }

    public static boolean isChacheDirDisabledAndFromCache(boolean chacheDirDisable, String tilesUrl) {
        return chacheDirDisable && "kramerius4://deepZoomCache".equals(tilesUrl);
    }

    public static Object extractImageSourceInfo(AkubraRepository akubraRepository, String pid) {
        JSONObject json = new JSONObject();
        String tilesUrl = akubraRepository.re().getTilesUrl(pid);
        boolean chacheDirDisable = KConfiguration.getInstance().getConfiguration().getBoolean("deepZoom.cachedir.disable", false);
        if (tilesUrl != null && (!isChacheDirDisabledAndFromCache(chacheDirDisable, tilesUrl))) {
            json.put("type", "tiles");
        } else if (!akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
            json.put("type", "none");
        } else {
            String imgFullMimetype = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();
            if (imgFullMimetype == null) {
                json.put("type", "none");
            } else {
                if (ImageMimeType.JPEG2000.getValue().equals(imgFullMimetype)) {
                    // convert to jpeg
                    json.put("type", ImageMimeType.JPEG.getValue());
                } else  if (ImageMimeType.DJVU.getValue().equals(imgFullMimetype) || ImageMimeType.VNDDJVU.getValue().equals(imgFullMimetype) || ImageMimeType.XDJVU.getValue().equals(imgFullMimetype)) {
                    json.put("type", ImageMimeType.JPEG.getValue());
                } else {
                    // transform jp2 or djvu
                    //jpeg, pdf, etc.
                    json.put("type", imgFullMimetype);
                }
            }
        }
        return json;
    }
}
