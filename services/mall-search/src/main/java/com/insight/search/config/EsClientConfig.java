package com.insight.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsClientConfig {

    @Bean
    public RestClient restClient(@Value("${elasticsearch.url}") String url) {
        return RestClient.builder(HttpHost.create(url)).build();
    }

    @Bean
    public ElasticsearchClient esClient(RestClient restClient) {
        // 客户端默认的 JacksonJsonpMapper 是"严格模式":_source 里出现 VO 没声明的字段
        // (比如 status、createdAt)会抛 UnrecognizedPropertyException。
        // 换成自己的 ObjectMapper 并关掉严格检查:ES 返回的字段多,VO 只挑自己要的。
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(mapper));
        return new ElasticsearchClient(transport);
    }
}
