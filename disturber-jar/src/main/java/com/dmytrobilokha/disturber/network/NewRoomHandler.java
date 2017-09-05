package com.dmytrobilokha.disturber.network;

/**
 * The interface should be implemented by the class which are going to handle new accounts and room sync events
 */
public interface NewRoomHandler {

    void onNewRoom(RoomKey roomKey);

}
