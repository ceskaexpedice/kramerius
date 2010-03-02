package cz.incad.kramerius.processes;

import java.util.concurrent.locks.ReentrantLock;

import java.util.concurrent.locks.Lock;

public class LockTest {

	public static boolean FINISHED = false;
	
	public static class TThread extends Thread {
		private Lock lock;

		
		public TThread(Lock lock) {
			super();
			this.lock = lock;
		}

		@Override
		public void run() {
			lock.lock();
			try {
				pocitej();
				FINISHED = true;
			} finally {
				lock.unlock();
			}
		}
		
		public void pocitej() {
			for(int i=0;i<4000000;i++) {
				int j = i+1;
				if ((j % 1000000 ) == 0) {
					System.out.println("Pocitam jak zbesily");
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws InterruptedException {
		Lock lock = new ReentrantLock();
		new TThread(lock).start();
		try {
			while(!FINISHED) {
				lock.tryLock();
			}
		} finally {
			lock.unlock();
		}
		System.out.println("Test");
	}
}
