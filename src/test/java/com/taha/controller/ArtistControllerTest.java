package com.taha.controller;

import com.taha.bean.ArtistEntity;
import com.taha.dto.ArtistDto;
import com.taha.exception.ArtistExceptions;
import com.taha.service.ArtistService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ArtistControllerTest {

    @InjectMocks
    ArtistController artistControllerMock;

    @Mock
    ArtistService artistServiceMock;

    private final ArtistDto artistDto = ArtistDto.builder().name("Artist").id(1L).build();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void given() {
        BDDMockito.when(artistServiceMock.getAll())
                .thenReturn(Flux.just(artistDto));

        BDDMockito.when(artistServiceMock.getById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(artistDto));

        BDDMockito.when(artistServiceMock.getByName(ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(artistDto));

        BDDMockito.when(artistServiceMock.create(artistDto))
                .thenReturn(Mono.just(artistDto));

        BDDMockito.when(artistServiceMock.delete(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        BDDMockito.when(artistServiceMock.update(artistDto))
                .thenReturn(Mono.just(artistDto));
    }

    @Test
    @DisplayName("check if blockHound works")
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("getAll returns a flux of artist")
    void getAllReturnFluxArtist_WhenSuccessful() {
        StepVerifier.create(artistControllerMock.getAll())
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("getById returns a Mono with artist when it exists")
    void getById_ReturnMonoArtist_WhenSuccessful() {
        StepVerifier.create(artistControllerMock.getById(1L))
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("getById returns Mono error when anime does not exist")
    public void getById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(artistServiceMock.getById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found")));

        StepVerifier.create(artistControllerMock.getById(1l))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("getByName returns a Mono with artist when it exists")
    void getByName_ReturnMonoArtist_WhenSuccessful() {
        StepVerifier.create(artistControllerMock.getByName("Artist"))
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("getByName returns Mono error when anime does not exist")
    public void getByName_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(artistServiceMock.getByName(ArgumentMatchers.anyString()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found")));

        StepVerifier.create(artistControllerMock.getByName("Artist"))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("creates an artist successfully")
    public void create_Artist_Successful() {
        StepVerifier.create(artistControllerMock.create(Mono.just(artistDto)))
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("updates an artist successfully")
    void update_Artist_Successful() {
        StepVerifier.create(artistControllerMock.update(Mono.just(artistDto)))
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("updates an artist return error when not exist")
    void update_Artist_ReturnMonoError() {
        BDDMockito.when(artistServiceMock.update(ArgumentMatchers.any()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found")));

        StepVerifier.create(artistControllerMock.update(Mono.just(artistDto)))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("deletes an artist successfully")
    void delete_Artist_Successful() {
        StepVerifier.create(artistControllerMock.delete(1L))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("deletes an artist return error when not exist")
    void delete_Artist_ReturnMonoError() {
        BDDMockito.when(artistServiceMock.delete(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found")));

        StepVerifier.create(artistControllerMock.delete(1L))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }
}