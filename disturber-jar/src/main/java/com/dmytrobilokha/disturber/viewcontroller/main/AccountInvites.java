package com.dmytrobilokha.disturber.viewcontroller.main;

import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import com.dmytrobilokha.disturber.viewcontroller.ViewFactory;
import javafx.scene.control.TreeItem;

import java.util.HashMap;
import java.util.Map;

class AccountInvites implements RoomsViewItem {

    private final ViewFactory viewFactory;
    private final TreeItem<RoomsViewItem> treeItem;
    private final Map<RoomKey, Invite> inviteMap;

    AccountInvites(ViewFactory viewFactory, TreeItem<RoomsViewItem> fatherItem) {
        this.viewFactory = viewFactory;
        this.treeItem = viewFactory.createTreeItem(this, fatherItem);
        this.inviteMap = new HashMap<>();
    }

    void reset() {
        inviteMap.clear();
        treeItem.getChildren().clear();
    }

    private Invite addNewInvite(RoomKey roomKey) {
        Invite invite = new Invite(roomKey, viewFactory, treeItem);
        inviteMap.put(roomKey, invite);
        return invite;
    }

    void onInvite(RoomKey roomKey, MatrixEvent event) {
        Invite invite = inviteMap.get(roomKey);
        if (invite == null)
            invite = addNewInvite(roomKey);
    }

    @Override
    public String getText() {
        return "Invites";
    }

}
