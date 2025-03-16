package ca.fuerth.ga4gh.schemaregistry.index;

import ca.fuerth.ga4gh.schemaregistry.crawler.IndexableSchema;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSource;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class LangChainIndexer {

    @Autowired
    EmbeddingModel embeddingModel;

    @Autowired
    EmbeddingStore<TextSegment> embeddingStore;

    public IndexingResult addToIndex(Stream<IndexableSchema> schemas) {
        TextDocumentParser textDocumentParser = new TextDocumentParser(StandardCharsets.UTF_8);
        List<Document> documents = schemas
                .map(indexableSchema -> DocumentLoader.load(new SchemaDocumentSource(indexableSchema), textDocumentParser))
                .toList();

        if (documents.isEmpty()) {
            // EmbeddingStoreIngestor throws an exception if you ask it to index nothing
            log.info("Found no documents in IndexableSchema stream. Skipping ingestion.");
        } else {
            EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build()
                    .ingest(documents);
        }

        return new IndexingResult(documents.size());
    }

    record SchemaDocumentSource(IndexableSchema schema) implements DocumentSource {

        @Override
        public InputStream inputStream() throws IOException {
            return new ByteArrayInputStream(schema.jsonSchema().getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Metadata metadata() {
            Metadata md = new Metadata();
            putIfNonNull(md, "registryBaseUri", schema.registryBaseUri().toString());
            putIfNonNull(md, "namespace.name", schema.namespace().namespaceName());
            putIfNonNull(md, "namespace.contactUrl", schema.namespace().contactUrl());
            putIfNonNull(md, "schema.name", schema.schemaInfo().schemaName());
            putIfNonNull(md, "schema.maturityLevel", schema.schemaInfo().maturityLevel());
            putIfNonNull(md, "schema.maintainers", schema.schemaInfo().maintainer().toString());
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
