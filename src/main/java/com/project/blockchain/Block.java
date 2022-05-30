package com.project.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.utils.Utils;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Block {
    //header fields
    private int epoch;
    private String previousHash;
    private String merkleRootHash;
    private long timestamp;

    //block hash is computed using the header fields
    private String hash;

    private final List<Transaction> transactions;

    private String stage;

    {
        transactions = new ArrayList<>();
    }

    private String ratingAgentId;
    private String eventId;


    public Block(String previousHash, int epoch) {

        this.previousHash = previousHash;
        this.hash = computeHash();
        this.epoch = epoch;
        this.stage = "init";

    }

    public String computeHash() {
        String merkleRoot = (getTransactions().size()>0)?computeMerkleRoot():null;
        setMerkleRootHash(merkleRoot);

        String hash = Utils.hash(
                previousHash
                        + epoch
                        + timestamp
                        + getMerkleRootHash());


        setHash(hash);
        return hash;
    }

    private String computeMerkleRoot(){
        List<String> txns = new ArrayList<>();
        for(int i =0;i< getTransactions().size();i++){
            txns.add(transactions.get(i).computeHash());
        }
        return merkleTree(txns);

    }


    private String merkleTree(List<String> txnHashes){
        if(txnHashes.size()==1) return txnHashes.get(0);
        if(txnHashes.size()%2!=0) txnHashes.add(txnHashes.get(txnHashes.size()-1));
        List<String> parentHashes = new ArrayList<>();
        for(int i=0;i<txnHashes.size();i+=2){
            parentHashes.add(Utils.hash(txnHashes.get(i)+txnHashes.get(i+1)));
        }
        return merkleTree(parentHashes);
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Block() {}

    public String toJSON() throws JSONException {

        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    public static Block fromJSON(String s) throws JSONException{

        try {
            return new ObjectMapper().readValue(s, Block.class);
        } catch (JsonProcessingException e) {
            throw new JSONException(e);
        }

    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }



    public String getRatingAgentId() {
        return ratingAgentId;
    }

    public void setRatingAgentId(String ratingAgentId) {
        this.ratingAgentId = ratingAgentId;
    }

    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    public void setMerkleRootHash(String merkleRootHash) {
        this.merkleRootHash = merkleRootHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


}