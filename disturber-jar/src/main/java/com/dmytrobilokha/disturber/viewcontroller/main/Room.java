package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;


class Room implements RoomsViewItem {

    private final RoomKey roomKey;
    private final ViewFactory viewFactory;
    private final TreeItem<RoomsViewItem> treeItem;
    private final ObservableList<String> eventsList;
    private final AccountRooms accountRooms;

    private int unreadMessages = 0;
    private boolean active = false;

    Room(RoomKey roomKey, ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem, AccountRooms accountRooms) {
        this.roomKey = roomKey;
        this.viewFactory = viewFactory;
        this.accountRooms = accountRooms;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.eventsList = viewFactory.createList();
    }

    void onEvent(MatrixEvent event) {
        eventsList.add(event.getContent());
        if (!active) {
            unreadMessages++;
            viewFactory.updateView(treeItem);
        }
    }

    void setActive(boolean active) {
        this.active = active;
        if (active) {
            unreadMessages = 0;
            viewFactory.updateView(treeItem);
        }
    }

    ObservableList<String> getEventsList() {
        return eventsList;
    }

    @Override
    public String getText() {
        return roomKey.getRoomId() + (unreadMessages == 0 ? "" : " (" + unreadMessages + ')');
    }

    @Override
    public void onMouseClick() {
        accountRooms.onMouseClick(this);
    }

}
