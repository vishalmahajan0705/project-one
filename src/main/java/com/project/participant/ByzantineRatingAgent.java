package com.project.participant;


import com.project.blockchain.Block;
import com.project.blockchain.Transaction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("Byzantine")
@Component
public class ByzantineRatingAgent extends RatingAgent{


    @Override
    public boolean propose(Block block) {

        // Simulate byzantine behaviour by taking the last transaction in block
        // and replacing it with incorrect value.

        Transaction t = block.getTransactions().get(block.getTransactions().size()-1);
        t.setFirmEvent("Byzantine");

        return super.propose(block);
    }


}


