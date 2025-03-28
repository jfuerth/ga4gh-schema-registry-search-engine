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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class LangChainIndexer {

    @Autowired
    Jdbi jdbi;

    @Autowired
    EmbeddingModel embeddingModel;

    @Autowired
    EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    JsonSchemaSplitter jsonSchemaSplitter;

    public int deleteAllFromRegistry(URI registryUri) {
        return jdbi.withExtension(IndexRepository.class, repo -> repo.deleteAllFromRegistry(registryUri.toString()));
    }

    public List<FailableResult<String>> addToIndex(Stream<FailableResult<CrawledSchema>> schemas) {
        TextDocumentParser textDocumentParser = new TextDocumentParser(StandardCharsets.UTF_8);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(jsonSchemaSplitter)
                .build();

        return schemas
                .map(fSchema ->
                        fSchema.map(
                                schema -> "loading schema for " + schema.namespace() + "/" + schema.schemaInfo().schemaName(),
                                schema -> DocumentLoader.load(new SchemaDocumentSource(schema), textDocumentParser))
                )
                .map(fDocument ->
                        fDocument.map(
                                doc -> "ingesting document " + doc,
                                doc -> tryIngest(ingestor, doc)))
                .peek(failableResult -> {
                    if (failableResult.isFailed()) {
                        log.info("Failure during indexing: {}", failableResult.errorMessage(), failableResult.exception());
                    }
                })
                .toList();
    }

    private static String tryIngest(EmbeddingStoreIngestor ingestor, Document document) {
        ingestor.ingest(document);
        return "Ingested " + document.metadata().getString("namespace.name") + "/" + document.metadata().getString("schema.name");
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
            putIfNonNull(md, "schema.name", schema.schemaInfo().schemaName());
            putIfNonNull(md, "schema.maturityLevel", schema.schemaInfo().maturityLevel());
            putIfNonNull(md, "schema.maintainers", schema.schemaInfo().maintainer().toString());
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
