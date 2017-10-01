package com.dmytrobilokha.disturber.viewcontroller;

public enum DialogButton {
    NONE(null)
    , RETRY("button.retry")
    , STOP("button.stop")
    ;

    private final String labelKey;

    DialogButton(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getLabelKey() {
        return labelKey;
    }

}
