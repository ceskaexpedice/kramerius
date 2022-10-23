package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import cz.incad.kramerius.utils.pid.LexerException;
import junit.framework.TestCase;


public class V5RedirectHandlerTest {
	
	
	@Ignore @Test
	public void testExtractAvailableDataInfo() throws IOException, JSONException, LexerException {
		String string = IOUtils.toString(V5RedirectHandlerTest.class.getResourceAsStream("article.streams.json"), Charset.forName("UTF-8"));
		V5RedirectHandler redirect = new V5RedirectHandler(null, "knav", "uuid:1");
		JSONObject info = redirect.extractAvailableDataInfo(new JSONObject(string));

		Assert.assertTrue(info.has("image"));
		Assert.assertTrue(info.has("metadata"));
		Assert.assertTrue(info.has("audio"));
		Assert.assertTrue(info.has("ocr"));

		Assert.assertTrue(info.getJSONObject("image").has("thumb"));
		Assert.assertTrue(info.getJSONObject("image").getBoolean("thumb"));
		Assert.assertTrue(info.getJSONObject("image").has("preview"));
		Assert.assertTrue(info.getJSONObject("image").getBoolean("preview"));
		Assert.assertTrue(info.getJSONObject("image").has("full"));
		Assert.assertTrue(info.getJSONObject("image").getBoolean("full"));
		
		Assert.assertTrue(info.getJSONObject("metadata").has("mods"));
		Assert.assertTrue(info.getJSONObject("metadata").getBoolean("mods"));
		Assert.assertTrue(info.getJSONObject("metadata").has("dc"));
		Assert.assertTrue(info.getJSONObject("metadata").getBoolean("dc"));

		Assert.assertTrue(info.getJSONObject("audio").has("mp3"));
		Assert.assertFalse(info.getJSONObject("audio").getBoolean("mp3"));
		Assert.assertTrue(info.getJSONObject("audio").has("wav"));
		Assert.assertFalse(info.getJSONObject("audio").getBoolean("wav"));
		Assert.assertTrue(info.getJSONObject("audio").has("ogg"));
		Assert.assertFalse(info.getJSONObject("audio").getBoolean("ogg"));

	}

	@Ignore @Test
	public void testPageArticleInfo() throws IOException, JSONException, LexerException {
		String string = IOUtils.toString(V5RedirectHandlerTest.class.getResourceAsStream("article/k5/page_article_58ddb459-d295-11e1-8393-0050569d679d.json"), Charset.forName("UTF-8"));
		V5RedirectHandler redirect = new V5RedirectHandler(null, "knav", "uuid:1");
		JSONObject struct = redirect.extractStructureInfo(new JSONObject(string));
		Assert.assertTrue(struct.has("model"));
		Assert.assertEquals(struct.getString("model"),"page");

		Assert.assertTrue(struct.has("parent"));
		Assert.assertTrue(struct.getJSONObject("parent").has("own"));
		Assert.assertTrue(struct.getJSONObject("parent").getJSONObject("own").optString("pid").equals("uuid:a1c53cdc-0031-467f-bfdc-76b824735862"));
		Assert.assertTrue(struct.getJSONObject("parent").getJSONObject("own").optString("relation").equals("hasPage"));
	}


	@Test
	public void testArticleInfo() throws IOException, JSONException, LexerException {
		String string = IOUtils.toString(V5RedirectHandlerTest.class.getResourceAsStream("article/k5/article_fe1191ba-98a7-4f21-b201-3c147fedc88e.json"), Charset.forName("UTF-8"));
		V5RedirectHandler redirect = new V5RedirectHandler(null, "knav", "uuid:1");
		JSONObject struct = redirect.extractStructureInfo(new JSONObject(string));

		System.out.println(struct.toString(3));
//		Assert.assertTrue(struct.has("model"));
//		Assert.assertEquals(struct.getString("model"),"page");
//
//		Assert.assertTrue(struct.has("parent"));
//		Assert.assertTrue(struct.getJSONObject("parent").has("own"));
//		Assert.assertTrue(struct.getJSONObject("parent").getJSONObject("own").optString("pid").equals("uuid:a1c53cdc-0031-467f-bfdc-76b824735862"));
//		Assert.assertTrue(struct.getJSONObject("parent").getJSONObject("own").optString("relation").equals("hasPage"));
	}
}
