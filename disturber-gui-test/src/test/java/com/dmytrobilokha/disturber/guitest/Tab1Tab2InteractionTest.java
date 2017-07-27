package com.dmytrobilokha.disturber.guitest;

import com.dmytrobilokha.disturber.boot.ConteinerizedTestCase;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.service.query.impl.NodeQueryUtils.hasText;

public class Tab1Tab2InteractionTest extends ConteinerizedTestCase {

    @Test
    public void testGreetingChanges() {
        verifyThat("#nameValueLabel", hasText(getResources().getString("undefined_name")));

        clickOn("#name")
                .write("TESTNAME")
                .clickOn("#submitButton");

        verifyThat("#nameValueLabel", hasText("Hello, TESTNAME"));
    }

    @Override
    protected String getFxmlLocation() {
        return "/fxml/TabPanel.fxml";
    }

}
