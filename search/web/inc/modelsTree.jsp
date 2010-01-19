<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<%@ page language="java" pageEncoding="UTF-8" %>
<%
Facet pathFacet = facets.get("path");
            if ((pathFacet != null) &&
                    (pathFacet.infos.size() > 1)){
                
                
int totalCount = 0;
int a=0;
try{
    
    String outScript = "<script type='text/javascript'><!--\nmodelsTree = new dTree('modelsTree'); " +
            "\nmodelsTree.add(0,-1,'Models');";
    String outText = "";
    HashMap nodeNumbers = new HashMap();
    HashMap nodeValues = new HashMap();
    int maxNodeNum = 0;
    out.println("var paths = " + pathFacet.infos.size());
    for (int k = 0; k < pathFacet.infos.size(); k++) {
        FacetInfo facetInfo = pathFacet.infos.get(k);
        String urlInTree = facetInfo.name;
        String [] path = facetInfo.name.split("/");
        //String [] path = imod.getName().split("/");
        String nodeNumberName = "";
        String nodeValue = "";
        int parent = 0;
        
        outText += facetInfo.displayName + " <b>" + facetInfo.count + "</b>";
        //outText += imod.getName() + " <b>" + imod.getCount() + "</b>";
        
        for(int i = 0; i < path.length; i++){
          
            nodeNumberName = parent + "_" + path[i];            
            outScript += "\n// " + facetInfo.name + "  nodeNumberName> " + nodeNumberName ;
            //outScript += "\n// " + imod.getValue() + "  nodeNumberName> " + nodeNumberName ;
                
            if( nodeNumbers.get(nodeNumberName) != null ){ // existuje node neni nutny ho vytvorit 
                if( i == (path.length-1)){
                    nodeValue = "\n modelsTree.add(" + nodeNumbers.get(nodeNumberName) + "," +
                        parent + ",'" +
                        path[i] + "("  + facetInfo.count +
                        ")','" + urlInTree + "'); "; 
                        nodeValues.put(nodeNumberName, nodeValue);
                }     
                parent = ((Integer)nodeNumbers.get(nodeNumberName)).intValue();
            }else{ // neni musim vytvorit
                totalCount++;
                nodeNumbers.put(nodeNumberName, new Integer(++maxNodeNum));                
                if( i == (path.length-1)){
                    nodeValue = "\n modelsTree.add(" + nodeNumbers.get(nodeNumberName) + "," +
                        parent + ",'" +
                        path[i] + "("  + facetInfo.count +
                        ")','" + urlInTree + "'); "; 
                }else{
                      nodeValue = "\n modelsTree.add(" + nodeNumbers.get(nodeNumberName) + "," +
                        parent + ",'" +
                        path[i] + "',''); ";    
                }
                nodeValues.put(nodeNumberName, nodeValue);
                parent = maxNodeNum;
            }
              
            
        }
        a += facetInfo.count;
        outText += "<hr>";
    }
    out.print(outScript);
    Iterator itnodes = nodeValues.values().iterator();
    while (itnodes.hasNext()) {
        outScript += "\n// kk" ;
        out.print((String)itnodes.next());
    }
    //outScript += "document.write(modelsTree); \n--> \n</script> ";
    out.print("document.write(modelsTree); \n--> \n</script> ");
    
// out.print(outText);
}catch(Exception e){
    out.write("Chyba " + e.toString());
}
}

%>