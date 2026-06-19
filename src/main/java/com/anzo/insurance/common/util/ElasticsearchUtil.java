package com.anzo.insurance.common.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.ObjectBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Elasticsearch 通用工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchUtil {

    private final ElasticsearchClient elasticsearchClient;

    public boolean indexExists(String indexName) {
        try {
            BooleanResponse response = elasticsearchClient.indices().exists(e -> e.index(indexName));
            return response.value();
        } catch (Exception e) {
            throw wrapException("检查索引是否存在失败: " + indexName, e);
        }
    }

    public boolean createIndex(String indexName) {
        try {
            if (indexExists(indexName)) {
                return true;
            }
            CreateIndexResponse response = elasticsearchClient.indices().create(c -> c.index(indexName));
            return Boolean.TRUE.equals(response.acknowledged());
        } catch (Exception e) {
            throw wrapException("创建索引失败: " + indexName, e);
        }
    }

    public boolean createIndex(String indexName,
                               Function<CreateIndexRequest.Builder, ObjectBuilder<CreateIndexRequest>> customizer) {
        try {
            if (indexExists(indexName)) {
                return true;
            }
            CreateIndexResponse response = elasticsearchClient.indices().create(customizer);
            return Boolean.TRUE.equals(response.acknowledged());
        } catch (Exception e) {
            throw wrapException("自定义创建索引失败: " + indexName, e);
        }
    }

    public boolean deleteIndex(String indexName) {
        try {
            if (!indexExists(indexName)) {
                return true;
            }
            DeleteIndexResponse response = elasticsearchClient.indices().delete(d -> d.index(indexName));
            return Boolean.TRUE.equals(response.acknowledged());
        } catch (Exception e) {
            throw wrapException("删除索引失败: " + indexName, e);
        }
    }

    public <T> boolean save(String indexName, Long id, T document) {
        try {
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index(indexName)
                    .id(String.valueOf(id))
                    .document(document)
            );
            return response.result() != null;
        } catch (Exception e) {
            throw wrapException("写入文档失败: " + indexName + "/" + id, e);
        }
    }

    public <T> boolean save(String indexName, T document) {
        try {
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index(indexName)
                    .document(document)
            );
            return response.result() != null;
        } catch (Exception e) {
            throw wrapException("写入文档失败: " + indexName, e);
        }
    }

    public <T> T getById(String indexName, Long id, Class<T> clazz) {
        try {
            GetResponse<T> response = elasticsearchClient.get(g -> g
                    .index(indexName)
                    .id(String.valueOf(id)), clazz);
            return response.found() ? response.source() : null;
        } catch (Exception e) {
            throw wrapException("查询文档失败: " + indexName + "/" + id, e);
        }
    }

    public boolean deleteById(String indexName, Long id) {
        try {
            DeleteResponse response = elasticsearchClient.delete(d -> d
                    .index(indexName)
                    .id(String.valueOf(id))
            );
            return response.result() != null;
        } catch (Exception e) {
            throw wrapException("删除文档失败: " + indexName + "/" + id, e);
        }
    }

    public <T> boolean update(String indexName, Long id, T document, Class<T> clazz) {
        try {
            UpdateResponse<T> response = elasticsearchClient.update(u -> u
                    .index(indexName)
                    .id(String.valueOf(id))
                    .doc(document), clazz);
            return response.result() != null;
        } catch (Exception e) {
            throw wrapException("更新文档失败: " + indexName + "/" + id, e);
        }
    }

    public <T> boolean bulkIndex(String indexName, Map<String, T> documents) {
        if (documents == null || documents.isEmpty()) {
            return true;
        }

        try {
            BulkRequest.Builder builder = new BulkRequest.Builder();
            documents.forEach((id, document) -> builder.operations(op -> op.index(idx -> idx
                    .index(indexName)
                    .id(id)
                    .document(document)
            )));

            BulkResponse response = elasticsearchClient.bulk(builder.build());
            return !response.errors();
        } catch (Exception e) {
            throw wrapException("批量写入文档失败: " + indexName, e);
        }
    }

    public <T> List<T> search(String indexName, Query query, Class<T> clazz) {
        try {
            SearchResponse<T> response = elasticsearchClient.search(s -> {
                s.index(indexName);
                if (query != null) {
                    s.query(query);
                }
                return s;
            }, clazz);
            return extractHits(response);
        } catch (Exception e) {
            throw wrapException("搜索文档失败: " + indexName, e);
        }
    }

    public <T> List<T> search(Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> customizer,
                              Class<T> clazz) {
        try {
            SearchResponse<T> response = elasticsearchClient.search(customizer, clazz);
            return extractHits(response);
        } catch (Exception e) {
            throw wrapException("自定义搜索文档失败", e);
        }
    }

    public long count(String indexName, Query query) {
        try {
            CountResponse response = elasticsearchClient.count(c -> {
                c.index(indexName);
                if (query != null) {
                    c.query(query);
                }
                return c;
            });
            return response.count();
        } catch (Exception e) {
            throw wrapException("统计文档失败: " + indexName, e);
        }
    }

    private <T> List<T> extractHits(SearchResponse<T> response) {
        if (response == null || response.hits() == null || response.hits().hits() == null) {
            return Collections.emptyList();
        }
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    private IllegalStateException wrapException(String message, Exception e) {
        log.error(message, e);
        return new IllegalStateException(message, e);
    }
}
