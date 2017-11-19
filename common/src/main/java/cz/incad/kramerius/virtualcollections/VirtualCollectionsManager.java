/*
 * Copyright (C) 2011 Alberto Hernandez
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
package cz.incad.kramerius.virtualcollections;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;

import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VirtualCollectionsManager {

    static final Logger logger = Logger.getLogger(VirtualCollectionsManager.class.getName());

    static final String SPARQL_NS = "http://www.w3.org/2001/sw/DataAccess/rf1/result";
    static final String TEXT_DS_PREFIX = "TEXT_";



    public static void main(String[] args) throws Exception {

        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        logger.log(Level.INFO, "process args: {0}", Arrays.toString(args));
        String action = args[0];
        String pid = args[1];
        String collection = args[2];

        if (action.equals("remove")) {
            ProcessStarter.updateName("Remove " + pid + " from collection " + collection);
            CollectionUtils.removeFromCollection(pid, collection, fa);
            CollectionUtils.startIndexer(pid, "fromKrameriusModel", "Reindex doc " + pid);
        } else if (action.equals("add")) {
            ProcessStarter.updateName("Add " + pid + " to collection " + collection);
            CollectionUtils.addToCollection(pid, collection, fa);
            CollectionUtils.startIndexer(pid, "fromKrameriusModel", "Reindex doc " + pid);
        } else if (action.equals("removecollection")) {
            ProcessStarter.updateName("Remove collection " + collection);
            CollectionUtils.delete(collection, fa);
        } else {
            logger.log(Level.INFO, "Unsupported action: {0}", action);
            return;
        }

        logger.log(Level.INFO, "Finished");

    }
}
