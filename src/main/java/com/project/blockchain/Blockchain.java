package com.project.blockchain;

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

    public synchronized void commitBlock(Block block) {
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

    public  List<Block> readBlocks(String firmId,long startTime,long endTime){
        List<Block> blocks = new ArrayList<>();

        try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(path));
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                blocks.add(Block.fromJSON(line));
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        System.out.println(blocks.size());
        return blocks;
    }



}
