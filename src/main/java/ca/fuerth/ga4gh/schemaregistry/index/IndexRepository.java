package ca.fuerth.ga4gh.schemaregistry.index;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface IndexRepository {

    @SqlQuery("SELECT * FROM index_storage_settings")
    @RegisterConstructorMapper(IndexStorageSettings.class)
    Optional<IndexStorageSettings> getIndexStorageSettings();

    @SqlUpdate("DELETE FROM index_storage_settings")
    void clearIndexInfo();

    @SqlUpdate("""
        INSERT INTO index_storage_settings (created_at, embedding_model_class, embedding_dimensions)
        VALUES (:createdAt, :embeddingModelClass, :embeddingDimensions)
        """)
    void setIndexInfo(@BindMethods IndexStorageSettings indexStorageSettings);

    @SqlUpdate("""
            DELETE FROM index_storage WHERE metadata->>'registry.uri' = :registryBaseUri
            """)
    int deleteAllFromRegistry(@Bind String registryBaseUri);

    @SqlQuery("""
            SELECT
                count(distinct metadata ->> 'registry.uri') AS registry_count,
                count(distinct metadata ->> 'schema.name') AS schema_count,
                count(*) AS schema_fragment_count
            FROM index_storage
            """)
    @RegisterConstructorMapper(IndexStatistics.class)
    IndexStatistics getStatistics();
}
