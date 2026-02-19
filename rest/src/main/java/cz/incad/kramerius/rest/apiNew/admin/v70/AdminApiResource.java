package cz.incad.kramerius.rest.apiNew.admin.v70;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.client.ProcessManagerMapper;
import cz.incad.kramerius.workmode.ReadOnlyWorkModeException;
import cz.incad.kramerius.workmode.WorkMode;
import cz.incad.kramerius.workmode.WorkModeReason;
import cz.incad.kramerius.workmode.WorkModeService;
import cz.incad.kramerius.rest.apiNew.ApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.UnauthorizedException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import org.ceskaexpedice.akubra.DistributedLocksException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public abstract class AdminApiResource extends ApiResource {

    public static Logger LOGGER = Logger.getLogger(AdminApiResource.class.getName());

    public static final String HEADER_PARENT_PROCESS_AUTH_TOKEN = "parent-process-auth-token";

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<User> userProvider;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    SearchIndexHelper searchIndexHelper;

    @Inject
    @Named("dbWorkMode")
    WorkModeService workModeService;

    protected final void checkReadOnlyWorkMode() {
        WorkMode workMode = workModeService.getWorkMode();
        if (workMode != null && workMode.isReadOnly()) {
            throw new ReadOnlyWorkModeException();
        }
    }

    private String inputstreamToString(InputStream in) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            return content.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private List<String> commaSeparatedItemsToList(String commaSeparated) {
        List<String> result = new ArrayList<>();
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return result;
        }
        String[] items = commaSeparated.split(",");
        for (String item : items) {
            result.add(item.trim());
        }
        return result;
    }

    public String getParentProcessAuthToken() {
        return requestProvider.get().getHeader(HEADER_PARENT_PROCESS_AUTH_TOKEN);
    }

    @Deprecated
    public String findLoggedUserKey() {
        //TODO: otestovat, nebo zmenit
        userProvider.get(); //TODO: neni uplne zrejme, proc tohle volat. Co se deje v AbstractLoggedUserProvider a LoggedUsersSingletonImpl vypada zmatecne
        return (String) requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
    }

    @Deprecated
    public void checkCurrentUserByJsessionidIsAllowedToPerformGlobalSecuredAction(SecuredActions action) {
        User user = this.userProvider.get();
        if (user == null || user.getLoginname().equals("not_logged")) {
            throw new UnauthorizedException(); //401
        } else {
            boolean allowed = this.rightsResolver.isActionAllowed(user, action.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag();
            if (!allowed) {
                throw new ForbiddenException("user '%s' is not allowed to perform global action '%s'", user.getLoginname(), action.getFormalName()); //403
            }
        }
    }

    protected JSONObject getScheduleReindexationPar(String objectPid, String userid, String indexationType, boolean ignoreInconsistentObjects, String title) {
        String processName = title != null
                ? String.format("Reindexace %s (%s, typ %s)", title, objectPid, indexationType)
                : String.format("Reindexace %s (typ %s)", objectPid, indexationType);
        Map<String, String> payload = new HashMap<>();
        payload.put("pid", objectPid);
        payload.put("title", processName);
        payload.put("type", indexationType);
        payload.put("ignoreInconsistentObjects", Boolean.toString(ignoreInconsistentObjects));
        JSONObject scheduleMainProcess = createScheduleProcess("new_indexer_index_object", payload, userid);
        return scheduleMainProcess;
    }

    private static JSONObject createScheduleProcess(String profileId, Map<String, String> payload, String ownerId) {
        JSONObject json = new JSONObject();
        json.put(ProcessManagerMapper.PCP_PROFILE_ID, profileId);
        json.put(ProcessManagerMapper.PCP_PAYLOAD, new JSONObject(payload));
        json.put(ProcessManagerMapper.PCP_OWNER_ID_SCH, ownerId);
        return json;
    }

    protected void deleteFromSearchIndex(String pid) throws IOException {
        this.searchIndexHelper.deleteFromIndex(pid);
    }

    protected void handleWorkMode(DistributedLocksException dle) {
        if (!dle.getCode().equals(DistributedLocksException.LOCK_TIMEOUT)) {
            workModeService.setWorkMode(new WorkMode(true, WorkModeReason.distributedLocksException));
        }
    }


}
