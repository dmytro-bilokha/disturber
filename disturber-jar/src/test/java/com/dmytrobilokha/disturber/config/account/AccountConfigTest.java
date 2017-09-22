package com.dmytrobilokha.disturber.config.account;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AccountConfigTest {

    @Test
    public void testPassesBuildParameters() {
        AccountConfig account = createPredefinedBuilder().build();
        checkPredefinedAccountData(account);
    }

    @Test
    public void testPassesProxyParameters() {
        AccountConfig account = createPredefinedBuilder()
                .proxyPort(12345)
                .proxyHost("proxy.host")
                .build();
        assertEquals("proxy.host", account.getProxyServer().getHost());
        assertEquals(12345, account.getProxyServer().getPort());
    }

    @Test(expected = IllegalStateException.class)
    public void testFailsOnServerAddressIsNull() {
        AccountConfig.Builder accountBuilder = createPredefinedBuilder();
        accountBuilder.serverAddress(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testFailsOnServerAddressIsEmpty() {
        AccountConfig.Builder accountBuilder = createPredefinedBuilder();
        accountBuilder.serverAddress("").build();
    }

    @Test
    public void testBuildsUserId() {
        AccountConfig account = createPredefinedBuilder().build();
        assertEquals("@LOGIN:my.mockserver.org", account.getUserId());
    }

    @Test(expected = IllegalStateException.class)
    public void testValidatesSyncTimeout() {
        createPredefinedBuilder()
                .syncTimeout(60000)
                .build();
    }

    @Test
    public void testValidatesSyncTimeoutOk() {
        createPredefinedBuilder()
                .syncTimeout(2000)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidatesNetworkTimeout() {
        createPredefinedBuilder()
                .networkTimeout(5)
                .build();
    }

    @Test
    public void testValidatesNetworkTimeoutOk() {
        createPredefinedBuilder()
                .networkTimeout(50000)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidatesTimeoutsRelation() {
        createPredefinedBuilder()
                .networkTimeout(20000)
                .syncTimeout(50000)
                .build();
    }

    @Test
    public void testValidatesTimeoutsRelationOk() {
        createPredefinedBuilder()
                .networkTimeout(3000)
                .syncTimeout(2000)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidatesProxyPort() {
        createPredefinedBuilder()
                .proxyHost("myproxy.net")
                .proxyPort(66000)
                .build();
    }

    @Test
    public void testValidatesProxyPortOk() {
        createPredefinedBuilder()
                .proxyHost("myproxy.net")
                .proxyPort(65000)
                .build();
    }

    private AccountConfig.Builder createPredefinedBuilder() {
        return AccountConfig.newBuilder()
                .serverAddress("http://my.mockserver.org/")
                .login("LOGIN")
                .password("PASSWORD")
                .betweenSyncPause(1)
                .syncTimeout(2000)
                .networkTimeout(3000);
    }

    private void checkPredefinedAccountData(AccountConfig account) {
        assertNotNull(account);
        assertEquals("http://my.mockserver.org/", account.getServerAddress());
        assertEquals("LOGIN", account.getLogin());
        assertEquals("PASSWORD", account.getPassword());
        assertEquals(1, account.getBetweenSyncPause());
        assertEquals(2000, account.getSyncTimeout());
        assertEquals(3000, account.getNetworkTimeout());
        assertNull(account.getProxyServer());
    }
}
