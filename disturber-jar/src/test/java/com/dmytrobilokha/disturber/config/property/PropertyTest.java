package com.dmytrobilokha.disturber.config.property;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PropertyTest {

    @Test
    public void testNullToStringIsNull() {
        assertTrue(Property.PROPERTIES_VERSION.valueToString(null) == null);
    }

    @Test
    public void testConvertsIntegerToString() {
        String integerValue = "1234567";
        assertEquals(integerValue, Property.PROPERTIES_VERSION.valueToString(Integer.valueOf(integerValue)));
    }

    @Test
    public void testParsingNullForMandatoryReturnsNull() {
        assertTrue(Property.PROPERTIES_VERSION.parseValue(null, new StringBuilder()) == null);
    }

    @Test
    public void testParsingNullForMandatoryWritesError() {
        StringBuilder errorMessageBuilder = new StringBuilder();
        Property.PROPERTIES_VERSION.parseValue(null, errorMessageBuilder);
        String errorMessage = errorMessageBuilder.toString();
        assertFalse(errorMessage.isEmpty());
        assertTrue(errorMessage.contains("null"));
        assertTrue(errorMessage.contains(Property.PROPERTIES_VERSION.toString()));
    }

}
