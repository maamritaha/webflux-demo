package com.taha.router;

import com.taha.handler.ArtistHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class ArtistRouter {
    private final ArtistHandler artistHandler;

    @Bean
    RouterFunction<ServerResponse> artistRoutes() {
        return route().path("artist", builder -> builder
                .GET("/{id}", artistHandler::getById)
                .GET("/name/{name}", artistHandler::getByName)
                .GET(artistHandler::getAll)
                .POST(artistHandler::create)
                .PUT(artistHandler::update)
                .DELETE("/{id}", artistHandler::delete)
        ).build();
    }
}
