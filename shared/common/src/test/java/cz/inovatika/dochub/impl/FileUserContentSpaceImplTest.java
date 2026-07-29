package cz.inovatika.dochub.impl;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.UserContentBundle;
import cz.inovatika.dochub.UserContentSpace;
import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FileUserContentSpaceImplTest {

    private Path storageRoot;
    private FileUserContentSpaceImpl space;

    @Before
    public void setUp() throws Exception {
        storageRoot = Files.createTempDirectory("dochub-user-storage");

        Configuration configuration = KConfiguration.getInstance().getConfiguration();
        configuration.setProperty("dochub.storage.user", storageRoot.toString());
        configuration.setProperty("dochub.user.expiration.hours", 48);
        configuration.setProperty("dochub.user.download.daily_limit", 2);

        space = new FileUserContentSpaceImpl();
    }

    @After
    public void tearDown() throws Exception {
        if (storageRoot != null && Files.exists(storageRoot)) {
            try (Stream<Path> stream = Files.walk(storageRoot)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }

    @Test
    public void storeBundleStoresContentInfoAndCanReadItBack() throws Exception {
        String user = "test-user";
        String pid = "uuid:test-pid";
        byte[] content = "test pdf content".getBytes(StandardCharsets.UTF_8);

        String token = space.storeBundle(new ByteArrayInputStream(content), user, pid, DocumentType.PDF, "audit");

        Assert.assertEquals(space.getToken(pid, user), token);
        Assert.assertTrue(space.exists(token, DocumentType.PDF));

        Path bundlePath = resolveBundlePath(token, DocumentType.PDF);
        Assert.assertEquals("test pdf content", new String(Files.readAllBytes(bundlePath.resolve("content.pdf")), StandardCharsets.UTF_8));

        JSONObject infoJson = new JSONObject(Files.readString(bundlePath.resolve("info.json"), StandardCharsets.UTF_8));
        Assert.assertEquals(pid, infoJson.getString("pid"));
        Assert.assertEquals(user, infoJson.getString("user"));
        Assert.assertEquals(token, infoJson.getString("token"));
        Assert.assertEquals(DocumentType.PDF.name(), infoJson.getString("type"));

        Optional<InputStream> loaded = space.getBundle(token, user, DocumentType.PDF);
        Assert.assertTrue(loaded.isPresent());
        try (InputStream is = loaded.get()) {
            Assert.assertEquals("test pdf content", new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void listBundlesReturnsBundlesForRequestedUserOnly() throws Exception {
        String user = "list-user";
        String firstToken = space.storeBundle(
                new ByteArrayInputStream("pdf content".getBytes(StandardCharsets.UTF_8)),
                user,
                "uuid:first",
                DocumentType.PDF,
                "audit");
        String secondToken = space.storeBundle(
                new ByteArrayInputStream("text content".getBytes(StandardCharsets.UTF_8)),
                user,
                "uuid:second",
                DocumentType.TEXT,
                "audit");
        space.storeBundle(
                new ByteArrayInputStream("other content".getBytes(StandardCharsets.UTF_8)),
                "other-user",
                "uuid:other",
                DocumentType.PDF,
                "audit");

        List<UserContentBundle> bundles = space.listBundles(user);

        Assert.assertEquals(2, bundles.size());
        Assert.assertTrue(containsBundle(bundles, firstToken, "uuid:first", DocumentType.PDF));
        Assert.assertTrue(containsBundle(bundles, secondToken, "uuid:second", DocumentType.TEXT));
        Assert.assertEquals(0, space.listBundles("unknown-user").size());
    }

    @Test
    public void listBundlesMarksBundleUnavailableWhenContentFileIsMissing() throws Exception {
        String user = "missing-file-user";
        String token = space.storeBundle(
                new ByteArrayInputStream("pdf content".getBytes(StandardCharsets.UTF_8)),
                user,
                "uuid:missing-file",
                DocumentType.PDF,
                "audit");

        Files.delete(resolveBundlePath(token, DocumentType.PDF).resolve("content.pdf"));

        List<UserContentBundle> bundles = space.listBundles(user);

        Assert.assertEquals(1, bundles.size());
        UserContentBundle bundle = bundles.get(0);
        Assert.assertEquals(token, bundle.getToken());
        Assert.assertEquals("uuid:missing-file", bundle.getPid());
        Assert.assertEquals(DocumentType.PDF, bundle.getType());
        Assert.assertFalse(bundle.isAvailable());
        Assert.assertEquals(0, bundle.getSize());
    }


    @Test
    public void getBundleReturnsEmptyAndCountsMissesUntilDailyLimitIsReached() throws Exception {
        String user = "limited-user";
        String missingToken = space.generateHash(user, "uuid:missing");

        Assert.assertFalse(space.getBundle(missingToken, user, DocumentType.PDF).isPresent());
        Assert.assertFalse(space.getBundle(missingToken, user, DocumentType.PDF).isPresent());

        Path counterFile = storageRoot
                .resolve(FileUserContentSpaceImpl.DATA_FOLDER)
                .resolve("counters")
                .resolve(LocalDate.now().toString())
                .resolve(user + ".count");
        Assert.assertEquals("2", Files.readString(counterFile, StandardCharsets.UTF_8));

        try {
            space.getBundle(missingToken, user, DocumentType.PDF);
            Assert.fail("Expected daily download limit exception");
        } catch (UserContentSpace.UsageException expected) {
            Assert.assertTrue(expected.getMessage().contains("2"));
        }
    }

    @Test
    public void deleteBundleRemovesStoredBundleDirectory() throws Exception {
        String token = space.storeBundle(
                new ByteArrayInputStream("epub content".getBytes(StandardCharsets.UTF_8)),
                "delete-user",
                "uuid:delete",
                DocumentType.EPUB,
                "audit");

        Path bundlePath = resolveBundlePath(token, DocumentType.EPUB);
        Assert.assertTrue(Files.exists(bundlePath));

        space.deleteBundle(token, DocumentType.EPUB);

        Assert.assertFalse(Files.exists(bundlePath));
        Assert.assertFalse(space.exists(token, DocumentType.EPUB));
        Assert.assertEquals(0, space.listBundles("delete-user").size());
    }

    @Test
    public void cleanupDeletesMatchingFilesAndRemovesEmptyDirectories() throws Exception {
        String token = space.storeBundle(
                new ByteArrayInputStream("text content".getBytes(StandardCharsets.UTF_8)),
                "cleanup-user",
                "uuid:cleanup",
                DocumentType.TEXT,
                "audit");

        Path bundlePath = resolveBundlePath(token, DocumentType.TEXT);
        Path contentFile = bundlePath.resolve("content.text");
        Path infoFile = bundlePath.resolve("info.json");

        space.cleanup((file, attrs) -> file.getFileName().toString().equals("content.text")
                || file.getFileName().toString().equals("info.json"));

        Assert.assertFalse(Files.exists(contentFile));
        Assert.assertFalse(Files.exists(infoFile));
        Assert.assertFalse(Files.exists(bundlePath));
    }

    @Test
    public void cleanupSkipsUserIndexButDeletesShardedBundleData() throws Exception {
        String user = "cleanup-index-user";
        String token = space.storeBundle(
                new ByteArrayInputStream("text content".getBytes(StandardCharsets.UTF_8)),
                user,
                "uuid:cleanup-index",
                DocumentType.TEXT,
                "audit");

        Path bundlePath = resolveBundlePath(token, DocumentType.TEXT);
        Path contentFile = bundlePath.resolve("content.text");
        Path infoFile = bundlePath.resolve("info.json");
        Path userIndexFile = findUserIndexFile(user, token, DocumentType.TEXT);

        space.cleanup((file, attrs) -> true);

        Assert.assertFalse(Files.exists(contentFile));
        Assert.assertFalse(Files.exists(infoFile));
        Assert.assertFalse(Files.exists(bundlePath));
        Assert.assertTrue(Files.exists(userIndexFile));
    }

    private Path resolveBundlePath(String token, DocumentType type) {
        String p1 = token.substring(0, Math.min(2, token.length()));
        String p2 = token.substring(Math.min(2, token.length()), Math.min(4, token.length()));
        String p3 = token.substring(Math.min(4, token.length()), Math.min(6, token.length()));
        return storageRoot
                .resolve(FileUserContentSpaceImpl.DATA_FOLDER)
                .resolve(p1)
                .resolve(p2)
                .resolve(p3)
                .resolve(token)
                .resolve(type.name().toLowerCase());
    }

    private boolean containsBundle(List<UserContentBundle> bundles, String token, String pid, DocumentType type) {
        for (UserContentBundle bundle : bundles) {
            if (token.equals(bundle.getToken())
                    && pid.equals(bundle.getPid())
                    && type == bundle.getType()
                    && bundle.getCreated() != null
                    && bundle.getSize() > 0) {
                return true;
            }
        }
        return false;
    }

    private Path findUserIndexFile(String user, String token, DocumentType type) throws Exception {
        Path usersPath = storageRoot
                .resolve(FileUserContentSpaceImpl.DATA_FOLDER)
                .resolve("users");
        try (Stream<Path> stream = Files.walk(usersPath)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(type.name().toLowerCase() + ".json"))
                    .filter(path -> {
                        try {
                            JSONObject json = new JSONObject(Files.readString(path, StandardCharsets.UTF_8));
                            return user.equals(json.getString("user"))
                                    && token.equals(json.getString("token"));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("User index file not found"));
        }
    }
}
