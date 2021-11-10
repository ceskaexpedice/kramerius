package indexer;

import cz.kramerius.searchIndex.indexer.conversions.extraction.AudioAnalyzer;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AudioAnalyzerTest {
    private AudioAnalyzer analyzer = new AudioAnalyzer();
    private File samplesDir = new File("src/test/resources/extraction/audio");

    private void analyzeWav(String filename, int expectedLength) throws IOException, UnsupportedAudioFileException {
        System.out.println(new File(".").getAbsoluteFile());
        File wavFile = new File(samplesDir, filename);
        System.out.println(wavFile.getAbsoluteFile());
        assertTrue(wavFile.exists());
        assertEquals(expectedLength, analyzer.analyze(wavFile, AudioAnalyzer.Format.WAV).duration);
    }

    @Test
    public void analyzeWav_1() throws IOException, UnsupportedAudioFileException {
        analyzeWav("file_example_WAV_1MG.wav", 34);
    }

    @Test
    public void analyzeWav_2() throws IOException, UnsupportedAudioFileException {
        analyzeWav("file_example_WAV_2MG.wav", 34);
    }

    @Test
    public void analyzeWav_3() throws IOException, UnsupportedAudioFileException {
        analyzeWav("file_example_WAV_5MG.wav", 30);
    }

    @Test
    public void analyzeWav_4() throws IOException, UnsupportedAudioFileException {
        analyzeWav("file_example_WAV_10MG.wav", 59);
    }

    /*
    private void analyzeMp3(String filename, int expectedLength) throws IOException, UnsupportedAudioFileException {
        File mp3File = new File(samplesDir, filename);
        assertTrue(mp3File.exists());
        assertEquals(expectedLength, analyzer.analyze(mp3File, AudioAnalyzer.Format.MP3).duration);
    }

    @Test
    public void analyzeMp3_1() throws IOException, UnsupportedAudioFileException {
        analyzeMp3("file_example_MP3_700KB.mp3", 27);
    }

    @Test
    public void analyzeMp3_2() throws IOException, UnsupportedAudioFileException {
        analyzeMp3("file_example_MP3_1MG.mp3", 27);
    }

    @Test
    public void analyzeMp3_3() throws IOException, UnsupportedAudioFileException {
        analyzeMp3("file_example_MP3_2MG.mp3", 53);
    }

    @Test
    public void analyzeMp3_4() throws IOException, UnsupportedAudioFileException {
        analyzeMp3("file_example_MP3_5MG.mp3", 172);
    }
    */


    /*
    private void analyzeOgg(String filename, int expectedLength) throws IOException, UnsupportedAudioFileException {
        File oggFile = new File(samplesDir, filename);
        assertTrue(oggFile.exists());
        assertEquals(expectedLength, analyzer.analyze(oggFile, AudioAnalyzer.Format.OGG).duration);
    }

    @Test
    public void analyzeOgg_1() throws IOException, UnsupportedAudioFileException {
        analyzeOgg("file_example_OOG_1MG.ogg", 74);
    }

    @Test
    public void analyzeOgg_2() throws IOException, UnsupportedAudioFileException {
        analyzeOgg("file_example_OOG_2MG.ogg", 74);
    }

    @Test
    public void analyzeOgg_3() throws IOException, UnsupportedAudioFileException {
        analyzeOgg("file_example_OOG_5MG.ogg", 81);
    }
    */
}
