package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The DTO represents Unsigned matrix protocol object
 */
@JsonIgnoreProperties(value = {"prev_content", "prev_sender", "replaces_state", "redacted_by", "redacted_because"})
public class UnsignedDto {

    @JsonProperty(value = "age", required = true)
    private Long age;
    @JsonProperty(value = "transaction_id", required = false)
    private String transactionId;

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
