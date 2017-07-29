package com.dmytrobilokha.disturber.service.property;

import com.dmytrobilokha.disturber.Constants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The test checks PropertyService functionality. Be aware the test manipulates with
 * the file system (create, copy, delete file).
 */
public class PropertyServiceTest {

    private static String oldConfigDirLocation;
    private static String configDirLocation;
    private static Path configFilePath;

    private PropertyService propertyService;

    @BeforeClass
    public static void setup() {
        oldConfigDirLocation = System.getProperty(Constants.CONFIG_DIR_PROPERTY_KEY);
        configDirLocation = System.getProperty("java.io.tmpdir");
        System.setProperty(Constants.CONFIG_DIR_PROPERTY_KEY, configDirLocation);
        configFilePath = Paths.get(configDirLocation + Constants.FILE_SEPARATOR + Constants.PROPERTIES_FILE_NAME);
    }

    @AfterClass
    public static void shutdown() {
        if (oldConfigDirLocation == null)
            System.clearProperty(Constants.CONFIG_DIR_PROPERTY_KEY);
        else
            System.setProperty(Constants.CONFIG_DIR_PROPERTY_KEY, oldConfigDirLocation);
    }

    @Before
    public void initService() {
        propertyService = new PropertyService();
    }

    @After
    public void cleanupConfigDir() throws IOException {
        Files.deleteIfExists(configFilePath);
    }

    @Test
    public void testCopiesDefaultsIfPropertiesFileNotFound() {
        assertFalse(Files.exists(configFilePath));
        propertyService.init();
        assertTrue(Files.exists(configFilePath));
    }

}
