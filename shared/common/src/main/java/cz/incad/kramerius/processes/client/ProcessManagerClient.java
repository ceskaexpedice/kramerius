/*
 * Copyright (C) 2025 Inovatika
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
package cz.incad.kramerius.processes.client;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * ProcessManagerClient
 * @author ppodsednik
 */
public class ProcessManagerClient {

    private static final Logger LOGGER = Logger.getLogger(ProcessManagerClient.class.getName());

    private final CloseableHttpClient closeableHttpClient;
    private final String baseUrl;

    @Inject
    public ProcessManagerClient(CloseableHttpClient closeableHttpClient) {
        this.closeableHttpClient = closeableHttpClient;
        baseUrl = KConfiguration.getInstance().getProcessManagerURL();
    }

    public String scheduleProcess(JSONObject scheduleMainProcess) {
        String url = baseUrl + "process";
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(scheduleMainProcess.toString(), ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        try (CloseableHttpResponse response = closeableHttpClient.execute(post)) {
            int statusCode = response.getCode();
            HttpEntity entityResp = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entityResp) : "";
            if (statusCode == 200 || statusCode == 204) {
                return new JSONObject(body).getString("processId");
            } else {
                JSONObject errorBody = new JSONObject(body);
                throw new ProcessManagerClientException(errorBody.has("error") ? errorBody.optString("error") : body, ErrorCode.findByStatusCode(statusCode), statusCode);
            }
        } catch (ProcessManagerClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e,-1);
        }
    }

    public JSONObject getOwners() {
        String url = baseUrl + "process/owner";
        HttpGet get = new HttpGet(url);

        try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "";
            if (code == 200) {
                return new JSONObject(body);
            } else {
                throw new ProcessManagerClientException("Failed to fetch owners. HTTP code" + ": " + code, ErrorCode.findByStatusCode(code), code);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public JSONObject getProcess(String processId) {
        String url = baseUrl + "process/" + processId;
        HttpGet get = new HttpGet(url);

        try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "";
            if (code == 200) {
                return new JSONObject(body);
            } else if (code == 404) {
                return null;
            } else {
                throw new ProcessManagerClientException("Failed to fetch process. HTTP code" + ": " + code, ErrorCode.findByStatusCode(code), code);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public JSONObject getBatches(String offset, String limit, String owner, String from, String to, String state, String workers) {
        String url = baseUrl + "process/batch";
        try {
            URIBuilder uriBuilder = new URIBuilder(url);

            if (offset != null) uriBuilder.addParameter("offset", offset);
            if (limit != null) uriBuilder.addParameter("limit", limit);
            if (owner != null) uriBuilder.addParameter("owner", owner);
            if (from != null) uriBuilder.addParameter("from", from);
            if (to != null) uriBuilder.addParameter("to", to);
            if (state != null) uriBuilder.addParameter("state", state);
            if (StringUtils.isAnyString(workers)) uriBuilder.addParameter("workers", workers);

            HttpGet get = new HttpGet(uriBuilder.build());
            try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
                int code = response.getCode();
                HttpEntity entity = response.getEntity();
                String body = entity != null ? EntityUtils.toString(entity) : "";
                if (code == 200) {
                    return new JSONObject(body);
                } else if (code == 400) {
                    throw new ProcessManagerClientException("Invalid input: " + body, ErrorCode.INVALID_INPUT, code);
                } else {
                    throw new ProcessManagerClientException("Failed to fetch batches. HTTP code" + ": " + code, ErrorCode.findByStatusCode(code), code);
                }
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public InputStream getProcessLog(String processId, boolean err) {
        String suffix = err ? "err" : "out";
        String url = baseUrl + "process/" + processId + "/log/" + suffix;
        HttpGet get = new HttpGet(url);
        try {
            CloseableHttpResponse response = closeableHttpClient.execute(get);
            int code = response.getCode();
            if (code == 200) {
                InputStream is = response.getEntity().getContent();
                return is;
            } else if (code == 404) {
                return null;
            } else {
                throw new ProcessManagerClientException("Failed to fetch logs. HTTP " + code, ErrorCode.findByStatusCode(code), code);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public JSONObject getProcessLogLines(String processId, String offset, String limit, boolean err) {
        String suffix = err ? "err" : "out";
        String url = baseUrl + "process/" + processId + "/log/" + suffix + "/lines";
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (offset != null) uriBuilder.addParameter("offset", offset);
            if (limit != null) uriBuilder.addParameter("limit", limit);

            HttpGet get = new HttpGet(uriBuilder.build());
            try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
                int code = response.getCode();
                HttpEntity entity = response.getEntity();
                String body = entity != null ? EntityUtils.toString(entity) : "";
                if (code == 200) {
                    return new JSONObject(body);
                } else if (code == 400) {
                    throw new ProcessManagerClientException("Invalid input: " + body, ErrorCode.INVALID_INPUT, code);
                } else {
                    throw new ProcessManagerClientException("Failed to fetch logs. HTTP " + code,ErrorCode.findByStatusCode(code), code);
                }
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public int deleteBatch(String mainProcessId) {
        String url = baseUrl + "process/batch/" + mainProcessId;
        HttpDelete httpDelete = new HttpDelete(url);
        try (CloseableHttpResponse response = closeableHttpClient.execute(httpDelete)) {
            int statusCode = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "";
            if (statusCode == 200) {
                return new JSONObject(body).getInt("deleted");
            } else if (statusCode == 400) {
                throw new ProcessManagerClientException("Invalid state", ErrorCode.INVALID_INPUT,statusCode);
            } else if (statusCode == 404) {
                throw new ProcessManagerClientException("Batch not found", ErrorCode.NOT_FOUND, statusCode);
            } else {
                throw new ProcessManagerClientException("Failed to delete batch. HTTP " + statusCode,ErrorCode.findByStatusCode(statusCode), statusCode);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public int killBatch(String mainProcessId) {
        String url = baseUrl + "process/batch/" + mainProcessId + "/execution";
        HttpDelete httpDelete = new HttpDelete(url);
        try (CloseableHttpResponse response = closeableHttpClient.execute(httpDelete)) {
            int statusCode = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "";
            if (statusCode == 200) {
                return new JSONObject(body).getInt("killed");
            } else if (statusCode == 400) {
                throw new ProcessManagerClientException("Invalid state", ErrorCode.INVALID_INPUT, statusCode);
            } else if (statusCode == 404) {
                throw new ProcessManagerClientException("Batch not found", ErrorCode.NOT_FOUND,  statusCode);
            } else {
                throw new ProcessManagerClientException("Failed to kill batch. HTTP " + statusCode,ErrorCode.findByStatusCode(statusCode), statusCode);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public JSONObject getProfile(String profileId) {
        String url = baseUrl + "profile/" + profileId;
        HttpGet get = new HttpGet(url);

        try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "";
            if (code == 200) {
                return new JSONObject(body);
            } else if (code == 404) {
                return null;
            } else {
                throw new ProcessManagerClientException("Failed to fetch profile. HTTP code" + ": " + code,ErrorCode.findByStatusCode(code), code);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e,-1);
        }
    }

    public JSONArray getProfiles() {
        String url = baseUrl + "profile";
        HttpGet get = new HttpGet(url);

        try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "";
            if (code == 200) {
                return new JSONArray(body);
            } else {
                throw new ProcessManagerClientException("Failed to fetch profiles. HTTP code" + ": " + code,ErrorCode.findByStatusCode(code), code);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public JSONObject getPlugin(String pluginId) {
        String url = baseUrl + "plugin/" + pluginId;
        HttpGet get = new HttpGet(url);

        try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "";
            if (code == 200) {
                return new JSONObject(body);
            } else if (code == 404) {
                return null;
            } else {
                throw new ProcessManagerClientException("Failed to fetch plugin. HTTP code" + ": " + code,ErrorCode.findByStatusCode(code), code);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public void updateProfile(String profileId, JSONObject profile) {
        String url = baseUrl + "profile/" + profileId;
        HttpPut put = new HttpPut(url);
        StringEntity entity = new StringEntity(profile.toString(), ContentType.APPLICATION_JSON);
        put.setEntity(entity);

        try (CloseableHttpResponse response = closeableHttpClient.execute(put)) {
            int statusCode = response.getCode();
            if (statusCode != 200 && statusCode != 204) {
                throw new ProcessManagerClientException("Failed to update profile. HTTP code" + ": " + statusCode, ErrorCode.findByStatusCode(statusCode), statusCode);
            }
        } catch (IOException e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }

    public JSONArray getNodes() {
        String url = baseUrl + "node";
        HttpGet get = new HttpGet(url);
        //LOGGER.info( String.format("Getting nodes url %s", url));
        try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity) : "[]";
            //LOGGER.info(String.format("Getting nodes json %s", body));
            if (code == 200) {
                return new JSONArray(body);
            } else if (code == 404) {
                return null;
            } else {
                throw new ProcessManagerClientException("Failed to fetch nodes. HTTP code" + ": " + code,ErrorCode.findByStatusCode(code), code);
            }
        } catch (Exception e) {
            throw new ProcessManagerClientException("I/O error while calling " + url, e, -1);
        }
    }
}
