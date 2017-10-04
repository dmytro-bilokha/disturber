package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.function.Consumer;

class RoomsViewCellFactory implements Callback<TreeView<RoomKey>,TreeCell<RoomKey>> {

    private final Consumer<RoomKey> onClickHandler;

    RoomsViewCellFactory(Consumer<RoomKey> onClickHandler) {
        this.onClickHandler = onClickHandler;
    }


    @Override
    public TreeCell<RoomKey> call(TreeView<RoomKey> roomKeyTreeView) {
        return new RoomsViewCell(onClickHandler);
    }

}
