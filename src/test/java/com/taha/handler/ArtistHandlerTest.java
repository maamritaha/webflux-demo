package com.taha.handler;

import com.taha.dto.ArtistDto;
import com.taha.exception.ArtistExceptions;
import com.taha.service.ArtistService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
class ArtistHandlerTest {

    @InjectMocks
    private ArtistHandler artistHandler;

    @Mock
    private ArtistService artistServiceMock;
    
    private final Map<String, String> idPathVariable = Collections.singletonMap("id", "1");
    private final Map<String, String> nonExistentIdPathVariable = Collections.singletonMap("id", "2");
    private final Map<String, String> namePathVariable = Collections.singletonMap("name", "Artist");
    private final ArtistDto artistDto = ArtistDto.builder().id(1L).name("Artist").build();
    private final ArtistDto artistDto1 = ArtistDto.builder().id(2L).name("Artist1").build();
    private final ArtistDto nonExistentArtistDtoForUpdate = ArtistDto.builder().id(3L).name("Artist").build();
    private final ArtistDto artistDtoDetached = ArtistDto.builder().name("Artist").build();
    private final String artistDtoDetachedJson = "{\"name\":\"Artist\"}";
    private final String nonExistentArtistJsonForUpdate = "{\"id\":3,\"name\":\"Artist\"}";
    private final String artistDtoJson = "{\"id\":1,\"name\":\"Artist\"}";
    private final String artistDtoJsonList = "data:{\"id\":1,\"name\":\"Artist\"}\n\n";
    private final String artistDtoJsonList2 = "data:{\"id\":1,\"name\":\"Artist\"}\n\ndata:{\"id\":2,\"name\":\"Artist1\"}\n\n";
    private final String notFoundError = "404 NOT_FOUND \"Artist not found\"";

    private ServerResponse.Context context;

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install(builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID"));
    }

    @BeforeEach
    public void createContext() {
        HandlerStrategies strategies = HandlerStrategies.withDefaults();
        context = new ServerResponse.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return strategies.messageWriters();
            }

            @Override
            public List<ViewResolver> viewResolvers() {
                return strategies.viewResolvers();
            }
        };
    }

    @BeforeEach
    public void given() {
        BDDMockito.when(artistServiceMock.getAll())
                .thenReturn(Flux.fromIterable(List.of(artistDto, artistDto1)));
        BDDMockito.when(artistServiceMock.getById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(artistDto));
        BDDMockito.when(artistServiceMock.getById(2L))
                .thenReturn(ArtistExceptions.artistNotFoundException(2L));
        BDDMockito.when(artistServiceMock.getByName(ArgumentMatchers.anyString()))
                .thenReturn(Flux.just(artistDto));
        BDDMockito.when(artistServiceMock.create(artistDtoDetached))
                .thenReturn(Mono.just(artistDto));
        BDDMockito.when(artistServiceMock.delete(1L))
                .thenReturn(Mono.empty());
        BDDMockito.when(artistServiceMock.delete(2L))
                .thenReturn(ArtistExceptions.artistNotFoundException(2L));
        BDDMockito.when(artistServiceMock.update(artistDto))
                .thenReturn(Mono.empty());
        BDDMockito.when(artistServiceMock.update(nonExistentArtistDtoForUpdate))
                .thenReturn(ArtistExceptions.artistNotFoundException(3L));
    }

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

    @Test
    void getById_Successful() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(""));
        exchange.getAttributes().put(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE, idPathVariable);
        MockServerHttpResponse mockResponse = exchange.getResponse();
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.getById(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.OK));
            assertThat(response instanceof EntityResponse, is(true));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectComplete().verify();
        assertThat(Objects.requireNonNull(mockResponse.getHeaders().getContentType())
                .includes(MediaType.TEXT_EVENT_STREAM), is(true));
        StepVerifier.create(mockResponse.getBodyAsString())
                .expectNext(artistDtoJsonList)
                .expectComplete()
                .verify();
    }

    @Test
    void getById_ReturnNotFoundError() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(""));
        exchange.getAttributes().put(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE, nonExistentIdPathVariable);
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.getById(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.NOT_FOUND));
            assertThat(response instanceof EntityResponse, is(true));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectErrorMessage(notFoundError)
                .verify();
    }


    @Test
    void getByName_Successful() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(""));
        exchange.getAttributes().put(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE, namePathVariable);
        MockServerHttpResponse mockResponse = exchange.getResponse();
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.getByName(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.OK));
            assertThat(response instanceof EntityResponse, is(true));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectComplete().verify();
        assertThat(Objects.requireNonNull(mockResponse.getHeaders().getContentType())
                .includes(MediaType.TEXT_EVENT_STREAM), is(true));
        StepVerifier.create(mockResponse.getBodyAsString())
                .expectNext(artistDtoJsonList)
                .expectComplete()
                .verify();
        StepVerifier.create(mockResponse.getBodyAsString())
                .expectNext(artistDtoJsonList)
                .expectComplete()
                .verify();
    }

    @Test
    void getAll_Successful() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(""));
        MockServerHttpResponse mockResponse = exchange.getResponse();
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.getAll(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.OK));
            assertThat(response instanceof EntityResponse, is(true));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectComplete().verify();
        assertThat(Objects.requireNonNull(mockResponse.getHeaders().getContentType())
                .includes(MediaType.TEXT_EVENT_STREAM), is(true));
        StepVerifier.create(mockResponse.getBodyAsString())
                .expectNext(artistDtoJsonList2)
                .expectComplete()
                .verify();
    }

    @Test
    void create_Successful() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(artistDtoDetachedJson));
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.create(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.CREATED));
            assertThat(response.headers().get("Location"), is(List.of("/artist/1")));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectComplete().verify();
    }

    @Test
    void update_Successful() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.put("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(artistDtoJson));
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.update(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.NO_CONTENT));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectComplete().verify();
    }

    @Test
    void update_ReturnNotFoundError() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.put("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(nonExistentArtistJsonForUpdate));
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.update(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.NO_CONTENT));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectErrorMessage(notFoundError)
                .verify();
    }

    @Test
    void delete_Successful() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete(""));
        exchange.getAttributes().put(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE, idPathVariable);
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.delete(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.NO_CONTENT));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectComplete().verify();
    }

    @Test
    void delete_ReturnNotFoundError() {
        //when
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete(""));
        exchange.getAttributes().put(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE, nonExistentIdPathVariable);
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<ServerResponse> serverResponseMono = artistHandler.delete(serverRequest);
        //then
        Mono<Void> resultMono = serverResponseMono.flatMap(response -> {
            assertThat(response.statusCode(), is(HttpStatus.NOT_FOUND));
            return response.writeTo(exchange, context);
        });
        StepVerifier.create(resultMono)
                .expectErrorMessage(notFoundError).verify();
    }
}