package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.scene.control.TreeItem;

import java.util.function.Consumer;

class Account implements RoomsViewItem {

    private final TreeItem<RoomsViewItem> treeItem;
    private final ViewFactory viewFactory;
    private final AccountConfig accountConfig;
    private final AccountRooms accountRooms;
    private final AccountInvites accountInvites;

    private AccountState state;

    Account(AccountConfig accountConfig, ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem
            , Consumer<Room> switchChat, Consumer<RoomKey> joinRequester) {
        this.accountConfig = accountConfig;
        this.viewFactory = viewFactory;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.accountRooms = new AccountRooms(viewFactory, this.treeItem, switchChat);
        this.accountInvites = new AccountInvites(viewFactory, this.treeItem, joinRequester);
    }

    Account setState(AccountState state) {
        if (this.state != state) {
            this.state = state;
            viewFactory.updateView(treeItem);
        }
        return this;
    }

    AccountState getState() {
        return state;
    }

    AccountConfig getAccountConfig() {
        return accountConfig;
    }

    Account reset() {
        accountRooms.reset();
        accountInvites.reset();
        return this;
    }

    Account onEvent(RoomKey roomKey, MatrixEvent event) {
        accountRooms.onEvent(roomKey, event);
        return this;
    }

    Account onInvite(RoomKey roomKey, MatrixEvent event) {
        accountInvites.onInvite(roomKey, event);
        return this;
    }

    Account onJoined(RoomKey roomKey) {
        accountInvites.onJoined(roomKey);
        return this;
    }

    @Override
    public String getText() {
        return accountConfig.getUserId() + (state == null ? "" : (" (" + state.toString() + ')'));
    }

}
