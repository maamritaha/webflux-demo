package com.taha.integration;

import com.taha.ArtistServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArtistServiceApplicationIT {


    @Test
    void contextLoads() {
        ArtistServiceApplication.main(new String[]{});
    }

}
