package com.project;

import com.project.participant.ByzantineRatingAgent;
import com.project.participant.InternalEventSource;
import com.project.participant.MarketEventSource;
import com.project.participant.RatingAgent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
//mvn spring-boot:run -Dspring-boot.run.arguments=R,2

@SpringBootApplication
public class Main {
    static String nodeType = null, nodeId =null;
    public static void main(String[] args) {
        parseArgs(args);

        SpringApplication app = new SpringApplication(Main.class);

        if(nodeType.equals("IS") || nodeType.equals("ES")) app.setLazyInitialization(true);

        if(nodeType.equals("R"))
            app.setAdditionalProfiles("Normal");

        if(nodeType.equals("BR"))
            app.setAdditionalProfiles("Byzantine");

        app.run(args);
    }



    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            try {

                switch (nodeType) {
                    case "IS":
                        new InternalEventSource(nodeId).sendEvent();
                        break;
                    case "ES":
                        new MarketEventSource(nodeId);
                        break;
                    case "R":
                        new RatingAgent();
                        break;
                    default:
                        new ByzantineRatingAgent();
                        break;
                }

                System.out.println("Started:: "+nodeType+" NodeID ::" +nodeId);

            } catch(Exception e){
                e.printStackTrace();
            }
        };
    }

    private static void parseArgs(String[] args) {
        if(args[0].contains(",")){
            String[] arg = args[0].split(",");
            System.out.println(arg.length);
            nodeType = arg[0];
            nodeId = arg[1];
        }else{
            nodeType = args[0];
            nodeId = "ZKAssigned";
        }
    }
}
