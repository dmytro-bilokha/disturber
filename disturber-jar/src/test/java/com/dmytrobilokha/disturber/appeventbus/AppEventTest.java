package com.dmytrobilokha.disturber.appeventbus;

import org.junit.Test;

public class AppEventTest {

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnNullType() {
        AppEvent.ofType(null);
    }

}
