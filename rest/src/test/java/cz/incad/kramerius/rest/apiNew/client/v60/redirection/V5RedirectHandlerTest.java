package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.utils.pid.LexerException;
import junit.framework.TestCase;


public class V5RedirectHandlerTest {
	
	
//	@Ignore @Test
//	public void testExtractAvailableDataInfo() throws IOException, JSONException, LexerException {
//		String string = IOUtils.toString(V5RedirectHandlerTest.class.getResourceAsStream("article.streams.json"), Charset.forName("UTF-8"));
//		V5RedirectHandler redirect = new V5RedirectHandler(null,null, "knav", "uuid:1");
//		JSONObject info = redirect.extractAvailableDataInfo(new JSONObject(string));
//
//		Assert.assertTrue(info.has("image"));
//		Assert.assertTrue(info.has("metadata"));
//		Assert.assertTrue(info.has("audio"));
//		Assert.assertTrue(info.has("ocr"));
//
//		Assert.assertTrue(info.getJSONObject("image").has("thumb"));
//		Assert.assertTrue(info.getJSONObject("image").getBoolean("thumb"));
//		Assert.assertTrue(info.getJSONObject("image").has("preview"));
//		Assert.assertTrue(info.getJSONObject("image").getBoolean("preview"));
//		Assert.assertTrue(info.getJSONObject("image").has("full"));
//		Assert.assertTrue(info.getJSONObject("image").getBoolean("full"));
//		
//		Assert.assertTrue(info.getJSONObject("metadata").has("mods"));
//		Assert.assertTrue(info.getJSONObject("metadata").getBoolean("mods"));
//		Assert.assertTrue(info.getJSONObject("metadata").has("dc"));
//		Assert.assertTrue(info.getJSONObject("metadata").getBoolean("dc"));
//
//		Assert.assertTrue(info.getJSONObject("audio").has("mp3"));
//		Assert.assertFalse(info.getJSONObject("audio").getBoolean("mp3"));
//		Assert.assertTrue(info.getJSONObject("audio").has("wav"));
//		Assert.assertFalse(info.getJSONObject("audio").getBoolean("wav"));
//		Assert.assertTrue(info.getJSONObject("audio").has("ogg"));
//		Assert.assertFalse(info.getJSONObject("audio").getBoolean("ogg"));
//
//	}
//
//	@Ignore @Test
//	public void testPageArticleInfo() throws IOException, JSONException, LexerException {
//		String string = IOUtils.toString(V5RedirectHandlerTest.class.getResourceAsStream("article/k5/page_article_58ddb459-d295-11e1-8393-0050569d679d.json"), Charset.forName("UTF-8"));
//		V5RedirectHandler redirect = new V5RedirectHandler(null, null, "knav", "uuid:1");
//		JSONObject struct = redirect.extractStructureInfo(new JSONObject(string));
//		Assert.assertTrue(struct.has("model"));
//		Assert.assertEquals(struct.getString("model"),"page");
//
//		Assert.assertTrue(struct.has("parent"));
//		Assert.assertTrue(struct.getJSONObject("parent").has("own"));
//		Assert.assertTrue(struct.getJSONObject("parent").getJSONObject("own").optString("pid").equals("uuid:a1c53cdc-0031-467f-bfdc-76b824735862"));
//		Assert.assertTrue(struct.getJSONObject("parent").getJSONObject("own").optString("relation").equals("hasPage"));
//	}
//
//
//	@Test
//	public void testArticleInfo() throws IOException, JSONException, LexerException {
//		String string = IOUtils.toString(V5RedirectHandlerTest.class.getResourceAsStream("article/k5/article_fe1191ba-98a7-4f21-b201-3c147fedc88e.json"), Charset.forName("UTF-8"));
//		V5RedirectHandler redirect = new V5RedirectHandler(null, null, "knav", "uuid:1");
//		JSONObject struct = redirect.extractStructureInfo(new JSONObject(string));
//
//		System.out.println(struct.toString(3));
//	}
	
	
//	@Test
//	public void testImageInfo() throws ProxyHandlerException {
//		Client client = Client.create();
//		V5RedirectHandler redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "knav", "uuid:851bc7f4-c2f1-48ec-9e14-7f8d03c7dc0e");
//		Response infoImage = redirect.infoImage();
//		
//		System.out.println(">> ..  ");
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "knav", "uuid:65df172d-a171-458d-936e-1d91cba589d0");
//		
//		// mzkpage uuid:8d7d9774-d24d-4628-bbd3-80beae376bac
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:8d7d9774-d24d-4628-bbd3-80beae376bac");
//		//redirect.info();
//		
//		// mzk uuid:8ba4c36b-473d-4012-a640-b6810b0a1d85
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:8ba4c36b-473d-4012-a640-b6810b0a1d85");
//		//redirect.info();
//		
//		// mzk uuid:afcf99a0-3a31-11ed-afb8-5ef3fc9bb22f
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:afcf99a0-3a31-11ed-afb8-5ef3fc9bb22f");
//		//redirect.info();
//		
//		// mzk article uuid:a10f06d6-9d72-41ec-af22-c2902e4fdb43
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:a10f06d6-9d72-41ec-af22-c2902e4fdb43");
//		//redirect.info();
//
//		// mzk audio uuid:9a824ea2-468a-4690-87c8-e93b848e36cd
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:9a824ea2-468a-4690-87c8-e93b848e36cd");
//		//redirect.info();
//		
//		
//		// mzk uuid:b9c2c154-0d74-4ab9-8f5b-dbfd00fcfe40
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:b9c2c154-0d74-4ab9-8f5b-dbfd00fcfe40");
//		//redirect.info();
//		
//		//mzk uuid:36fb2729-b3eb-4c10-9094-f1038b0327ae
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:36fb2729-b3eb-4c10-9094-f1038b0327ae");
//		//redirect.info();
//		
//		//mzk uuid:25e5ee4f-10d8-475e-906d-bb52e80bafd1
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:25e5ee4f-10d8-475e-906d-bb52e80bafd1");
//		//redirect.info();
//		
//		//mzk uuid:5945ff40-35ac-11ed-be63-005056827e52
//		redirect = new V5RedirectHandler(client, new SolrAccessImplNewIndex(), "mzk", "uuid:5945ff40-35ac-11ed-be63-005056827e52");
//		redirect.info();
//	}
}
