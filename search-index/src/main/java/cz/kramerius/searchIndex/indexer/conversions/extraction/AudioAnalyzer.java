package cz.kramerius.searchIndex.indexer.conversions.extraction;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

public class AudioAnalyzer {
    public enum Format {
        WAV, MP3, OGG;
    }

    public static class Result {
        /* description of audio format (bitrate etc.)*/
        public String format;
        /* duration of audio in whole seconds (ceiling, i.e. 2.001 -> 3) */
        public int duration;
    }

    public Result analyze(InputStream data, Format format) throws UnsupportedAudioFileException, IOException {
        switch (format) {
            case WAV:
                return analyzeWav(data);
            case MP3:
                //return analyzeMp3(data);
            case OGG:
                //return analyzeOgg(data);
            default:
                throw new RuntimeException("not implemented");
        }
    }

    public Result analyze(File file, Format format) throws IOException, UnsupportedAudioFileException {
        return analyze(new FileInputStream(file), format);
    }

    private Result analyzeWav(InputStream data) throws UnsupportedAudioFileException, IOException {
        //System.out.println("analyzeWav START: used memory (MB): " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
        try (
                InputStream is = new BufferedInputStream(data);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is);
        ) {
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            float duration = (frames / format.getFrameRate());
            int durationInWholeSeconds = (int) Math.ceil(duration);
            //System.out.println("duration: " + duration);
            Result result = new Result();
            result.duration = durationInWholeSeconds;
            result.format = format.toString();
            //System.out.println("analyzeWav FINISH: used memory (MB): " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
            return result;
        }
    }

    /*private Result analyzeMp3(InputStream data) throws UnsupportedAudioFileException, IOException {
        InputStream inMemoryWav = convertMp3ToWav(data);
        return analyzeWav(inMemoryWav);
    }*/

    /*private Result analyzeOgg(InputStream data) throws UnsupportedAudioFileException, IOException {
        InputStream inMemoryWav = convertMp3ToWav(data);
        return analyzeWav(inMemoryWav);
    }*/
}
