package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

class RoomsViewCell extends TreeCell<RoomKey> {

    private final Consumer<RoomKey> onClickHandler;

    RoomsViewCell(Consumer<RoomKey> onClickHandler) {
        this.onClickHandler = onClickHandler;
        this.setEditable(false);
        this.setOnMouseClicked(this::mouseEventDispatch);
    }

    @Override
    protected void updateItem(RoomKey item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.hasRoomId() ? item.getRoomId() : item.getUserId());
        }
    }

    private void mouseEventDispatch(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() < 1)
            return;
        if (isContainRoom())
            onClickHandler.accept(getItem());
    }

    private boolean isContainRoom() {
        RoomKey roomKey = getItem();
        return roomKey != null && roomKey.hasRoomId();
    }

}
