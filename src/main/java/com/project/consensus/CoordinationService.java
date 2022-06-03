package com.project.consensus;

import com.project.Constants;
import com.project.blockchain.Block;
import com.project.participant.RatingAgent;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;

import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.*;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
    This service uses ZK to facilitate Leader Election at each Epoc interval.
    Epoc is managed via a shared counter in ZK and incremented every Constants.EPOCH_INTERVAL_MILLIS
    A new Leader Rating agent is elected at each epoch interval. (leader path in ZK is /leader/epoch-leader)
 */

@Service
public class CoordinationService {

    public ZooKeeper zConn;
    public CuratorFramework client;
    SharedCount epoch;
    LeaderSelector leaderSelector;

    RetryPolicy retryPolicy = new RetryNTimes(3, 100);


    @PostConstruct
    public void init()  throws Exception
    {
        zConn =  new ZooKeeper(Constants.ZK_CONNECTION_STRING, 3000, we -> {
            if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.println("Connected...");
            }
        });

        if(zConn.exists(Constants.RATING_AGENT_DIR, true)==null){
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(new Date().getTime());
            zConn.create("/rating-agents",buffer.array(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        }
    }


    public String create(String path, byte[] data) throws KeeperException, InterruptedException
    {
        return zConn.create(path,data,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);

    }



    public void election(RatingAgent r){
        System.out.println("Starting election...");

        client = CuratorFrameworkFactory
                .newClient(Constants.ZK_CONNECTION_STRING, retryPolicy);
        client.start();


        // Epoch number is maintained as a shared counter managed by zookeeper (/counter/epoch Persistent Node)
        // A new rating agent is elected at each epoc interval


        epoch = new SharedCount(client, "/counter/epoch", 0);
        try {
            epoch.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         Epoch is incremented every Constants.EPOCH_INTERVAL_MILLIS,  for the duration of this interval the leader assembles transactions to block,
         proposes it, the block is voted for and committed post consensus.
         At the end of the epoc leadership is relinquished and the leader goes back into the queue for subsequent election at a later time.
         */

        leaderSelector = new LeaderSelector(client,
                "/leader/epoch-leader",
                new LeaderSelectorListener() {
                    @Override
                    public void stateChanged(
                            CuratorFramework client,
                            ConnectionState newState) {
                    }

                    @Override
                    public void takeLeadership(
                            CuratorFramework client) throws Exception {
                        epoch.setCount(epoch.getCount() + 1);
                        System.out.println(" is now the leader. Waiting ..." + epoch.getCount());
                        long startTime = new Date().getTime();

                        try
                        {
                            Map<String, List<com.project.blockchain.Transaction>> m = r.getLocalBlockchain().getPendingTransactions();

                            String eventId = "";
                            int size = 0;
                            for (String id : m.keySet()) {
                                int newsize = m.get(id).size();
                                if (newsize > size) {
                                    size = newsize;
                                    eventId = id;
                                }
                            }
                            if(size>1) {
                                Block b = r.getLocalBlockchain().getLastBlock();
                                Block newBlock = new Block();
                                newBlock.setEpoch(epoch.getCount());
                                newBlock.setTimestamp(new Date().getTime());
                                newBlock.setPreviousHash(b.getHash());

                                newBlock.setEventId(eventId);
                                newBlock.setRatingAgentId(r.getRatingAgentId());
                                newBlock.getTransactions().addAll(m.get(eventId));
                                newBlock.computeHash();
                                newBlock.setStage(Constants.PROPOSE);
                                boolean majorityVote = r.propose(newBlock);

                                if(majorityVote) {
                                    newBlock.setStage(Constants.PREPARE);
                                    r.send(newBlock.toJSON());
                                    System.out.println("proposed block in epoch " + b.getEpoch() );
                                }else{
                                    System.out.println("Not proposing block");
                                }
                            }
                            long endTime = new Date().getTime();
                            //if sometime is remaining from defined epoc time ,wait it out.
                            Thread.sleep(Constants.EPOCH_INTERVAL_MILLIS-(endTime-startTime));

                        }
                        catch ( Exception e )
                        {
                            System.out.println(" was interrupted." );
                            e.printStackTrace();
                        }
                        finally
                        {
                            leaderSelector.autoRequeue();
                        }
                    }
                });

        leaderSelector.start();

    }

    @PreDestroy
    public void close() throws Exception {
        epoch.close();
        leaderSelector.close();
        client.close();
        zConn.close();
    }

    public void delete(String node){
        try {
            zConn.delete(node,0);
            System.out.print("deleted " +node);
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int totalNodes(String path){
        try {
            List<String> p = ZKPaths.getSortedChildren(zConn, path);
            return p.size();
        } catch (Exception e) {
            return -1;
        }
    }


}
