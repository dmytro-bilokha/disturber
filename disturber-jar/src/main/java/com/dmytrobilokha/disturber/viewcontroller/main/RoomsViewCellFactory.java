package com.dmytrobilokha.disturber.viewcontroller.main;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;


class RoomsViewCellFactory implements Callback<TreeView<RoomsViewItem>,TreeCell<RoomsViewItem>> {

    @Override
    public TreeCell<RoomsViewItem> call(TreeView<RoomsViewItem> roomsViewItemTreeView) {
        return new RoomsViewCell();
    }

}
