package ca.fuerth.ga4gh.schemaregistry.index;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Configuration
public class LangChainConfiguration {

    enum IndexTableDropMode {
        ALWAYS,
        IF_EMBEDDING_MODEL_CHANGED,
        NEVER
    }

    @Bean
    EmbeddingModel embeddingModel() {
        return new BgeSmallEnV15EmbeddingModel();
    }

    @Bean
    @ConditionalOnProperty("langchain.embedding-store.in-memory.enabled")
    EmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @ConditionalOnProperty("langchain.embedding-store.pgvector.enabled")
    @DependsOn("liquibase")
    EmbeddingStore<TextSegment> pgvectorEmbeddingStore(
            Jdbi jdbi,
            DataSource dataSource,
            EmbeddingModel embeddingModel,
            @Value("${langchain.embedding-store.pgvector.useIndex}") boolean useIndex,
            @Value("${langchain.embedding-store.pgvector.indexListSize}") int indexListSize,
            @Value("${langchain.embedding-store.pgvector.dropTableOnStartup}") IndexTableDropMode dropTableOnStartup
            ) {
        IndexStorageSettings newSettings = new IndexStorageSettings(Instant.now(), embeddingModel.getClass().getName(), embeddingModel.dimension());
        Optional<IndexStorageSettings> oldSettings = jdbi.withExtension(IndexRepository.class, IndexRepository::getIndexStorageSettings);
        boolean oldSettingsAreCompatible = oldSettings
                .map(old -> old.isCompatibleWith(newSettings))
                .orElse(true);

        boolean shouldDropIndex = switch (dropTableOnStartup) {
            case ALWAYS -> true;
            case IF_EMBEDDING_MODEL_CHANGED -> !oldSettingsAreCompatible;
            case NEVER -> false;
        };

        if (!shouldDropIndex && !oldSettingsAreCompatible) {
            throw new IndexException("""
                    Existing index storage is not compatible with current configuration, and configuration says not to drop the index table on startup.
                    Existing: %s; Current: %s.
                    Consider changing Spring property langchain.embedding-store.pgvector.dropTableOnStartup to IF_EMBEDDING_MODEL_CHANGED.
                    """.formatted(oldSettings, newSettings));
        }

        if (shouldDropIndex) {
            log.info("Recreating index_storage table (will wipe the existing index)...");
        }

        PgVectorEmbeddingStore embeddingStore = PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("index_storage")
                .dimension(embeddingModel.dimension())
                .useIndex(useIndex)
                .indexListSize(indexListSize)
                .createTable(true)
                .dropTableFirst(shouldDropIndex)
                .build();

        if (oldSettings.isEmpty() || !oldSettingsAreCompatible) {
            jdbi.useExtension(IndexRepository.class, repo -> {
                repo.clearIndexInfo();
                repo.setIndexInfo(newSettings);
            });
        }

        return embeddingStore;
    }
}
