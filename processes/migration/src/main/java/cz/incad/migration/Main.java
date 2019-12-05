package cz.incad.migration;

import cz.incad.kramerius.utils.conf.KConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.logging.Logger;

public class Main {

    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());



    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args.length < 5) {
            //for (String arg : args) {
                Command.valueOf(args[0].toUpperCase()).doCommand(args);
            //}
        } else {
            Arrays.stream(Command.values()).forEach(cmd->{
                System.out.println(cmd.name());
                System.out.println(cmd.desc());
                System.out.println();
            });
        }
    }

    static enum Command {

 /*       AKUBRA {
            @Override
            public void doCommand() throws Exception {
                AkubraMigrationParts.OBJECT_AND_STREAMS.doMigrationPart();
            }

            @Override
            public String desc() {
                StringBuilder builder = new StringBuilder();
                builder.append("Migrace z  akubra_fs(##) -> akubra_fs(##/##/##) ").append('\n');
                builder.append("Nutne promenne pro migraci: ").append('\n');

                builder.append("\takubrafs.streams.source").append(" - adresar zdrojoveho akubra_fs").append('\n');
                builder.append("\takubrafs.objects.source").append(" - adresar zdrojoveho akubra_fs").append('\n');

                builder.append("\takubrafs.streams.target").append(" - adresar ciloveho akubra_fs").append('\n');
                builder.append("\takubrafs.objects.target").append(" - adresar ciloveho akubra_fs").append('\n');

                return builder.toString();
            }
        },
*/
        LEGACY {
            @Override
            public void doCommand(String[] args) throws Exception {
                Class.forName("org.postgresql.Driver");
                Connection db = null;
                try {
                    String url = KConfiguration.getInstance().getProperty("legacyfs.jdbcURL");
                    String userName = KConfiguration.getInstance().getProperty("legacyfs.dbUsername");
                    String userPass = KConfiguration.getInstance().getProperty("legacyfs.dbPassword");

                    db = DriverManager.getConnection(url, userName, userPass);

                    LOGGER.info("Moving streams ... ");
                    LegacyMigrationParts.STREAMS.doMigrationPart(db, args);
                    LOGGER.info("Finished moving streams.");
                    LOGGER.info("Moving objects ...");
                    LegacyMigrationParts.OBJECTS.doMigrationPart(db, args);
                    LOGGER.info("Finished moving objects.");
                } finally {
                    db.close();
                }
            }

            @Override
            public String desc() {
                StringBuilder builder = new StringBuilder();
                builder.append("Migrace z legacy_fs -> akubra_fs").append('\n');
                builder.append("Nutne promenne pro migraci: ").append('\n');
                builder.append("\tlegacyfs.jdbcURL").append(" - db konekce do fedory").append('\n');
                builder.append("\tlegacyfs.dbUsername").append(" - db uzivatel").append('\n');
                builder.append("\tlegacyfs.dbPassword").append(" - db pass").append('\n');

                builder.append("\tdatastreamStore.path").append(" - adresar ciloveho akubra_fs").append('\n');
                builder.append("\tobjectStore.path").append(" - adresar ciloveho akubra_fs pro objekty").append('\n');

                return builder.toString();
            }
        };


        public abstract  void doCommand(String[] args) throws Exception;

        public abstract String desc();
    }

}
