package com.ollamaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * Created by: Sharan MH
 * on: 11/08/25
 */

@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * for testing
     */
    @Bean
    public CommandLineRunner test(
            OllamaService.StreamingService.MemoryDependentService memoryDependentService,
            OllamaService.StreamingService.WithOutTools withOutTools,

            OllamaService.NonStreamingService.MemoryDependentService memoryDependentServiceNonStream,
            OllamaService.NonStreamingService.WithOutTools withOutToolsNonStream
    ) {

        return args -> {
            //streaming
            memoryDependentService.question("some random id","Hi tell me about your self, and the tools you have", Map.of("Context Name", "Context value that is needed"))
                    .doOnNext(v-> System.out.println("Response: "+v))
                    .doFinally(System.out::println)
                    .subscribe();
            withOutTools.question("Hi tell me about your self, what tools you have")
                    .doOnNext(v-> System.out.println("Response: "+v))
                    .doFinally(System.out::println)
                    .subscribe();//no tools added here


            //non streaming
            System.out.println(
                    memoryDependentServiceNonStream.question("some random id","Hi tell me about your self, and the tools you have", Map.of("Context Name", "Context value that is needed"))
            );

            System.out.println(
                    withOutToolsNonStream.question("Hi tell me about your self, what tools you have")
            );
        };
    }

}