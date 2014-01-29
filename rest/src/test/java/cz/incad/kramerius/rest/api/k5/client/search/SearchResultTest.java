/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.api.k5.client.search;

import org.junit.Assert;
import org.junit.Test;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SearchResultTest {
	
	
	@Test
	public void testRepairMasterPID() {
    	String str= "{\"PID\":\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3/@1\",\"fedora.model\":\"article\",\"dc.title\":\"Contents\",\"title_sort\":\"CONTENTS\",\"status\":\"Active\",\"handle\":\"\",\"created_date\":\"2013-09-13T23:50:10.599Z\",\"modified_date\":\"2014-01-28T14:55:11.475Z\",\"dostupnost\":\"private\",\"issn\":\"\",\"mdt\":\"\",\"ddt\":\"\",\"img_full_mime\":\"application/pdf\",\"viewable\":true,\"rels_ext_index\":0,\"root_title\":\"Contents\",\"root_pid\":\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\",\"level\":0,\"datum_str\":\"\",\"datum\":\"1970-01-01T01:00:00Z\",\"virtual\":false,\"datum_begin\":0,\"pages_count\":0,\"rok\":0,\"datum_end\":0,\"dc.identifier\":[\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\",\"5bb0280c-3146-060f-6b75-045d7d9648c3\"],\"pid_path\":[\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\"],\"parent_pid\":[\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\"],\"language\":[\"eng\"],\"model_path\":[\"article\"],\"document_type\":[\"article\"]}";
    	JSONObject jsonObj = JSONObject.fromObject(str);
    	SearchResource.changeMasterPid(jsonObj);
    	Assert.assertTrue(jsonObj.containsKey("PID"));
    	Assert.assertTrue(jsonObj.getString("PID").equals("uuid:5bb0280c-3146-060f-6b75-045d7d9648c3@1"));
	}

	@Test
	public void testRepairPathsPID() {
    	String str= "{\"PID\":\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\",\"fedora.model\":\"article\",\"dc.title\":\"Contents\",\"title_sort\":\"CONTENTS\",\"status\":\"Active\",\"handle\":\"\",\"created_date\":\"2013-09-13T23:50:10.599Z\",\"modified_date\":\"2014-01-28T14:55:11.475Z\",\"dostupnost\":\"private\",\"issn\":\"\",\"mdt\":\"\",\"ddt\":\"\",\"img_full_mime\":\"application/pdf\",\"viewable\":true,\"rels_ext_index\":0,\"root_title\":\"Contents\",\"root_pid\":\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\",\"level\":0,\"datum_str\":\"\",\"datum\":\"1970-01-01T01:00:00Z\",\"virtual\":false,\"datum_begin\":0,\"pages_count\":0,\"rok\":0,\"datum_end\":0,\"dc.identifier\":[\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\",\"5bb0280c-3146-060f-6b75-045d7d9648c3\"],\"pid_path\":[\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3/@1\"],\"parent_pid\":[\"uuid:5bb0280c-3146-060f-6b75-045d7d9648c3\"],\"language\":[\"eng\"],\"model_path\":[\"article\"],\"document_type\":[\"article\"]}";
    	JSONObject jsonObj = JSONObject.fromObject(str);
    	SearchResource.replacePids(jsonObj);
    	JSONArray jsonArray = jsonObj.getJSONArray("pid_path");
    	for (Object object : jsonArray) {
			if (object instanceof String) {
				String arrayVal = (String) object;
				Assert.assertFalse(arrayVal.contains("/"));
			}
		}
	}
}
