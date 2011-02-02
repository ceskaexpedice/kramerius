package cz.incad.kramerius.rights.server.arragements.triggers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.users.AbstractUser;
import org.aplikator.client.data.RecordDTO;
import org.aplikator.client.descriptor.PropertyDTO;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.utils.GeneratePasswordUtils;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.security.utils.PasswordDigest;

public class UserTriggers extends AbstractUserTriggers implements PersisterTriggers {
	
	public static final Logger LOGGER = Logger.getLogger(UserTriggers.class.getName());
	
	private Structure structure;
	
	public UserTriggers(Structure structure) {
		super();
		this.structure = structure;
	}

	@Override
	public RecordDTO beforeCreate(RecordDTO record, Context ctx) {
		try {
			List<Integer> groupsList = GetAdminGroupIds.getAdminGroupId(ctx);
			PropertyDTO personalAdminDTO = structure.user.PERSONAL_ADMIN.clientClone(ctx);
			record.setValue(personalAdminDTO, groupsList.get(0));

			PropertyDTO pswdDTO = structure.user.PASSWORD.clientClone(ctx);
			String generated = GeneratePasswordUtils.generatePswd();

			record.setValue(pswdDTO, PasswordDigest.messageDigest(generated));
			
		} catch (NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
		return record;
	}

	@Override
	public RecordDTO afterCreate(RecordDTO record, Context ctx) {
		return null;
	}

	@Override
	public RecordDTO beforeUpdate(RecordDTO recordDTO, Context ctx) {
		return null;
	}

	@Override
	public RecordDTO afterUpdate(RecordDTO recordDTO, Context ctx) {
		return null;
	}

	@Override
	public RecordDTO beforeDelete(RecordDTO recordDTO, Context ctx) {
		return null;
	}

	@Override
	public RecordDTO afterDelete(RecordDTO recordDTO, Context ctx) {
		return null;
	}

	
}
