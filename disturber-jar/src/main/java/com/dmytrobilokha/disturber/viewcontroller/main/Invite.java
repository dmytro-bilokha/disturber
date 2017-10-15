package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.scene.control.TreeItem;

import java.util.function.Consumer;

class Invite implements RoomsViewItem {

    private final RoomKey roomKey;
    private final ViewFactory viewFactory;
    private final Consumer<RoomKey> joinRequester;
    private final TreeItem<RoomsViewItem> treeItem;

    Invite(RoomKey roomKey, ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem
            , Consumer<RoomKey> joinRequester) {
        this.roomKey = roomKey;
        this.viewFactory = viewFactory;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.joinRequester = joinRequester;
    }

    @Override
    public String getText() {
        return roomKey.getRoomId();
    }

    @Override
    public void onMouseClick() {
        joinRequester.accept(roomKey);
    }

}
