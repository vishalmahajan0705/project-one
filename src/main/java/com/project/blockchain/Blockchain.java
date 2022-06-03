package com.project.blockchain;

import com.project.Constants;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Blockchain {
    private final List<Block> bChain = new ArrayList<>();
    private String path;

    /*
    All transactions are first added to pendingTransactions and maintained here until they are confirmed in a block.
    Once confirmed for a block transactions are removed from pendingTransactions.
     */
    private final Map<String, List<Transaction>> pendingTransactions;

    {
        pendingTransactions = new ConcurrentHashMap<>();
    }

    public Blockchain(String path) {
        this.path = path;
        System.out.println(path);
        bChain.add(new Block("0",0));
    }

    public synchronized void addBlock(Block b){
        bChain.add(b);
    }

    public synchronized Block getLastBlock(){
        return bChain.get(bChain.size() - 1);
    }

    public synchronized void addTransaction(Transaction transaction){
        pendingTransactions.computeIfAbsent(transaction.getEventId(), k -> new ArrayList<>());
        pendingTransactions.get(transaction.getEventId()).add(transaction);
    }

    public Map<String, List<Transaction>> getPendingTransactions() {
        return pendingTransactions;
    }

    /*
    persist block to path defined for ratingAgent holding instance of this class.
     */
    public void commitBlock(Block block) {
        BufferedWriter bw = null;
        try {

            FileWriter fw = new FileWriter(path, true);
            bw = new BufferedWriter(fw);
            bw.write(block.toJSON());

            bw.newLine();
            bw.flush();

            addBlock(block);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert bw != null;
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void log(Block block) {
        BufferedWriter bw = null;
        try {

            FileWriter fw = new FileWriter(Constants.BLOCKCHAIN_PATH_PREFIX+"master"+Constants.BLOCKCHAIN_PATH_SUFFIX, true);
            bw = new BufferedWriter(fw);
            bw.write(block.toJSON());

            bw.newLine();
            bw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert bw != null;
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /*
    basic query method to read blocks by firmId or by firmId and date range
    */
    public  List<Block> readBlocks(String firmId,long startTime,long endTime){
        List<Block> blocks = new ArrayList<>();

        try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(path));
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Block b = Block.fromJSON(line);
                if(b.getTransactions().get(0).getFirmId().equals(firmId)){
                    if(startTime>0 && endTime>0){
                        if(b.getTimestamp()>=startTime && b.getTimestamp()<=endTime){
                            blocks.add(b);
                        }
                    }else if(startTime>0 && endTime==0){
                        if(b.getTimestamp()>=startTime){
                            blocks.add(b);
                        }
                    }else if(startTime==0 && endTime>=0){
                        if(b.getTimestamp()<=startTime){
                            blocks.add(b);
                        }
                    }else{
                        blocks.add(b);
                    }

                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        System.out.println(blocks.size());
        return blocks;
    }


    public  List<Block> readBlocks(String firmId){
        return readBlocks(firmId,0,0);
    }



}
