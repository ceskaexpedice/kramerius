package cz.incad.kramerius.rest.apiNew.admin.v70.monitor;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.cache.CacheStatistics;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.security.impl.http.RightsResolverFromRequestCached;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/v7.0/monitor/cache")
public class CacheMonitorResource extends AdminApiResource {

    @Inject
    private Provider<User> cacheMonitorUserProvider;

    @Inject
    private RightsResolver cacheMonitorRightsResolver;

    @Inject
    private CDKRequestCacheSupport cdkRequestCacheSupport;

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getCacheStatistics() {
        requirePermission(SecuredActions.A_ADMIN_READ, "read cache statistics");

        org.json.JSONObject snapshot = CacheStatistics.snapshot();
        if (!isCdkModeEnabled()) {
            org.json.JSONObject caches = snapshot.getJSONObject("caches");
            caches.remove(CacheStatistics.CDK_ITEMS);
            caches.remove(CacheStatistics.CDK_USERS);
        }
        return Response.ok(snapshot.toString())
                .type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                .build();
    }

    @GET
    @Path("keys")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getCacheKeys(
            @QueryParam("cache") String cacheName,
            @QueryParam("limit") Integer requestedLimit,
            @QueryParam("pid") String pid,
            @QueryParam("user") String user,
            @QueryParam("ip") String ip,
            @QueryParam("path") String path,
            @QueryParam("source") String source,
            @QueryParam("url") String url) {
        requirePermission(SecuredActions.A_RIGHTS_EDIT, "inspect cache keys");

        int limit = requestedLimit == null ? 100 : requestedLimit;
        if (limit < 1) {
            throw new BadRequestException("limit must be between 1 and 100");
        }
        limit = Math.min(limit, 100);

        if ("rights".equals(cacheName)) {
            if (source != null || url != null) {
                throw new BadRequestException("source and url filters apply only to CDK caches");
            }
            return Response.ok(RightsResolverFromRequestCached.inspectCache(pid, user, ip, path, limit).toString())
                    .type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                    .build();
        }
        if ("cdkItems".equals(cacheName) || "cdkUsers".equals(cacheName)
                || "cdkBibliomods".equals(cacheName) || "cdkThumbs".equals(cacheName)) {
            if (!isCdkModeEnabled()) {
                throw new NotFoundException("CDK cache is unavailable when CDK server mode is disabled");
            }
            if (ip != null || path != null) {
                throw new BadRequestException("ip and path filters apply only to the rights cache");
            }
            return Response.ok(cdkRequestCacheSupport.inspect(cacheName, source, url, pid, user, limit).toString())
                    .type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                    .build();
        }
        throw new BadRequestException("cache must be one of: rights, cdkItems, cdkUsers, cdkBibliomods, cdkThumbs");
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response clearCache(
            @QueryParam("cache") String cacheName,
            @QueryParam("pid") String pid,
            @QueryParam("user") String user,
            @QueryParam("ip") String ip,
            @QueryParam("path") String path) {
        requirePermission(SecuredActions.A_RIGHTS_EDIT, "clear cache entries");
        if (!"rights".equals(cacheName)) {
            throw new BadRequestException("only the rights cache can be cleared through this endpoint");
        }

        int removed = RightsResolverFromRequestCached.clearCache(pid, user, ip, path);
        org.json.JSONObject result = new org.json.JSONObject();
        result.put("cache", "rights");
        result.put("scope", "process-local");
        result.put("removed", removed);
        return Response.ok(result.toString())
                .type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                .build();
    }

    private void requirePermission(SecuredActions action, String operation) {
        User user = cacheMonitorUserProvider.get();
        boolean allowed = user != null && cacheMonitorRightsResolver.isActionAllowed(
                user,
                action.getFormalName(),
                SpecialObjects.REPOSITORY.getPid(),
                null,
                ObjectPidsPath.REPOSITORY_PATH).flag();
        if (!allowed) {
            throw new ForbiddenException("user '%s' is not allowed to %s",
                    user == null ? "unknown" : user.getLoginname(), operation);
        }
    }

    private boolean isCdkModeEnabled() {
        return KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode", false);
    }
}
