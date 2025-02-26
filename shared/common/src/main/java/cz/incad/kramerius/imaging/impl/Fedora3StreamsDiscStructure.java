package cz.incad.kramerius.imaging.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.paths.Path;
import cz.incad.kramerius.imaging.paths.impl.DirPathImpl;
import cz.incad.kramerius.utils.FedoraUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;

/**
 * Manage same structure for storing objects as fedora 3.<br>  
 * Need connection provider  to fedora 3 database
 * @author pavels
 */
public class Fedora3StreamsDiscStructure implements DiscStrucutreForStore {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Fedora3StreamsDiscStructure.class.getName());



    /* TODO AK_NEW
    @Named("securedFedoraAccess")
    private FedoraAccess fedoraAccess;

     */
    private AkubraRepository akubraRepository;


    
    @Inject
    public Fedora3StreamsDiscStructure(
        // TODO AK_NEW    @Named("securedFedoraAccess") FedoraAccess fedoraAccess
            AkubraRepository akubraRepository
    ) throws IOException {
        super();
        this.akubraRepository = akubraRepository;
    }

    

    @Override
    public Path getUUIDFile(String uuid,  String rootPath) throws IOException {
        try {
            Date dateFromFedora = akubraRepository.getDatastreamMetadata("uuid:" + uuid, KnownDatastreams.IMG_FULL).getLastModified();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFromFedora);
            
            List<String> relativeDataStreamPath = Arrays.asList(
                    ""+calendar.get(Calendar.YEAR),
                    ""+calendar.get(Calendar.MONTH),
                    ""+calendar.get(Calendar.DAY_OF_MONTH),
                    ""+calendar.get(Calendar.HOUR),
                    ""+calendar.get(Calendar.MINUTE)
            );
                    
            File rootDir = new File(rootPath);
            if (!rootDir.exists()) {
                if (!rootDir.mkdirs()) {
                    throw new IOException("cannot create dir '" + rootPath + "'");
                }
            }
            StringTemplate template = new StringTemplate("$files;separator=sep$$sep$$uuid$");
            template.setAttribute("files", relativeDataStreamPath);
            template.setAttribute("sep", File.separator);
            template.setAttribute("uuid", uuid);
            String filePath = template.toString();

            return new DirPathImpl(new File(rootDir, filePath), null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            throw new IOException(e);
        }
    }
    
    

    public static Date disectCreateDate(String data) throws DatatypeConfigurationException {
        XMLGregorianCalendar gregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(data);
        int year = gregorianCalendar.getYear();
        int day = gregorianCalendar.getDay();
        int month = gregorianCalendar.getMonth();
        int minute = gregorianCalendar.getMinute();
        int hour = gregorianCalendar.getHour();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MONTH, month-1);
        return calendar.getTime();
    }
    
}
