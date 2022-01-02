package com.taha.event;

import com.taha.bean.ArtistEntity;
import com.taha.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
@Log4j2
@RequiredArgsConstructor
public class SimpleDataInitializer {
    private final ArtistRepository artistRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void go() {
        var artists = Flux.just("James", "Kirk", "Lars")
                .map(name -> ArtistEntity.builder().name(name).build())
                .flatMap(this.artistRepository::save);
        this.artistRepository
                .deleteAll()
                .thenMany(artists)
                .thenMany(this.artistRepository.findAll())
                .subscribe(log::info);
    }
}
