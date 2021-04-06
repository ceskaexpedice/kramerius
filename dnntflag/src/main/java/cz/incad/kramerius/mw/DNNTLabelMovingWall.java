package cz.incad.kramerius.mw;

import com.sun.jersey.api.client.Client;
import cz.cas.lib.knav.ProcessCriteriumContext;
import cz.incad.kramerius.AbstractDNNTProcess;
import cz.incad.kramerius.DNNTExport;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.csv.DDNTCSVLabeledFlag;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.impl.criteria.MovingWall;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.services.MigrationUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.workers.DNNTLabeledWrokerFlag;
import cz.incad.kramerius.workers.DNNTWorker;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DNNTLabelMovingWall extends AbstractDNNTProcess {

    public static final Logger LOGGER = Logger.getLogger(DNNTLabelMovingWall.class.getName());

    private String label;
    private String model;
    private String years;


    @Override
    protected DNNTWorker createWorker(String pid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        return new DNNTLabeledWrokerFlag(pid, fedoraAccess, client, this.label, flag);
    }

    @Override
    protected void initializeFromArgs(String[] args) throws IOException {
        if (args.length < 3) throw new IllegalArgumentException("At least one argument");
        this.addRemoveFlag = true;
        this.model = args[0];
        this.label = args[1];
        this.years = args[2];
    }

    @Override
    public void process(String[] args) throws IOException, BrokenBarrierException, InterruptedException {
        initializeFromArgs(args);
        iterateOverModelAndRepo();
    }

    private void iterateOverModelAndRepo() throws IOException, InterruptedException, BrokenBarrierException {
        List<String> allSelectedPids = new ArrayList<>();

        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        SolrAccess sa = new SolrAccessImpl();

        Client client = Client.create();
        String q = String.format("model_path:\"%s/*\" NOT fedora.model:\"page\" NOT dnnt-labels:\"%s\"", model, this.label);

        try {
            IterationUtils.cursorIteration(client, KConfiguration.getInstance().getSolrHost() ,  URLEncoder.encode(q,"UTF-8"),(em, i) -> {
                List<String> pp = MigrationUtils.findAllPids(em);
                if (!pp.isEmpty()) {

                    pp.stream().forEach(onePid-> {
                        ProcessCriteriumContext ctx = new ProcessCriteriumContext(onePid, fa, sa);
                        MovingWall mw = new MovingWall();
                        mw.setEvaluateContext(ctx);
                        int wall = -1;
                        if (this.years != null) {
                            try {
                                wall = Integer.parseInt(this.years);
                            } catch (NumberFormatException e) {
                                LOGGER.severe("Cannot parse user value");
                                LOGGER.severe(e.getMessage());
                                return;
                            }
                        }

                        if (wall != -1) {
                            try {
                                LOGGER.info("Used value is: " + wall);
                                mw.setCriteriumParamValues(new Object[] { "" + wall });
                                EvaluatingResultState result = mw.evalute();
                                if (result == EvaluatingResultState.TRUE) {
                                    allSelectedPids.add(onePid);
                                }
                            } catch (RightCriteriumException e) {
                                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                            }
                        }
                    });
                }
            }, ()->{});

            final List<DNNTWorker> dnntWorkers = new ArrayList<>();
            for (String pid :  allSelectedPids) {
                if (dnntWorkers.size() >= numberofThreads) {
                    startWorkers(dnntWorkers);
                    dnntWorkers.clear();
                    dnntWorkers.add(createWorker(pid, fa, client, addRemoveFlag));
                } else {
                    dnntWorkers.add(createWorker(pid, fa, client, addRemoveFlag));
                }
            }
            if (!dnntWorkers.isEmpty()) {
                startWorkers(dnntWorkers);
                dnntWorkers.clear();
            }
            this.commit(client);

        } catch (ParserConfigurationException | MigrateSolrIndexException | SAXException e ) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException, IOException {
        new DNNTLabelMovingWall().process(args);
    }
}
