
package com.hotel.chatbox.config;

import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // Still needed for @Configuration

/**
 * Spring configuration class for defining and customizing Spring AI components.
 * This class now primarily serves to hold configuration properties if needed elsewhere,
 * and relies on Spring AI's auto-configuration for the EmbeddingModel.
 */
@Configuration
public class ChatConfig {

    // Inject the embedding dimensions from application.properties.
    // This value is crucial for the PostgreSQL vector store and the OpenAI embedding model.
    @Value("${spring.ai.vectorstore.pgvector.dimensions}")
    private Integer embeddingDimensions;

    /**
     * Defines the OpenAiEmbeddingOptions bean.
     * By explicitly creating this bean, we ensure that the embedding dimensions
     * are correctly passed to the OpenAI embedding model, overriding any
     * potential issues with auto-configuration not picking up this specific option.
     *
     * @return A configured OpenAiEmbeddingOptions instance with the specified dimensions.
     */
    @Bean
    public OpenAiEmbeddingOptions openAiEmbeddingOptions() {
        // Explicitly build the OpenAiEmbeddingOptions with the desired dimensions.
        // The model itself (text-embedding-3-small) will still be primarily
        // configured via 'spring.ai.openai.embedding.options.model' in application.properties
        // and picked up by Spring AI's auto-configured OpenAiEmbeddingModel.
        return OpenAiEmbeddingOptions.builder()
                .dimensions(embeddingDimensions) // Set the dimensions for the embedding model
                .build();
    }
}

