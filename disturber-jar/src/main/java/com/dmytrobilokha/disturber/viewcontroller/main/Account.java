package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.chatstate.AccountState;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.function.Consumer;

class Account implements RoomsViewItem {

    private final TreeItem<RoomsViewItem> treeItem;
    private final ViewFactory viewFactory;
    private final String userId;
    private final AccountRooms accountRooms;

    private AccountState state;

    Account(String userId, ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem, Consumer<ObservableList<String>> switchChat) {
        this.userId = userId;
        this.viewFactory = viewFactory;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.accountRooms = new AccountRooms(viewFactory, this.treeItem, switchChat);
    }

    void setState(AccountState state) {
        this.state = state;
        viewFactory.updateView(treeItem);
    }

    void reset() {
        accountRooms.reset();
    }

    void addNewRoom(RoomKey roomKey) {
        accountRooms.addNewRoom(roomKey);
    }

    void onEvent(RoomKey roomKey, MatrixEvent event) {
        accountRooms.onEvent(roomKey, event);
    }

    @Override
    public String getText() {
        return userId + (state == null ? "" : (" (" + state.toString() + ')'));
    }

}
