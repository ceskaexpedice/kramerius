package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.matchGeolocationByIP;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.DataMockExpectation;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumLabelAware;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaLicenseUtils;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class LicensesGEOIPFiltered extends AbstractCriterium implements RightCriteriumLabelAware{

    private static final String GEOLOCATION_DATABASE_FILE = "geolocation.database.file";

    public transient static final Logger LOGGER = Logger.getLogger(LicensesIPFiltered.class.getName());
    
    static DatabaseReader GEOIP_DATABASE = null;
    
    static {
        try {
            String file = KConfiguration.getInstance().getConfiguration().getString(GEOLOCATION_DATABASE_FILE);
            if (file != null) {
                GEOIP_DATABASE = new DatabaseReader.Builder(new File(file)).build();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    
    private License license;

    @Override
    public EvaluatingResultState evalute(Right right) throws RightCriteriumException {
        try {
            RightCriteriumContext ctx =  getEvaluateContext();
            String pid = ctx.getRequestedPid();
            if (!SpecialObjects.isSpecialObject(pid)) {
                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
                    Document doc = solrAccess.getSolrDataByPid(pid);
                    License lic = getLicense();
                    boolean applied = CriteriaLicenseUtils.matchLicense(doc,  lic);
                    // musi se z
                    if (applied)  {
                        if (GEOIP_DATABASE != null) {
                            EvaluatingResultState result = matchGeolocationByIP(GEOIP_DATABASE, super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
                            if (result.equals(EvaluatingResultState.TRUE)) {
                                if (lic.exclusiveLockPresent()) {
                                    return CriteriaLicenseUtils.licenseLock(right, ctx, pid, lic);
                                } else {
                                    getEvaluateContext().getEvaluateInfoMap().put(Licenses.PROVIDED_BY_LABEL, getLicense().getName());
                                    getEvaluateContext().getEvaluateInfoMap().put(Licenses.PROVIDED_BY_LICENSE, getLicense().getName());
                                    return EvaluatingResultState.TRUE;
                                }
                            }
                            return result;
                        } else {
                            LOGGER.log(Level.WARNING, String.format("Missing property %s",GEOLOCATION_DATABASE_FILE));
                            return EvaluatingResultState.NOT_APPLICABLE;
                        }
                    }
                }
            }
            return EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException | GeoIp2Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    
    
    @Override
    public EvaluatingResultState mockEvaluate(Right right, DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        try {
            if (GEOIP_DATABASE != null) {
                return matchGeolocationByIP(GEOIP_DATABASE, super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
            } else {
                LOGGER.log(Level.WARNING, String.format("Missing property %s",GEOLOCATION_DATABASE_FILE));
                return EvaluatingResultState.NOT_APPLICABLE;
            }
        } catch (IOException |  GeoIp2Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.DNNT_EXCLUSIVE_MIN;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return  new SecuredActions[] {SecuredActions.A_READ};
    }

    @Override
    public boolean isRootLevelCriterum() {
        return true;
    }

    @Override
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException {
        //checkContainsCriteriumPDFDNNT(this.evalContext, manager);
    }

    @Override
    public boolean isLicenseAssignable() {
        return true;
    }

    @Override
    public License getLicense() {
        return this.license;
    }

    @Override
    public void setLicense(License license) {
        this.license = license;
    }
}
