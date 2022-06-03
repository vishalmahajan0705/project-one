# Permissioned Blockchain for establishing Trusted Ratings

## (1) Clone the repository
git clone https://github.com/vishalmahajan0705/project-one.git

## (2) Build
cd project-one
mvn clean install

## (3) Start Zookeeper
Download Zookeeper 3.8.0 <br>
navigate to bin directory of zookeeper <br>
run  **zkServer** <br>
This will start zookeeper at 127.0.0.1:2181

## (4) Start Internal Event Source(s)
cd project-one <br>
mvn spring-boot:run -Dspring-boot.run.arguments=IS,2 

## (5) Start External  Event Source(s)
mvn spring-boot:run -Dspring-boot.run.arguments=ES,2 


## (6) Start Rating Agent 1

mvn spring-boot:run -Dspring-boot.run.arguments=R

## (7) Start Rating Agent 2
mvn spring-boot:run -Dspring-boot.run.arguments=R


## (8) Start Rating Agent 3
mvn spring-boot:run -Dspring-boot.run.arguments=R <br>

Note:More rating agents can be added if needed. <br>

Any of the rating agents will be elected leader and remain leader for ecoch interval, at the end of the epoc another rating agent would be made leader
messages on the prompt will indicate the rating agent that is currently the leader.

## (9) Start Byzantine Rating Agent 
mvn spring-boot:run -Dspring-boot.run.arguments=**BR** <br>


## (10) Send Events
Goto Internal Event Source window (started in Step 4) and type event GOOD  <br>
Type more events as needed - followiong values can be used GOOD  BAD AVERAGE <br>
_View logs showing up on leader and follower nodes._ <br>

Approved blocks will be committed by each rating agent to their local blockchains 
which can be  seen under /resources/local/rating-agent-**{ratingAgentID}**-blockchain.json







