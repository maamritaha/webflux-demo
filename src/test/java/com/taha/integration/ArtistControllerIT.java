package com.taha.integration;

import com.taha.dto.ArtistDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ArtistControllerIT {

    @Autowired
    WebTestClient client;

    private ArtistDto artistDto = ArtistDto.builder().id(1L).name("Artist1").build();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install(builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID"));
    }

    @Order(1)
    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0); //NOSONAR
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Order(2)
    @Test
    @DisplayName("creates an artist successfully")
    public void create_artist() {

        this.client
                .post()
                .uri("/artist")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ArtistDto.builder().name("Artist1").build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ArtistDto.class)
                .returnResult()
                .getResponseBody()
                .getName()
                .equals(artistDto.getName())
                ;
    }

    @Order(3)
    @Test
    @DisplayName("update an artist successfully")
    public void update_artist() {

        this.client
                .put()
                .uri("/artist")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ArtistDto.builder().id(1L).name("Artist2").build()))
                .exchange()
                .expectStatus()
                .isNoContent();
        ;
    }

    @Order(4)
    @Test
    @DisplayName("get all artists")
    public void getAll() {
        this.client
                .get()
                .uri("/artist")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(ArtistDto.class)
                .returnResult()
                .getResponseBody()
                .get(0)
                .getName()
                .equals("Artist2");
    }



}
