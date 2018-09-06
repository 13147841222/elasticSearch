package com.elasticsearch.demo.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author zhumingli
 * @create 2018-09-06 上午10:41
 * @desc ES配置
 **/
@Configuration
public class ElasticsearchConfig {

    @Bean
    public TransportClient transportClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name","zhuml")
                //增加嗅探机制，找到ES集群
                .put("client.transport.sniff", true)
                //增加线程池个数，暂时设为5
                .put("thread_pool.search.size", Integer.parseInt("5"))
                .build();

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"),9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"),8200))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"),8000));

        return client;
    }

}
