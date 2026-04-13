package org.kramerius.genebook;

import cz.inovatika.dochub.DocumentType;
import org.json.JSONObject;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SpecialNeedsEbookService {
    
    public JSONObject scheduleRemoteJob(String exportServiceBaseUrl, String pid, String authHeader, String k7BaseUrl);

    public JSONObject checkRemoteJob(String exportServiceBaseUrl, String jobId, String authHeader);

    /**
     * Downloads the result of the job to a temporary file and returns it. Caller is responsible for deleting the file after use.
     */
    public File saveJobResultToTmpFile(String exportServiceBaseUrl, String pid, String authHeader, JSONObject job);

    /**
     * Saves file to user content space
     *
     * @param file input file
     * @param type
     * @param user owner
     * @param pid  source document pid
     * @return token for retrieving the file from user content space
     */
    public String saveFileToUserContentSpace(File file, DocumentType type, String user, String pid);

    public void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException;

}
