package com.qbizm.kramerius.imptool.poc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * Sprava konfigurace programu - nacitani konfigurace z externiho souboru
 * 
 * @author xholcik
 */
public class ConfigurationUtils {

    private Properties properties;

    private static final ConfigurationUtils instance = new ConfigurationUtils();

    public static ConfigurationUtils getInstance() {
        return instance;
    }

    private ConfigurationUtils() {
        properties = new Properties();
        String configDirName = System.getProperty("user.home") + System.getProperty("file.separator") + ".kramerius";
        File configDir = new File(configDirName);
        if (!configDir.exists()) {
            configDir.mkdir();
        }

        String configFileName = configDirName + System.getProperty("file.separator") + "migration.properties";
        File configFile = new File(configFileName);
        try {
            if (configFile.createNewFile()) {
                Writer writer = new FileWriter(configFile);
                writer.append("uuid.mac=00:1b:63:bd:97:ba").append('\n');
                writer.append("k3.db.driver=org.postgresql.Driver").append('\n');
                writer.append("k3.db.url=jdbc:postgresql://194.108.215.227:5432/kramerius").append('\n');
                writer.append("k3.db.user=kramerius").append('\n');
                writer.append("k3.db.password=f8TasR").append('\n');
                writer.append("k3.replication.sigla=ABA002").append('\n');
                writer.append("k3.replication.login=replication").append('\n');
                writer.append("k3.replication.password=f29fabe637a472bf5222b12a0bc5df77").append('\n');
                writer.append("k3.replication.url=http://194.108.215.227:8080/kramerius/").append('\n');
                writer.append("migration.directory=/Work/Kramerius/data/replication").append('\n');
                writer.append("migration.periodicals=/Work/Kramerius/data/periodicals.txt").append('\n');
                writer.append("migration.monographs=/Work/Kramerius/data/monographs.txt").append('\n');
                writer.append("contractNo.length=5").append('\n');
                writer.close();
            }

            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

}
