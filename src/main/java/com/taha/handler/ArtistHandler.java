package com.taha.handler;

import com.taha.dto.ArtistDto;
import com.taha.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@Component
@RequiredArgsConstructor
public class ArtistHandler {
    private final ArtistService artistService;

    public Mono<ServerResponse> getById(ServerRequest serverRequest) {
        return artistService
                .getById(Long.parseLong(serverRequest.pathVariable("id")))
                .flatMap(artistDto -> ServerResponse
                        .ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .bodyValue(artistDto));
    }

    public Mono<ServerResponse> getByName(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(TEXT_EVENT_STREAM)
                .body(artistService.getByName(serverRequest.pathVariable("name")), ArtistDto.class);
    }

    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(TEXT_EVENT_STREAM)
                .body(artistService.getAll(), ArtistDto.class);
    }

    public Mono<ServerResponse> create(ServerRequest serverRequest){
        return serverRequest.bodyToMono(ArtistDto.class)
                .flatMap(artistService::create)
                .flatMap(artist -> ServerResponse.created(URI.create("/artist/" + artist.getId())).build());
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest){
        return serverRequest.bodyToMono(ArtistDto.class)
                .flatMap(artistService::update)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest){
        return artistService
                .delete(Long.parseLong(serverRequest.pathVariable("id")))
                .then(ServerResponse.noContent().build());
    }
}