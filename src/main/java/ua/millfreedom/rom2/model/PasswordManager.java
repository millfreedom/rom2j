package ua.millfreedom.rom2.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PasswordManager {
    //0x0
    public final List<String> array = new ArrayList<>();
    //0x14
    public String name = "";
    //0x18
    public long time;
    //0x1c
    public Object reserved0x1c;

    /**
     * Native: PasswordManager::setPasswordFile @004EEFD2.
     * Fully ported.
     */
    public void setPasswordFile(String fileName) {
        name = fileName;
        Path path = Path.of(fileName);
        if (!Files.exists(path)) {
            name = "";
            return;
        }
        time = lastModifiedMillis(path);
        load();
    }

    /**
     * Native: PasswordManager::Load @004EF06B.
     * Fully ported.
     */
    public void load() {
        Path path = Path.of(name);
        if (!Files.exists(path)) {
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.ISO_8859_1);
        } catch (IOException exception) {
            return;
        }
        array.clear();
        for (int index = 0; index < lines.size(); index += 4) {
            String login = lines.get(index);
            String password = lines.get(index + 1);
            array.add(login + '\u0001' + password);
        }
    }

    /**
     * Native: PasswordManager::CheckPassword @004EF1F5.
     * Fully ported.
     */
    public boolean checkPassword(String value) {
        if (name.isEmpty()) {
            return false;
        }
        Path path = Path.of(name);
        if (!Files.exists(path)) {
            name = "";
            return false;
        }
        long previousTime = time;
        long currentTime = lastModifiedMillis(path);
        time = currentTime;
        if (currentTime > previousTime) {
            load();
        }
        for (String entry : array) {
            if (value.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Native support extracted from PasswordManager::setPasswordFile @004EEFD2 and
     * PasswordManager::CheckPassword @004EF1F5 CFileFind::GetLastWriteTime calls.
     */
    private static long lastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
