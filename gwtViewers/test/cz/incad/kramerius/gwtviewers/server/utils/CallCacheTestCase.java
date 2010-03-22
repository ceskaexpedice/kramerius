package cz.incad.kramerius.gwtviewers.server.utils;

import java.util.Random;
import java.util.UUID;


import cz.incad.kramerius.gwtviewers.server.utils.CallCache;

import junit.framework.TestCase;

public class CallCacheTestCase extends TestCase {

	public void testCallCache() {
		String uuid = ""+System.currentTimeMillis();
		String valueA = ClassOne.getMethodA(uuid);
		String ident = CallCache.makeIdent(ClassOne.class.getName(), "methodA", uuid);
//		Assert.assertFalse(CallCache.isInCache(ident));
		CallCache.cacheValue(ident, valueA);
//		Assert.assertTrue(CallCache.isInCache(ident));
//		Assert.assertEquals(CallCache.valueFromCache(ident), valueA);
	}

	public void testCacheMiss() {
		String uuid = ""+System.currentTimeMillis();
		String valueA = ClassOne.getMethodA(uuid);
		String identA = CallCache.makeIdent(ClassOne.class.getName(), "getMethodA", uuid);
		CallCache.cacheValue(identA, valueA);
//		Assert.assertTrue(CallCache.isInCache(identA));
//		Assert.assertEquals(CallCache.valueFromCache(identA), valueA);


		for (int i = 0; i < CallCache.MAX_CALL; i++) {
			cacheRandomB();
//			Assert.assertTrue(CallCache.isInCache(identA));
		}
		// dalsi 
		cacheRandomB();
//		Assert.assertFalse(CallCache.isInCache(identA));
	}

	private void cacheRandomB() {
		String randomUUID = UUID.randomUUID().toString();
		String identB = CallCache.makeIdent(ClassOne.class.getName(), "getMethodB", randomUUID);
		String valueB = ClassOne.getMethodB(randomUUID);
//		Assert.assertFalse(CallCache.isInCache(identB));
		CallCache.cacheValue(identB, valueB);
//		Assert.assertTrue(CallCache.isInCache(identB));
	}

}
