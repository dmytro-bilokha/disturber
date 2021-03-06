package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.Constants;
import com.dmytrobilokha.disturber.config.account.dto.AccountConfigDto;
import com.dmytrobilokha.disturber.config.account.dto.AccountsDto;
import com.dmytrobilokha.disturber.config.property.PropertyService;
import com.dmytrobilokha.disturber.fs.FsService;
import com.dmytrobilokha.disturber.util.ThrowingConsumer;
import com.dmytrobilokha.disturber.util.ThrowingFunction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class AccountConfigServiceTest {

    private static final String LOCATION_PREFIX = "/accountset/";
    private static final String FAKE_CONFIGDIR_LOCATION = "blablabla";
    private static final String FAKE_ACCOUNTS = FAKE_CONFIGDIR_LOCATION + Constants.FILE_SEPARATOR + "accounts.xml";

    private FsService mockFsService;
    private PropertyService mockPropertyService;
    private StringWriter mockWriter;

    private AccountConfigService accountConfigService;

    @Before
    public void init() throws Exception {
        mockFsService = Mockito.mock(FsService.class);
        when(mockFsService.pathExists(any())).thenReturn(true);
        mockPropertyService = Mockito.mock(PropertyService.class);
        when(mockPropertyService.getConfigDirLocation()).thenReturn(FAKE_CONFIGDIR_LOCATION);
        setupFsServiceMockWriter();
        accountConfigService = new AccountConfigService(mockPropertyService, mockFsService
                , new ExceptionFactory(ResourceBundle.getBundle("messages")));
    }

    private void setupFsServiceMockWriter() throws Exception {
        mockWriter = new StringWriter(5000);
        doAnswer(invocation -> {
            ((ThrowingConsumer<Writer>)invocation.getArguments()[1]).accept(mockWriter);
            mockWriter.close();
            return null;
        }).when(mockFsService).writeFile(any(), any(ThrowingConsumer.class));
    }

    @Test
    public void testReturnsEmptyListIfAccountsFileNotFound() throws AccountConfigAccessException {
        when(mockFsService.pathExists(any())).thenReturn(false);
        assertEquals(0, accountConfigService.getAccountConfigs().size());
    }

    @Test
    public void testReadsOneAccountFromFile() throws Exception {
        setupFsServiceMockReader("One.xml");
        List<AccountConfig> configs = accountConfigService.getAccountConfigs();
        assertEquals(1, configs.size());
        AccountConfig config = configs.get(0);
        assertEquals(AccountConfig.newBuilder()
                .serverAddress("http://address1.mockserver.org/")
                .login("login2")
                .password("password3")
                .betweenSyncPause(1)
                .syncTimeout(2000)
                .networkTimeout(3000)
                .build()
                , config);
    }

    @Test
    public void testReadsOneAccountWithProxyServerFromFile() throws Exception {
        setupFsServiceMockReader("OneWithProxy.xml");
        List<AccountConfig> configs = accountConfigService.getAccountConfigs();
        assertEquals(1, configs.size());
        AccountConfig config = configs.get(0);
        assertEquals(AccountConfig.newBuilder()
                        .serverAddress("http://address1.mockserver.org/")
                        .login("login2")
                        .password("password3")
                        .betweenSyncPause(1)
                        .syncTimeout(2000)
                        .networkTimeout(3000)
                        .proxyHost("proxy.host.net")
                        .proxyPort(42001)
                        .build()
                , config);
    }

    @Test
    public void testReadsTwoAccountsFromFile() throws Exception {
        setupFsServiceMockReader("Two.xml");
        List<AccountConfig> configs = accountConfigService.getAccountConfigs();
        assertEquals(2, configs.size());
        assertEquals(AccountConfig.newBuilder()
                        .serverAddress("http://address11.mockserver.org/")
                        .login("login12")
                        .password("password13")
                        .betweenSyncPause(1)
                        .syncTimeout(2000)
                        .networkTimeout(3000)
                        .build()
                , configs.get(0));
        assertEquals(AccountConfig.newBuilder()
                        .serverAddress("http://address21.mockserver.org/")
                        .login("login22")
                        .password("password23")
                        .betweenSyncPause(4)
                        .syncTimeout(5000)
                        .networkTimeout(6000)
                        .build()
                , configs.get(1));
    }

    private void setupFsServiceMockReader(String resourceLocation) throws Exception {
        doAnswer(invocation -> {
            Reader reader = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(LOCATION_PREFIX + resourceLocation)));
            AccountsDto accountsDto = ((ThrowingFunction<Reader, AccountsDto>)invocation.getArguments()[1]).apply(reader);
            reader.close();
            return accountsDto;
        }).when(mockFsService).readFile(any(), any(ThrowingFunction.class));
    }

    @Test
    public void testSavesAccountConfig() throws Exception {
        AccountConfigDto accountConfigDto = new AccountConfigDto();
        accountConfigDto.setServerAddress("http://address1x.mockserver.org/");
        accountConfigDto.setLogin("login2x");
        accountConfigDto.setPassword("password3x");
        AccountsDto accountsDto = new AccountsDto();
        accountsDto.setVersion(11);
        accountsDto.setAccounts(Arrays.asList(accountConfigDto));
        accountConfigService.saveAccountConfigs(accountsDto);
        String savedXml = mockWriter.toString();
        assertTrue(savedXml.contains("<version>11</version>"));
        assertTrue(savedXml.contains("<serverAddress>http://address1x.mockserver.org/</serverAddress>"));
        assertTrue(savedXml.contains("<login>login2x</login>"));
        assertTrue(savedXml.contains("<password>password3x</password>"));
        assertTrue(savedXml.contains("<betweenSyncPause>0</betweenSyncPause>"));
    }

    @Test
    public void testFailsOnInvalidXml() throws Exception {
        setupFsServiceMockReader("Invalid.xml");
        checkExceptionDetails(FAKE_ACCOUNTS, "syncTimeout", "integer", "valid");
    }

    private void checkExceptionDetails(String mainMessage, String... details) {
        try {
            accountConfigService.getAccountConfigs();
        } catch (AccountConfigAccessException ex) {
            String exceptionMessage = ex.getSystemMessage().getMessage();
            assertTrue("SystemMessage mainMessage extracted from exception should contain string '"
                    + mainMessage + "', but it doesn't: " + exceptionMessage,  exceptionMessage.contains(mainMessage));
            String exceptionDetails = ex.getSystemMessage().getDetails();
            for (String detail : details)
                assertTrue("SystemMessage details extracted from exception should contain string '"
                        + detail + "', but it doesn't: " + exceptionDetails,  exceptionDetails.contains(detail));
            return;
        }
        fail("Expected to get exception");
    }

    @Test
    public void testVersionInXmlIsMandatory() throws Exception {
        setupFsServiceMockReader("NoVersion.xml");
        checkExceptionDetails(FAKE_ACCOUNTS, "version", "Invalid content");
    }

    @Test
    public void testFailsOnIOException() throws Exception {
        setupMockFsServiceFail(new IOException("EPIC_FAIL"));
        checkExceptionDetails(FAKE_ACCOUNTS, "EPIC_FAIL");
    }

    private void setupMockFsServiceFail(Exception ex) throws Exception {
        doAnswer(invocation -> {
            throw ex;
        }).when(mockFsService).readFile(any(), any(ThrowingFunction.class));
        mockWriter = new StringWriter(5000);
        doAnswer(invocation -> {
            throw ex;
        }).when(mockFsService).writeFile(any(), any(ThrowingConsumer.class));
    }

}
