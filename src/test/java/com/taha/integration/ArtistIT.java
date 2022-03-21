package com.taha.integration;

import com.taha.dto.ArtistDto;
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

import java.util.Collections;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT36S")
public class ArtistIT {

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
    public void createArtist_Successfully() {
        this.client
                .post()
                .uri("/artist")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(ArtistDto.builder().name("Artist1").build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .location("/artist/1");
    }

    @Order(3)
    @Test
    public void updateArtist_Successfully() {
        this.client
                .put()
                .uri("/artist")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(ArtistDto.builder().id(1L).name("Artist2").build()))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Order(4)
    @Test
    public void updateArtist_NotFound() {
        this.client
                .put()
                .uri("/artist")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(ArtistDto.builder().id(2L).name("Artist2").build()))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Order(5)
    @Test
    public void getById_Successfully() {
        this.client
                .get()
                .uri("/artist/1")
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

    @Order(6)
    @Test
    public void getById_NotFound() {
        this.client
                .get()
                .uri("/artist/2")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Order(7)
    @Test
    public void getByName_Successfully() {
        this.client
                .get()
                .uri("/artist/name/Artist2")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(ArtistDto.class)
                .returnResult()
                .getResponseBody()
                .get(0)
                .getId()
                .equals(1L);
    }

    @Order(8)
    @Test
    public void getAll_Successfully() {
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

    @Order(9)
    @Test
    public void deleteArtist_Successfully() {
        this.client
                .delete()
                .uri("/artist/1")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Order(10)
    @Test
    public void deleteArtist_NotFound() {
        this.client
                .delete()
                .uri("/artist/1")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

}
