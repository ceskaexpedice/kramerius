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
import cz.incad.kramerius.database.VersionInitializer;
import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.processes.GCScheduler;
import cz.incad.kramerius.processes.ProcessScheduler;
import cz.incad.kramerius.processes.database.MostDesirableDatabaseInitializator;
import cz.incad.kramerius.processes.database.ProcessDatabaseInitializator;
import cz.incad.kramerius.security.database.SecurityDatabaseInitializator;
import cz.incad.kramerius.service.LifeCycleHookRegistry;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.statistics.database.StatisticDatabaseInitializator;
import cz.incad.kramerius.users.database.LoggedUserDatabaseInitializator;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;

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

        Connection connection = this.connectionProvider.get();
        try {
            // read previous db version
            VersionInitializer.initDatabase(connection);

            // mostdesirable table
            MostDesirableDatabaseInitializator.initDatabase(connection, versionService);
            // all security tables
            SecurityDatabaseInitializator.initDatabase(connection, versionService);
            // process tables - > must be after security tables and must be
            // after logged user tables
            ProcessDatabaseInitializator.initDatabase(connection, versionService);

            // statistics tables
            StatisticDatabaseInitializator.initDatabase(connection, versionService);

            // stores new db version to doatabase
            versionService.updateNewVersion();

            this.pdfService.init();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                DatabaseUtils.tryClose(connection);
            }
        }

        this.lifecycleRegistry.startNotification();

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
