/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.Kramerius.security.strenderers;

import java.io.IOException;
import java.util.logging.Level;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.DCUtils;

public class RightWrapper implements Right{
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RightWrapper.class.getName());
    
    private Right right;
    private String pidTitle;

    public RightWrapper(FedoraAccess fedoraAccess,Right right) {
        super();
        this.right = right;
        if (SpecialObjects.REPOSITORY.getPid().equals(right.getPid())) {
            this.pidTitle = SpecialObjects.REPOSITORY.name();
        } else {
            try {
                this.pidTitle = DCUtils.titleFromDC(fedoraAccess.getDC(right.getPid()));
            } catch (IOException e) {
               LOGGER.log(Level.SEVERE, e.getMessage(),e); 
            }
        }
    }

    public int getId() {
        return right.getId();
    }

    
    public String getPid() {
        return this.right.getPid();
    }
    
    public String getDCTitle() {
        return this.pidTitle;
    }
    
    public String getTitle() {
        return this.pidTitle;
    }
    
    public String getUuid() {
        String pid = right.getPid();
        return pid.substring("uuid:".length());
    }

    public String getAction() {
        return right.getAction();
    }

    public AbstractUser getUser() {
        return  new AbstractUserWrapper(right.getUser());
    }

    public void setUser(AbstractUser user) {
        throw new UnsupportedOperationException("this is unsupported");
    }

    public RightCriteriumWrapper getCriteriumWrapper() {
        if (right.getCriteriumWrapper() != null) {
            return new CriteriumGuiWrapper(right.getCriteriumWrapper());
        } else return null;
    }
    

    public EvaluatingResult evaluate(RightCriteriumContext ctx) throws RightCriteriumException {
        throw new IllegalStateException();
    }
    
    public static RightWrapper[] wrapRights(FedoraAccess fedoraAccess, Right...rights) {
        RightWrapper[] wrappers = new RightWrapper[rights.length];
        for (int i = 0; i < rights.length; i++) {
            wrappers[i]= new RightWrapper(fedoraAccess, rights[i]);
        }
        return wrappers;
    }

    @Override
    public void setAction(String action) {
        throw new UnsupportedOperationException("this is unsupported!");
    }
    
    
    public int getFixedPriority() {
        return this.right.getFixedPriority();
    }
    
    @Override
    public void setFixedPriority(int priority) {
        this.right.setFixedPriority(priority);
    }

    public String getFixedPriorityName() {
        if (right.getFixedPriority() == 0) return "";
        else return ""+right.getFixedPriority();
    }

    @Override
    public void setCriteriumWrapper(RightCriteriumWrapper rightCriterium) {
        throw new UnsupportedOperationException("");
    }
    
}
