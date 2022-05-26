package com.project;

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
        String[] arg = args[0].split(",");
        nodeType = arg[0];
        nodeId = arg[1];


        SpringApplication app = new SpringApplication(Main.class);

        if(!nodeType.equals("R")) app.setLazyInitialization(true);

        app.run(args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            try {



                if(nodeType.equals("IS")) {
                   new InternalEventSource(nodeId).sendEvent();
                }else if(nodeType.equals("ES")) {
                    new MarketEventSource(nodeId);
                }else{
                    new RatingAgent();

                }

                System.out.println("Started:: "+nodeType+" " +nodeId);

            } catch(Exception e){
                e.printStackTrace();
            }
        };
    }
}
