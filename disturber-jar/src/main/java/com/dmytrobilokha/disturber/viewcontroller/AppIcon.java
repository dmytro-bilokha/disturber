package com.dmytrobilokha.disturber.viewcontroller;

import static com.dmytrobilokha.disturber.Constants.ICONS_PATH;

public enum AppIcon {

    MAIN_QUIET(ICONS_PATH + "disturber-quiet.png")
    , MAIN_MESSAGE(ICONS_PATH + "disturber-message.png")
    ;


    private final String location;

    AppIcon(String location) {
        this.location = location;
    }

    String getLocation() {
        return location;
    }

}
