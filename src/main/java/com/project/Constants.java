package com.project;

public class Constants {
    //multicast
    public static final String HOST = "224.0.0.0";
    public static final int PORT = 4321;

    //zookeeper
    public static final String ZK_CONNECTION_STRING = "127.0.0.1:2181";

    public static final String BLOCK_PREFIX = "0000";
    public static final String RATING_AGENT = "/rating-agents/";
    public static final String RATING_AGENT_DIR = "/rating-agents";

    public static final String INTERNAL_EVENT_SOURCE = "IS";
    public static final String EXTERNAL_EVENT_SOURCE = "ES";

    public static final String BLOCK_ATTRIBUTE = "stage";
    public static final String PROPOSE = "propose";
    public static final String VOTE = "vote";
    public static final String PREPARE = "prepare";

    public static final String BLOCKCHAIN_PATH_PREFIX = "src/main/resources/local/rating-agent-";
    public static final String BLOCKCHAIN_PATH_SUFFIX = "-blockchain.json";

}
