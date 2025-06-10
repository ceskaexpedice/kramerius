package cz.incad.migration;

import cz.incad.kramerius.resourceindex.ProcessingIndexRebuild;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.NewIndexerProcessIndexModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.logging.Logger;

public class Main {

    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            Command.valueOf(args[0].toUpperCase()).doCommand(args);
        } else {
            Arrays.stream(Command.values()).forEach(cmd->{
                System.out.println(cmd.name());
                System.out.println(cmd.desc());
                System.out.println();
            });
        }
    }

    static enum Command {

        AKUBRA {
            @Override
            public void doCommand(String[] args) throws Exception {
                AkubraMigrationParts.OBJECT_AND_STREAMS.doMigrationPart(args);
            }

            @Override
            public String desc() {
                StringBuilder builder = new StringBuilder();
                builder.append("Migrace z  akubra_fs() nebo legacy_fs() -> akubra_fs(pattern) ").append('\n');
                builder.append("Parametry: AKUBRA nebo AKUBRA legacy").append('\n');
                builder.append("Nutne promenne pro migraci: ").append('\n');

                builder.append("\tdatastreamStore.migrationsource").append(" - adresar zdrojoveho akubra_fs nebo legacy_fs pro datastreamy").append('\n');
                builder.append("\tobjectStore.migrationsource").append(" - adresar zdrojoveho akubra_fs nebo legacy_fs pro objekty").append('\n');

                builder.append("\tdatastreamStore.path").append(" - adresar ciloveho akubra_fs pro datastreamy").append('\n');
                builder.append("\tobjectStore.path").append(" - adresar ciloveho akubra_fs pro objekty").append('\n');
                builder.append("\tdatastreamStore.pattern").append(" -  struktura ciloveho adresare akubra_fs pro datastreamy").append('\n');
                builder.append("\tobjectStore.pattern").append(" - struktura ciloveho adresare akubra_fs pro objekty").append('\n');

                return builder.toString();
            }
        },

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
                builder.append("Migrace z legacy_db -> akubra_fs").append('\n');
                builder.append("Parametry: LEGACY 0 1000 0 1000 false -m||-c").append('\n');
                builder.append("2. param., od kterého tokendbid v tabulce datastreampaths chci datastreams nacitat").append('\n');
                builder.append("3. param., před kterým tokendbid v tabulce datastreampaths chci skončit").append('\n');
                builder.append("4. param., od kterého tokendb id v tabulce objectpaths chci objects načítat").append('\n');
                builder.append("5. param., před kterým tokendb id v tabulce objectpaths chci skončit").append('\n');
                builder.append("6. param., pokud nepřevádím všechna data najednou, tak musím process index pouštět až po převední všeho. Takže musí býd vždy false.").append('\n');
                builder.append("7. param., -c soubory se budou kopirovat nebo -m soubory se budou presouvat").append('\n');
                builder.append("-c pokud se budou soubory kopirovat, budou se rovnez porovnavat(zdroj,cil)").append('\n');
                builder.append("Pokud posledni parametr vůbec nezadate, bude se cist pouze vstupni databaze, se soubory se nebude delat nic - (zkusebni rezim)").append('\n');
                builder.append("Nutne konfiguracni promenne pro migraci: ").append('\n');
                builder.append("\tlegacyfs.jdbcURL").append(" - db konekce do fedory").append('\n');
                builder.append("\tlegacyfs.dbUsername").append(" - db uzivatel").append('\n');
                builder.append("\tlegacyfs.dbPassword").append(" - db pass").append('\n');

                builder.append("\tdatastreamStore.path").append(" - adresar ciloveho akubra_fs pro datastreamy").append('\n');
                builder.append("\tobjectStore.path").append(" - adresar ciloveho akubra_fs pro objekty").append('\n');
                builder.append("\tdatastreamStore.pattern").append(" -  struktura ciloveho adresare akubra_fs pro datastreamy").append('\n');
                builder.append("\tobjectStore.pattern").append(" - struktura ciloveho adresare akubra_fs pro objekty").append('\n');

                return builder.toString();
            }
        },

        REBUILDPROCESSING {
            @Override
            public void doCommand(String[] args) throws Exception {
                ProcessingIndexRebuild.main(args);
            }

            @Override
            public String desc() {
                StringBuilder builder = new StringBuilder();
                builder.append("Rebuild processing indexu. ").append('\n');
                builder.append("Parametry: REBUILDPROCESSING").append('\n');
                builder.append("Nutne promenne pro migraci: ").append('\n');
                builder.append("\tobjectStore.path").append(" - adresar akubra_fs pro objekty").append('\n');
                return builder.toString();
            }
        },

        REBUILDSEARCH {
            @Override
            public void doCommand(String[] args) throws Exception {
                NewIndexerProcessIndexModel.main(new String[]{"xxxtoken", "TREE_AND_FOSTER_TREES", args[1],"true","true","true","true", "true", "false"});
            }

            @Override
            public String desc() {
                StringBuilder builder = new StringBuilder();
                builder.append("Rebuild search indexu. ").append('\n');
                builder.append("Parametry: REBUILDSEARCH model:modelname").append('\n');
                builder.append("Nutne promenne pro migraci: ").append('\n');
                builder.append("\tobjectStore.path").append(" - adresar akubra_fs pro objekty").append('\n');
                return builder.toString();
            }
        };


        public abstract  void doCommand(String[] args) throws Exception;

        public abstract String desc();
    }

}

