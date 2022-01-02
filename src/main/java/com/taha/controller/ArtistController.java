package com.taha.controller;

import com.taha.dto.ArtistDto;
import com.taha.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("artist")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE, value = "/all")
    public Flux<ArtistDto> getAll() {
        return artistService.getAll();
    }
    @GetMapping("{id}")
    public Mono<ArtistDto> getById(@PathVariable Long id){
        return artistService.getById(id);
    }

    @GetMapping("/name/{name}")
    public Mono<ArtistDto> getByName(@PathVariable String name){
        return artistService.getByName(name);
    }

    @PostMapping
    public Mono<ArtistDto> create(@RequestBody @Valid Mono<ArtistDto> artistDto){
        return artistDto.flatMap(artistService::create);
    }

    @PutMapping
    public Mono<ArtistDto> update(@RequestBody @Valid Mono<ArtistDto> artistDto){
        return artistDto.flatMap(artistService::update);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id){
        return artistService.delete(id);
    }


}
