package com.teakter.video.config;

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

   /*
    * configuracion de cache con Caffeine para refrescar si una instancia de microservicio cambia de IP en consul o en otro descubridor de servicios
* es recomendable agregar esta dependencia y clase de configuracion en los microservicios que solicitan datos de otros microservicios actuando como clientes y ademas a el gateway que se comunica con todos
    * pero si se quiere agregar a todos los microservicios no hay problema y asi ya se tiene la dependencia caffeine y clase de configuracion por si en el futuro esos microservicios piden datos de otros, actuando como clientes
*/

@Bean
    public CacheManager cacheManager(){
    CaffeineCacheManager cacheManager=new CaffeineCacheManager("loadValancerCache");
    cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS) //tiempo de expiracion para refrescar instancias
            .maximumSize(500)); //limite maximo de elementos en cache
    return cacheManager;
}

}
