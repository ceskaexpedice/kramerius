package cz.incad.kramerius.rest.api.processes.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.processes.definition.ProcessDefinition;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;

public class SecurityProcessUtils {
    
    public static final Logger LOGGER = Logger.getLogger(SecurityProcessUtils.class.getName());
    
    private SecurityProcessUtils() {}

    // full access
    public static boolean permitManager(RightsResolver rightsResolver, User user) {
        boolean permited = user != null ? rightsResolver.isActionAllowed(user,SecuredActions.A_PROCESS_EDIT.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag() : false;
        return permited;
    }

    public static boolean permitReader(RightsResolver rightsResolver, User user) {
        boolean permited = user != null ? rightsResolver.isActionAllowed(user,SecuredActions.A_PROCESS_READ.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag() : false;
        return permited;
    }
    
    public static boolean permitProcessByDefinedAction(RightsResolver rightsResolver, User user,  ProcessDefinition def) {
        SecuredActions action = securedAction(def.getId(), def);
        boolean permited = user!= null? (rightsResolver.isActionAllowed(user,SecuredActions.A_PROCESS_EDIT.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag() ||
      (action != null && rightsResolver.isActionAllowed(user, action.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null, ObjectPidsPath.REPOSITORY_PATH).flag())) : false ;
      return permited;
    }

    public static boolean permitProcessByDefinedActionWithPid(RightsResolver rightsResolver, User user, ProcessDefinition def, String pid, ObjectPidsPath[] oPidPaths) {
        SecuredActions action = securedAction(def.getId(), def);
        for (int i = 0; i < oPidPaths.length; i++) {
            boolean permited = user!= null ?  (action != null && rightsResolver.isActionAllowed(user, action.getFormalName(), pid,null, oPidPaths[i]).flag()) : false ;
            LOGGER.log(Level.FINE,String.format("Secured action, pid, permitted:  %s,%s,%s", action.toString(), pid, ""+permited));
            if (permited) return permited;
        }
        return false;
    }

    // muze naplanovat i ten, co ma definovanou akci v lp.xml. To asi tak muze byt.
    public static SecuredActions securedAction(String def, ProcessDefinition definition) {
        return definition.getSecuredAction() != null ? SecuredActions.findByFormalName(definition.getSecuredAction()) : SecuredActions.findByFormalName(def);
    }

    public static ProcessDefinition processDefinition(ProcessDefinitionManager definitionManager, String def) {
        try {
            definitionManager.load();
            ProcessDefinition definition = definitionManager.getProcessDefinition(def);
            return definition;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    
}
