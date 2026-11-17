package com.teakter.subscription.repository;

import com.teakter.subscription.entity.SubscriptionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findBySubscriberId_ReturnsList(){
        //guardar datos reales en la base en memoria H2
        repository.save(SubscriptionEntity.builder()
                .subscriberId("fshti8ehtjdkg")
                .channelId("fkjsjt98e5ujlkmhglirt")
                .build());
        repository.save(SubscriptionEntity.builder()
                .subscriberId("fshti8ehtjdkg")
                .channelId("gdfjjg9845ulj")
                .build());

        List<String> ids=repository.findBySubscriberId("fshti8ehtjdkg");

        //validaciones
        assertEquals(2, ids.size());
        assertTrue(ids.contains("fkjsjt98e5ujlkmhglirt"));
    }

@Test
    @DisplayName("debe eliminar las subscripciones de el usuario baneado")
    void shouldDeleteSubscriptions(){
        SubscriptionEntity subscription=repository.save(SubscriptionEntity.builder()
                .subscriberId("gdfjjg9845ulj").channelId("jjfo8tlknrgkhfhk").build());
        Long id=subscription.getId();

        entityManager.flush();

        repository.deleteSubscriptionsByChannelId("gdfjjg9845ulj");

entityManager.clear();
        Optional<SubscriptionEntity> deleted=repository.findById(id);

        assertTrue(deleted.isEmpty());
}

@Test
    @DisplayName("debe eliminar los subscriptores de un canal baneado")
    void shouldDeleteSubscribers(){
        SubscriptionEntity subscription=repository.save(SubscriptionEntity.builder()
                .subscriberId("gdfjjg9845ulj").channelId("bfgihjlikhmflkhjfk").build());
        Long id=subscription.getId();

        entityManager.flush();

        repository.deleteSubscriptorsOfChannelId("bfgihjlikhmflkhjfk");

entityManager.clear();
        Optional<SubscriptionEntity> deleted=repository.findById(id);

        assertTrue(deleted.isEmpty());
}

}
