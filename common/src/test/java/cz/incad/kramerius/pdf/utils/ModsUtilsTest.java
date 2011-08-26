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
package cz.incad.kramerius.pdf.utils;

import java.io.IOException;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lowagie.text.Chunk;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.imaging.ImagingModuleForTest;
import cz.incad.kramerius.utils.FedoraUtils;

public class ModsUtilsTest extends AbstractGuiceTestCase {
    
    //@Test
    public void testMonograph() throws ProcessSubtreeException, IOException {
        final FedoraAccess instance = injector().getInstance(FedoraAccess.class);
        String[] pds = Monographs.PIDS;
        for (String pid : pds) {
            BiblioMods rootMods = ModsUtils.biblioMods(pid, instance);
            monograph(rootMods);
            
            instance.processSubtree(pid, new TreeNodeProcessor() {

                
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                
                    try {
                        BiblioMods curMods = ModsUtils.biblioMods(pid, instance);
                        if (curMods.getModelName().equals("internalpart")) {
                            internalpart(curMods);
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                        
                }
                
                @Override
                public boolean breakProcessing(String pid, int level) {
                    try {
                        String modelName = instance.getKrameriusModelName(pid);
                        if (modelName.equals("page")) {
                            return true;
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return false;
                }
            });
            System.out.println("---------");
        }

    }
    
//    @Test
    public void testPeriodics() throws ProcessSubtreeException, IOException {
        final FedoraAccess instance = injector().getInstance(FedoraAccess.class);
        String[] pds = Periodics.PIDS;
        for (String pid : pds) {
            BiblioMods rootMods = ModsUtils.biblioMods(pid, instance);
            periodical(rootMods);
            instance.processSubtree(pid, new TreeNodeProcessor() {
                
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    if (level == 1) {
                        try {
                            BiblioMods curMods = ModsUtils.biblioMods(pid, instance);
                            periodicalvolume(curMods);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (level == 2) {
                        try {
                            BiblioMods curMods = ModsUtils.biblioMods(pid, instance);
                            if (curMods.getModelName().equals("periodicalitem")) {
                                periodicalitem(curMods);
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("skip level =="+level);
                        
                    }
                        
                        
                }
                
                @Override
                public boolean breakProcessing(String pid, int level) {
                    try {
                        return level > 2 || instance.getKrameriusModelName(pid).equals("page");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return true;
                    }
                }
            });
            System.out.println("---------");
        }
        
    }
    
    private void monograph(BiblioMods bmods) {
        BiblioModsTitleInfo title = bmods.findTitle();
        if (title != null) {
            System.out.println("Hlavni nazev:"+title.getTitle());
        }

        BiblioModsIdentifier isbn =  bmods.findIdent("isbn");
        if (isbn != null) {
            System.out.println("ISBN:"+isbn.getIdent());
        }
    }

    private void internalpart(BiblioMods bmods) {
        BiblioModsTitleInfo title = bmods.findTitle();
        if (title != null) {
            System.out.println("Nazev:"+title.getTitle());
        }
    }
    
    private void periodical(BiblioMods bmods) {
        BiblioModsTitleInfo title = bmods.findTitle();
        if (title != null) {
            System.out.println("Hlavni nazev:"+title.getTitle());
        }
        
        BiblioModsIdentifier issn =  bmods.findIdent("issn");
        if (issn != null) {
            System.out.println("ISSN:"+issn.getIdent());
        }
    }
    
    private void periodicalvolume(BiblioMods bmods) {
        BiblioModsPart part = bmods.findDetail("volume");
        if (part != null) {
            String detail = part.findDetail("volume").getNumber();
            System.out.println("Cislo rocniku:"+detail);

            String date = part.getDate();
            if (date != null) {
                System.out.println("Rok:"+date);
            }
        }
    }
    
    private void periodicalitem(BiblioMods bmods) {
        BiblioModsPart part = bmods.findDetail("issue");
        if (part != null) {
            String number = part.findDetail("issue").getNumber();
            System.out.println("Cislo:"+number);
            
            if (part.getDate() != null) {
                System.out.println("datum vydani:"+part.getDate());
            }
        }
    }


    
    //@Test
    public void testMods() throws IOException, XPathExpressionException {
//        FedoraAccess instance = injector().getInstance(FedoraAccess.class);
//        System.out.println(instance);
//
//        for (int i = 0; i < PIDS.length; i++) {
//            String pid = PIDS[i];
//            String modelName = instance.getKrameriusModelName(pid);
//            System.out.println(modelName +" ----- >>");
//            
//            Map<String, String> map = ModsUtils.getTitleInfo(pid, instance);
//            System.out.println(map);
//            
//            Map<String, BiblioModsPage> pageParts = ModsUtils.getPageParts(pid, instance);
//            System.out.println(pageParts);
//            
//        }
//        
        
        
    }
   
    
    
    @Override
    protected Injector injector() {
        Injector injector = Guice.createInjector(new SimpleFedoraAccessModule());
        return injector;
    }
    
}
