package com.dmytrobilokha.disturber.config.account;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Container for the AccountConfigDto objects
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "accounts")
public class AccountsDto {

    private int version;
    @XmlElement(name = "account", type = AccountConfigDto.class)
    private List<AccountConfigDto> accounts;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<AccountConfigDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountConfigDto> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "AccountsDto{" +
                "version=" + version +
                ", accounts=" + accounts +
                '}';
    }
}
