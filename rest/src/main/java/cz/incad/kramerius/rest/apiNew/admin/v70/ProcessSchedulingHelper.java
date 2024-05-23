package cz.incad.kramerius.rest.apiNew.admin.v70;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.new_api.ProcessManager;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class ProcessSchedulingHelper {

    @Deprecated
    private static final String HEADER_AUTH_TOKEN = "auth-token";
    @Deprecated
    private static final String HEADER_TOKEN = "token";

    //@Inject
    //LRProcessManager lrProcessManager; //here only for scheduling

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    DefinitionManager definitionManager; //process definitions*/

    @Inject
    ProcessManager processManager;

    @Inject
    LRProcessManager lrProcessManager;  //for obtaining common synchronization lock and database connection
    // TODO: merge ProcessManagerImplDb and DatabaseProcessManager

    /**
     * @param defid
     * @param params
     * @param ownerId
     * @param ownerName
     * @param batchToken
     * @return
     */
    public LRProcess scheduleProcess(String defid, List<String> params, String ownerId, String ownerName, String batchToken, String procesName) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            String newProcessAuthToken = UUID.randomUUID().toString();
            LRProcessDefinition definition = processDefinition(defid);
            if (definition == null) {
                throw new BadRequestException("process definition for defid '%s' not found", defid);
            }

            LRProcess newProcess = definition.createNewProcess(null, batchToken);
            List<String> paramsWithAuthToken = new ArrayList<>();
            paramsWithAuthToken.add(newProcessAuthToken);
            for (String param : params) {
                paramsWithAuthToken.add(param);
            }

            newProcess.setParameters(paramsWithAuthToken);
            newProcess.setOwnerId(ownerId);
            newProcess.setOwnerName(ownerName);
            newProcess.setProcessName(procesName);

            Properties properties = definition.isInputTemplateDefined()
                    ? new Properties() //'plain' process
                    : extractPropertiesForParametrizedProcess(); //'parametrized' process
            Integer processId = newProcess.planMe(properties, getRemoteAddress());
            if (processId == null) {
                throw new InternalErrorException("error scheduling new process");
            }
            processManager.setProcessAuthToken(processId, newProcessAuthToken); //TODO: nestaci to u definition.createNewProcess?
            //lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
            return newProcess;
        } finally {
            lrProcessManager.getSynchronizingLock().unlock();
        }

    }

    private Properties extractPropertiesForParametrizedProcess() {
        //System.out.println("parametrized process");
        Properties props = new Properties();
            /*for (Iterator iterator = mapping.keys(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                try {
                    props.put(key.toString(), mapping.get(key).toString());
                } catch (JSONException e) {
                    throw new GenericApplicationException(e.getMessage());
                }
            }*/
        return props;
    }

    //TODO: proverit
    public String getRemoteAddress() {
        return IPAddressUtils.getRemoteAddress(this.requestProvider.get());
    }

    @Deprecated
    public String authToken() {
        return requestProvider.get().getHeader(HEADER_AUTH_TOKEN);
    }

    @Deprecated
    public String groupToken() {
        return requestProvider.get().getHeader(HEADER_TOKEN);
    }


    //TODO: proverit fungovani, prejmenovat
    private LRProcessDefinition processDefinition(String id) {
        definitionManager.load();
        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(id);
        return definition;
    }

    //TODO: cleanup

}
