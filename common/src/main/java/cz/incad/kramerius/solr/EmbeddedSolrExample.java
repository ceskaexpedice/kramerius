package cz.incad.kramerius.solr;


public class EmbeddedSolrExample {
    
//    public static File createFolder() {
//        String workingDir = Constants.WORKING_DIR;
//        File f = new File(workingDir+File.separator+"solr");
//        if (f.exists()) {
//            f.mkdirs();
//        }
//        return f;
//    }
//    public static void main(String[] args) throws Exception {
//        String solrPath = KConfiguration.getInstance().getConfiguration().getString("solr.home",createFolder().getAbsolutePath());
//        CoreContainer container = new CoreContainer(solrPath);
//        System.out.println(container);
//        container.load();
//        EmbeddedSolrServer server = new EmbeddedSolrServer(container,"collection1");
//        Collection<File> files = FileUtils.listFiles(new File(System.getProperty("user.dir")), TrueFileFilter.INSTANCE,
//            TrueFileFilter.INSTANCE);
//        for (File file : files) {
//            String name = file.getName();
//            String content = FileUtils.readFileToString(file);
//            SolrInputDocument document = new SolrInputDocument();
//            document.addField("name", name);
//            document.addField("text", content);
//            document.addField("id", name.hashCode());
//            server.add(document);
//        }
//       
//        server.commit();
//
//        Thread.sleep(5000);
//        container.shutdown();
//        server.close();
//        
//        
//        System.out.println("Quering ");
//        container = new CoreContainer(solrPath);
//        container.load();
//        server = new EmbeddedSolrServer(container, "collection1");
//        ModifiableSolrParams solrParams = new ModifiableSolrParams();
//        solrParams.add(CommonParams.Q, "*:*");
//        QueryResponse queryResponse = server.query(solrParams);
//        for (SolrDocument document : queryResponse.getResults()) {
//            System.out.println(document);
//        }
//    }
}