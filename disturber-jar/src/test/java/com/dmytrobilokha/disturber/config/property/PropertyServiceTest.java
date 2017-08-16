package com.dmytrobilokha.disturber.config.property;

import com.dmytrobilokha.disturber.Constants;
import com.dmytrobilokha.disturber.service.fs.FsService;
import com.dmytrobilokha.disturber.service.fs.IoConsumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Paths;

import static com.dmytrobilokha.disturber.config.property.MockProperties.INVALID_VERSION;
import static com.dmytrobilokha.disturber.config.property.MockProperties.MINIMAL_PROPERTIES;
import static com.dmytrobilokha.disturber.config.property.MockProperties.MISSING_MANDATORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * The unit test to test PropertyService
 */
public class PropertyServiceTest {

    private static final String FAKE_CONFIGDIR_LOCATION = "blablabla";

    private FsService mockFsService;
    private StringWriter mockWriter;

    @Before
    public void init() throws IOException {
        mockFsService = Mockito.mock(FsService.class);
        System.setProperty(Constants.CONFIG_DIR_PROPERTY_KEY, FAKE_CONFIGDIR_LOCATION);
    }

    @Test
    public void testEnsuresConfigFileExistsOnConstruction() throws IOException {
        setupMockFsService(MINIMAL_PROPERTIES);
        PropertyService propertyService = new PropertyService(mockFsService);
        Mockito.verify(mockFsService, Mockito.times(1))
                .copyResourceIfFileAbsent(Paths.get(FAKE_CONFIGDIR_LOCATION + Constants.FILE_SEPARATOR
                        + Constants.PROPERTIES_FILE_NAME), "/defaults.properties");
    }

    private void setupMockFsService(String dataToRead) throws IOException {
        doAnswer(invocation -> {
                Reader reader = new StringReader(dataToRead);
                ((IoConsumer<Reader>)invocation.getArguments()[1]).accept(reader);
                return null;
            })
            .when(mockFsService).consumeFile(any(), any(IoConsumer.class));
        mockWriter = new StringWriter(1000);
        doAnswer(invocation -> {
            ((IoConsumer<Writer>)invocation.getArguments()[1]).accept(mockWriter);
            return null;
            })
            .when(mockFsService).writeFile(any(), any(IoConsumer.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testFailsIfMandatoryPropertyAbsent() throws IOException {
        initPropertyService(MISSING_MANDATORY);
    }

    @Test(expected = IllegalStateException.class)
    public void testFailsIfPropertyValueIsNotParsable() throws IOException {
        initPropertyService(INVALID_VERSION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsIfWrongMethodCalled() throws IOException {
        PropertyService propertyService = initPropertyService(MINIMAL_PROPERTIES);
        propertyService.getString(Property.PROPERTIES_VERSION);
    }

    @Test
    public void testWritesChanges() throws IOException {
        PropertyService propertyService = initPropertyService(MINIMAL_PROPERTIES);
        propertyService.setInteger(Property.PROPERTIES_VERSION, 42);
        propertyService.saveProperties();
        String output = mockWriter.toString();
        assertTrue(output.contains("properties.version=42"));
    }

    @Test
    public void testReturnsConfigDirLocation() throws IOException {
        PropertyService propertyService = initPropertyService(MINIMAL_PROPERTIES);
        assertEquals(FAKE_CONFIGDIR_LOCATION, propertyService.getConfigDirLocation());
    }

    private PropertyService initPropertyService(String propertyString) throws IOException {
        setupMockFsService(propertyString);
        return new PropertyService(mockFsService);
    }
}
