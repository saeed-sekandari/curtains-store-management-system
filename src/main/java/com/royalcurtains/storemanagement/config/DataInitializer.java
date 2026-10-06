package com.royalcurtains.storemanagement.config;

import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner addDefaultStores(StoreRepository storeRepository) {
        return args -> {

            // Add the stores only when the database does not contain any stores yet.
            if (storeRepository.count() == 0) {
                storeRepository.save(
                        new Store("Royal Curtains Store", "royal")
                );

                storeRepository.save(
                        new Store("Kabul Dubai Curtains Store", "kabul")
                );
            }
        };
    }
}