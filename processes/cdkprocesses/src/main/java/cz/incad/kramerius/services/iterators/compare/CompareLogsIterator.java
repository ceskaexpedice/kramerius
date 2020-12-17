package cz.incad.kramerius.services.iterators.compare;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.services.iterators.ProcessIterationCallback;
import cz.incad.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONObject;
//import org.mapdb.DB;
//import org.mapdb.DBMaker;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class CompareLogsIterator implements ProcessIterator {

    public static final int DEFAULT_PAGE_SIZE = 1000;

    public static Logger LOGGER = Logger.getLogger(CompareLogsIterator.class.getName());

    private List<File> input1Files = new ArrayList<>();
    private List<File> input2Files = new ArrayList<>();

    protected boolean esType;
    protected int rows;
    private String esAddress;
    private String cacheIndex1;
    private String cacheIndex2;

    private int bulkSize = DEFAULT_PAGE_SIZE;


    public CompareLogsIterator(String address, int rows, String esAddress, boolean esType, int bulkSize) throws IOException, URISyntaxException {
        this.rows = rows;
        this.esType = esType;


        this.bulkSize = bulkSize;

        URL url = new URL(address);
        URI uri = url.toURI();
        File folder = new File(uri);

        boolean existsFlag = folder.exists();
        boolean folderFlag = folder.isDirectory();

        if (!existsFlag || !folderFlag) throw new IllegalArgumentException(String.format("item %s doesn't exist or is not folder ", address ));

        File[] files = folder.listFiles();
        if (files == null || files.length != 2) {
            throw  new IllegalArgumentException(String.format("folder %s doesnt contain two items  ", folder.getAbsolutePath() ));
        }

        input1Files.addAll(files[0].isDirectory() ? FileUtils.listFiles(files[0], new String[]{}, true) : Arrays.asList(files[0]));
        input2Files.addAll(files[1].isDirectory() ? FileUtils.listFiles(files[1], new String[]{}, true) : Arrays.asList(files[1]));

        this.esAddress = esAddress;

        this.cacheIndex1 = files[0].getName();
        this.cacheIndex2 = files[1].getName();

    }


    @Override
    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {

        try {
            processEverything(client, this.cacheIndex1 , this.input1Files, ES.INDEX);
            processEverything(client, this.cacheIndex2, this.input2Files, ES.INDEX);

            processEverything(client, this.cacheIndex1, this.input2Files, ES.DELETE);
            processEverything(client, this.cacheIndex2, this.input1Files, ES.DELETE);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private void processEverything(Client client, String indexName, List<File> inputs, ES es) throws IOException, NoSuchAlgorithmException {
        LOGGER.info("Starting");
        long start = System.currentTimeMillis();
        int counter = 0;
        List<JSONObject> page = new ArrayList<>();
        for (File input1File : inputs) {
            LineIterator lineIterator = FileUtils.lineIterator(input1File);
            while(lineIterator.hasNext()) {
                String next = lineIterator.next();
                JSONObject jsonObject = new JSONObject(next);
                jsonObject.put("source", input1File.getAbsolutePath());

                page.add(jsonObject);
                if (counter % this.bulkSize == 0 ) {
                    es.request(client,esAddress,indexName, page, this.esType);
                    page = new ArrayList<>();
                }
                if (counter % 50000 == 0) {
                    LOGGER.info(String.format("Counter is %s", counter));
                }
                counter++;
            }
        }
        if (!page.isEmpty()) {
            es.request(client,esAddress,indexName, page, this.esType);
        }
        long stop = System.currentTimeMillis();
        LOGGER.info(String.format("It took %d", stop - start));
    }


}
