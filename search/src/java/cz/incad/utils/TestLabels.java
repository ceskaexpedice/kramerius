package cz.incad.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class TestLabels {
    public static void main(String[] args) throws IOException {
        Properties czProps = new Properties();
        czProps.load(TestLabels.class.getClassLoader().getResourceAsStream("labels_cs.properties"));

        Properties enProps = new Properties();
        enProps.load(TestLabels.class.getClassLoader().getResourceAsStream("labels_en.properties"));

        Set<Object> keySet = czProps.keySet();
        for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
            Object object = (Object) iterator.next();
            if (!enProps.containsKey(object)) {
                System.out.println("Chybi =="+object);
            }
        }
    }
}
