package cz.inovatika.dochub.impl;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.CleanableSpace;
import cz.inovatika.dochub.CleanupStrategy;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.PermanentContentSpace;
import cz.inovatika.dochub.utils.ResolvePathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FilePermanentContentSpaceImpl implements PermanentContentSpace, CleanableSpace {

    //dochub.root=${sys:user.home}/.kramerius4/docs
    //dochub.storage.permanent=${dochub.root}/permanent
    //dochub.storage.user=${dochub.root}/user-out

    private final Path rootPath;

    public FilePermanentContentSpaceImpl() {
        String permanentRoot = KConfiguration.getInstance().getConfiguration().getString("dochub.storage.permanent");
        if (permanentRoot == null || permanentRoot.isEmpty()) {
            throw new IllegalStateException("Configuration key 'dochub.storage.permanent' is missing or empty.");
        }
        this.rootPath = Paths.get(permanentRoot);
        try {
            Files.createDirectories(this.rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create root storage directory: " + permanentRoot, e);
        }
    }

    public OutputStream createOutputStream(String pid, DocumentType type) throws IOException {
        Path filePath = ResolvePathUtils.resolvePath(rootPath, pid, type);
        Path parentDir = filePath.getParent();
        Files.createDirectories(parentDir);
        Path pidFile = parentDir.resolve("_pid_");
        Files.writeString(pidFile, pid, StandardCharsets.UTF_8);
        return Files.newOutputStream(filePath);
    }


    @Override
    public void storeContent(String pid, DocumentType type, InputStream is) throws IOException {
        Path filePath = ResolvePathUtils.resolvePath(rootPath, pid, type);
        Path parentDir = filePath.getParent();
        Files.createDirectories(filePath.getParent());

        try (OutputStream os = Files.newOutputStream(filePath)) {
            is.transferTo(os);
        }
        Path pidFile = parentDir.resolve("_pid_");
        Files.writeString(pidFile, pid, StandardCharsets.UTF_8);
    }

    @Override
    public InputStream getContent(String pid, DocumentType type) throws IOException {
        return Files.newInputStream(ResolvePathUtils.resolvePath(rootPath, pid, type));
    }

    @Override
    public boolean exists(String pid, DocumentType type) throws IOException {
        return Files.exists(ResolvePathUtils.resolvePath(rootPath, pid, type));
    }


    public void cleanup(CleanupStrategy strategy) throws IOException {
        if (!Files.exists(rootPath)) return;
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (strategy.shouldDelete(file, attrs)) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (!dir.equals(rootPath)) {
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                        if (!ds.iterator().hasNext()) {
                            Files.delete(dir);
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
