package com.project.participant;

import com.project.Constants;
import com.project.blockchain.Transaction;
import com.project.network.AbstractMulticastNode;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class InternalEventSource extends AbstractMulticastNode {

    private String firmId;
    String[] eventTypes = new String[]{"GOOD","BAD","AVERAGE"};
    Random rnd= new Random();


    public InternalEventSource(String firmId) {
        this.firmId = firmId;
        this.initialize();
    }

    private String createEvent(String event){
        Transaction a = new Transaction();
        a.setFirmEvent(event);
        a.setFirmId(firmId);
        a.setEventId(firmId +"-"+(new Date()).getTime());
        a.setSentBy(Constants.INTERNAL_EVENT_SOURCE);
        return a.toJSON();
    }

    public void initialize()  {
        try {
            this.receive();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void processMessage(String message){
    }

    public void sendEvent(){
        while(true){
            Scanner in = new Scanner(System.in);

            System.out.print("Enter Event:");
            String event = in.next();
            try {
                super.send(createEvent(event));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
