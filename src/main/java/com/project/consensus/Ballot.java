package com.project.consensus;

public class Ballot {
    private String blockhash;
    private int voteCount;
    private int totalNodes;
    private int epoch;

    public Ballot(String blockhash,int totalNodes, int epoch) {
        this.blockhash = blockhash;
        this.totalNodes = totalNodes;
        this.epoch = epoch;
        this.voteCount =0;
    }

    public void addVote(){
        voteCount++;
    }

    public boolean hasConsensus(){
        return voteCount>=Math.ceil((2/3)*totalNodes);
    }

    @Override
    public String toString() {
        return "Ballot{" +
                "blockhash='" + blockhash + '\'' +
                ", voteCount=" + voteCount +
                ", totalNodes=" + totalNodes +
                ", epoch=" + epoch +
                '}';
    }
}
