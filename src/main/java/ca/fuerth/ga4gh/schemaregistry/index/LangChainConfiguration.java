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

import javax.sql.DataSource;
import java.time.Instant;

@Slf4j
@Configuration
public class LangChainConfiguration {

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
    EmbeddingStore<TextSegment> pgvectorEmbeddingStore(
            Jdbi jdbi,
            EmbeddingModel embeddingModel,
            DataSource dataSource,
            @Value("${langchain.embedding-store.pgvector.useIndex}") boolean useIndex,
            @Value("${langchain.embedding-store.pgvector.indexListSize}") int indexListSize,
            @Value("${langchain.embedding-store.pgvector.dropTableOnStartup}") boolean dropTableOnStartup
            ) {
        IndexInfo oldIndexInfo = jdbi.withExtension(IndexRepository.class, repo -> repo.getIndexInfo());
        IndexInfo newIndexInfo = new IndexInfo(Instant.now(), embeddingModel.getClass().getName(), embeddingModel.dimension());
        if (!dropTableOnStartup && oldIndexInfo != null) {
            log.info("Verifying that the existing index matches the current configuration...");
            if (!oldIndexInfo.isCompatibleWith(newIndexInfo)) {
                throw new IndexException(
                        "Existing index storage is not compatible with current configuration. Existing: %s; Current: %s"
                                .formatted(oldIndexInfo, newIndexInfo));
            }
        } else {
            log.info("No previous index info found");
        }
        PgVectorEmbeddingStore embeddingStore = PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("schema_vectors")
                .dimension(embeddingModel.dimension())
                .useIndex(useIndex)
                .indexListSize(indexListSize)
                .createTable(true)
                .dropTableFirst(dropTableOnStartup)
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
