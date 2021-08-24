package com.github.fmjsjx.entrepot.server.conf;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class EntrepotServerConfigTest {

    @Test
    public void testLoadFromYaml() {
        try (var in = getClass().getResourceAsStream("/conf/entrepot-server.yml")) {
            var cfg = EntrepotServerProperties.loadFromYaml(in);
            assertNotNull(cfg);
        } catch (Exception e) {
            fail(e);
        }
    }

}
