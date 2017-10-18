package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


class Room implements RoomsViewItem {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final RoomKey roomKey;
    private final ViewFactory viewFactory;
    private final TreeItem<RoomsViewItem> treeItem;
    private final ObservableList<TextField> eventsList;
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
        eventsList.add(viewFactory.createSelectableField(formatEvent(event)));
        if (!active) {
            unreadMessages++;
            viewFactory.updateView(treeItem);
        }
    }

    private String formatEvent(MatrixEvent event) {
        StringBuilder eventBuilder = new StringBuilder();
        LocalDateTime timestamp = event.getServerTimestamp();
        if (timestamp != null)
            eventBuilder
                    .append('(')
                    .append(timestamp.format(DATE_TIME_FORMATTER))
                    .append(')');
        eventBuilder
                .append(' ')
                .append(event.getSender())
                .append(": ")
                .append(event.getContent());
        return eventBuilder.toString();
    }

    void setActive(boolean active) {
        this.active = active;
        if (active) {
            unreadMessages = 0;
            viewFactory.updateView(treeItem);
        }
    }

    ObservableList<TextField> getEventsList() {
        return eventsList;
    }

    RoomKey getRoomKey() {
        return roomKey;
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
