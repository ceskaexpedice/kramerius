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
    //HashMap nodeNumbers = new HashMap();
    //HashMap nodeValues = new HashMap();
    ArrayList<String> nodeNumberNames = new ArrayList<String>();
    ArrayList<Integer> nodeNumbers = new ArrayList<Integer>();
    ArrayList<String> nodeValues = new ArrayList<String>();
    int maxNodeNum = 0;
    
    for (int k = 0; k < pathFacet.infos.size(); k++) {
        FacetInfo facetInfo = pathFacet.infos.get(k);
        String urlInTree = "path#" + facetInfo.name;
        String onclickInTree = "addNavigation(\\'path\\', \\'" + facetInfo.name + "\\')";
        String [] path = facetInfo.name.split("/");
        String nodeNumberName = "";
        String nodeValue = "";
        int parent = 0;
        
        outText += facetInfo.displayName + " <b>" + facetInfo.count + "</b>";
        int index = -1;
        for(int i = 0; i < path.length; i++){
          
            nodeNumberName = parent + "_" + path[i];            
            outScript += "\n// " + facetInfo.name + "  nodeNumberName> " + nodeNumberName ;
                
            //if( nodeNumbers.containsKey(nodeNumberName) ){ // existuje node neni nutny ho vytvorit 
            index = nodeNumberNames.indexOf(nodeNumberName);
            if( index>-1 ){ // existuje node neni nutny ho vytvorit 
                
                if( i == (path.length-1)){
                    //nodeValue = "\n modelsTree.add(" + nodeNumbers.get(nodeNumberName) + "," +
                    nodeValue = "\n modelsTree.add(" + nodeNumbers.get(index) + "," +
                        parent + ",'" +
                        path[i] + "("  + facetInfo.count +
                        ")','" + urlInTree + "','" + onclickInTree + "'); "; 
                        //nodeValues.put(nodeNumberName, nodeValue);
                        nodeValues.set(index, nodeValue);
                }     
                //parent = ((Integer)nodeNumbers.get(nodeNumberName)).intValue();
                parent = nodeNumbers.get(index).intValue();
            }else{ // neni musim vytvorit
                totalCount++;
                //nodeNumbers.put(nodeNumberName, new Integer(++maxNodeNum));             
                nodeNumberNames.add(nodeNumberName);          
                maxNodeNum++;
                nodeNumbers.add(new Integer(maxNodeNum));           
                if( i == (path.length-1)){
                    //nodeValue = "\n modelsTree.add(" + nodeNumbers.get(nodeNumberName) + "," +
                    nodeValue = "\n modelsTree.add(" + maxNodeNum + "," +
                        parent + ",'" +
                        path[i] + "("  + facetInfo.count +
                        ")','" + urlInTree + "','" + onclickInTree + "'); "; 
                }else{
                      //nodeValue = "\n modelsTree.add(" + nodeNumbers.get(nodeNumberName) + "," +
                      nodeValue = "\n modelsTree.add(" + maxNodeNum + "," +
                        parent + ",'" +
                        path[i] + "',''); ";    
                }
                //nodeValues.put(nodeNumberName, nodeValue);
                nodeValues.add(nodeValue);
                parent = maxNodeNum;
            }
              
            
        }
        a += facetInfo.count;
        outText += "<hr>";
    }
    out.print(outScript);
    //Iterator itnodes = nodeValues.values().iterator();
    Iterator itnodes = nodeValues.iterator();
    while (itnodes.hasNext()) {
        outScript += "\n// kk" ;
        out.print((String)itnodes.next());
    }
    //outScript += "document.write(modelsTree); \n--> \n</script> ";
    out.println("document.write(modelsTree); \n--> ");
    out.println("</script> ");
    
// out.print(outText);
}catch(Exception e){
    out.write("Chyba " + e.toString());
}
}

%>