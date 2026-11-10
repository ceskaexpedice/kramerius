package cz.incad.kramerius.cdkcollections;

import java.util.ArrayList;
import java.util.List;

public class ReferencePath {
        private final String collectionPid;
        private final String referencedPid;
        private final List<String> cdkPath;
        private final String fullPath;

    public ReferencePath(String collectionPid, String referencedPid, List<String> cdkPath, String fullPath) {
        this.collectionPid = collectionPid;
        this.referencedPid = referencedPid;
        this.cdkPath = new ArrayList<>(cdkPath);
        this.fullPath = fullPath;
    }

    public List<String> getCdkPath() {
        return cdkPath;
    }

    public String getCollectionPid() {
        return collectionPid;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getReferencedPid() {
        return referencedPid;
    }

    @Override
        public String toString() {
            return "ReferencePath{" +
                    "collectionPid='" + collectionPid + '\'' +
                    ", referencedPid='" + referencedPid + '\'' +
                    ", cdkPath=" + cdkPath +
                    ", fullPath='" + fullPath + '\'' +
                    '}';
        }
    }
