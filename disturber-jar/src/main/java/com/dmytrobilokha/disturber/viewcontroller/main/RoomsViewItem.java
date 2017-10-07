package com.dmytrobilokha.disturber.viewcontroller.main;

public interface RoomsViewItem {

    String getText();
    default void onMouseClick() {
        //By default, do nothing
    }
}
