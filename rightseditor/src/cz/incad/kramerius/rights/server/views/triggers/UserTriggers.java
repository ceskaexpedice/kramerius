package cz.incad.kramerius.rights.server.views.triggers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.descriptor.PropertyDTO;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

import cz.incad.kramerius.rights.server.Mailer;
import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.impl.PropertiesMailer;
import cz.incad.kramerius.rights.server.utils.GeneratePasswordUtils;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.rights.server.utils.GetCurrentLoggedUser;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.PasswordDigest;

public class UserTriggers extends PersisterTriggers.Default {

    public static final Logger LOGGER = Logger.getLogger(UserTriggers.class.getName());

    PropertiesMailer mailer =  new PropertiesMailer();

    public UserTriggers() {
        super();
    }

    @Override
    public void beforeCreate(Record record, Context ctx) {
        try {
            User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
            if ((user == null) || (!user.hasSuperAdministratorRole())) {
                List<Integer> groupsList = GetAdminGroupIds.getAdminGroupId(ctx);
                PropertyDTO<Integer> personalAdminDTO = Structure.user.PERSONAL_ADMIN.clientClone(ctx);
                personalAdminDTO.setValue(record, groupsList.get(0));
            }

            PropertyDTO<String> pswdDTO = Structure.user.PASSWORD.clientClone(ctx);
            String generated = GeneratePasswordUtils.generatePswd();

            GeneratePasswordUtils.sendGeneratedPasswordToMail( Structure.user.EMAIL.getValue(record), Structure.user.LOGINNAME.getValue(record), generated, mailer, ctx);

            pswdDTO.setValue(record, PasswordDigest.messageDigest(generated));

        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (AddressException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    @Override
    public void beforeUpdate(Record record, Context ctx) {/*
        String[] bfs = recordDTO.getModifiedByBfs();
        if (bfs.length == 0) {
            PropertyDTO<String> pswdDTO = structure.user.PASSWORD.clientClone(ctx);
            recordDTO.setNotForSave(pswdDTO, true);
            User user = GetCurrentLoggedUser.getCurrentLoggedUser(ctx.getHttpServletRequest());
            if ((user == null) || (!user.hasSuperAdministratorRole())) {
                PropertyDTO<Integer> personalAdminDTO = structure.user.PERSONAL_ADMIN.clientClone(ctx);
                recordDTO.setNotForSave(personalAdminDTO, true);
            }
        }
*/
    }

    @Override
    public void afterLoad(Record record, Context ctx){
        record.setPreview("<b>"+Structure.user.LOGINNAME.getValue(record)+"</b><br>"+Structure.user.SURNAME.getValue(record)+" "+Structure.user.NAME.getValue(record));
    }

    /*
    public Mailer getMailer() {
        return mailer;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }*/

}
