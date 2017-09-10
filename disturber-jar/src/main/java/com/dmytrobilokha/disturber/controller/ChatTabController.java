package com.dmytrobilokha.disturber.controller;

import com.dmytrobilokha.disturber.MatrixEventsHistoryKeeper;
import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.network.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by dimon on 10.09.17.
 */
@Dependent
public class ChatTabController {


    private final AppEventListener<RoomKey, MatrixEvent> newEventHandler = this::onNewEvent;
    private final ObservableList<String> messageList = FXCollections.observableArrayList();
    private final AppEventBus appEventBus;
    private final MatrixEventsHistoryKeeper historyKeeper;

    private RoomKey myRoomKey;

    @FXML
    private Tab tab;
    @FXML
    private ListView<String> messageListView;

    @Inject
    public ChatTabController(AppEventBus appEventBus, MatrixEventsHistoryKeeper historyKeeper) {
        this.appEventBus = appEventBus;
        this.historyKeeper = historyKeeper;
    }

    @FXML
    public void initialize() {
        messageListView.setItems(messageList);
    }

    public void setRoomToFollow(RoomKey roomKey) {
        if (myRoomKey != null && myRoomKey.equals(roomKey))
            return; //Don't need to do anything if we already following the room
        tab.setText(roomKey.getRoomId());
        appEventBus.unsubscribe(newEventHandler, AppEventType.MATRIX_NEW_EVENT_GOT, myRoomKey);
        appEventBus.subscribe(newEventHandler, AppEventType.MATRIX_NEW_EVENT_GOT, roomKey);
        messageList.clear();
        myRoomKey = roomKey;
        historyKeeper.getRoomEventsHistory(roomKey).stream()
                .map(MatrixEvent::toString)
                .forEach(messageList::add);
        messageListView.refresh();
    }

    private void onNewEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        messageList.add(appEvent.getPayload().toString());
    }

}
