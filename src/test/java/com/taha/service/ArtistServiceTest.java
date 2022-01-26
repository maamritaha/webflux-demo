package com.taha.service;

import com.taha.bean.ArtistEntity;
import com.taha.dto.ArtistDto;
import com.taha.repository.ArtistRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

@ExtendWith(SpringExtension.class)
class ArtistServiceTest {

    @InjectMocks
    private ArtistService artistServiceMock;

    @Mock
    private ArtistRepository artistRepositoryMock;

    private final ArtistEntity artistEntity = ArtistEntity.builder().name("Artist").id(1L).build();
    private final ArtistEntity artistEntityDetached = ArtistEntity.builder().name("Artist").build();
    private final ArtistDto artistDto = ArtistDto.builder().name("Artist").id(1L).build();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void given() {
        BDDMockito.when(artistRepositoryMock.findAll())
                .thenReturn(Flux.just(artistEntity));

        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(artistEntity));

        BDDMockito.when(artistRepositoryMock.findByName(ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(artistEntity));

        BDDMockito.when(artistRepositoryMock.save(artistEntityDetached))
                .thenReturn(Mono.just(artistEntity));

        BDDMockito.when(artistRepositoryMock.deleteById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        BDDMockito.when(artistRepositoryMock.save(artistEntityDetached))
                .thenReturn(Mono.just(artistEntity));
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
    @DisplayName("getById returns a Mono with artist when it exists")
    void getById_ReturnMonoArtist_WhenSuccessful() {
        StepVerifier.create(artistServiceMock.getById(1L))
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("getByName returns a Mono with artist when it exists")
    void getByName_ReturnMonoArtist_WhenSuccessful() {
        StepVerifier.create(artistServiceMock.getByName("Artist"))
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("getAll returns a flux of artist")
    void getAllReturnFluxArtist_WhenSuccessful() {
        StepVerifier.create(artistServiceMock.getAll())
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("getById returns Mono error when anime does not exist")
    public void getById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(artistServiceMock.getById(1l))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("getByName returns Mono error when artist does not exist")
    public void getByName_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(artistRepositoryMock.findByName(ArgumentMatchers.anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(artistServiceMock.getByName("Artist"))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("creates an artist successfully")
    public void create_Artist_Successful() {
        ArtistDto artistDtoToBeSaved = ArtistDto.builder().name("Artist").build();

        StepVerifier.create(artistServiceMock.create(artistDtoToBeSaved))
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("updates an artist successfully")
    void update_Artist_Successful() {
        ArtistEntity artistEntityUpdated = ArtistEntity.builder().name("ArtistUpdated").id(1L).build();
        ArtistDto artistDtoUpdated = ArtistDto.builder().name("ArtistUpdated").id(1L).build();
        BDDMockito.when(artistRepositoryMock.save(artistEntity))
                .thenReturn(Mono.just(artistEntityUpdated));

        StepVerifier.create(artistServiceMock.update(artistDto))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("updates an artist return error when not exist")
    void update_Artist_ReturnMonoError() {
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(artistServiceMock.update(artistDto))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("deletes an artist successfully")
    void delete_Artist_Successful() {
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(artistEntity));
        BDDMockito.when(artistRepositoryMock.delete(artistEntity))
                .thenReturn(Mono.empty());
        StepVerifier.create(artistServiceMock.delete(artistDto.getId()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("deletes an artist return error when not exist")
    void delete_Artist_ReturnMonoError() {
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        BDDMockito.when(artistRepositoryMock.delete(artistEntity))
                .thenReturn(Mono.empty());
        StepVerifier.create(artistServiceMock.delete(artistDto.getId()))
                .expectError(ResponseStatusException.class)
                .verify();
    }

}