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


    /* if votes exceed 2/3 times the total nodes, consensus is achieved.
       vote count maintained at a epoc level by each rating agent
       and is incremented each time a node receives vote on a block in given epoch
     */

    public boolean hasConsensus(){
        return voteCount>=Math.ceil(totalNodes*(2/3f));
    }



    @Override
    public String toString() {
        return "Ballot{" +
                "blockhash='" + blockhash + '\'' +
                ", voteCount=" + voteCount +
                ", totalNodes=" + totalNodes +
                ", epoch=" + epoch  +
                '}';
    }
}
