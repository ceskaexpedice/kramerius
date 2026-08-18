package cz.incad.kramerius.rest.apiNew.client.v70;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ResourceReferenceExtractor {

    public List<String> extractResourceReferences(
            Object value,
            List<String> resourceReferencePatterns
    ) {
        Set<String> references = new LinkedHashSet<>();

        List<List<String>> patterns = resourceReferencePatterns.stream()
                .map(this::normalizeJsonPathPattern)
                .toList();

        visit(value, List.of(), patterns, references);

        return new ArrayList<>(references);
    }

    private void visit(
            Object node,
            List<String> path,
            List<List<String>> patterns,
            Set<String> references
    ) {
        if (node == null || node == JSONObject.NULL) {
            return;
        }

        if (node instanceof String text) {
            if (isResourceReferencePath(path, patterns) && isResourceReference(text)) {
                references.add(normalizeResourceKey(text));
            }
            return;
        }

        if (node instanceof JSONArray array) {
            for (int i = 0; i < array.length(); i++) {
                // Stejne jako v TS: index pole se do path nepridava.
                visit(array.opt(i), path, patterns, references);
            }
            return;
        }

        if (node instanceof JSONObject object) {
            for (Object k : object.keySet()) {
                String key = k.toString();
                List<String> childPath = new ArrayList<>(path);
                childPath.add(key);

                visit(object.opt(key), childPath, patterns, references);
            }
        }
    }

    private List<String> normalizeJsonPathPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return List.of();
        }

        return Arrays.stream(pattern.split("/"))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .toList();
    }

    private boolean isResourceReferencePath(
            List<String> path,
            List<List<String>> patterns
    ) {
        return patterns.stream().anyMatch(pattern -> matchesPath(path, pattern));
    }

    private boolean matchesPath(List<String> path, List<String> pattern) {
        if (path.size() != pattern.size()) {
            return false;
        }

        for (int i = 0; i < path.size(); i++) {
            String patternSegment = pattern.get(i);

            if (!"*".equals(patternSegment) && !patternSegment.equals(path.get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean isResourceReference(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeResourceKey(String value) {
        return value.trim();
    }
}
