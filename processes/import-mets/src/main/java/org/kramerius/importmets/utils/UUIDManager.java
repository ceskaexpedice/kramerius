package org.kramerius.importmets.utils;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kramerius.importmets.valueobj.ServiceException;
import org.safehaus.uuid.EthernetAddress;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;


import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Class for working with uuid
 * <p>
 * Copyright (c) 2006 Qbizm technologies, a.s. All rights reserved.
 * <p>
 * This software is the proprietary information of Qbizm technologies, a.s. Use is subject to
 * license terms.
 * <p>
 * Requirements: <br>
 * Keywords: <br>
 * 
 * @author <A href="mailto:pavel.rotek@qbizm.cz">Pavel Rotek</a>
 * @version $Revision: 1.11 $ $Date: 2008/05/06 16:01:06 $
 */
public class UUIDManager {

  /** logger */
  private static final Logger log = Logger.getLogger(UUIDManager.class);

  private static Pattern pattern = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");

  /**
   * instance of uuid generator
   */
  private static UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();

  /**
   * cached ethernet address of the server
   */
  private static EthernetAddress ea = null;

  static {
    String macAddress = KConfiguration.getInstance().getConfiguration().getString("uuid.mac");
    macAddress = macAddress.replaceAll("-", ":"); // pro jistotu
    if (StringUtils.isEmpty(macAddress)) {
      log.error("MAC address not set!");
    }
    ea = new EthernetAddress(macAddress);
  }

  /**
   * generate UUID version 2, type 1
   * 
   * @return uuid
   * @throws ServiceException
   */
  public static UUID generateUUID() throws ServiceException {
    if (ea != null) {
      return uuidGenerator.generateTimeBasedUUID(ea);
    } else {
      throw new ServiceException("No MAC address set, can't generate UUID!");
    }
  }

  /**
   * validate uuid string
   * 
   * @param uuid
   * @return true if uuid is valid uuid otherwise false
   */
  public static boolean validateUUID(String uuid) {
    if (StringUtils.trimToNull(uuid) == null)
      return false;
    return pattern.matcher(uuid).find();
  }
}
