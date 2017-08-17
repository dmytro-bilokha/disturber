package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.Constants;
import com.dmytrobilokha.disturber.config.property.PropertyService;
import com.dmytrobilokha.disturber.service.fs.FsService;
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
public class AccountConfigFactory {

    private static final String ACCOUNTS_FILE_NAME = "accounts.xml";

    private static final Logger LOG = LoggerFactory.getLogger(AccountConfigFactory.class);

    private Path accountsFilePath;
    private List<AccountConfig> accountConfigs;

    private FsService fsService;

    protected AccountConfigFactory() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public AccountConfigFactory(PropertyService propertyService, FsService fsService) {
        this.fsService = fsService;
        accountsFilePath = Paths.get(propertyService.getConfigDirLocation() + Constants.FILE_SEPARATOR + ACCOUNTS_FILE_NAME);
    }

    public List<AccountConfig> getAccountConfigs() {
        if (accountConfigs == null)
            accountConfigs = loadAccountConfigs();
        return Collections.unmodifiableList(accountConfigs);
    }

    private List<AccountConfig> loadAccountConfigs() {
        if (!fsService.pathExists(accountsFilePath))
            return new ArrayList<>();
        AccountsDto accountsDto;
        try {
            accountsDto = unmarshalAccountsDto(accountsFilePath);
        } catch (JAXBException ex) {
            LOG.error("Unable to read accounts from file '{}'. Looks like file is broken", accountsFilePath, ex);
            return Collections.emptyList(); //TODO implement some nice UI error message instead of doing this!
        } catch (IOException ex) {
            LOG.error("Unable to open and read accounts file '{}'. File read error", accountsFilePath, ex);
            return Collections.emptyList(); //TODO implement some nice UI error message instead of doing this!
        }
        return convertDtosToConfig(accountsDto.getAccounts());
    }

    //TODO: introduce application-specific exception
    private AccountsDto unmarshalAccountsDto(Path accountsFilePath) throws JAXBException, IOException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(AccountsDto.class).createUnmarshaller();
        try {
            return fsService.readFile(accountsFilePath
                    , reader -> (AccountsDto) unmarshaller.unmarshal(reader));
        } catch (JAXBException | IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw (RuntimeException) ex;
        }
    }

    private List<AccountConfig> convertDtosToConfig(Collection<AccountConfigDto> configDtos) {
        return configDtos.stream().map(this::mapDtoToConfig).collect(Collectors.toList());
    }

    private AccountConfig mapDtoToConfig(AccountConfigDto configDto) {
        return new AccountConfig(configDto.getServerAddress(), configDto.getLogin(), configDto.getPassword());
    }

    //TODO: introduce application-specific exceptions
    public void saveAccountConfigs(AccountsDto accountsDto) throws JAXBException, IOException {
        Marshaller marshaller = JAXBContext.newInstance(AccountsDto.class).createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
        try {
            fsService.writeFile(accountsFilePath
                    , writer -> marshaller.marshal(accountsDto, writer));
        } catch (JAXBException | IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw (RuntimeException) ex;
        }
    }

}
