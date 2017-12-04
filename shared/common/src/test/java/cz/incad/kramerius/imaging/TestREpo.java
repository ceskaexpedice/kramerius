package cz.incad.kramerius.imaging;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.solr.client.solrj.SolrServerException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestREpo {

    public static void main(String[] args) throws RepositoryException, TransformerException, IOException, SolrServerException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        FedoraAccess fedoraAccess = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        // TODO: transaction !!
        //Repository repo = fedoraAccess.getTransactionAwareInternalAPI();
        Repository repo = fedoraAccess.getInternalAPI();
        System.out.println("TEST");
        //removeOneRelation(repo);
        //removeRelations(repo);
        //removeRelationsByNs(repo);

//        List<Triple<String, String, String>> relations = repo.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46").getRelations(FedoraNamespaces.KRAMERIUS_URI);
//        System.out.println(relations);

        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        final List<Integer> alist =new ArrayList<>();
        feeder.iterateProcessing((doc)->{
            alist.add(alist.size());
        });
        System.out.println(alist);
    }


    private static void removeRelations(Repository repo) throws RepositoryException {
        RepositoryObject object = repo.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.removeRelationsByNameAndNamespace("hasPage",FedoraNamespaces.KRAMERIUS_URI);
    }

    private static void removeOneRelation(Repository repo) throws RepositoryException {
        RepositoryObject object = repo.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.removeRelation("hasPage", FedoraNamespaces.KRAMERIUS_URI, "uuid:ed4765c8-20c4-45c9-b809-4933afceb6b1");
    }

    private static void removeRelationsByNs(Repository repo) throws RepositoryException {
        RepositoryObject object = repo.getObject("uuid:5035a48a-5e2e-486c-8127-2fa650842e46");
        object.removeRelationsByNamespace(FedoraNamespaces.KRAMERIUS_URI);

    }
}
