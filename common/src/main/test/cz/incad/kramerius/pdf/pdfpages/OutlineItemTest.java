package cz.incad.kramerius.pdf.pdfpages;

import cz.incad.kramerius.KrameriusModels;
import junit.framework.TestCase;

public class OutlineItemTest extends TestCase {

	public void testOutline() {
		OutlineItem root = new OutlineItem();
		root.setDestination("root");
		root.setLevel(1);
		root.setTitle("root");
		
		addChild(root, root.getLevel()+1);
		StringBuffer buffer = new StringBuffer();
		root.debugInformations(buffer, root.getLevel());
		System.out.println(buffer);
		
		System.out.println("<=======================>");
		
		AbstractRenderedDocument doc = new RenderedDocument(KrameriusModels.MONOGRAPH, "uuid");
		doc.setOutlineItemRoot(root);
		
		OutlineItem left = root.copy();
		OutlineItem right = root.copy();
		
		doc.divide(left, right, "root -a-  -3-  -a- ");
		
		buffer = new StringBuffer();
		left.debugInformations(buffer, 2);
		System.out.println(buffer);
		
		buffer = new StringBuffer();
		right.debugInformations(buffer, 2);
		System.out.println(buffer);
		
		//		
//		System.out.println("-------------");
	}
	
	private void addChild(OutlineItem root, int level) {
		switch(level) {
			case 2: {
				OutlineItem first = new OutlineItem();
				first.setDestination(root.getTitle()+" -a- ");
				first.setLevel(1);
				first.setTitle(root.getTitle()+" -a- ");
				root.addChild(first);
				first.setParent(root);
				addChild(first, level+1);
				
				OutlineItem second = new OutlineItem();
				second.setDestination(root.getTitle()+" -b- ");
				second.setLevel(1);
				second.setTitle(root.getTitle()+" -b- ");
				root.addChild(second);
				second.setParent(root);
				addChild(second, level+1);

				OutlineItem third = new OutlineItem();
				third.setDestination(root.getTitle()+" -c- ");
				third.setLevel(1);
				third.setTitle(root.getTitle()+" -c- ");
				third.setParent(root);
				root.addChild(third);
				addChild(third, level+1);
			}
			break;
		case 3: {
				OutlineItem first = new OutlineItem();
				first.setDestination(root.getTitle()+" -1- ");
				first.setLevel(1);
				first.setTitle(root.getTitle()+" -1- ");
				first.setParent(root);
				root.addChild(first);
				addChild(first, level+1);
				
				OutlineItem second = new OutlineItem();
				second.setDestination(root.getTitle()+" -2- ");
				second.setLevel(1);
				second.setTitle(root.getTitle()+" -2- ");
				root.addChild(second);
				second.setParent(root);
				addChild(second, level+1);

				OutlineItem third = new OutlineItem();
				third.setDestination(root.getTitle()+" -3- ");
				third.setLevel(1);
				third.setTitle(root.getTitle()+" -3- ");
				root.addChild(third);
				third.setParent(root);
				addChild(third, level+1);
			}
			break;
		case 4: {
				OutlineItem first = new OutlineItem();
				first.setDestination(root.getTitle()+" -a- ");
				first.setLevel(1);
				first.setTitle(root.getTitle()+" -a- ");
				first.setParent(root);
				root.addChild(first);
				
				OutlineItem second = new OutlineItem();
				second.setDestination(root.getTitle()+" -b- ");
				second.setLevel(1);
				second.setTitle(root.getTitle()+" -b- ");
				second.setParent(root);
				root.addChild(second);
	
				OutlineItem third = new OutlineItem();
				third.setDestination(root.getTitle()+" -c- ");
				third.setLevel(1);
				third.setTitle(root.getTitle()+" -c- ");
				third.setParent(root);
				root.addChild(third);
			}
			break;
		}
	}
}
