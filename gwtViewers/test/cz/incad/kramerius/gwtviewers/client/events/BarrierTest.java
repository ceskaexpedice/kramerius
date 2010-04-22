package cz.incad.kramerius.gwtviewers.client.events;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class BarrierTest extends TestCase {

	public void testBarrier() {
		final List<Integer> list = new ArrayList<Integer>();
		Barrier barr = new Barrier(4, new BarrierAction() {
			
			@Override
			public void action() {
				System.out.println("Barrier action");
				list.clear();
			}
		});
		barr.bound();
		list.add(1);
		barr.bound();
		list.add(1);
		barr.bound();
		list.add(1);
		barr.bound();
		assertTrue(list.isEmpty());
	}
}
