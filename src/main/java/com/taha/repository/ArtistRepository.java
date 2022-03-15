package com.taha.repository;

import com.taha.bean.ArtistEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface ArtistRepository extends R2dbcRepository<ArtistEntity,Long> {
    Flux<ArtistEntity> findByName(String name);
}
