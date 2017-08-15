package com.dmytrobilokha.disturber.config.property;

import com.dmytrobilokha.disturber.Constants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The test checks PropertyService functionality. Be aware the test manipulates with
 * the file system (create, copy, delete file).
 */
public class PropertyServiceTest {

    private static final String PROPERTYSET_PREFIX = "/propertyset/";

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

    @Test
    public void testParsesPropertiesFile() throws IOException {
        copyConfigFile("full.properties");
        propertyService.init();
        assertEquals(Integer.valueOf(1), propertyService.getInteger(Property.PROPERTIES_VERSION));
        assertEquals("Blue", propertyService.getString(Property.COLOR));
        assertEquals(Property.COLOR, propertyService.getEnum(Property.TEST_ENUM));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChecksTypeOnGet() {
        propertyService.init();
        propertyService.getString(Property.PROPERTIES_VERSION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChecksTypeOnSet() {
        propertyService.init();
        propertyService.setString(Property.PROPERTIES_VERSION, "STRING");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChecksTypeOnSetNull() {
        propertyService.init();
        propertyService.setInteger(Property.COLOR, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreventsUnsettingMandatoryProperty() {
        propertyService.init();
        propertyService.setInteger(Property.PROPERTIES_VERSION, null);
    }

    @Test
    public void testSetsAndSavesProperty() throws IOException {
        propertyService.init();
        assertEquals(Integer.valueOf(0), propertyService.getInteger(Property.PROPERTIES_VERSION));
        propertyService.setInteger(Property.PROPERTIES_VERSION, 42);
        assertEquals(Integer.valueOf(42), propertyService.getInteger(Property.PROPERTIES_VERSION));
        propertyService.saveProperties();
        Properties savedProperties = loadPropertiesFile();
        assertEquals("42", savedProperties.getProperty("properties.version"));
    }

    private void copyConfigFile(String fileName) throws IOException {
        try (InputStream defaultPropertiesInputStream = getClass().getResourceAsStream(PROPERTYSET_PREFIX + fileName);
             BufferedInputStream defaultPropertiesBufferedInputStream
                     = new BufferedInputStream(defaultPropertiesInputStream)){
            Files.copy(defaultPropertiesBufferedInputStream, configFilePath);
        }
    }

    private Properties loadPropertiesFile() throws IOException {
        Properties appProperties = new Properties();
        try (Reader configFileReader = Files.newBufferedReader(configFilePath)) {
            appProperties.load(configFileReader);
        }
        return appProperties;
    }

}
