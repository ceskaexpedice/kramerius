package cz.incad.kramerius.rest.apiNew.admin.v70;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
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

    /**
     * @param defid
     * @param params
     * @param ownerId
     * @param ownerName
     * @param batchToken
     * @return
     */
    public LRProcess scheduleProcess(String defid, List<String> params, String ownerId, String ownerName, String batchToken, String procesName) {
        String newProcessAuthToken = UUID.randomUUID().toString();
        LRProcessDefinition definition = processDefinition(defid);
        if (definition == null) {
            throw new BadRequestException("process definition for defid '%s' not found", defid);
        }
        //String authToken = authToken(); //jen pro ilustraci, jak funguje stare api a jak se jmenovala hlavicka
        //String groupToken = groupToken(); //jen pro ilustraci, jak funguje stare api a jak se jmenovala hlavicka
        //groupToken = batchToken;
        //System.out.println("groupToken: " + groupToken);

        LRProcess newProcess = definition.createNewProcess(null, batchToken);
        //System.out.println("newProcess: " + newProcess);
        //tohle vypada, ze se je k nicemu, ve vysledku se to jen uklada do databaze do processes.params_mapping a to ani ne vzdy
        // select planned, params_mapping from processes where params_mapping!='' order by planned desc limit 10;
        //newProcess.setLoggedUserKey(loggedUserKey);

        //process auth token is passed as a param, because running process may need to schedule another process
        List<String> paramsWithAuthToken = new ArrayList<>();
        paramsWithAuthToken.add(newProcessAuthToken);
        for (String param : params) {
            paramsWithAuthToken.add(param);
        }

        newProcess.setParameters(paramsWithAuthToken);
        //newProcess.setUser(user);
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
        return IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
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
