package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.chatstate.AccountState;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;

public interface RoomsView {

    void addAccount(String userId);
    void resetAccount(String userId);
    void changeAccountState(String userId, AccountState newState);
    void addNewRoom(RoomKey roomKey);
    void onEvent(RoomKey roomKey, MatrixEvent event);

}
