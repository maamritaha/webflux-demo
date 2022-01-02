package com.taha.mapper;

import com.taha.bean.ArtistEntity;
import com.taha.dto.ArtistDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArtistMapper {
    ArtistMapper INSTANCE = Mappers.getMapper(ArtistMapper.class);
    ArtistDto artistEntityToArtistDto(ArtistEntity artistEntity);
    ArtistEntity artistDtoToArtistEntity(ArtistDto artistDto);
}
