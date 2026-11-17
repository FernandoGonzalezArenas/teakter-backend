package com.teakter.admin.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching //activa el soporte general de cache en spring boot
public class CacheConfig {

    //configuracion de cache con Caffeine para refrescar si una instancia de microservicio cambia de IP en consul o en otro descubridor de servicios

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager cacheManager=new CaffeineCacheManager("loadValancerCache");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS) //tiempo de expiracion para refrescar instancias
                .maximumSize(500)); //limite maximo de elementos en cache
        return cacheManager;
    }

}
