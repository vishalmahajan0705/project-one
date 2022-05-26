package com.project.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.project.utils.Utils;
import org.json.JSONException;

import java.util.Objects;

public class Transaction implements java.io.Serializable{
    private String firmId;
    private String eventId;
    private String firmEvent;
    private String sentBy;
    private String marketEventSourceId;
    private String marketEvent;

    public Transaction() {
    }

    public String getFirmId() {
        return firmId;
    }

    public void setFirmId(String firmId) {
        this.firmId = firmId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getFirmEvent() {
        return firmEvent;
    }

    public void setFirmEvent(String firmEvent) {
        this.firmEvent = firmEvent;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getMarketEventSourceId() {
        return marketEventSourceId;
    }

    public void setMarketEventSourceId(String marketEventSourceId) {
        this.marketEventSourceId = marketEventSourceId;
    }

    public String getMarketEvent() {
        return marketEvent;
    }

    public void setMarketEvent(String marketEvent) {
        this.marketEvent = marketEvent;
    }

    public String toJSON() throws JSONException {

        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static Transaction fromJSON(String s) throws JSONException{

        try {
            return new ObjectMapper().readValue(s, Transaction.class);
        } catch (JsonProcessingException e) {
            throw new JSONException(e);
        }

    }

    public String computeHash(){
        return Utils.hash(this.toJSON());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return firmId.equals(that.firmId) && eventId.equals(that.eventId) && firmEvent.equals(that.firmEvent) && sentBy.equals(that.sentBy) && Objects.equals(marketEventSourceId, that.marketEventSourceId) && Objects.equals(marketEvent, that.marketEvent);
    }


}