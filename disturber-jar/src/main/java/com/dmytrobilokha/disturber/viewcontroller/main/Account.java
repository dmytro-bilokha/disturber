package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.config.account.AccountConfig;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.function.Consumer;

class Account implements RoomsViewItem {

    private final TreeItem<RoomsViewItem> treeItem;
    private final ViewFactory viewFactory;
    private final AccountConfig accountConfig;
    private final AccountRooms accountRooms;

    private AccountState state;

    Account(AccountConfig accountConfig, ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem
            , Consumer<ObservableList<String>> switchChat) {
        this.accountConfig = accountConfig;
        this.viewFactory = viewFactory;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.accountRooms = new AccountRooms(viewFactory, this.treeItem, switchChat);
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
        return this;
    }

    Account onEvent(RoomKey roomKey, MatrixEvent event) {
        accountRooms.onEvent(roomKey, event);
        return this;
    }

    @Override
    public String getText() {
        return accountConfig.getUserId() + (state == null ? "" : (" (" + state.toString() + ')'));
    }

}
