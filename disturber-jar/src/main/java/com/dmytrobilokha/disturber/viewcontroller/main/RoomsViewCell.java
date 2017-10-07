package com.dmytrobilokha.disturber.viewcontroller.main;

import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;

class RoomsViewCell extends TreeCell<RoomsViewItem> {

    RoomsViewCell() {
        this.setEditable(false);
        this.setOnMouseClicked(this::mouseEventDispatch);
    }

    @Override
    protected void updateItem(RoomsViewItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getText());
        }
    }

    private void mouseEventDispatch(MouseEvent mouseEvent) {
        if (getItem() == null)
            return;
        if (mouseEvent.getClickCount() < 1)
            return;
        getItem().onMouseClick();
    }

}
