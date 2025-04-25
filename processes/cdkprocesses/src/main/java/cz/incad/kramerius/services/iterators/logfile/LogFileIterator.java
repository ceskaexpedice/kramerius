package cz.incad.kramerius.services.iterators.logfile;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.ProcessIterationCallback;
import cz.incad.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.utils.IterationUtils;
import cz.incad.kramerius.timestamps.TimestampStore;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LogFileIterator implements ProcessIterator{


    protected String address;
    protected int rows;

    public LogFileIterator(String address, int rows) {
        this.rows = rows;
        this.address = address;
    }

    @Override
    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        iterate(iterationCallback, endCallback);
    }

    private void iterate(ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            URL url = new URL(this.address);
            InputStream is = url.openStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            List<String> pids = new ArrayList<>();
            while((line=bufferedReader.readLine()) != null) {
                JSONObject object = new JSONObject(line);
                String pid = object.getString("pid");
                pids.add(pid);
                if (pids.size() >= rows) {
                    iterationCallback.call(IterationUtils.pidsToIterationItem(this.address, pids));
                    pids = new ArrayList<>();
                }
            }
            if (!pids.isEmpty()) iterationCallback.call(IterationUtils.pidsToIterationItem(this.address,pids));
            endCallback.end();
        } catch (IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void iterate(CloseableHttpClient client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        iterate(iterationCallback, endCallback);
    }
}
