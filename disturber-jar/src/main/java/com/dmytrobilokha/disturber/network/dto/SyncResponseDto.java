package com.dmytrobilokha.disturber.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class represents response of the matrix server to the synchronize request
 */
@JsonIgnoreProperties(value = {"device_one_time_keys_count", "to_device", "device_lists", "groups"}) //TODO: investigate and fix this
public class SyncResponseDto {

    @JsonProperty(value = "next_batch", required = true)
    private String nextBatch;
    @JsonProperty(value = "rooms", required = true)
    private RoomsDto rooms;
    @JsonProperty(value = "presence", required = true)
    private PresenceDto presence;
    @JsonProperty(value = "account_data", required = false)
    private AccountDataDto accountData; //TODO: investigate why in the sync response example this is present, but in
                                        //the documentation (6.2.1) -- no???

    public String getNextBatch() {
        return nextBatch;
    }

    public void setNextBatch(String nextBatch) {
        this.nextBatch = nextBatch;
    }

    public RoomsDto getRooms() {
        return rooms;
    }

    public void setRooms(RoomsDto rooms) {
        this.rooms = rooms;
    }

    public PresenceDto getPresence() {
        return presence;
    }

    public void setPresence(PresenceDto presence) {
        this.presence = presence;
    }

    public AccountDataDto getAccountData() {
        return accountData;
    }

    public void setAccountData(AccountDataDto accountData) {
        this.accountData = accountData;
    }
}
