package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.config.property.PropertyService;
import com.dmytrobilokha.disturber.service.fs.FsService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class AccountConfigFactoryTest {

    private static final String FAKE_CONFIGDIR_LOCATION = "blablabla";

    private FsService mockFsService;
    private PropertyService mockPropertyService;
    private AccountConfigFactory accountConfigFactory;

    @Before
    public void init() throws IOException {
        mockFsService = Mockito.mock(FsService.class);
        when(mockFsService.pathExists(any())).thenReturn(true);
        mockPropertyService = Mockito.mock(PropertyService.class);
        when(mockPropertyService.getConfigDirLocation()).thenReturn(FAKE_CONFIGDIR_LOCATION);
        accountConfigFactory = new AccountConfigFactory(mockPropertyService, mockFsService);
    }

    @Test
    public void testReturnsEmptyListIfAccountsFileNotFound() {
        when(mockFsService.pathExists(any())).thenReturn(false);
        assertEquals(0, accountConfigFactory.getAccountConfigs().size());
    }

    @Ignore("This is a bootstrap test developed to create a basic accounts.xml file")
    @Test
    public void testSavesRealAccountConfigsFile() throws JAXBException, IOException {
        when(mockPropertyService.getConfigDirLocation()).thenReturn("/usr/home/dimon/temp");
        AccountConfigFactory realAccountConfigFactory = new AccountConfigFactory(mockPropertyService, new FsService());
        AccountsDto accountsDto = new AccountsDto();
        accountsDto.setVersion(0);
        List<AccountConfigDto> accountConfigDtos = new ArrayList<>();
        AccountConfigDto testAccount = new AccountConfigDto();
        testAccount.setServerAddress("http://matrix.org");
        testAccount.setLogin("login");
        testAccount.setPassword("password");
        accountConfigDtos.add(testAccount);
        accountsDto.setAccounts(accountConfigDtos);
        realAccountConfigFactory.saveAccountConfigs(accountsDto);
    }

    //TODO: implement more test -- read predefined accounts.xml and check results
}
