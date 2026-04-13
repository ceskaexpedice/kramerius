package org.kramerius.genebook;

import org.json.JSONObject;

import javax.mail.MessagingException;
import java.util.List;

public interface SpecialNeedsEbookService {

    public void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException;

    public JSONObject scheduleRemoteJob(String exportApiBaseUrl, String pid, String authHeader, String k7BaseUrl);

    public JSONObject checkRemoteJob(String exportApiBaseUrl, String jobId, String authHeader);
}
