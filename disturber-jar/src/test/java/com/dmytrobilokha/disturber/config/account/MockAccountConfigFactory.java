package com.dmytrobilokha.disturber.config.account;

public class MockAccountConfigFactory {

    public static AccountConfig createMockAccountConfig() {
        return AccountConfig.newBuilder()
                .serverAddress("http://my.mockserver.org/")
                .login("LOGIN")
                .password("PASSWORD")
                .betweenSyncPause(100)
                .syncTimeout(2000)
                .networkTimeout(3000)
                .build();
    }

    public static AccountConfig createMockAccountConfig(String postfix) {
        return AccountConfig.newBuilder()
                .serverAddress("http://my" + postfix + ".mockserver.org/")
                .login("LOGIN" + postfix)
                .password("PASSWORD" + postfix)
                .betweenSyncPause(100)
                .syncTimeout(2000)
                .networkTimeout(3000)
                .build();
    }

    public static AccountConfig createMockAccountConfigWithProxy() {
        return AccountConfig.newBuilder()
                .serverAddress("http://my.mockserver.org/")
                .login("LOGIN")
                .password("PASSWORD")
                .betweenSyncPause(100)
                .syncTimeout(2000)
                .networkTimeout(3000)
                .proxyHost("my.proxy.net")
                .proxyPort(8000)
                .build();
    }
}
