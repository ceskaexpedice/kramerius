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
/**
 * 
 */
package cz.incad.Kramerius.statistics.formatters.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import cz.incad.kramerius.statistics.formatters.main.StatisticsExportMainLogFormatter;
import cz.incad.kramerius.statistics.formatters.main.impl.CSVFormatter;
import cz.incad.kramerius.statistics.formatters.main.impl.XMLFormatter;
import cz.incad.kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.kramerius.statistics.formatters.report.annual.AnnualCSVFormatter;
import cz.incad.kramerius.statistics.formatters.report.author.AuthorCSVFormatter;
import cz.incad.kramerius.statistics.formatters.report.author.AuthorXMLFormatter;
import cz.incad.kramerius.statistics.formatters.report.lang.LangCSVFormatter;
import cz.incad.kramerius.statistics.formatters.report.lang.LangXMLFormatter;
import cz.incad.kramerius.statistics.formatters.report.model.ModelCSVFormatter;
import cz.incad.kramerius.statistics.formatters.report.model.ModelXMLFormatter;
import cz.incad.kramerius.statistics.formatters.report.nkp.NKPJSONFormatter;
import cz.incad.kramerius.statistics.formatters.report.nkp.NKPXMLFormatter;
import cz.incad.kramerius.statistics.formatters.report.pids.PidsCSVFormatter;
import cz.incad.kramerius.statistics.formatters.report.pids.PidsXMLFormatter;
import cz.incad.kramerius.statistics.formatters.report.summary.SummaryCSVFormatter;

/**
 * @author pavels
 *
 */
public class FormatterModule extends AbstractModule {

    @Override
    protected void configure() {
        // statistics formatters
        Multibinder<StatisticsExportMainLogFormatter> mainFormatters = Multibinder.newSetBinder(binder(),
                StatisticsExportMainLogFormatter.class);
        mainFormatters.addBinding().to(CSVFormatter.class);
        mainFormatters.addBinding().to(XMLFormatter.class);

        Multibinder<StatisticsReportFormatter> reportFormatter = Multibinder.newSetBinder(binder(),
                StatisticsReportFormatter.class);

        reportFormatter.addBinding().to(LangCSVFormatter.class);
        reportFormatter.addBinding().to(LangXMLFormatter.class);

        reportFormatter.addBinding().to(ModelCSVFormatter.class);
        reportFormatter.addBinding().to(ModelXMLFormatter.class);

        reportFormatter.addBinding().to(AuthorCSVFormatter.class);
        reportFormatter.addBinding().to(AuthorXMLFormatter.class);

        reportFormatter.addBinding().to(AnnualCSVFormatter.class);
        
        reportFormatter.addBinding().to(PidsCSVFormatter.class);
        reportFormatter.addBinding().to(PidsXMLFormatter.class);

        reportFormatter.addBinding().to(NKPXMLFormatter.class);
        reportFormatter.addBinding().to(NKPJSONFormatter.class);

        reportFormatter.addBinding().to(SummaryCSVFormatter.class);
    }
}