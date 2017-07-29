package com.dmytrobilokha.disturber.service.property;

import java.util.function.Function;

import static com.dmytrobilokha.disturber.Constants.NEW_LINE;

/**
 * The enumeration represents application property
 */
public enum Property {
    COLOR("color", true, Type.STRING)
    , PROPERTIES_VERSION("properties.version", true, Type.INTEGER)
    , TEST_ENUM("enum", true, Type.PROPERTY2);

    final String key;
    final boolean isMandatory;
    final Class clazz;
    private final Function<String, Object> parser;
    private final Function<Object, String> toStringConverter;

    Property(String key, boolean isMandatory, Type type) {
        this.key = key;
        this.isMandatory = isMandatory;
        this.clazz = type.clazz;
        this.parser = type.parser;
        this.toStringConverter = type.toStringConverter;
    }

    Object parseValue(String value, StringBuilder errorMessageBuilder) {
        if (value == null) {
            if (isMandatory) {
                errorMessageBuilder
                        .append("Property '" + this + "' is mandatory, but got null instead of value")
                        .append(NEW_LINE);
            }
            return null;
        }
        try {
            return parser.apply(value);
        } catch (Exception ex) {
            errorMessageBuilder.append("Property '" + this + "' should be convertable to '" + this.clazz.getName()
                    + "', but provided value '"
                    + value + "' is not valid").append(NEW_LINE);
            return null;
        }
    }

    String valueToString(Object value) {
        if (value == null)
            return null;
        return toStringConverter.apply(value);
    }

    enum Type {
        STRING(String.class, string -> string)
        , INTEGER(Integer.class, Integer::valueOf)
        , BOOLEAN(Boolean.class, Boolean::valueOf)
        , PROPERTY2(Property.class);

        private final Class clazz;
        private final Function<String, Object> parser;
        private final Function<Object, String> toStringConverter;

        <T extends Enum<T>> Type(Class<T> clazz) {
           this.clazz = clazz;
            parser = string -> Enum.valueOf(clazz, string);
            toStringConverter = obj -> (clazz.cast(obj)).name();
        }

        Type(Class clazz, Function<String, Object> parser) {
            this(clazz, parser, Object::toString);
        }


        Type(Class clazz, Function<String, Object> parser, Function<Object, String> toStringConverter) {
            this.clazz = clazz;
            this.parser = parser;
            this.toStringConverter = toStringConverter;
        }
    }
}
