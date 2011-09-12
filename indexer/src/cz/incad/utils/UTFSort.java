package cz.incad.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author alberto
 */
public class UTFSort {

    Map<String, String> maps = new HashMap<String, String>();
    
    public UTFSort() {
        
    }
    
    public void init() throws IOException{
        loadMapFile();
    }

    private void loadMapFile() throws IOException {
        
        InputStream is = UTFSort.class.getResourceAsStream("/cz/incad/kramerius/indexer/res/unicode_map.st");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String strLine;
        int sp;
        String l,t;
        
        //Read File Line By Line
        while ((strLine = br.readLine()) != null) {
            sp = strLine.indexOf(" ");
            l = strLine.substring(0, sp);
            //l = (char)Integer.parseInt(l, 16) + "";
            //System.out.println(l + " -> " + (char)Integer.parseInt(l, 16) + "");
            t = strLine.substring(sp+1);
            String r = "";
            for(String s:t.split(" ")){
                r += (char)Integer.parseInt(s, 16);
            }
            maps.put(l, r);
            
        }
        is.close();
    }
    
    public String translate(String old){
        String newStr = old;
        Iterator it = maps.keySet().iterator();
        String key;
        String c;
        while(it.hasNext()){
            key = (String) it.next();
            c = (char)Integer.parseInt(key, 16) + "";
            newStr = newStr.replace(c, maps.get(key));
        }
        return newStr;
    }
    
    public void printMap(){
        Iterator it = maps.keySet().iterator();
        String key;
        while(it.hasNext()){
            key = (String) it.next();
            System.out.println(key + " -> " + maps.get(key));
        }
    }
    
    public static void main(String[] args){
        try{
            UTFSort u = new UTFSort();
            u.init();
            //u.printMap();
            System.out.println(u.translate("která mají řadicí platnost (tj. č,ř,š,ž)"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
