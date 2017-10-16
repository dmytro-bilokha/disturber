package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.scene.control.TreeItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

class AccountInvites implements RoomsViewItem {

    private final ViewFactory viewFactory;
    private final TreeItem<RoomsViewItem> treeItem;
    private final Map<RoomKey, Invite> inviteMap;
    private final Consumer<RoomKey> joinRequester;

    AccountInvites(ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem, Consumer<RoomKey> joinRequester) {
        this.viewFactory = viewFactory;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.joinRequester = joinRequester;
        this.inviteMap = new HashMap<>();
    }

    void reset() {
        inviteMap.clear();
        treeItem.getChildren().clear();
    }

    private Invite addNewInvite(RoomKey roomKey) {
        Invite invite = new Invite(roomKey, viewFactory, treeItem, joinRequester);
        inviteMap.put(roomKey, invite);
        return invite;
    }

    void onInvite(RoomKey roomKey, MatrixEvent event) {
        Invite invite = inviteMap.get(roomKey);
        if (invite == null)
            addNewInvite(roomKey);
    }

    void onJoined(RoomKey roomKey) {
        Invite inviteAccepted = inviteMap.remove(roomKey);
        for (Iterator<TreeItem<RoomsViewItem>> inviteIterator = treeItem.getChildren().iterator();
             inviteIterator.hasNext();) {
            TreeItem<RoomsViewItem>  inviteItem = inviteIterator.next();
            if (inviteItem.getValue() == inviteAccepted) {
                inviteIterator.remove();
                break;
            }
        }
    }

    @Override
    public String getText() {
        return "Invites"; //TODO: change it here and in the AccountRooms to fetch text from resource bundle, not hardcode
    }

}
