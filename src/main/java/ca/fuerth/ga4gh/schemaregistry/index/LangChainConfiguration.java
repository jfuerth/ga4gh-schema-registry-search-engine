package ca.fuerth.ga4gh.schemaregistry.index;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;

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
        IndexInfo oldIndexInfo = jdbi.withExtension(IndexRepository.class, repo -> repo.getIndexInfo());
        IndexInfo newIndexInfo = new IndexInfo(Instant.now(), embeddingModel.getClass().getName(), embeddingModel.dimension());
        boolean shouldDropIndex = switch (dropTableOnStartup) {
            case ALWAYS -> true;
            case IF_EMBEDDING_MODEL_CHANGED -> !oldIndexInfo.isCompatibleWith(newIndexInfo);
            case NEVER -> false;
        };

        if (!shouldDropIndex && oldIndexInfo != null) {
            if (!oldIndexInfo.isCompatibleWith(newIndexInfo)) {
                throw new IndexException("""
                        Existing index storage is not compatible with current configuration, and configuration says not to drop the index table on startup.
                        Existing: %s; Current: %s.
                        Consider changing Spring property langchain.embedding-store.pgvector.dropTableOnStartup to IF_EMBEDDING_MODEL_CHANGED.
                        """.formatted(oldIndexInfo, newIndexInfo));
            }
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

        if (oldIndexInfo == null || !newIndexInfo.isCompatibleWith(oldIndexInfo)) {
            jdbi.useExtension(IndexRepository.class, repo -> {
                repo.clearIndexInfo();
                repo.setIndexInfo(newIndexInfo);
            });
        }

        return embeddingStore;
    }
}
