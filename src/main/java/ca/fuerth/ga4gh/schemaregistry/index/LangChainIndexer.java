package ca.fuerth.ga4gh.schemaregistry.index;

import ca.fuerth.ga4gh.schemaregistry.crawler.CrawledSchema;
import ca.fuerth.ga4gh.schemaregistry.jsonschema.JsonSchemaSplitter;
import ca.fuerth.ga4gh.schemaregistry.shared.FailableResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSource;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class LangChainIndexer {

    private final Jdbi jdbi;
    private final TextDocumentParser textDocumentParser = new TextDocumentParser(StandardCharsets.UTF_8);
    private final EmbeddingStoreIngestor ingestor;

    @Autowired
    public LangChainIndexer(Jdbi jdbi, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, JsonSchemaSplitter jsonSchemaSplitter) {
        this.jdbi = jdbi;
        ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(jsonSchemaSplitter)
                .build();
    }

    public int deleteAllFromRegistry(URI registryUri) {
        return jdbi.withExtension(IndexRepository.class, repo -> repo.deleteAllFromRegistry(registryUri.toString()));
    }

    public List<FailableResult<String>> addToIndex(Stream<FailableResult<CrawledSchema>> schemas) {
        List<FailableResult<String>> indexingResults = schemas
                .map(this::parseSchemaToDocument)
                .map(this::ingestDocument)
                .peek(failableResult -> {
                    if (failableResult.isFailed()) {
                        log.info("Failure during indexing: {}", failableResult.errorMessage(), failableResult.exception());
                    }
                })
                .toList();
        if (hasAnySuccess(indexingResults)) {
            log.info("Indexing complete. Refreshing statistics...");
            jdbi.useExtension(IndexRepository.class, IndexRepository::refreshStatistics);
        }
        return indexingResults;
    }

    private boolean hasAnySuccess(Collection<? extends FailableResult<?>> failableResults) {
        return failableResults.stream().anyMatch(FailableResult::isSuccess);
    }

    @NotNull
    private FailableResult<Document> parseSchemaToDocument(FailableResult<CrawledSchema> fSchema) {
        return fSchema.map(
                failedSchema -> "loading schema for " + failedSchema.namespace() + "/" + failedSchema.schemaRecord().schemaName(),
                schema -> DocumentLoader.load(new SchemaDocumentSource(schema), textDocumentParser));
    }

    @NotNull
    private FailableResult<String> ingestDocument(FailableResult<Document> fDocument) {
        return fDocument.map(
                failedDoc -> "ingesting document " + failedDoc,
                doc -> {
                    ingestor.ingest(doc);
                    return "Ingested " + doc.metadata().getString("namespace.name") + "/" + doc.metadata().getString("schema.name");
                });
    }

    record SchemaDocumentSource(CrawledSchema schema) implements DocumentSource {

        @Override
        public InputStream inputStream() throws IOException {
            return new ByteArrayInputStream(schema.jsonSchema().getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Metadata metadata() {
            Metadata md = new Metadata();
            putIfNonNull(md, "registry.uri", schema.registryBaseUri().toString());
            putIfNonNull(md, "namespace.name", schema.namespace().namespaceName());
            putIfNonNull(md, "namespace.contactUrl", schema.namespace().contactUrl());
            putIfNonNull(md, "schema.name", schema.schemaRecord().schemaName());
            putIfNonNull(md, "schema.maturityLevel", schema.schemaRecord().maturityLevel());
            putIfNonNull(md, "schema.maintainers", schema.schemaRecord().maintainer().toString());
            putIfNonNull(md, "schema.version.uri", schema.schemaVersionUri().toString());
            // TODO add all SchemaVersion properties here once we have them
            return md;
        }
    }

    private static void putIfNonNull(Metadata md, String key, String value) {
        if (value != null) {
            md.put(key, value);
        }
    }
}
