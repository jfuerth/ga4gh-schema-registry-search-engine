package ca.fuerth.ga4gh.schemaregistry.index;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface IndexRepository {

    @SqlQuery("select * from index_storage_settings")
    @RegisterConstructorMapper(IndexStorageSettings.class)
    Optional<IndexStorageSettings> getIndexStorageSettings();

    @SqlUpdate("delete from index_storage_settings")
    void clearIndexInfo();

    @SqlUpdate("""
        insert into index_storage_settings (created_at, embedding_model_class, embedding_dimensions)
        values (:createdAt, :embeddingModelClass, :embeddingDimensions)
        """)
    void setIndexInfo(@BindMethods IndexStorageSettings indexStorageSettings);

    @SqlUpdate("""
            DELETE FROM index_storage WHERE metadata->>'registry.uri' = :registryBaseUri
            """)
    int deleteAllFromRegistry(@Bind String registryBaseUri);
}
