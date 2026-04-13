package org.kramerius.gentext;

import cz.inovatika.dochub.DocumentType;
import org.json.JSONObject;

import javax.mail.MessagingException;
import java.io.File;
import java.util.List;

public interface SpecialNeedsTextService {

    /**
     * Schedules new TEXT export process with the Export service
     *
     * @param exportServiceBaseUrl    Export service base url, for example https://alto-processing.trinera.cloud
     * @param exportServiceAuthHeader header for authentication against Export service
     * @param k7ClientApiBasUrl       K7 client API base url, for example https://api.kramerius.mzk.cz/search/api/client/v7.0
     * @param pid                     pid of the document to be exported
     * @return JSON object returned by Export service with job id and other information
     */
    public JSONObject scheduleRemoteJob(String exportServiceBaseUrl, String pid, String exportServiceAuthHeader, String k7ClientApiBasUrl);


    /**
     * Checks job's status in Export service
     *
     * @param exportServiceBaseUrl    Export service base url, for example https://alto-processing.trinera.cloud
     * @param exportServiceAuthHeader header for authentication against Export service
     * @param jobId                   id of the job to be checked
     * @return JSON object returned by Export service with job status and other information
     */
    public JSONObject checkRemoteJob(String exportServiceBaseUrl, String jobId, String exportServiceAuthHeader);

    /**
     * Downloads the result of the finished job to a temporary file and returns it. Caller is responsible for deleting the file after use.
     *
     * @param exportServiceBaseUrl    Export service base url, for example https://alto-processing.trinera.cloud
     * @param exportServiceAuthHeader header for authentication against Export service
     * @param job                     JSON object from Export service containing url for downloading the result file
     * @param pid                     pid of the document to be exported
     * @return Temporary file containing the downloaded data
     */
    public File saveJobResultToTmpFile(String exportServiceBaseUrl, String pid, String exportServiceAuthHeader, JSONObject job);

    /**
     * Saves file to user content space
     *
     * @param file input file (temporary file storing export results)
     * @param type EPUB/TEXT
     * @param user owner
     * @param pid  pid of the document to be exported
     * @return token for retrieving the file from user content space
     */
    public String saveFileToUserContentSpace(File file, DocumentType type, String user, String pid);

    /**
     * Sends email notification(s) containing ling for downloading the results
     *
     * @param emailFrom
     * @param recipients
     * @param subject
     * @param text
     * @throws MessagingException
     */
    public void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException;

}
