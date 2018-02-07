/*
 * Copyright (C) 2012 Pavel Stastny
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
package org.kramerius.replications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.kramerius.consistency.Consistency;
import org.kramerius.consistency.Consistency.NotConsistentRelation;
import org.kramerius.consistency.Consistency._Module;
import org.kramerius.replications.SecondPhase.Emitter;
import org.kramerius.replications.pidlist.PIDsListLexer;
import org.kramerius.replications.pidlist.PIDsListParser;
import org.kramerius.replications.pidlist.PidsListCollect;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.pid.LexerException;

public class ThirdPhase extends AbstractPhase {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ThirdPhase.class.getName());
    
    @Override
    public void start(String url, String userName, String pswd, String replicationCollections, String replicationImages) throws PhaseException {
        try {
            List<String> paths = processIterateToFindRoot(getIterateFile());
            String rootPid = paths.isEmpty() ?  K4ReplicationProcess.pidFrom(url) : rootFromPaths(paths);
            LOGGER.info(" found root is "+rootPid);
            
            // check consistency 
            Consistency consistency = new Consistency();
            Injector injector = Guice.createInjector(new _Module());
            injector.injectMembers(consistency);
            consistency.checkConsitency(rootPid, true);

            String title = "_"; //TODO: title
            IOUtils.cleanDirectory(new File(SecondPhase.DONE_FOLDER_NAME));
            File descFile = getDescriptionFile();
            if ((descFile != null) && (descFile.canRead())) {
                String raw = IOUtils.readAsString(new FileInputStream(descFile), Charset.forName("UTF-8"), true);
                JSONObject jsonObject = JSONObject.fromObject(raw);
                title = jsonObject.getString("title");
            }
            IndexerProcessStarter.spawnIndexer(true, title, rootPid);
        } catch (FileNotFoundException e) {
            throw new PhaseException(this,e);
        } catch (IOException e) {
            throw new PhaseException(this,e);
        } catch (ProcessSubtreeException e) {
            throw new PhaseException(this,e);
        } catch (LexerException e) {
            throw new PhaseException(this,e);
        } catch (RecognitionException e) {
            throw new PhaseException(this,e);
        } catch (TokenStreamException e) {
            throw new PhaseException(this,e);
        }
    }

    /**
     * @param paths
     * @return
     */
    private String rootFromPaths(List<String> paths) {
        if (!paths.isEmpty()) {
            String[] patharr = paths.get(0).split("/");
            return patharr.length > 0 ?  patharr[0] : null; 
        }
        return null;
    }

    public static List<String> processIterateToFindRoot(File iterate) throws FileNotFoundException, PhaseException, RecognitionException, TokenStreamException {
        return processIterateToFindRoot(new FileReader(iterate));
    }

    public static List<String> processIterateToFindRoot(Reader iterateReader) throws FileNotFoundException, PhaseException, RecognitionException, TokenStreamException {
        PIDsListLexer lexer = new PIDsListLexer(iterateReader);
        PIDsListParser parser = new PIDsListParser(lexer);
        Paths pth = new Paths();
        parser.setPidsListCollect(pth);
        parser.pids();
        return pth.getPaths();
        //return parser.getPidsListCollect().
    }

    @Override
    public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd, String replicationCollections, String replicationImages) throws PhaseException {
        if (!phaseCompleted) {
            this.start(url, userName, pswd, replicationCollections, replicationImages);
        }
    }
    
    static class Paths implements PidsListCollect {
        private List<String> foundPaths = new ArrayList<String>();

        @Override
        public void pidEmitted(String pid) {
            // NOT 
        }

        @Override
        public void pathEmitted(String  path) {
            LOGGER.info("emitted path '"+path+"'");
            if ((path.startsWith("'")) || (path.startsWith("\""))){
                path = path.substring(1, path.length() - 1);
                LOGGER.info("changed path is "+path);
            }
            this.foundPaths.add(path);
        }

        public List<String> getPaths() {
            return this.foundPaths;
        }
    }
}
