package ua.millfreedom.rom2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Startup class index used by archive runtime-class compatibility checks.
 * Scans classpath entries once and caches name -> FQCN mapping.
 */
public final class ClassNameIndex {
    private static final String BASE_PACKAGE_PREFIX = ClassNameIndex.class.getPackageName() + ".";
    private static final ClassNameIndex INSTANCE = new ClassNameIndex();

    private final Map<String, List<String>> index;

    // not ported.
    private ClassNameIndex() {
        this.index = buildIndex();
    }

    // not ported.
    public static ClassNameIndex get() {
        return INSTANCE;
    }

    /**
     * Lookup by simple name or fully-qualified class name.
     * Returns cached FQCN candidates.
      * not ported.
     */
    public List<String> lookup(String name) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }
        List<String> list = index.get(name.trim());
        return list == null ? Collections.emptyList() : list;
    }

    // not ported.
    private static Map<String, List<String>> buildIndex() {
        Map<String, Set<String>> tmp = new HashMap<>();
        String classPath = System.getProperty("java.class.path", "");
        String[] entries = classPath.split(File.pathSeparator);
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            try {
                Path path = Paths.get(entry);
                if (Files.isDirectory(path)) {
                    scanDirectory(path, tmp);
                } else if (Files.isRegularFile(path) && entry.endsWith(".jar")) {
                    scanJar(path, tmp);
                }
            } catch (Exception ignored) {
            }
        }
        return freeze(tmp);
    }

    // not ported.
    private static void scanDirectory(Path root, Map<String, Set<String>> out) {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".class"))
                    .forEach(p -> addBinaryName(out, toBinaryName(root, p)));
        } catch (IOException ignored) {
        }
    }

    // not ported.
    private static void scanJar(Path jarPath, Map<String, Set<String>> out) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            jar.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.endsWith(".class"))
                    .forEach(name -> addBinaryName(out, toBinaryName(name)));
        } catch (IOException ignored) {
        }
    }

    // not ported.
    private static void addBinaryName(Map<String, Set<String>> out, String binaryName) {
        if (binaryName.isBlank()) {
            return;
        }
        if (!binaryName.startsWith(BASE_PACKAGE_PREFIX)) {
            return;
        }
        addKey(out, binaryName, binaryName);
        String simple = simpleName(binaryName);
        if (!simple.isBlank()) {
            addKey(out, simple, binaryName);
        }
    }

    // not ported.
    private static void addKey(Map<String, Set<String>> out, String key, String fqcn) {
        out.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(fqcn);
    }

    // not ported.
    private static Map<String, List<String>> freeze(Map<String, Set<String>> src) {
        Map<String, List<String>> result = new HashMap<>(src.size());
        for (Map.Entry<String, Set<String>> e : src.entrySet()) {
            result.put(e.getKey(), Collections.unmodifiableList(new ArrayList<>(e.getValue())));
        }
        return Collections.unmodifiableMap(result);
    }

    // not ported.
    private static String toBinaryName(Path root, Path classFile) {
        String rel = root.relativize(classFile).toString();
        String noSuffix = rel.substring(0, rel.length() - ".class".length());
        return noSuffix.replace('\\', '.').replace('/', '.');
    }

    // not ported.
    private static String toBinaryName(String jarEntry) {
        String noSuffix = jarEntry.substring(0, jarEntry.length() - ".class".length());
        return noSuffix.replace('/', '.');
    }

    // not ported.
    private static String simpleName(String binaryName) {
        int pkgIdx = binaryName.lastIndexOf('.');
        String tail = pkgIdx >= 0 ? binaryName.substring(pkgIdx + 1) : binaryName;
        int dollarIdx = tail.lastIndexOf('$');
        if (dollarIdx >= 0 && dollarIdx + 1 < tail.length()) {
            return tail.substring(dollarIdx + 1);
        }
        return tail;
    }
}
