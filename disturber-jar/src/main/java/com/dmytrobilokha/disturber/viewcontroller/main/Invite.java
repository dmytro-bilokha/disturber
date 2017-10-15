package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.scene.control.TreeItem;

class Invite implements RoomsViewItem {

    private final RoomKey roomKey;
    private final ViewFactory viewFactory;
    private final TreeItem<RoomsViewItem> treeItem;

    Invite(RoomKey roomKey, ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem) {
        this.roomKey = roomKey;
        this.viewFactory = viewFactory;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
    }

    @Override
    public String getText() {
        return roomKey.getRoomId();
    }
}
