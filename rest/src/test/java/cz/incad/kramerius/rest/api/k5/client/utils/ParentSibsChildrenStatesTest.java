package cz.incad.kramerius.rest.api.k5.client.utils;

import static cz.incad.kramerius.fedora.impl.DataPrepare.narodniListyRelsExt;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class ParentSibsChildrenStatesTest extends TestCase {

    public void testProcessTree() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException {
//        final Map<String, Integer> mapping = new HashMap<String, Integer>();
//        
//        final List<Integer> order = new ArrayList<Integer>();
//
        StatisticsAccessLog aclog = EasyMock.createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),aclog)
        .addMockedMethod("getRelsExt")
        .createMock();
        
        narodniListyRelsExt(fa);
        
        replay(fa,aclog);
        
//        // volume
//        "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
//
//        // item - cisla
//        "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
//        "uuid:983a4660-938d-11dc-913a-000d606f5dc6",
//        "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
        Document relsExt = fa.getRelsExt("uuid:ae876087-435d-11dd-b505-00145e5790ea");
        System.out.println(relsExt);
        //StateTreeNodeProcessor st = new StateTreeNodeProcessor(ParentSibsChildrenStates.START, "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6","uuid:983a4660-938d-11dc-913a-000d606f5dc6");
        
//        ItemTreeNode node = null;
//        MStates state = MStates.START;
//        
//        ObjectPidsPath path  = new ObjectPidsPath("A","B","C","D");
//        while(!path.isEmptyPath()) {
//        	state  = state.nextState();
//        	
//        	String[] pathFromRootToLeaf = path.getPathFromRootToLeaf();
//			String pid = pathFromRootToLeaf[path.getLength()-1];
//
//			ItemTreeNode parent = new ItemTreeNode(pid, "-model-");
//        	
//			Map<String, Object> options = new HashMap<String, Object>(); {
//        		options.put("PID", pid);
//        		options.put("NODE", parent);
//        	}
//
//			
//			System.out.println("PID "+pid+" state "+state);
//        	state.processState(null, null);
//			
//			if (node != null) parent.addItemTreeNode(node);
//			node = parent;
//	    	path = path.cutTail(0);
//        }
//        System.out.println(node);
//        

//		String[] pathFromRootToLeaf = selPath.getPathFromRootToLeaf();
//
//		
//		ItemTreeNode rootNode = new ItemTreeNode(pid, this.fedoraAccess.getKrameriusModelName(pid));
//		
//		ChildrenNodeProcessor chproc = new ChildrenNodeProcessor();
//		this.fedoraAccess.processSubtree(pid, chproc);
//		for (String chpid : chproc.getChildren()) {
//			ItemTreeNode itemchpid = new ItemTreeNode(chpid, this.fedoraAccess.getKrameriusModelName(chpid));
//			rootNode.addItemTreeNode(itemchpid);
//		}
//		
//		
//		if (pathFromRootToLeaf.length >= 2) {
//			String parentPid = pathFromRootToLeaf[pathFromRootToLeaf.length-2];
//			ItemTreeNode parentNode = new ItemTreeNode(parentPid, this.fedoraAccess.getKrameriusModelName(parentPid));
//			chproc = new ChildrenNodeProcessor();
//    		this.fedoraAccess.processSubtree(parentPid, chproc);
//    		for (String mySibs : chproc.getChildren()) {
//    			if (mySibs.equals(pid)) {
//    				parentNode.addItemTreeNode(rootNode);
//    			} else  {
//    				parentNode.addItemTreeNode(new ItemTreeNode(mySibs, this.fedoraAccess.getKrameriusModelName(mySibs)));
//    			}
//			}
//		}
		

        
        
//        long start = System.currentTimeMillis();
//        fa.processSubtree("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6", rootChildren);
//        System.out.println("takes "+(System.currentTimeMillis() - start));
//        System.out.println(rootChildren.getChildren());
//        
//        
//
//        fa.processSubtree("uuid:ae876087-435d-11dd-b505-00145e5790ea", new TreeNodeProcessor() {
//            
//            @Override
//            public void process(String pid, int level) throws ProcessSubtreeException {
//                mapping.put(pid, new Integer(level));
//                order.add(new Integer(level));
//            }
//
//
//            @Override
//            public boolean skipBranch(String pid, int level) {
//                return  (level >= 3) ? true: false;
//            }
//
//            @Override
//            public boolean breakProcessing(String pid, int level) {
//                return  false;
//            }
//        });
//
//
//        Assert.assertEquals(Arrays.asList(new Integer(0),new Integer(1), new Integer(2),new Integer(2),new Integer(2)), order);
//        Assert.assertEquals(new Integer(0),mapping.get("uuid:ae876087-435d-11dd-b505-00145e5790ea"));
//        Assert.assertEquals(new Integer(1),mapping.get("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6"));
//        Assert.assertEquals(new Integer(2), mapping.get("uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6"));
//        Assert.assertEquals(new Integer(2), mapping.get("uuid:983a4660-938d-11dc-913a-000d606f5dc6"));
//        Assert.assertEquals(new Integer(2),mapping.get("uuid:53255e00-938a-11dc-8b44-000d606f5dc6"));
    }

}
