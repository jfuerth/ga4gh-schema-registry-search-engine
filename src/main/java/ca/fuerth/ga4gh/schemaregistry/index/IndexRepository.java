package ca.fuerth.ga4gh.schemaregistry.index;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface IndexRepository {

    @SqlQuery("select * from index_info")
    @RegisterConstructorMapper(IndexInfo.class)
    IndexInfo getIndexInfo();

    @SqlUpdate("delete from index_info")
    void clearIndexInfo();

    @SqlUpdate("""
        insert into index_info (created_at, embedding_model_class, embedding_dimensions)
        values (:createdAt, :embeddingModelClass, :embeddingDimensions)
        """)
    void setIndexInfo(@BindMethods IndexInfo indexInfo);
}
