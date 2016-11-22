/*
 * Copyright (C) 2016 Pavel Stastny
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

package cz.incad.feedrepo.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import cz.incad.feedrepo.RepoAbstraction;
import cz.incad.feedrepo.impl.fc4.F4Repo;
import cz.incad.feedrepo.impl.jackrabbit.JackRabbitRepo;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Create repository abstraction
 * @author pavels
 */
public class RepositoryAbstractionFactory {
    
    public static Map<String, String> INSTANCE_MAPPER = new HashMap<String, String>();
    public static final String REPO_KEY = RepositoryAbstractionFactory.class.getName();
    
    static {
        INSTANCE_MAPPER.put("jackrabbit", JackRabbitRepo.class.getName());
        INSTANCE_MAPPER.put("fcrepo4", F4Repo.class.getName());
    }
    
    public static synchronized final RepoAbstraction getRepoInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String prop = System.getProperty(REPO_KEY);
        if (prop == null) {
           String instance = KConfiguration.getInstance().getConfiguration().getString("fedora.implementation");
           if (INSTANCE_MAPPER.containsKey(instance)) {
               return _instance(INSTANCE_MAPPER.get(instance));
           } throw new InstantiationException("No implementation ! ");
        } else {
            return _instance(prop);
        }
    }

    /**
     * @param prop
     * @return
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private static RepoAbstraction _instance(String prop) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<RepoAbstraction> forName = (Class<RepoAbstraction>) Class.forName(prop);
        return forName.newInstance();
    }
}
