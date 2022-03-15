package com.taha.service;

import com.taha.bean.ArtistEntity;
import com.taha.dto.ArtistDto;
import com.taha.exception.ArtistExceptions;
import com.taha.mapper.ArtistMapper;
import com.taha.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ArtistService {
    private final ArtistRepository artistRepository;

    public Mono<ArtistDto> getById(Long id) {
        return artistRepository.findById(id).map(ArtistMapper.INSTANCE::artistEntityToArtistDto)
                .switchIfEmpty(ArtistExceptions.artistNotFoundException(id));
    }

    public Flux<ArtistDto> getByName(String name) {
        return artistRepository.findByName(name).map(ArtistMapper.INSTANCE::artistEntityToArtistDto);
    }

    public Flux<ArtistDto> getAll() {
        return artistRepository.findAll().map(ArtistMapper.INSTANCE::artistEntityToArtistDto);
    }

    public Mono<ArtistDto> create(final ArtistDto artistDto) {
        ArtistEntity artistEntity = ArtistMapper.INSTANCE.artistDtoToArtistEntity(artistDto);
        return artistRepository.save(artistEntity).map(ArtistMapper.INSTANCE::artistEntityToArtistDto);
    }

    public Mono<Void> update(final ArtistDto artistDto) {
        ArtistEntity artistEntity = ArtistMapper.INSTANCE.artistDtoToArtistEntity(artistDto);
        return artistRepository.findById(artistEntity.getId())
                .switchIfEmpty(ArtistExceptions.artistNotFoundException(artistEntity))
                .map(artist -> artistEntity)
                .flatMap(artistRepository::save)
                .then();
    }

    public Mono<Void> delete(final Long id) {
        return artistRepository.findById(id)
                .switchIfEmpty(ArtistExceptions.artistNotFoundException(id))
                .flatMap(artistRepository::delete).then();
    }

}
