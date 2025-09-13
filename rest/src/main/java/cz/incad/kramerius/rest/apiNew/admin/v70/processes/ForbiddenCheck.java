/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.rest.api.processes.utils.SecurityProcessUtils;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForbiddenCheck {
    private static Logger LOGGER = Logger.getLogger(ForbiddenCheck.class.getName());

    public static void checkGeneral(User user, RightsResolver rightsResolver) {
        boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) || SecurityProcessUtils.permitReader(rightsResolver, user);
        if (!permitted) {
            throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
        }
    }

    public static void checkByProfile(User user, RightsResolver rightsResolver, DefinitionManager definitionManager,
                                      String profileId, boolean checkReader) {
        //authorization
        /* TODO pepo - pravdepodobne pomoci pcpProcess.profileId */
        //LRProcess lrProcess = this.lrProcessManager.getLongRunningProcess(processInBatch.processUuid);
        boolean permitted = false;
        if(checkReader){
            permitted = SecurityProcessUtils.permitReader(rightsResolver, user);
        }
        if(!permitted){
            permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                    SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user, SecurityProcessUtils.processDefinition(definitionManager, profileId));
        }
        if (!permitted) {
            throw new ForbiddenException("user '%s' is not allowed to manage processes (missing action '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
        }
    }

    public static void checkByProfileAndParamsPids(User user, RightsResolver rightsResolver, DefinitionManager definitionManager,
                                                   String profileId, JSONObject params, SolrAccess solrAccess) {
        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(profileId);

        AtomicBoolean pidPermitted = new AtomicBoolean(false);
        checkParamsPids(user, rightsResolver, definitionManager, profileId, params, flag -> {
            if (flag) pidPermitted.getAndSet(true);
        }, solrAccess);

        boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user, definition) || pidPermitted.get();

        if (!permitted) {
            throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
        }
        // getProfile podle defid
        // getPlugin podle profile.pluginId
        // ziskej z PluginInfo Set<String> scheduledProfiles => pro kazdy znich podobne security jako pro main
    }

    private static void checkParamsPids(User user, RightsResolver rightsResolver, DefinitionManager definitionManager,
                                        String profileId, JSONObject params, Consumer<Boolean> consumer, SolrAccess solrAccess) {
        switch (profileId) {
            case "set_policy": {
                String pid = extractMandatoryParamWithValuePrefixed(params, "pid", "uuid:");
                try {
                    ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(pid);
                    LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("set_policy");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                    consumer.accept(permit);
                } catch (IOException e) {
                    consumer.accept(false);
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            case "remove_policy": {
                String pid = extractOptionalParamString(params, "pid", null);
                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);
                if (pid != null) {
                    try {
                        ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(pid);
                        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("set_policy");
                        boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                        consumer.accept(permit);
                    } catch (IOException e) {
                        consumer.accept(false);
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else if (pidlist != null) {
                    pidlist.stream().forEach(p -> {
                        try {
                            ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(p);
                            LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("set_policy");
                            boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, p, pidPaths);
                            consumer.accept(permit);
                        } catch (IOException e) {
                            consumer.accept(false);
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    });
                } else {
                    // musi mit prava pro cely repozitar
                    ObjectPidsPath[] pidPaths = new ObjectPidsPath[]{ObjectPidsPath.REPOSITORY_PATH};
                    LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                    consumer.accept(permit);
                }
            }
            case "processing_rebuild_for_object": {
                boolean permitProcessingIndex = user != null ? (rightsResolver.isActionAllowed(user, SecuredActions.A_REBUILD_PROCESSING_INDEX.getFormalName(),
                        SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH)).flag() : false;
                consumer.accept(permitProcessingIndex);
            }
            case "add_license":
            case "remove_license": {
                String pid = extractOptionalParamString(params, "pid", null);
                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);
                if (pid != null) {
                    try {
                        ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(pid);
                        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                        boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                        consumer.accept(permit);
                    } catch (IOException e) {
                        consumer.accept(false);
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else if (pidlist != null) {
                    pidlist.forEach(p -> {
                        try {
                            ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(p);
                            LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                            boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, p, pidPaths);
                            consumer.accept(permit);
                        } catch (IOException e) {
                            consumer.accept(false);
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    });

                } else {
                    // musi mit prava pro cely repozitar
                    ObjectPidsPath[] pidPaths = new ObjectPidsPath[]{ObjectPidsPath.REPOSITORY_PATH};
                    LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                    consumer.accept(permit);
                }
            }
            case "flag_to_license": {
                ObjectPidsPath[] pidPaths = new ObjectPidsPath[]{ObjectPidsPath.REPOSITORY_PATH};
                LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(),
                        pidPaths);
                consumer.accept(permit);
            }
            case "backup-collections": {
                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);
                if (pidlist != null) {
                    pidlist.forEach(p -> {
                        try {
                            ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(p);
                            LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                            boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, p, pidPaths);
                            consumer.accept(permit);
                        } catch (IOException e) {
                            consumer.accept(false);
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    });
                }
            }
            case "restore-collections": {
                ObjectPidsPath[] pidPaths = new ObjectPidsPath[]{
                        ObjectPidsPath.REPOSITORY_PATH
                };
                LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                consumer.accept(permit);
            }
            case "migrate-collections-from-k5": {
                ObjectPidsPath[] pidPaths = new ObjectPidsPath[]{ObjectPidsPath.REPOSITORY_PATH};
                LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("add_license");
                boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                consumer.accept(permit);
            }
            case "delete_tree": {
                String pid = extractMandatoryParamWithValuePrefixed(params, "pid", "uuid:");
                try {
                    ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(pid);
                    LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition("delete_tree");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                    consumer.accept(permit);
                } catch (IOException e) {
                    consumer.accept(false);
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            default: {
            }
        }
    }

    private static String extractMandatoryParamWithValuePrefixed(JSONObject params, String paramName, String prefix) {
        String value = extractOptionalParamString(params, paramName, null);
        if (value == null) {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        } else {
            if (!value.toLowerCase().startsWith(prefix)) {
                throw new BadRequestException("invalid value of %s (doesn't start with '%s'): '%s'", paramName, prefix, value);
            }
            return value;
        }
    }

    private static List<String> extractOptionalParamStringList(JSONObject params, String paramName, List<String> defaultValue) {
        if (params.has(paramName)) {
            if (!(params.get(paramName) instanceof JSONArray)) {
                throw new BadRequestException("mandatory parameter %s is not array", paramName);
            }
            JSONArray jsonArray = params.getJSONArray(paramName);
            List<String> result = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i));
            }
            return result;
        } else {
            return defaultValue;
        }
    }

    private static String extractOptionalParamString(JSONObject params, String paramName, String defaultValue) {
        return params.has(paramName) ? params.getString(paramName) : defaultValue;
    }


}
