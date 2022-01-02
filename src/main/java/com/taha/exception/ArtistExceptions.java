package com.taha.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
public class ArtistExceptions {
    public static <T> Mono<T> artistNotFoundException(Object notFoundObject) {
        return Mono.defer(() -> {
            log.error("Artist not found : " + notFoundObject);
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
        });
    }
}
