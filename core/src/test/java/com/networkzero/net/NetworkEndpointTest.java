package com.networkzero.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkEndpointTest {
    @Test void parsesBareHostAndAdvertisedHostWithPort() {
        assertEquals(new Network.Endpoint("10.10.123.206", Network.TCP_PORT),
                Network.parseEndpoint("10.10.123.206"));
        assertEquals(new Network.Endpoint("10.10.123.206", Network.TCP_PORT),
                Network.parseEndpoint(" 10.10.123.206:54555 "));
    }

    @Test void parsesBracketedIpv6Address() {
        assertEquals(new Network.Endpoint("::1", 60000), Network.parseEndpoint("[::1]:60000"));
    }

    @Test void rejectsInvalidPortsAndHosts() {
        assertThrows(IllegalArgumentException.class, () -> Network.parseEndpoint(""));
        assertThrows(IllegalArgumentException.class, () -> Network.parseEndpoint("host:"));
        assertThrows(IllegalArgumentException.class, () -> Network.parseEndpoint("host:abc"));
        assertThrows(IllegalArgumentException.class, () -> Network.parseEndpoint("host:65536"));
    }
}
