package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.config.connection.NetworkConnectionConfig;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class represents Matrix account (configuration and current state)
 */
public class MatrixAccount {

    private final AccountConfig accountConfig;
    private final NetworkConnectionConfig networkConnectionConfig;

    //This queue is accessed from two threads: FX application thread and matrix sync worker thread
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();

    private State state = State.NOT_CONNECTED;
    private String accessToken;
    private String homeServer;
    private String userId;
    private String nextBatchId;

    MatrixAccount(AccountConfig accountConfig, NetworkConnectionConfig networkConnectionConfig) {
        this.accountConfig = accountConfig;
        this.networkConnectionConfig = networkConnectionConfig;
    }

    void addMessage(String message) {
        messageQueue.add(message);
    }

    void addMessages(Collection<String> messages) {
        messageQueue.addAll(messages);
    }

    String pollMessage() {
        return messageQueue.poll();
    }

    AccountConfig getAccountConfig() {
        return accountConfig;
    }

    NetworkConnectionConfig getNetworkConnectionConfig() {
        return networkConnectionConfig;
    }

    String getAccessToken() {
        return accessToken;
    }

    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    String getHomeServer() {
        return homeServer;
    }

    void setHomeServer(String homeServer) {
        this.homeServer = homeServer;
    }

    String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    String getNextBatchId() {
        return nextBatchId;
    }

    void setNextBatchId(String nextBatchId) {
        this.nextBatchId = nextBatchId;
    }

    public enum State {
        NOT_CONNECTED, CONNECTED, LOGGEDIN, INITIAL_SYNCED;
    }

}
