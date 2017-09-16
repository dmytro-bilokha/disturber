package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.Constants;
import com.dmytrobilokha.disturber.SystemMessage;
import com.dmytrobilokha.disturber.config.property.PropertyService;
import com.dmytrobilokha.disturber.fs.FsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The factory responsible for providing accounts configuration
 */
@ApplicationScoped
public class AccountConfigService {

    private static final String ACCOUNTS_FILE_NAME = "accounts.xml";
    private static final Logger LOG = LoggerFactory.getLogger(AccountConfigService.class);

    private Path accountsFilePath;
    private List<AccountConfig> accountConfigs;

    private FsService fsService;

    protected AccountConfigService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public AccountConfigService(PropertyService propertyService, FsService fsService) {
        this.fsService = fsService;
        accountsFilePath = Paths.get(propertyService.getConfigDirLocation() + Constants.FILE_SEPARATOR + ACCOUNTS_FILE_NAME);
    }

    public List<AccountConfig> getAccountConfigs() throws AccountConfigAccessException {
        if (accountConfigs == null)
            accountConfigs = loadAccountConfigs();
        return Collections.unmodifiableList(accountConfigs);
    }

    private List<AccountConfig> loadAccountConfigs() throws AccountConfigAccessException {
        if (!fsService.pathExists(accountsFilePath))
            return new ArrayList<>();
        AccountsDto accountsDto = unmarshalAccountsDto(accountsFilePath);
        return convertDtosToConfig(accountsDto.getAccounts());
    }

    private AccountsDto unmarshalAccountsDto(Path accountsFilePath) throws AccountConfigAccessException {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(AccountsDto.class).createUnmarshaller();
            return fsService.readFile(accountsFilePath
                    , reader -> (AccountsDto) unmarshaller.unmarshal(reader));
        } catch (IOException ex) {
            LOG.error("Unable to open and read accounts file '{}'", accountsFilePath, ex);
            throw new AccountConfigAccessException(
                    new SystemMessage("account.config.load.exception.io", accountsFilePath), ex);
        } catch (JAXBException ex) {
            LOG.error("Failed to unmarshal accounts xml file '{}'", accountsFilePath, ex);
            throw new AccountConfigAccessException(
                    new SystemMessage("account.config.load.exception.xml", accountsFilePath), ex);
        } catch (Exception ex) {
            LOG.error("Unexpected exception during reading accounts file {}", accountsFilePath, ex);
            throw new AccountConfigAccessException(
                    new SystemMessage("account.config.load.exception.unexpected", accountsFilePath), ex);
        }
    }

    private List<AccountConfig> convertDtosToConfig(Collection<AccountConfigDto> configDtos) {
        return configDtos.stream().map(this::mapDtoToConfig).collect(Collectors.toList());
    }

    private AccountConfig mapDtoToConfig(AccountConfigDto configDto) {
        return AccountConfig.newBuilder()
                .serverAddress(configDto.getServerAddress())
                .login(configDto.getLogin())
                .password(configDto.getPassword())
                .betweenSyncPause(configDto.getBetweenSyncPause())
                .syncTimeout(configDto.getSyncTimeout())
                .networkTimeout(configDto.getNetworkTimeout())
                .build();
    }

    public void saveAccountConfigs(AccountsDto accountsDto) throws AccountConfigAccessException {
        try {
            Marshaller marshaller = JAXBContext.newInstance(AccountsDto.class).createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            fsService.writeFile(accountsFilePath
                    , writer -> marshaller.marshal(accountsDto, writer));
        } catch (IOException ex) {
            LOG.error("Failed to save {} to file {}", accountsDto, accountsFilePath, ex);
            throw new AccountConfigAccessException(
                    new SystemMessage("account.config.save.exception.io", accountsFilePath), ex);
        } catch (JAXBException ex) {
            LOG.error("XML marshalling exception during saving {} to file {}", accountsDto, accountsFilePath, ex);
            throw new AccountConfigAccessException(
                    new SystemMessage("account.config.save.exception.xml", accountsFilePath), ex);
        } catch (Exception ex) {
            LOG.error("Unexpected exception during saving {} to file {}", accountsDto, accountsFilePath, ex);
            throw new AccountConfigAccessException(
                    new SystemMessage("account.config.save.exception.unexpected", accountsFilePath), ex);
        }
    }

}
