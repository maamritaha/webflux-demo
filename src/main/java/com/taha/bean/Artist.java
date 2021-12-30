package com.taha.bean;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;


@Value
@Builder
public class Artist {
    @Id
    Long id;
    String name;
}
