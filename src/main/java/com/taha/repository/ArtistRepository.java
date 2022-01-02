package com.taha.repository;

import com.taha.bean.ArtistEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ArtistRepository extends R2dbcRepository<ArtistEntity,Long> {
    Mono<ArtistEntity> findByName(String name);
}
