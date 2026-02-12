package cz.inovatika.dochub.utils;

import cz.inovatika.dochub.DocumentType;

import java.nio.file.Path;

public class ResolvePathUtils {

    private ResolvePathUtils() {}

    public static Path resolvePath(Path rootPath, String pid, DocumentType type) {
        String cleanId = pid.startsWith("uuid:") ? pid.substring(5) : pid;
        String safeId = cleanId.replace(":", "_");

        String p1 = safeId.substring(0, Math.min(2, safeId.length()));
        String p2 = safeId.substring(Math.min(2, safeId.length()), Math.min(4, safeId.length()));
        String p3 = safeId.substring(Math.min(4, safeId.length()), Math.min(6, safeId.length()));

        String fileName = (pid.startsWith("uuid:") ? "uuid_" : "") + safeId + "." + type.name().toLowerCase();

        return rootPath.resolve(type.name().toLowerCase())
                .resolve(p1)
                .resolve(p2)
                .resolve(p3)
                .resolve(fileName);
    }
}
