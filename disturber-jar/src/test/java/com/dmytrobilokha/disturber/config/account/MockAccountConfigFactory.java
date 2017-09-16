package com.dmytrobilokha.disturber.config.account;

public class MockAccountConfigFactory {

    public static AccountConfig createMockAccountConfig() {
        return AccountConfig.newBuilder()
                .serverAddress("SERVER_ADDRESS")
                .login("LOGIN")
                .password("PASSWORD")
                .betweenSyncPause(1)
                .syncTimeout(2)
                .networkTimeout(3)
                .build();
    }

    public static AccountConfig createMockAccountConfig(String postfix) {
        return AccountConfig.newBuilder()
                .serverAddress("SERVER_ADDRESS" + postfix)
                .login("LOGIN" + postfix)
                .password("PASSWORD" + postfix)
                .betweenSyncPause(1)
                .syncTimeout(2)
                .networkTimeout(3)
                .build();
    }
}
