package com.dmytrobilokha.disturber.config.account;

import com.dmytrobilokha.disturber.Constants;
import com.dmytrobilokha.disturber.config.account.dto.AccountConfigDto;
import com.dmytrobilokha.disturber.config.account.dto.AccountsDto;
import com.dmytrobilokha.disturber.config.account.dto.ProxyServerDto;
import com.dmytrobilokha.disturber.config.property.PropertyService;
import com.dmytrobilokha.disturber.fs.FsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringWriter;
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
    private static final String ACCOUNTS_XSD_RESOURCE = "/accounts.xsd";
    private static final Logger LOG = LoggerFactory.getLogger(AccountConfigService.class);

    private Path accountsFilePath;
    private List<AccountConfig> accountConfigs;

    private FsService fsService;
    private ExceptionFactory exceptionFactory;

    protected AccountConfigService() {
        //Empty no-args constructor to keep CDI framework happy
    }

    @Inject
    public AccountConfigService(PropertyService propertyService, FsService fsService, ExceptionFactory exceptionFactory) {
        this.fsService = fsService;
        this.exceptionFactory = exceptionFactory;
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
        AccountXmlValidationEventHandler validationEventHandler = new AccountXmlValidationEventHandler();
        AccountsDto accountsDto = null;
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(AccountsDto.class).createUnmarshaller();
            unmarshaller.setSchema(getAccountSchema());
            unmarshaller.setEventHandler(validationEventHandler);
            accountsDto = fsService.readFile(accountsFilePath
                    , reader -> (AccountsDto) unmarshaller.unmarshal(reader));
        } catch (SAXException ex) {
            LOG.error("Unable to create validation schema from internal resource '{}'", ACCOUNTS_XSD_RESOURCE, ex);
            throw exceptionFactory.failedSchemaCreationOnLoad(ACCOUNTS_XSD_RESOURCE, ex);
        } catch (IOException ex) {
            LOG.error("Unable to open and read accounts file '{}'", accountsFilePath, ex);
            throw exceptionFactory.failedRead(accountsFilePath, ex);
        } catch (JAXBException ex) {
            LOG.error("Failed to unmarshal accounts xml file '{}'. Following errors found: {}"
                    , accountsFilePath, validationEventHandler.getErrorMessage(), ex);
            throw exceptionFactory.failedValidationOnLoad(accountsFilePath, validationEventHandler.getErrorMessage(), ex);
        } catch (Exception ex) {
            LOG.error("Unexpected exception during reading accounts file {}", accountsFilePath, ex);
            throw exceptionFactory.failedRead(accountsFilePath, ex);
        }
        if (validationEventHandler.isErrorDetected()) {
            LOG.error("Failed to unmarshal accounts xml file '{}'. Following errors found: {}"
                    , accountsFilePath, validationEventHandler.getErrorMessage());
            throw exceptionFactory.failedValidationOnLoad(accountsFilePath, validationEventHandler.getErrorMessage());
        }
        return accountsDto;
    }

    private Schema getAccountSchema() throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return sf.newSchema(getClass().getResource(ACCOUNTS_XSD_RESOURCE));
    }

    private List<AccountConfig> convertDtosToConfig(Collection<AccountConfigDto> configDtos) {
        return configDtos.stream().map(this::mapDtoToConfig).collect(Collectors.toList());
    }

    private AccountConfig mapDtoToConfig(AccountConfigDto configDto) {
        AccountConfig.Builder accountBuilder = AccountConfig.newBuilder()
                .serverAddress(configDto.getServerAddress())
                .login(configDto.getLogin())
                .password(configDto.getPassword())
                .betweenSyncPause(configDto.getBetweenSyncPause())
                .syncTimeout(configDto.getSyncTimeout())
                .networkTimeout(configDto.getNetworkTimeout());
        ProxyServerDto proxyDto = configDto.getProxyServer();
        if (proxyDto != null)
            accountBuilder
                    .proxyHost(proxyDto.getHost())
                    .proxyPort(proxyDto.getPort());
        return accountBuilder.build();
    }

    public void saveAccountConfigs(AccountsDto accountsDto) throws AccountConfigAccessException {
        AccountXmlValidationEventHandler validationEventHandler = new AccountXmlValidationEventHandler();
        try {
            Marshaller marshaller = JAXBContext.newInstance(AccountsDto.class).createMarshaller();
            marshaller.setSchema(getAccountSchema());
            marshaller.setEventHandler(validationEventHandler);
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            StringWriter intermediateWriter = new StringWriter();
            marshaller.marshal(accountsDto, intermediateWriter);
            if (!validationEventHandler.isErrorDetected()) {
                fsService.writeFile(accountsFilePath
                        , writer -> writer.write(intermediateWriter.toString()));
                accountConfigs = null;
            }
        } catch (SAXException ex) {
            LOG.error("Unable to create validation schema from internal resource '{}'", ACCOUNTS_XSD_RESOURCE, ex);
            throw exceptionFactory.failedSchemaCreationOnSave(ACCOUNTS_XSD_RESOURCE, ex);
        } catch (IOException ex) {
            LOG.error("Failed to save {} to file {}", accountsDto, accountsFilePath, ex);
            throw exceptionFactory.failedWrite(accountsFilePath, ex);
        } catch (JAXBException ex) {
            LOG.error("XML marshalling exception during saving {} to file {}. Following errors found: {}"
                    , accountsDto, accountsFilePath, validationEventHandler.getErrorMessage(), ex);
            throw exceptionFactory.failedValidationOnSave(accountsFilePath, validationEventHandler.getErrorMessage(), ex);
        } catch (Exception ex) {
            LOG.error("Unexpected exception during saving {} to file {}", accountsDto, accountsFilePath, ex);
            throw exceptionFactory.failedWrite(accountsFilePath, ex);
        }
        if (validationEventHandler.isErrorDetected()) {
            LOG.error("XML marshalling exception during saving {} to file {}. Following errors found: {}"
                    , accountsDto, accountsFilePath, validationEventHandler.getErrorMessage());
            throw exceptionFactory.failedValidationOnSave(accountsFilePath, validationEventHandler.getErrorMessage());
        }
    }

}
