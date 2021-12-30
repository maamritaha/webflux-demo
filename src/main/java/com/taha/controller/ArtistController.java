package com.taha.controller;

import com.taha.bean.Artist;
import com.taha.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("artist")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistRepository artistRepository;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE, value = "/all")
    public Flux<Artist> getAll() {
        return artistRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE, value = "/{name}")
    public Mono<Object> findByName(@PathVariable String name) {
        return artistRepository.findByName(name)
                .handle((artist, synchronousSink) -> {
                    if ("James".equals(artist.getName())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "what are you doing James is a rock star not a rest resource !");
                    }
                    synchronousSink.next(artist);
                }).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "artist not found")));
        // return Flux.fromStream(Stream.generate(() ->"hello " + name + " " + Instant.now()))
        //        .delayElements(Duration.ofSeconds(1)).log();
    }

    @PostMapping
    public Mono<Artist> add(Artist artist) {
        return artistRepository.save(artist);
    }
}
