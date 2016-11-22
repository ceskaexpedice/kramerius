package cz.incad.feedrepo;

import static org.fcrepo.kernel.api.RdfLexicon.CONTAINS;
import static org.fcrepo.kernel.api.RdfLexicon.HAS_MIXIN_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.entity.StringEntity;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.impl.FedoraDatastreamImpl;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.fcrepo.kernel.api.RdfLexicon;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import cz.incad.kramerius.utils.IOUtils;

public class Main {

    public static final void readStream(FedoraDatastream dataStream) throws FedoraException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream content = dataStream.getContent();
        Long size = dataStream.getSize();
        
        IOUtils.copyStreams(content,bos);
        System.out.println(bos.size()+" X "+size);
        System.out.println();
    }
    
    public static void main(String[] args) throws FedoraException, IOException {

        FedoraRepository repo = new FedoraRepositoryImpl("http://localhost:18080/rest/");
        boolean exists = repo.exists("resource/43101770-b03b-11dd-8673-000d606f5dc6/IMG_FULL");
        System.out.println(exists);
        FedoraDatastream object =  repo.getDatastream("43101770-b03b-11dd-8673-000d606f5dc6/IMG_THUMB");
        readStream(object);
        
//        Iterator<Triple> properties2 = object.getProperties();
//        while(properties2.hasNext()) {
//            Triple next = properties2.next();
//            System.out.println(next);
//            System.out.println(next.getObject());
////            Node predicate = next.getPredicate();
////            //System.out.println(next);
////            
////            if (predicate.toString().equals("http://fedora.info/definitions/v4/repository#createdBy")) {
////                Node tripleObject = next.getObject();
////                LiteralLabel literal = tripleObject.getLiteral();
////                
////                System.out.println(literal.getValue());
////                System.out.println(literal.getDatatype());
////                //System.out.println(next.getObject());
////            }
//            
////            if (predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
////                if (next.getObject().toString().equals("http://fedora.info/definitions/v4/repository#Binary")) {
////                    System.out.println("STREAM :"+next.getSubject());
////                }
////            }
////            System.out.println(predicate.toString());
////            System.out.println(predicate.getLocalName());
////            System.out.println(predicate.getNameSpace());
////            System.out.println("indexiv val:"+predicate.getIndexingValue());
////            System.out.println();
//            //System.out.println(next);
//        }
        
        
//        FedoraDatastream dataStream =  repo.getDatastream("resource/43101770-b03b-11dd-8673-000d606f5dc6/IMG_FULL");
//        Iterator<Triple> properties = dataStream.getProperties();
//        while(properties.hasNext()) {
//            System.out.println(properties.next());
//        }
        
        
//        object.updateProperties(
//                "INSERT DATA { <> <http://fedora.info/definitions/v4/repository#mixinTypes> \"fedora:Binary\"^^xsd:string . } ");

        
        
//        FedoraObjectImpl object = (FedoraObjectImpl) repo.getObject("43101770-b03b-11dd-8673-000d606f5dc6");
//        Graph graph = object.getGraph();
//        ExtendedIterator<Triple> find = graph.find(Node.ANY, Node.ANY, Node.ANY);
//        while(find.hasNext()) {
//            Triple n = find.next();
//            System.out.println(n.getPredicate());
//        }
        
        
        
//        Collection<FedoraResource> children = object.getChildren(null);
//        System.out.println(children);

        //repo.createObject("aloha-af36-11dd-ae9c-000d606f5dc6?mixin=fedora:object");
        
//        FedoraObjectImpl object = (FedoraObjectImpl) repo.getObject("4a8a8630-af36-11dd-ae9c-000d606f5dc6");
//        object.updateProperties("PREFIX premis: <http://www.loc.gov/premis/rdf/v1#>"+
//"PREFIX image: <http://www.modeshape.org/images/1.0>"+
//"PREFIX sv: <http://www.jcp.org/jcr/sv/1.0>"+
//"PREFIX test: <info:fedora/test/>"+
//"PREFIX nt: <http://www.jcp.org/jcr/nt/1.0>"+
//"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
//"PREFIX xsi: <http://www.w3.org/2001/XMLSchema-instance>"+
//"PREFIX mode: <http://www.modeshape.org/1.0>"+
//"PREFIX xmlns: <http://www.w3.org/2000/xmlns/>"+
//"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
//"PREFIX fedora: <http://fedora.info/definitions/v4/repository#>"+
//"PREFIX xml: <http://www.w3.org/XML/1998/namespace>"+
//"PREFIX ebucore: <http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#>"+
//"PREFIX ldp: <http://www.w3.org/ns/ldp#>"+
//"PREFIX xs: <http://www.w3.org/2001/XMLSchema>"+
//"PREFIX fedoraconfig: <http://fedora.info/definitions/v4/config#>"+
//"PREFIX mix: <http://www.jcp.org/jcr/mix/1.0>"+
//"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"+
//"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
//""+
//"INSERT { "+
//"<> fedora:primaryType \"nt:Folder\" ."+
//"}"+
//"WHERE { }"+
// ")");
//        
//        
//        mixins(object.getGraph());
//        //testChildren(object.getGraph());
//        
////        FedoraDatastreamImpl datastream = (FedoraDatastreamImpl) repo.getDatastream("4a8a8630-af36-11dd-ae9c-000d606f5dc6/IMG_THUMB");
////        //System.out.println(datastream.getGraph());
////        testChildren(datastream.getGraph());
    }
    
    
    public static void mixins(Graph graph) {
        System.out.println(HAS_MIXIN_TYPE);
        ExtendedIterator<Triple> found = graph.find(Node.ANY, HAS_MIXIN_TYPE.asNode(), Node.ANY);
        while(found.hasNext()) {
            System.out.println(found.next());
        }
    }
    
    
    public static void testChildren(Graph graph) {
        Node mixinLiteral = null;
//        if ( mixin != null ) {
//            mixinLiteral = NodeFactory.createLiteral(mixin);
//        }
        final ExtendedIterator<Triple> it = graph.find(Node.ANY,RdfLexicon.CONTAINS.asNode() , Node.ANY);
        final Set<FedoraResource> set = new HashSet<FedoraResource>();
        while (it.hasNext()) {
            Triple next = it.next();
            System.out.println(next.getSubject());
            System.out.println(next.getPredicate());
            System.out.println(next.getObject());

            System.out.println(".............");
//            //graph.
//            if ( mixin == null || graph.contains(child, HAS_MIXIN_TYPE.asNode(), mixinLiteral) ) {
//                final String path = child.getURI().toString()
//                        .replaceAll(repository.getRepositoryUrl(),"");
//                if ( graph.contains(child, HAS_MIXIN_TYPE.asNode(), binaryType) ) {
//                    set.add( repository.getDatastream(path) );
//                } else {
//                    set.add( repository.getObject(path) );
//                }
//            }
        }
    }
}
