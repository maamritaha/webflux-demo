package com.taha.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import org.springframework.data.relational.core.mapping.Table;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("artist")
public class ArtistEntity {
    @Id
    Long id;
    String name;
}
