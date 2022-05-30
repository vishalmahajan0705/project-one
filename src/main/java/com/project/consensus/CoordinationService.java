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
import java.util.concurrent.TimeUnit;

import static com.project.Constants.RATING_AGENT_DIR;
import static com.project.Constants.ZK_CONNECTION_STRING;


@Service
public class CoordinationService {

    public ZooKeeper zConn;
    public CuratorFramework client;
    SharedCount epoch;
    LeaderSelector leaderSelector;

    RetryPolicy retryPolicy = new RetryNTimes(3, 100);
    final int epocInterval = 10;

    @PostConstruct
    public void init()  throws Exception
    {
        zConn =  new ZooKeeper(ZK_CONNECTION_STRING, 3000, we -> {
            if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.println("Connected...");
            }
        });

        if(zConn.exists(RATING_AGENT_DIR, true)==null){
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

        epoch = new SharedCount(client, "/counter/epoch", 0);
        try {
            epoch.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


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

                            Thread.sleep(TimeUnit.SECONDS.toMillis(epocInterval));

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
