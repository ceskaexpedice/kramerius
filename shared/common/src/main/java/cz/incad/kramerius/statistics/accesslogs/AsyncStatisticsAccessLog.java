package cz.incad.kramerius.statistics.accesslogs;


import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaLicenseUtils;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.statistics.accesslogs.solr.SolrStatisticsAccessLogImpl;
import cz.incad.kramerius.utils.IPAddressUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class AsyncStatisticsAccessLog extends AbstractStatisticsAccessLog {
    private static final int QUEUE_CAPACITY = 10000;

    @Inject
    Provider<User> userProvider;
    @Inject
    Provider<HttpServletRequest> requestProvider;

    private final SolrStatisticsAccessLogImpl delegate;
    private final BlockingQueue<AccessEvent> queue;
    private final Thread worker;
    private volatile boolean running = true;

    @Inject
    public AsyncStatisticsAccessLog(SolrStatisticsAccessLogImpl delegate) {
        this.delegate = delegate;
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.worker = new Thread(this::runWorker, "async-statistics-access-log");
        this.worker.start();
    }

    @Override
    public void reportAccess(final String pid, final String streamName, final String actionName) throws IOException {
        User user = userProvider.get();
        HttpServletRequest request = requestProvider.get();

        String sessionToken = null;
        String userLogin = null;
        String userSessionAttributes = null;

        if (user != null) {
            sessionToken = request.getSession().getId();
            userLogin = user.getLoginname();
            if (user.getSessionAttributes() != null) {
                userSessionAttributes = new JSONObject(user.getSessionAttributes()).toString();
            }
        }

        RightsReturnObject rightsReturnObject = CriteriaLicenseUtils.currentThreadReturnObject.get();
        Map<String, String> evaluateInfoMap =
                rightsReturnObject != null ? rightsReturnObject.getEvaluateInfoMap() : new HashMap<>();

        String referer = request.getHeader("Referer");
        String client = request.getHeader("client");

        AccessEvent event = new AccessEvent(
                pid,
                streamName,
                actionName,
                new Date(),
                sessionToken,
                request.getRequestURL().toString(),
                IPAddressUtils.getRemoteAddress(request),
                referer,
                client,
                userLogin,
                userSessionAttributes,
                evaluateInfoMap
        );

        if (!queue.offer(event)) {
            LOGGER.warning("Statistics access log queue is full. Event dropped for PID: " + pid);
        }
    }

    private void runWorker() {
        while (running || !queue.isEmpty()) {
            try {
                AccessEvent event = queue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    processBatch(event);
                }
            } catch (InterruptedException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Statistics access log worker interrupted.", e);
                }
                // TODO Při vypínání pokračujeme a zpracujeme zbývající události.
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error while processing statistics access event.", e);
            }
        }
    }

    private void processBatch(AccessEvent firstEvent) {
        List<AccessEvent> batch = new ArrayList<>();
        batch.add(firstEvent);

        for (AccessEvent event : batch) {
            try {
                delegate.processAccessEvent(event);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing statistics access event for PID: " + event.getPid(), e);
            }
        }
    }

    @Override
    public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup) {
        delegate.processAccessLog(reportedAction, sup);
    }

    @Override
    public StatisticReport[] getAllReports() {
        return delegate.getAllReports();
    }

    @Override
    public StatisticReport getReportById(String reportId) {
        return delegate.getReportById(reportId);
    }

    @Override
    public int cleanData(Date dateFrom, Date dateTo) throws IOException {
        return delegate.cleanData(dateFrom, dateTo);
    }

    @Override
    public void refresh() throws IOException {
        delegate.refresh();
    }

    public void shutdown() {
        running = false;
        worker.interrupt();
        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interrupted while waiting for statistics access log worker.", e);
        }
    }

}