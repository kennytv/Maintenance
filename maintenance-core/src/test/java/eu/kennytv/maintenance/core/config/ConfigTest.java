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

import com.google.common.io.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

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
    void testDeep() throws IOException {
        final Config config = new Config(getTestFile("config.yml"));
        config.load();
        assert config.getInt("mysql.port") == 3306;
    }

    @Test
    void testSaveConsistency() throws IOException {
        final Config config = new Config(getTestFile("config.yml"));
        config.load();

        final File saveTo = getTestFile("config-saved.yml");
        config.saveTo(saveTo);

        final Config newlyLoaded = new Config(saveTo);
        newlyLoaded.load();

        assert newlyLoaded.toString().equals(config.toString());
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

        assert newlyLoaded.contains("waiting-server");
        assert newlyLoaded.getComments().get("maintenance-enabled") != null;
        assert newlyLoaded.getComments().get("mysql.use-ssl") != null;
        assert newlyLoaded.getComments().get("update-checks") != null;
        assert newlyLoaded.getHeader().equals(newestConfig.getHeader());
        assert newlyLoaded.getSection("mysql").getString("username").equals("useer");
        assert newlyLoaded.getInt("config-version") == 4;
    }

    @Test
    void testSetAndContains() throws IOException {
        final Config config = new Config(getTestFile("config.yml"));
        config.load();

        // Test for pre-existing values
        assert config.contains("fallback");
        assert config.contains("mysql.password");
        assert !config.contains("test");
        assert config.getComments().containsKey("fallback");
        assert config.getComments().containsKey("mysql.use-ssl");

        config.set("test", "abc");
        config.remove("fallback");
        final ConfigSection section = config.getSection("mysql");
        section.set("port", 1000);
        section.remove("use-ssl");

        assert config.contains("test") && config.getString("test").equals("abc");
        assert config.getInt("mysql.port") == 1000;
        assert !config.contains("mysql.use-ssl") && !config.getSection("mysql").contains("use-ssl");
        assert !config.contains("fallback");
        assert !config.getComments().containsKey("fallback");
        assert !config.getComments().containsKey("mysql.use-ssl");
    }

    private File getTestFile(final String path) {
        return new File("src/test/resources/" + path);
    }

    private void copyOriginalToTest(final String from, final String to) {
        final File file = new File("src/main/resources/" + from);
        try {
            Files.copy(file, getTestFile(to));
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
