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
package cz.incad.Kramerius;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.database.VersionDbInitializer;
import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.processes.database.MostDesirableDbInitializer;
import cz.incad.kramerius.processes.database.ProcessDbInitializer;
import cz.incad.kramerius.rest.oai.db.OAIDBInitializer;
import cz.incad.kramerius.security.database.SecurityDbInitializer;
import cz.incad.kramerius.service.LifeCycleHookRegistry;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.statistics.database.StatisticDbInitializer;
import cz.incad.kramerius.users.database.LoggedUserDbHelper;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.workmode.WorkModeDbInitializer;
import cz.inovatika.folders.db.FolderDatabaseInitializer;

/**
 * Starting point for K4 application
 */
public class StartupServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(StartupServlet.class.getName());

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider = null;

    @Inject
    TextsService textsService;

    @Inject
    GeneratePDFService pdfService;

    @Inject
    VersionService versionService;

    @Inject
    LifeCycleHookRegistry lifecycleRegistry;

    @Override
    public void init() throws ServletException {
        super.init();

        /**
         * try { GuiceConfigBean.Grapher grapher = new
         * GuiceConfigBean.Grapher(); grapher.graph("google-dependency.dot",
         * getInjector()); } catch (IOException e1) { LOGGER.log(Level.SEVERE,
         * e1.getMessage(),e1); }
         **/

        Connection connection = null;
        try {
            connection = this.connectionProvider.get();

            // read previous db version
            VersionDbInitializer.initDatabase(connection);

            // work mode table
            WorkModeDbInitializer.initDatabase(connection);

            // mostdesirable table
            MostDesirableDbInitializer.initDatabase(connection, versionService);

            // all security tables
            SecurityDbInitializer.initDatabase(connection, versionService);

            // process tables - > must be after security tables and must be
            // after logged user tables
            ProcessDbInitializer.initDatabase(connection, versionService);

            // statistics tables
            StatisticDbInitializer.initDatabase(connection, versionService);

            // folder database
            FolderDatabaseInitializer.initDatabase(connection, versionService);

            // Default OAI sets initializer - configuration part
            OAIDBInitializer.initDatabase(connection, versionService);

            // delete session keys
            LoggedUserDbHelper.deleteAllSessionKeys(connection);

            
            // stores new db version to database if necessary
            versionService.updateVersionIfOutdated();

            this.pdfService.init();

            this.lifecycleRegistry.startNotification();

        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException("Failed to start Kramerius", e);
        } finally {
            if (connection != null) {
                DatabaseUtils.tryClose(connection);
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (this.lifecycleRegistry != null) {
            this.lifecycleRegistry.shutdownNotification();
        }
    }
}
