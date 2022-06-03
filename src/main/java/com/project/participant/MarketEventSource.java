package com.project.participant;

import com.project.Constants;
import com.project.blockchain.Transaction;
import com.project.network.AbstractMulticastNode;
import java.io.IOException;

import static com.project.Constants.EXTERNAL_EVENT_SOURCE;
import static com.project.Constants.INTERNAL_EVENT_SOURCE;


public class MarketEventSource extends AbstractMulticastNode {


    private String marketEventSourceId;


    public MarketEventSource(String marketEventSourceId) {
        this.marketEventSourceId = marketEventSourceId;
        this.initialize();
    }



    public void initialize()  {
        try {
            this.receive();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void processMessage(String message){

        if(message.contains(Constants.BLOCK_ATTRIBUTE)){
            return;
        }

        Transaction transaction = Transaction.fromJSON(message);
        if(!transaction.getSentBy().equals(INTERNAL_EVENT_SOURCE)){
            return;
        }

        transaction.setSentBy(EXTERNAL_EVENT_SOURCE);
        transaction.setMarketEventSourceId(marketEventSourceId);


        int rating = -1;
        try {
            if(transaction.getFirmEvent().equals("GOOD")) rating = 5;
            if(transaction.getFirmEvent().equals("AVERAGE")) rating = 3;
            transaction.setMarketEvent(""+rating);

            super.send(transaction.toJSON());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
