package com.project.participant;

import com.project.Constants;
import com.project.blockchain.Block;
import com.project.blockchain.Blockchain;
import com.project.blockchain.Transaction;
import com.project.consensus.Ballot;
import com.project.consensus.CoordinationService;
import com.project.network.AbstractMulticastNode;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Profile("Normal")
@Component
public class RatingAgent extends AbstractMulticastNode implements DisposableBean{



    private String ratingAgentId;
    private final Object mutex = new Object();

    private Blockchain localBlockchain;

    @Autowired
    CoordinationService zks;

    @PostConstruct
    public void init() throws Exception{
        this.setRatingAgentId(zks.create(Constants.RATING_AGENT, null));
        this.localBlockchain = new Blockchain(Constants.BLOCKCHAIN_PATH_PREFIX + ratingAgentId.substring(Constants.RATING_AGENT.length()) + Constants.BLOCKCHAIN_PATH_SUFFIX);
        zks.election(this);
        this.initializeListener();
    }


    public void initializeListener() {
        try {
            this.receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Map<Integer, Ballot> ballotsByEpoch = new ConcurrentHashMap<>();


    public synchronized void  processMessage(String message) {
        if(!message.contains(Constants.BLOCK_ATTRIBUTE)){
            Transaction transaction = Transaction.fromJSON(message);
            localBlockchain.addTransaction(transaction);
            return;
        }

        Block block = Block.fromJSON(message);
        if(block.getEpoch()>0){
            if(block.getStage().equals(Constants.PROPOSE)){
                vote(block);
            }else if(block.getStage().equals(Constants.VOTE)){
                if(ballotsByEpoch.get(block.getEpoch())==null){
                    ballotsByEpoch.put(block.getEpoch(), new Ballot(block.getHash(), zks.totalNodes(Constants.RATING_AGENT_DIR),block.getEpoch() ));
                }
                ballotsByEpoch.get(block.getEpoch()).addVote();
                if(ballotsByEpoch.get(block.getEpoch()).hasConsensus()){
                    synchronized (mutex) {
                        mutex.notifyAll();
                    }
                }
            }
            else{
                prepare(block);
            }

        }
    }


    /*
    If the block does not receive sufficient votes in the epoc , its not committed.
    Corresponding transactions can be picked up in subsequent epochs for inclusion in future blocks.
    */

    public  boolean propose(Block block){


        System.out.println("Proposing ... "+block.getEpoch()+" " +block.getHash() + " ");


        try {
            this.send(block.toJSON());

            try {
                synchronized (mutex){
                    mutex.wait(3000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return ballotsByEpoch.get(block.getEpoch()).hasConsensus();


        } catch (Exception e) {
            System.out.println("Exception "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    /*
    Proposed block is checked for validity (should be in a higher epoc than the current highest epoc,
    previous hash on the block should match hash of the last block
    and all transactions on the block should be available in pending transactions pool.
    If all conditions meet, the rating agent not will cast a vote of acceptance.
    */
    public void vote(Block block) {


        if (getLocalBlockchain().getLastBlock().getEpoch() >= block.getEpoch()) {
            System.out.println("Can't add lower epoc " + getLocalBlockchain().getLastBlock().getEpoch() + " " + block.getEpoch());
            return;
        }

        if (!getLocalBlockchain().getLastBlock().getHash().equals(block.getPreviousHash())) {
            System.out.println("PreviousHash on current block must match the hash of last block.");
            return;
        }

        List<Transaction> blockTransactions = block.getTransactions();
        List<Transaction> poolTransactions = localBlockchain.getPendingTransactions().get(block.getEventId());

        if (poolTransactions != null) {
            for (Transaction blockTransaction : blockTransactions) {
                if (!poolTransactions.contains(blockTransaction)) {
                    System.out.println("All transactions did not match , Invalid Block:: " + block.getEpoch() +" "+block.getTransactions());
                    return;
                }
            }
        }


        try {

            block.setStage(Constants.VOTE);
            this.send(block.toJSON());
            System.out.println("Voted..... " + block.getEventId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /*
    To eliminate the effect of a byzantine leader who issued a prepare for block,
    Individual nodes retest for 2n/3 votes before issuing a final commit call.
     */
    public void prepare(Block block){
        System.out.println("Entered Prepared..");

        if(ballotsByEpoch.get(block.getEpoch()).hasConsensus()){
            commit(block);
        }

    }

    /*
    A committed block is considered final, all transactions that a are a part of the block at this stage are removed
    from the pending transactions.
     */
    public void commit(Block block){
        List<Transaction> blockTransactions = block.getTransactions();
        List<Transaction> poolTransactions = localBlockchain.getPendingTransactions().get(block.getEventId());
        for (Transaction blockTransaction : blockTransactions) {
            poolTransactions.remove(blockTransaction);
        }
        this.getLocalBlockchain().commitBlock(block);
        if(block.getRatingAgentId().equals(this.getRatingAgentId())){
            Blockchain.log(block);
        }

        System.out.println("Committed..");


    }

    public Blockchain getLocalBlockchain() {
        return localBlockchain;
    }


    @PreDestroy
    public void destroy() {
        System.out.println(
                "Callback triggered - DisposableBean."+this.ratingAgentId);
        zks.delete(this.ratingAgentId);
    }

    public String getRatingAgentId() {
        return ratingAgentId;
    }

    public void setRatingAgentId(String ratingAgentId) {
        this.ratingAgentId = ratingAgentId;
    }






}


