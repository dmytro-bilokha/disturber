package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class AccountRooms implements RoomsViewItem {

    private final ViewFactory viewFactory;
    private final Consumer<ObservableList<String>> switchChat;
    private final TreeItem<RoomsViewItem> treeItem;
    private final Map<RoomKey, Room> roomMap;

    AccountRooms(ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem, Consumer<ObservableList<String>> switchChat) {
        this.viewFactory = viewFactory;
        this.switchChat = switchChat;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.roomMap = new HashMap<>();
    }

    void reset() {
        roomMap.clear();
        treeItem.getChildren().clear();
    }

    void addNewRoom(RoomKey roomKey) {
        Room room = new Room(roomKey, viewFactory, treeItem, this);
        roomMap.put(roomKey, room);
    }

    void onEvent(RoomKey roomKey, MatrixEvent event) {
        roomMap.get(roomKey).onEvent(event);
    }

    void onMouseClick(Room roomToActivate) {
        for (Room room : roomMap.values()) {
            if (room == roomToActivate) {
                room.setActive(true);
                switchChat.accept(room.getEventsList());
            } else {
                room.setActive(false);
            }
        }
    }

    @Override
    public String getText() {
        return "Rooms";
    }

}
