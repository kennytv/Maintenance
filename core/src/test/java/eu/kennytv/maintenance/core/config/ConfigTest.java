/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.kennytv.maintenance.core.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

// I'm sorry in advance
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigTest {

    @BeforeAll
    void initAll() {
        delete("config.yml");
        delete("config-saved.yml");
        delete("config-set.yml");
        delete("dirty-config-saved.yml");
        delete("dirty-config-upgraded.yml");
        copyOriginalToTest("config.yml", "config.yml");
    }

    @Test
    void testHeader() throws Exception {
        final Config config = new Config(getTestFile("config.yml"));
        config.load();

        final String header = config.getHeader();
        config.resetAwesomeHeader();
        assertTrue(config.getHeader().endsWith("\n"), "Header needs to end with a new line");
        assertEquals(config.getHeader(), header);
    }

    @Test
    void testDeep() throws IOException {
        final Config config = new Config(getTestFile("config.yml"));
        config.load();
        assertEquals(3306, config.getInt("mysql.port"));
    }

    @Test
    void testSaveConsistency() throws IOException {
        final Config config = new Config(getTestFile("config.yml"));
        config.load();

        final File saveTo = getTestFile("config-saved.yml");
        config.saveTo(saveTo);

        final Config newlyLoaded = new Config(saveTo);
        newlyLoaded.load();

        assertEquals(config.toString(), newlyLoaded.toString());
    }

    @Test
    void testParsing() throws IOException {
        final Config config = new Config(getTestFile("dirty-config.yml"));
        config.load();
        final File saveTo = getTestFile("dirty-config-saved.yml");
        config.saveTo(saveTo);
    }

    @Test
    void testUpgrading() throws IOException {
        final Config config = new Config(getTestFile("dirty-config.yml"));
        config.load();

        final Config newestConfig = new Config(getTestFile("config.yml"));
        newestConfig.load();

        config.addMissingFields(newestConfig.getValues(), newestConfig.getComments());
        config.set("config-version", 4);
        final File saveTo = getTestFile("dirty-config-upgraded.yml");
        config.resetAwesomeHeader();
        config.saveTo(saveTo);

        final Config newlyLoaded = new Config(saveTo);
        newlyLoaded.load();

        assertTrue(newestConfig.contains("waiting-server"));
        assertNotNull(newestConfig.getComments().get("maintenance-enabled"));
        assertNotNull(newestConfig.getComments().get("mysql.use-ssl"));
        assertNotNull(newestConfig.getComments().get("update-checks"));
        assertEquals(newlyLoaded.getHeader(), newestConfig.getHeader());
        assertEquals("useer", newlyLoaded.getSection("mysql").getString("username"));
        assertEquals(4, newlyLoaded.getInt("config-version"));
    }

    @Test
    void testSetAndContains() throws IOException {
        final Config config = new Config(getTestFile("config.yml"));
        config.load();

        // Test for pre-existing values
        assertTrue(config.contains("fallback"));
        assertTrue(config.contains("mysql.password"));
        assertFalse(config.contains("test"));
        assertTrue(config.getComments().containsKey("fallback"));
        assertTrue(config.getComments().containsKey("mysql.use-ssl"));

        config.set("test", "abc");
        config.remove("fallback");
        final ConfigSection section = config.getSection("mysql");
        section.set("port", 1000);
        section.remove("use-ssl");

        assertTrue(config.contains("test"));
        assertEquals("abc", config.getString("test"));
        assertEquals(1000, config.getInt("mysql.port"));
        assertFalse(config.contains("mysql.use-ssl"));
        assertFalse(config.getSection("mysql").contains("use-ssl"));
        assertFalse(config.contains("fallback"));
        assertFalse(config.getComments().containsKey("fallback"));
        assertFalse(config.getComments().containsKey("mysql.use-ssl"));
    }

    private File getTestFile(final String path) {
        return new File("src/test/resources/" + path);
    }

    private void copyOriginalToTest(final String from, final String to) {
        final File file = new File("src/main/resources/" + from);
        try {
            Files.copy(file.toPath(), getTestFile(to).toPath());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void delete(final String path) {
        final File file = getTestFile(path);
        if (file.exists()) {
            file.delete();
        }
    }
}