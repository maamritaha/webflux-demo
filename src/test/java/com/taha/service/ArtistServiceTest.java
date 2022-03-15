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
    private ArtistService artistService;

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
                .thenReturn(Flux.just(artistEntity));
        BDDMockito.when(artistRepositoryMock.save(artistEntityDetached))
                .thenReturn(Mono.just(artistEntity));
        BDDMockito.when(artistRepositoryMock.deleteById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        BDDMockito.when(artistRepositoryMock.save(artistEntityDetached))
                .thenReturn(Mono.just(artistEntity));
    }

    @Test
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
    void getById_ReturnMonoArtist_WhenSuccessful() {
        //when
        StepVerifier.create(artistService.getById(1L))
                //then
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    void getByName_ReturnMonoArtist_WhenSuccessful() {
        //when
        StepVerifier.create(artistService.getByName("Artist"))
                //then
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    void getAllReturnFluxArtist_WhenSuccessful() {
        //when
        StepVerifier.create(artistService.getAll())
                //then
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    public void getById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        //given
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        //when
        StepVerifier.create(artistService.getById(1l))
                //then
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void create_Artist_Successful() {
        //given
        ArtistDto artistDtoToBeSaved = ArtistDto.builder().name("Artist").build();
        //when
        StepVerifier.create(artistService.create(artistDtoToBeSaved))
                //then
                .expectSubscription()
                .expectNext(artistDto)
                .verifyComplete();
    }

    @Test
    void update_Artist_Successful() {
        //given
        ArtistEntity artistEntityUpdated = ArtistEntity.builder().name("ArtistUpdated").id(1L).build();
        BDDMockito.when(artistRepositoryMock.save(artistEntity))
                .thenReturn(Mono.just(artistEntityUpdated));
        //when
        StepVerifier.create(artistService.update(artistDto))
                //then
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void update_Artist_ReturnMonoError() {
        //given
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        //when
        StepVerifier.create(artistService.update(artistDto))
                //then
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void delete_Artist_Successful() {
        //given
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(artistEntity));
        BDDMockito.when(artistRepositoryMock.delete(artistEntity))
                .thenReturn(Mono.empty());
        //when
        StepVerifier.create(artistService.delete(artistDto.getId()))
                //then
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void delete_Artist_ReturnMonoError() {
        //given
        BDDMockito.when(artistRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        //when
        StepVerifier.create(artistService.delete(artistDto.getId()))
                //then
                .expectError(ResponseStatusException.class)
                .verify();
    }
}