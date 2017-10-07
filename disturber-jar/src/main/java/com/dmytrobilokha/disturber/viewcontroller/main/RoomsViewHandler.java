package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.chatstate.AccountState;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RoomsViewHandler implements RoomsView {

    private final TreeItem<RoomsViewItem> root;
    private final ViewFactory viewFactory;
    private final Map<String, Account> accountMap = new HashMap<>();
    private final Consumer<ObservableList<String>> switchChat;

    RoomsViewHandler(ViewFactory viewFactory, TreeView<RoomsViewItem> view, Consumer<ObservableList<String>> switchChat) {
        this.viewFactory = viewFactory;
        this.switchChat = switchChat;
        this.root = viewFactory.createTreeRoot();
        view.setShowRoot(false);
        view.setEditable(false);
        view.setRoot(root);
    }

    @Override
    public void addAccount(String userId) {
        accountMap.put(userId, new Account(userId, viewFactory, root, switchChat));
    }

    @Override
    public void resetAccount(String userId) {
        accountMap.get(userId).reset();
    }

    @Override
    public void changeAccountState(String userId, AccountState newState) {
        accountMap.get(userId).setState(newState);
    }

    @Override
    public void addNewRoom(RoomKey roomKey) {
        accountMap.get(roomKey.getUserId()).addNewRoom(roomKey);
    }

    @Override
    public void onEvent(RoomKey roomKey, MatrixEvent event) {
        accountMap.get(roomKey.getUserId()).onEvent(roomKey, event);
    }
}
