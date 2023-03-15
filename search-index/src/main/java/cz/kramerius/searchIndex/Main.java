package cz.kramerius.searchIndex;

import com.google.common.collect.ObjectArrays;
import cz.kramerius.adapters.RepositoryAccess;
import cz.kramerius.adapters.ProcessingIndex;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexAccess;
import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.SolrInputBuilder;
import cz.kramerius.searchIndex.indexer.conversions.extraction.AudioAnalyzer;
import cz.kramerius.searchIndex.indexer.execution.IndexationType;
import cz.kramerius.searchIndex.indexer.execution.Indexer;
import cz.kramerius.searchIndex.krameriusRepositoryAccess.KrameriusRepositoryFascade;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNode;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNodeManager;
import cz.kramerius.adapters.impl.krameriusNewApi.ProcessingIndexImplByKrameriusNewApis;
import cz.kramerius.adapters.impl.krameriusNewApi.RepositoryAccessImplByKrameriusNewApis;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main {

    private static final boolean DEV_MODE = false;
    private static final boolean PRESENTATION_MODE = false; //v tomhle modu funguje CLI tak, ze ma na vstupu bud "ALL", cimz preindexuje vsechny zname dokumenty, nebo konretni PID(y)

    private static final String JAR_NAME = "indexer.jar";
    private static final String ACTION_BUILD_SOLR_PASSWORD_HASH = "build_solr_passord_hash";
    private static final String ACTION_CONVERT_AND_INDEX_FOXML_EXAMPLES = "convert_and_index_foxml_examples";
    private static final String ACTION_INDEX_OBJECT_FROM_KRAMERIUS = "index_object_from_kramerius";
    private static final String ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS = "index_objects_from_kramerius_with_process";
    private static final String ACTION_CLEAR_SOLR = "clear_solr";

    public static void main(String[] args) throws Exception {
        if (PRESENTATION_MODE) {
            if (args.length == 0) {
                System.err.println(buildPresentationUsage());
                System.exit(1);
            } else {
                switch (args[0]) {
                    case "ALL":
                        args = devArgsIndexBatch(pidsAll());
                        break;
                    default:
                        args = devArgsIndexBatch(args);
                        break;
                }
            }
        } else if (DEV_MODE) {
            args = devArgs();
        }
        if (args.length == 0) {
            System.err.println("Error: not enough parameters");
            System.err.println(buildUsage());
            System.exit(1);
        } else {
            String action = args[0];
            switch (action) {
                case ACTION_CLEAR_SOLR:
                    clearSolr(withoutFirstItem(args));
                    break;
                case ACTION_BUILD_SOLR_PASSWORD_HASH:
                    buildSolrBasicAuthPluginCredentials(withoutFirstItem(args));
                    break;
                case ACTION_CONVERT_AND_INDEX_FOXML_EXAMPLES:
                    indexFoxmlExamples(withoutFirstItem(args));
                    break;
                case ACTION_INDEX_OBJECT_FROM_KRAMERIUS:
                    indexObjectFromKramerius(withoutFirstItem(args));
                    break;
                case ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS:
                    indexObjectFromKrameriusWithProcess(withoutFirstItem(args));
                    break;
                default:
                    System.err.println("Error: unknown action '" + action + "'");
                    System.err.println(buildUsage());
            }
        }
    }


    private static String[] devArgs() {
        //====================
        // INDEX WITH PROCESS
        //====================

        //test
        /*return devArgsIndexBatch(new String[]{
                //"uuid:3a3e4190-6a1c-11dd-a532-000d606f5dc6",
                //"uuid:9a7feaa4-32e3-4806-93a7-771611da9cbf",

                //c3_
                "uuid:f478da13-33c2-4c5d-aab9-a7471b211a0a",
                //c4_
                "uuid:e7c94b4d-a980-4c40-9402-9c391806ea65",


                "uuid:9a7feaa4-32e3-4806-93a7-771611da9cbf",
                "uuid:ae7b3450-207c-409d-bf88-f60606f82b8f",
                "uuid:7b7223c8-510b-4a3c-91fa-1499fcd279f3",
                "uuid:dc8d1605-2791-45ec-abae-2049187e2357",
                "uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6",
                "uuid:fbf5efba-ff5f-4921-aee6-2d7f4141561b",
                "uuid:17fe155d-a975-11e0-a5e1-0050569d679d",
                "uuid:d55a3270-4937-11dd-afd9-000d606f5dc6",

                "uuid:9a7feaa4-32e3-4806-93a7-771611da9cbf",
                "uuid:ba4934d1-0a1e-4a01-a89d-c948477ca833",
                "uuid:7622b082-e64b-4b13-8507-01cab1f29340",
                "uuid:8f6e74d2-4f32-46e5-9b41-bb96d68164f0",

                //"5fe5b920-6e07-11dd-90a2-000d606f5dc6"

                "uuid:770808d3-4c2c-4f95-8d32-996fd25e0e90"
        });*/

        //periodika
        return devArgsIndexBatch(pidsPeriodicals());
        //monografie
        //return devArgsIndexBatch(pidsMonographs());
        //sbirky
        //return devArgsIndexBatch(pidsCollections());
        //ostatni
        //return devArgsIndexBatch(pidsOther());
        //vse
        //return devArgsIndexBatch(pidsAll());
        //test
        //return devArgsIndexBatch(pidsTest());
        //ocr test
        //return devArgsIndexBatch(pidsOcrTest());

        //jeden top-level objekt
        //return new String[]{ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS, "http://localhost:8080/search", "localhost:8983/solr", "search", "krameriusIndexer", "krameriusIndexerRulezz", "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6"};
        //return new String[]{ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS, "http://localhost:8080/search", "localhost:8983/solr", "search", "krameriusIndexer", "krameriusIndexerRulezz", "uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6"};
        //jen dokumenty s ocr
        //return devArgsIndexBatch(pidsWithOcr());

        //============
        // CLEAR SOLR
        //============

        //clear all solr cores
        //return new String[]{ACTION_CLEAR_SOLR, "localhost:8983/solr", "krameriusIndexer", "krameriusIndexerRulezz", "processing", "kramerius", "search"};
        //clear solr cores kramerius and processing
        //return new String[]{ACTION_CLEAR_SOLR, "localhost:8983/solr", "krameriusIndexer", "krameriusIndexerRulezz", "processing", "kramerius"};
        //clear solr core search
        //return new String[]{ACTION_CLEAR_SOLR, "localhost:8983/solr", "krameriusIndexer", "krameriusIndexerRulezz", "search};

        //=======
        // OTHER
        //=======

        //return new String[]{ACTION_BUILD_SOLR_PASSWORD_HASH, "krameriusIndexer", "krameriusIndexerRulezz"};
        //convert examples
        //return new String[]{ACTION_CONVERT_AND_INDEX_FOXML_EXAMPLES, "src/main/resources/conversion-foxml-to-solr-examples/monograph", "localhost:8983/solr", "search", "krameriusIndexer", "krameriusIndexerRulezz"};
        //monografie
        //return new String[]{ACTION_INDEX_OBJECT_FROM_KRAMERIUS, "http://localhost:8080/search", "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", "localhost:8983/solr", "search", "krameriusIndexer", "krameriusIndexerRulezz"};
        //stranka
        //return new String[]{ACTION_INDEX_OBJECT_FROM_KRAMERIUS, "http://localhost:8080/search", "uuid:431e4840-b03b-11dd-8818-000d606f5dc6", "localhost:8983/solr", "search", "krameriusIndexer", "krameriusIndexerRulezz"};
        //stranka s vlastnim rodicem i nevlastnim (tim je clanek)
        //return new String[]{ACTION_INDEX_OBJECT_FROM_KRAMERIUS, "http://localhost:8080/search", "uuid:a8263737-eb03-4107-9723-7200d00036f5", "localhost:8983/solr", "search", "krameriusIndexer", "krameriusIndexerRulezz"};
    }

    private static String[] pidsTest() {
        return new String[]{
                //m1_drobnustky
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6",
        };
    }

    private static String[] pidsWithOcr() {
        return new String[]{
                //m1_drobnustky
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6",
                //p3_mitteilungs_blatt
                "uuid:8f6e74d2-4f32-46e5-9b41-bb96d68164f0"
        };
    }

    private static String[] devArgsIndexBatch(String[] pids) {
        String[] args = new String[]{ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS, "http://localhost:8080/search", "localhost:8983/solr", "search", "krameriusIndexer", "krameriusIndexerRulezz"};
        return ObjectArrays.concat(args, pids, String.class);
    }

    private static String[] pidsPeriodicals() {
        return new String[]{
                //p1_deutsche_nachrichten_no_ocr
                //"uuid:af925a94-682c-4f18-9d25-0664c5d08dbb",
                //p2_lidovky_1941-1943
                "uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6",
                //p3_mitteilungs_blatt
                //"uuid:8f6e74d2-4f32-46e5-9b41-bb96d68164f0",
                //p4_brnenske_noviny (no longer available in MZK)
                //"uuid:a101de00-2119-11e3-a5bb-005056827e52"
        };
    }

    private static String[] pidsMonographs() {
        return new String[]{
                //m1_drobnustky
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6",
                //m2_monograph_units
                "uuid:1ad9c320-8be5-11e7-927c-001018b5eb5c",
        };
    }

    private static String[] pidsOcrTest() {
        return new String[]{
                //stranka z brnenske_noviny (ocr-text i ocr-alto na vstupu Managed jako base64 binaryContent)
                "uuid:4aac0ae0-2776-11e3-b79f-5ef3fc9bb22f",
                //stranka z mitteilungs_blatt (ocr-text i ocr-alto na vstupu Managed s odkazem na url)
                "uuid:c19133b2-c402-42ff-94ff-64a00390f4c9",
        };
    }

    private static String[] pidsCollections() {
        return new String[]{
                //c1_evropa
                "uuid:f478da13-33c2-4c5d-aab9-a7471b211a0a",
                //c2_cr
                "uuid:e7c94b4d-a980-4c40-9402-9c391806ea65",
                //c3_empty_test
                "uuid:c911e8b7-a2ec-4863-9a75-abad024dfe35",
                //test1
                "uuid:c911e8b7-a2ec-4863-9a75-abad024dfe35",
                //test2
                "uuid:c9ed9710-7d18-4680-8b96-3b9dd01a1822",
        };
    }

    private static String[] pidsOther() {
        return new String[]{
                //ARCHIVES
                //a1_avertissement
                "uuid:7b7223c8-510b-4a3c-91fa-1499fcd279f3",

                //GRAPHICS
                //g1_divadlo_v_kral_maste_plzni
                "uuid:3ea38826-01cb-4098-9994-6ffb3d4c8054",
                //g2_ceska_ulice_c3
                "uuid:9a7feaa4-32e3-4806-93a7-771611da9cbf",
                //g3_dlazba_kostela
                "uuid:ae7b3450-207c-409d-bf88-f60606f82b8f",
                //g4_chrlice_u_brna
                "uuid:61acc90c-52df-473b-8c2d-02adcaaf66e3",

                //MANUSCRIPTS
                //m1_erganzung_zur_gesinde_ordnung
                "uuid:770808d3-4c2c-4f95-8d32-996fd25e0e90",
                //m2_with_internal_part
                "uuid:dc8d1605-2791-45ec-abae-2049187e2357",

                //MAPS
                //m1_situations_plan_der_landeshauptstadt_brunn
                "uuid:093527ff-2652-433c-afe0-ed9c1d5a9eb8",
                //m2_geologicka_mapa_zem_i_koruny_ceske
                "uuid:3572124e-d1bd-4f4e-8dfb-b16045e8aa80",
                //m3_plan_velkeho_brna
                "uuid:ba4934d1-0a1e-4a01-a89d-c948477ca833",
                //m4_karte_von_ireland
                "uuid:0c5e9951-253e-4566-8c43-9599cfe0374e",
                //m5_kort_over_gronland
                "uuid:14ea9442-b077-46b3-a70e-dea858549a2e",
                //m6_general_carte_von_gross_britannien
                "uuid:8fe89821-9756-4b1b-b8fd-ab8521abdb5a",
                //m7_ceske_velnice
                "uuid:66146f27-8376-44f1-a8aa-b35b1e18fff7",
                //m8_cejkov1
                "uuid:2412ae3c-b785-4cb0-83ff-2896bf885c04",
                //TODO: bug, nedari se obcas indexovat starym indexerem (nedeterministicke, po chvilce uz to slo)
                //https://kramerius.dev.digitallibrary.cz/search/inc/admin/_processes_outputs.jsp?uuid=bba06a32-a5b3-4e49-9f1e-ab1ee268af98
                //m9_cejkov2
                "uuid:7abe3bfb-d094-4a27-a9d5-e1fe63be7a8b",

                //SHEETMUSIC
                //s1_quintetto_in_f
                "uuid:17fe155d-a975-11e0-a5e1-0050569d679d",

                //SOUNDRECORDING
                //s1_klavirni_skladby
                "uuid:fbf5efba-ff5f-4921-aee6-2d7f4141561b",
                //s2_zdravy_nemocny
                "uuid:1cc63251-50dc-4e5d-ae97-6a48ff38526c",
                //s3_bile_noci
                "uuid:b332069d-c5b0-47c9-8364-ee6094472814",
                //s4_a_letter_home
                "uuid:05452672-81f0-4121-b545-4572913b382e",
        };
    }

    private static String[] pidsAll() {
        List<String[]> pidArrays = new ArrayList<>();
        pidArrays.add(pidsPeriodicals());
        pidArrays.add(pidsMonographs());
        pidArrays.add(pidsCollections());
        pidArrays.add(pidsOther());
        List<String> pidsAll = new ArrayList<>();
        for (String[] pidArray : pidArrays) {
            pidsAll.addAll(Arrays.asList(pidArray));
        }
        String[] result = new String[pidsAll.size()];
        pidsAll.toArray(result);
        return result;
    }

    private static void clearSolr(String[] args) throws IOException, SolrServerException {
        if (args.length < 4) {
            System.err.println("Error: not enough parameters");
            System.err.println(buildUsage(ACTION_CLEAR_SOLR));
        } else {
            String solrBaseUrl = args[0];
            boolean solrUseHttps = false;
            String solrLogin = args[1];
            String solrPassword = args[2];
            String[] solrCollections = subArray(args, 3);
            for (String solrCollection : solrCollections) {
                System.out.println("clearing collection " + solrCollection);
                new SolrIndexAccess(new SolrConfig(solrBaseUrl, solrCollection, solrUseHttps, solrLogin, solrPassword)).deleteAll();
            }
        }
    }

    private static void indexObjectFromKrameriusWithProcess(String[] args) {
        if (args.length < 9) {
            System.err.println("Error: not enough parameters");
            System.err.println(buildUsage(ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS));
        } else {
            int index = 0;
            //Kramerius
            String krameriusBackendBaseUrl = args[index++];
            String krameriusApiAuthClient = args[index++];
            String krameriusApiAuthUid = args[index++];
            String krameriusApiAuthAccessToken = args[index++];
            //SOLR
            String solrBaseUrl = args[index++];
            String solrCollection = args[index++];
            boolean solrUseHttps = false;
            String solrLogin = args[index++];
            String solrPassword = args[index++];

            //pids
            String[] pids = subArray(args, 5);
            for (String pid : pids) {
                SolrConfig solrConfig = new SolrConfig(solrBaseUrl, solrCollection, solrUseHttps, solrLogin, solrPassword);
                //TODO: extract to param
                //String krameriusBackendBaseUrl = "http://localhost:8080/search";
                //FedoraAccess repository = new RepositoryAccessImplDummy();
                //FedoraAccess repository = new RepositoryAccessImplByKrameriusOldApis(krameriusBackendBaseUrl);
                RepositoryAccess repository = new RepositoryAccessImplByKrameriusNewApis(krameriusBackendBaseUrl,
                        new RepositoryAccessImplByKrameriusNewApis.Credentials(krameriusApiAuthClient, krameriusApiAuthUid, krameriusApiAuthAccessToken));
                //IResourceIndex resourceIndex = new ResourceIndexImplByKrameriusOldApis(krameriusBackendBaseUrl);
                ProcessingIndex resourceIndex = new ProcessingIndexImplByKrameriusNewApis(krameriusBackendBaseUrl);
                KrameriusRepositoryFascade krameriusRepositoryFascade = new KrameriusRepositoryFascade(repository, resourceIndex);
                Indexer process = new Indexer(krameriusRepositoryFascade, solrConfig, System.out, false);
                //process.indexByObjectPid(pid, IndexationType.TREE);
                //process.indexByObjectPid(pid, IndexationType.OBJECT);
                //process.indexByObjectPid(pid, IndexationType.OBJECT_AND_CHILDREN);
                process.indexByObjectPid(pid, IndexationType.TREE, null);
            }
        }
    }

    private static void indexObjectFromKramerius(String[] args) throws IOException, DocumentException, SolrServerException {
        if (args.length < 9) {
            System.err.println("Error: not enough parameters");
            System.err.println(buildUsage(ACTION_INDEX_OBJECT_FROM_KRAMERIUS));
        } else if (args.length > 9) {
            System.err.println("Error: too many parameters");
            System.err.println(buildUsage(ACTION_INDEX_OBJECT_FROM_KRAMERIUS));
        } else {
            int index = 0;
            //Kramerius
            String krameriusBackendBaseUrl = args[index++];
            String krameriusApiAuthClient = args[index++];
            String krameriusApiAuthUid = args[index++];
            String krameriusApiAuthAccessToken = args[index++];
            //SOLR
            String solrBaseUrl = args[index++];
            String solrCollection = args[index++];
            boolean solrUseHttps = false;
            String solrLogin = args[index++];
            String solrPassword = args[index++];
            //pid
            String pid = args[index++];

            //FedoraAccess repository = new RepositoryAccessImplDummy();
            //FedoraAccess repository = new RepositoryAccessImplByKrameriusOldApis(krameriusBackendBaseUrl);
            RepositoryAccess repository = new RepositoryAccessImplByKrameriusNewApis(krameriusBackendBaseUrl,
                    new RepositoryAccessImplByKrameriusNewApis.Credentials(krameriusApiAuthClient, krameriusApiAuthUid, krameriusApiAuthAccessToken));
            //IResourceIndex processingIndex = new ResourceIndexImplByKrameriusOldApis(krameriusBackendBaseUrl);
            ProcessingIndex processingIndex = new ProcessingIndexImplByKrameriusNewApis(krameriusBackendBaseUrl);
            KrameriusRepositoryFascade repositoryAdapter = new KrameriusRepositoryFascade(repository, processingIndex);
            RepositoryNodeManager nodeManager = new RepositoryNodeManager(repositoryAdapter, false);
            SolrInputBuilder solrInputBuilder = new SolrInputBuilder();
            SolrIndexAccess solrAccess = new SolrIndexAccess(new SolrConfig(solrBaseUrl, solrCollection, solrUseHttps, solrLogin, solrPassword));

            boolean objectAvailable = repositoryAdapter.isObjectAvailable(pid);
            if (!objectAvailable) {
                throw new IOException("object " + pid + " not available");
            }
            Document foxmlDoc = repositoryAdapter.getObjectFoxml(pid, true);
            //the isOcrTextAvailable method (and for other datastreams) is inefficient for implementation through http stack (because of HEAD requests)
           /* boolean ocrAvailable = repositoryAdapter.isOcrTextAvailable(pid);
            if (!ocrAvailable) {
                System.out.println("ocr text not available");
            } else {
                System.out.println("ocr text available");
            }*/
            //String ocrText = repositoryAdapter.isOcrTextAvailable(pid) ? repositoryAdapter.getOcrText(pid) : null;
            String ocrText = repositoryAdapter.getOcrText(pid);
            //System.out.println("ocr text: " + ocrText);
            RepositoryNode repositoryNode = nodeManager.getKrameriusNode(pid);
            if (repositoryNode == null) {
                System.err.println("object not found or in inconsistent state: " + pid + ", ignoring");
            } else {
                String imgFullMime = repositoryAdapter.getImgFullMimetype(pid);
                Integer audioLength = "track".equals(repositoryNode.getModel()) ? detectAudioLength(repositoryNode.getPid(), repositoryAdapter) : null;
                SolrInput solrInput = solrInputBuilder.processObjectFromRepository(foxmlDoc, ocrText, repositoryNode, nodeManager, imgFullMime, audioLength, true);
                String solrInputStr = solrInput.getDocument().asXML();
                //System.out.println(solrInputStr);
                System.out.println("indexing " + pid);
                solrAccess.indexFromXmlString(solrInputStr, true);
            }
        }
    }

    private static Integer detectAudioLength(String pid, KrameriusRepositoryFascade repositoryConnector) {
        try {
            AudioAnalyzer analyzer = new AudioAnalyzer();
            if (repositoryConnector.isAudioWavAvailable(pid)) {
                AudioAnalyzer.Result result = analyzer.analyze(repositoryConnector.getAudioWav(pid), AudioAnalyzer.Format.WAV);
                return result.duration;
            }
            System.out.println("failed to detect audio length of " + pid);
            return null;
        } catch (IOException | UnsupportedAudioFileException e) {
            System.err.println("error extracting audio length from " + pid);
            e.printStackTrace();
            return null;
        }
    }

    private static void indexFoxmlExamples(String[] args) throws IOException, DocumentException, SolrServerException {
        if (args.length < 5) {
            System.err.println("Error: not enough parameters");
            System.err.println(buildUsage(ACTION_CONVERT_AND_INDEX_FOXML_EXAMPLES));
        } else if (args.length > 5) {
            System.err.println("Error: too many parameters");
            System.err.println(buildUsage(ACTION_CONVERT_AND_INDEX_FOXML_EXAMPLES));
        } else {
            File foxmlDir = new File(args[0]);
            String solrBaseUrl = args[1];
            String solrCollection = args[2];
            boolean solrUseHttps = false;
            String solrLogin = args[3];
            String solrPassword = args[4];

            System.out.println("converting foxml -> solr_import_xml (in " + foxmlDir.getAbsolutePath() + ") and indexing into " + solrBaseUrl);
            SolrInputBuilder builder = new SolrInputBuilder();
            SolrIndexAccess solrAccess = new SolrIndexAccess(new SolrConfig(solrBaseUrl, solrCollection, solrUseHttps, solrLogin, solrPassword));
            String[] foxmlFilenames = foxmlDir.list((dir, name) -> name.endsWith(".foxml.xml"));
            for (String foxmlFilename : foxmlFilenames) {
                String pid = foxmlFilename.substring(0, foxmlFilename.length() - ".foxml.xml".length());
                System.out.println("processing " + foxmlFilename);
                File outSolrInputFile = new File(foxmlDir, pid + ".solr.xml");
                builder.convertFoxmlToSolrInput(new File(foxmlDir, foxmlFilename), outSolrInputFile);
                //TODO: indexation temporarily disabled
                //solrAccess.indexFromXmlFile(outSolrInputFile, true);
            }
            System.out.println("converted " + foxmlFilenames.length + " files");
        }
    }

    private static void convertFoxmlsToSolrInputs(File monDir) throws IOException, DocumentException {
        System.out.println("converting foxml -> solr_import_xml (in " + monDir.getAbsolutePath() + ")");
        SolrInputBuilder builder = new SolrInputBuilder();
        String[] foxmlFilenames = monDir.list((dir, name) -> name.endsWith(".foxml.xml"));
        for (String foxmlFilename : foxmlFilenames) {
            String pid = foxmlFilename.substring(0, foxmlFilename.length() - ".foxml.xml".length());
            builder.convertFoxmlToSolrInput(new File(monDir, foxmlFilename), new File(monDir, pid + ".solr.xml"));
        }
        System.out.println("converted " + foxmlFilenames.length + " files");
    }

    private static void buildSolrBasicAuthPluginCredentials(String[] args) {
        if (args.length < 2) {
            System.err.println("Error: not enough parameters");
            System.err.println(buildUsage(ACTION_BUILD_SOLR_PASSWORD_HASH));
        } else if (args.length > 2) {
            System.err.println("Error: too many parameters");
            System.err.println(buildUsage(ACTION_BUILD_SOLR_PASSWORD_HASH));
        } else {
            String login = args[0];
            String password = args[1];
            try {
                System.out.println("\t\"credentials\": {");
                System.out.println("\t\t\"" + login + "\": \"" + Utils.buildHashOfSaltAndPasswordForSolr(password) + "\"");
                System.out.println("\t}");
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Error: unknown algorithm: " + e.getMessage());
            }
        }
    }

    private static String[] withoutFirstItem(String[] original) {
        String[] result = new String[original.length - 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = original[i + 1];
        }
        return result;
    }

    private static String buildPresentationUsage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Usage: java -jar ").append(JAR_NAME).append(" ALL|pid(s)").append('\n');
        builder.append("\tS hodnotou ALL přeindexuje všechny známé (hardcoded) objekty, s konkrétním pidem/seznamem pidů jen vybraný objekt/objekty").append('\n');
        builder.append('\n');
        return builder.toString();
    }

    private static String buildUsage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Usage: java -jar ").append(JAR_NAME).append(" ACTION").append('\n');
        builder.append('\t').append("available actions: ");
        builder.append(ACTION_BUILD_SOLR_PASSWORD_HASH).append(", ");
        builder.append(ACTION_CONVERT_AND_INDEX_FOXML_EXAMPLES).append(", ");
        builder.append(ACTION_INDEX_OBJECT_FROM_KRAMERIUS).append(", ");
        builder.append(ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS).append(", ");
        builder.append(ACTION_CLEAR_SOLR);
        builder.append('\n');
        return builder.toString();
    }

    private static String buildUsage(String action) {
        StringBuilder builder = new StringBuilder();
        builder.append("Usage: java -jar ").append(JAR_NAME).append(" ").append(action);
        switch (action) {
            case ACTION_BUILD_SOLR_PASSWORD_HASH:
                builder.append(" LOGIN PASSWORD").append('\n');
                builder.append('\t').append("Result of this action is credentials containing login, Base64-encoded hash of password+salt and Base64-encoded salt. " +
                        "This should be put in security.json in authentication section for BasicAuthPlugin. For example:").append('\n');
                builder.append("\"credentials\": {").append('\n');
                builder.append('\t').append("\"krameriusIndexer\": \"IV0EHq1OnNrj6gvRCwvFwTrZ1+z1oBbnQdiVC3otuq0= Ndd7LKvVBAaZIF0QAVi1ekCfAJXr1GGfLtRUXhgrF8c=\"").append('\n');
                builder.append("}").append('\n');
                break;
            case ACTION_CONVERT_AND_INDEX_FOXML_EXAMPLES:
                builder.append(" KRAMERIUS_BACKEND_BASE_URL FOXML_DIR SOLR_BASE_URL SOLR_COLLECTION SOLR_LOGIN SOLR_PASSWORD").append('\n');
                builder.append('\t').append("The BASE_URL mustn't start with protocol prefix and must end with '/solr' suffix, for example: \"localhost:8983/solr\"").append('\n');
                break;
            case ACTION_INDEX_OBJECT_FROM_KRAMERIUS:
                builder.append(" KRAMERIUS_BACKEND_BASE_URL KRAMERIUS_API_AUTH_CLIENT KRAMERIUS_API_AUTH_UID KRAMERIUS_API_AUTH_ACCESS_TOKEN" +
                        " SOLR_BASE_URL SOLR_LOGIN SOLR_COLLECTION SOLR_PASSWORD" +
                        " OBJECT_PID").append('\n');
                builder.append('\t').append("The SOLR_BASE_URL mustn't start with protocol prefix and must end with '/solr' suffix, for example: \"localhost:8983/solr\"").append('\n');
                builder.append('\t').append("The KRAMERIUS_BACKEND_BASE_URL, on the other hand, must be an url with defined protocol, for example 'http://localhost:8080/search'").append('\n');
                break;
            case ACTION_INDEX_OBJECTS_FROM_KRAMERIUS_WITH_PROCESS:
                builder.append(" KRAMERIUS_BACKEND_BASE_URL KRAMERIUS_API_AUTH_CLIENT KRAMERIUS_API_AUTH_UID KRAMERIUS_API_AUTH_ACCESS_TOKEN" +
                        " SOLR_BASE_URL SOLR_LOGIN SOLR_COLLECTION SOLR_PASSWORD" +
                        " OBJECTS_PIDS").append('\n');
                builder.append('\t').append("The SOLR_BASE_URL mustn't start with protocol prefix and must end with '/solr' suffix, for example: \"localhost:8983/solr\"").append('\n');
                builder.append('\t').append("The KRAMERIUS_BACKEND_BASE_URL, on the other hand, must be an url with defined protocol, for example 'http://localhost:8080/search'").append('\n');
                break;
            case ACTION_CLEAR_SOLR:
                builder.append(" SOLR_BASE_URL SOLR_LOGIN SOLR_PASSWORD SOLR_COLLECTIONS").append('\n');
                builder.append('\t').append("The SOLR_BASE_URL mustn't start with protocol prefix and must end with '/solr' suffix, for example: \"localhost:8983/solr\"").append('\n');
                break;
        }
        return builder.toString();
    }

    private static String[] subArray(String[] array, int start) {
        return Arrays.copyOfRange(array, start, array.length);
    }
}
