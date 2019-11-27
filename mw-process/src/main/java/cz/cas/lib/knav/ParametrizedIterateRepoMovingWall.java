package cz.cas.lib.knav;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.IterateNonParametrized.SolrRepoItemsSupport;
import cz.incad.kramerius.processes.impl.RepositoryItemsSupport;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedIterateRepoMovingWall {

    @Process
    public static void process(@ParameterName("userValue") String uVal, @ParameterName("mode") String mode) throws XPathExpressionException, IOException, RightCriteriumException {
        List<String> topLevelModels = KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels");
        RepositoryItemsSupport repoItems = new SolrRepoItemsSupport("PID");
        for (String m : topLevelModels) {
            List<String> pids = repoItems.findPidsByModel(m);
            for (int i = 0; i < pids.size(); i++) {
                String p = pids.get(i);
                String [] processArgs = new String[] {uVal, mode, p};
                ProcessUtils.startProcess("applymw", processArgs);
            }
        }
    }    
}
