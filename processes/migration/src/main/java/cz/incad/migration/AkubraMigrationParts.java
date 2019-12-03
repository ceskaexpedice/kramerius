package cz.incad.migration;

import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.migration.Utils.BUILDER;
import static cz.incad.migration.Utils.MD5;
import static cz.incad.kramerius.utils.XMLUtils.*;

public enum AkubraMigrationParts {


    OBJECT_AND_STREAMS {
        @Override
        public void doMigrationPart() throws SQLException, IOException, SAXException {
            long start = System.currentTimeMillis();
            try {
                String objectsSource = KConfiguration.getInstance().getProperty("akubrafs.objects.source");
                String objectsTarget = KConfiguration.getInstance().getProperty("akubrafs.objects.target");

                String streamsSource = KConfiguration.getInstance().getProperty("akubrafs.streams.source");
                String streamsTarget = KConfiguration.getInstance().getProperty("akubrafs.streams.target");

                solrSelect(new File(objectsTarget), (pid)->{
                    try {
                        if (!pid.contains("/@")) {
                            String hex = Utils.asHex(MD5.digest(("info:fedora/"+pid).getBytes(Charset.forName("UTF-8"))));

                            File sourceDirectory = Utils.directory(new File(objectsSource), hex, 2, 1);
                            File sourceFile = new File(sourceDirectory, Utils.encode("info:fedora/" + pid));

                            File targetDirectory = Utils.directory(new File(objectsTarget), hex, 2, 3);
                            File targetFile = new File(targetDirectory, sourceFile.getName());

                            if (!targetDirectory.exists()) {  targetDirectory.mkdirs();  }

                            FileUtils.moveFile(sourceFile , targetFile);

                            Document parsed = BUILDER.parse(targetFile);
                            Element rootElement = parsed.getDocumentElement();

                            NodeList childNodes = rootElement.getChildNodes();
                            for (int j=0,lj=childNodes.getLength();j<lj;j++) {
                                Node n = childNodes.item(j);
                                if (n.getNodeType() == Node.ELEMENT_NODE) {
                                    Element elm = (Element) n;
                                    String state = elm.getAttribute("STATE");
                                    String controlGroup =elm.getAttribute("CONTROL_GROUP");
                                    if (state.equals("A") && controlGroup.equals("M")) {
                                        NodeList contentLocation = elm.getElementsByTagNameNS("info:fedora/fedora-system:def/foxml#","contentLocation");
                                        if (contentLocation.getLength() == 1) {
                                            Element elemLocation = (Element) contentLocation.item(0);
                                            String type = elemLocation.getAttribute("TYPE");
                                            if (type.equals("INTERNAL_ID")) {
                                                String ref = elemLocation.getAttribute("REF");

                                                String s = ref.replaceAll("\\+", "/");

                                                String hexStream = Utils.asHex(MD5.digest(("info:fedora/"+s).getBytes(Charset.forName("UTF-8"))));
                                                File sourceStreamFolder = Utils.directory(new File(streamsSource), hexStream, 2, 1);
                                                File destStreamFolder = Utils.directory(new File(streamsTarget), hexStream, 2, 3);

                                                FileUtils.moveFileToDirectory(new File(sourceStreamFolder, Utils.encode("info:fedora/"+s)) , destStreamFolder, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SAXException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                });
            }catch(IOException ex) {
                throw  new RuntimeException(ex);
            } finally {
                long stop = System.currentTimeMillis();
                LOGGER.info("POKUS "+(stop - start )+ " ms");
            }
        }

    };


    abstract  void doMigrationPart() throws SQLException, IOException, SAXException;


    static void fileSelect(File targetDir,  Consumer<String> consumer) throws IOException {
        File file = new File("pids_1.txt");
        FileReader freader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(freader);
        String line = null;
        while((line = bufferedReader.readLine()) != null) {
            consumer.accept(line);
        }
    }



    static void solrSelect(File targetDir,  Consumer<String> consumer) throws SQLException, IOException, SAXException {
        int start = 0;
        int numFound = Integer.MAX_VALUE;
        Element rootElm = returnStream(start);
        Element result = results(rootElm);
        numFound = Integer.parseInt(result.getAttribute("numFound"));
        do {
            List<Element> docs = getElements(result, new ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String localName = element.getLocalName();
                    return localName.equals("doc");
                }
            });
            docs.stream().forEach((doc) -> {
                Element pidElm = XMLUtils.findElement(doc, new ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element field) {
                        if (field.getLocalName().equals("str")) {
                            return (field.getAttribute("name").equals("PID"));
                        }
                        return false;
                    }
                });
                consumer.accept(pidElm.getTextContent());
            });
            start += ROWS;
            result = results(returnStream(start));
        } while(start < numFound);
    }

    private static Element results(Element rootElm) {
        Element result = findElement(rootElm, new ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getLocalName().equals("result");
            }
        });
        return result;
    }

    private static Element returnStream(int start) throws IOException, SAXException {
        long startRequest = System.currentTimeMillis();

        String reduce = "\""+ Arrays.asList(SOURCES).stream().reduce("", (i, t) -> {
            if (i.length()==0) {
                return  t;
            } else {
                return i + "\" OR \"" + t;
            }
        })+"\"";

        StringTemplate template = new StringTemplate(CDK_ADDRESS);
        template.setAttribute("condition", URLEncoder.encode(reduce,"UTF-8"));
        template.setAttribute("start", start);
        template.setAttribute("rows", ROWS);

        InputStream inputStream = RESTHelper.inputStream(template.toString(), "", "");
        Document parsed = Utils.BUILDER.parse(inputStream);
        LOGGER.info("\tRequest "+template.toString()+" took "+(System.currentTimeMillis() - startRequest)+ " ms ");
        return parsed.getDocumentElement();
    }


    public static String[] SOURCES = new String[] {
        "vc:3c06120c-ffc0-4b96-b8df-80bc12e030d9",
        "vc:b7b1b67a-25d1-4055-905d-45fedfc6a2b5",
        "vc:b7b1b67a-25d1-4055-905d-45fedfc6a2b5",
        "vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26",
        "vc:cd324f70-c034-46f1-9674-e0df4f93de86",
        "vc:d34ba74b-026a-4c60-aee7-9250a307952c",
        "vc:d4b466de-5435-4b76-bff7-2838bbae747b",
        "vc:f750b424-bda4-4113-849a-5e9dbbfb5846"
    };

    public static final int ROWS = 2000;

    public static final String CDK_ADDRESS="https://cdk.lib.cas.cz/search/api/v5.0/search?q=collection:($condition$)&fl=collection,PID&rows=$rows$&start=$start$";

    static Logger LOGGER = Logger.getLogger(AkubraMigrationParts.class.getName());

}
