package cz.incad.kramerius.gwtviewers.server.utils;

import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Barriertest {
	
	public static class Worker implements Runnable {
		
		private CyclicBarrier barrier;
		
		public Worker(CyclicBarrier barrier) {
			super();
			this.barrier = barrier;
		}


		@Override
		public void run() {
			try {
				for (int i = 0; i < 100/*00000*/; i++) {
					UUID.randomUUID();
				}
				barrier.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("After barrier");
		}
		
	}
	
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		CyclicBarrier barrier = new CyclicBarrier(2, new Runnable() {
			
			@Override
			public void run() {
				System.out.println("Bariera prolomena");
			}
		});
		new Thread(new Worker(barrier)).start();
		barrier.await();
	}
}
