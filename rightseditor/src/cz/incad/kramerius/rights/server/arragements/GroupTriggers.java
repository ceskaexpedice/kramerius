package cz.incad.kramerius.rights.server.arragements;

import java.util.logging.Logger;

import org.aplikator.client.data.RecordDTO;
import org.aplikator.client.descriptor.PropertyDTO;
import org.aplikator.server.persistence.PersisterTriggers;

import cz.incad.kramerius.rights.server.Structure;

public class GroupTriggers implements PersisterTriggers {
	
	private static Logger LOGGER = Logger.getLogger(GroupTriggers.class.getName());
	
	private Structure structure;
	
	@Override
	public RecordDTO beforeCreate(RecordDTO record) {
		
		
		PropertyDTO propertyDTO = record.getProperty(structure.group.PERSONAL_ADMIN.getReadableName());
		record.setValue(propertyDTO, 3);
		return record;
	}

	@Override
	public RecordDTO afterCreate(RecordDTO record) {
		return record;
	}

	@Override
	public RecordDTO beforeUpdate(RecordDTO recordDTO) {
		return recordDTO;
	}

	@Override
	public RecordDTO afterUpdate(RecordDTO recordDTO) {
		return recordDTO;
	}

	@Override
	public RecordDTO beforeDelete(RecordDTO recordDTO) {
		return recordDTO;
	}

	@Override
	public RecordDTO afterDelete(RecordDTO recordDTO) {
		return recordDTO;
	}
	
	
}
