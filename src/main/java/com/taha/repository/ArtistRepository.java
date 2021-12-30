package com.taha.repository;

import com.taha.bean.Artist;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ArtistRepository extends R2dbcRepository<Artist,Long> {
    Mono<Artist> findByName(String name);
}
