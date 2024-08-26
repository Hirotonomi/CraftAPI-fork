package com.github.games647.craftapi.resolver.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RotatingSourceFactoryTest {

    @Mock
    private SSLSocketFactory oldFactory;

    @InjectMocks
    private RotatingSourceFactory rotatingSourceFactory;

    private InetAddress address1;
    private InetAddress address2;

    @BeforeEach
    void setUp() throws Exception {
        address1 = InetAddress.getByName("192.168.1.1");
        address2 = InetAddress.getByName("192.168.1.2");
    }

    @Test
    void testCreateSocket_noAddresses_defaultBehavior() throws IOException {
        Socket socket = mock(Socket.class);
        when(oldFactory.createSocket()).thenReturn(socket);

        Socket createdSocket = rotatingSourceFactory.createSocket();

        assertEquals(socket, createdSocket);
        verify(oldFactory).createSocket();
    }

    @Test
    void testSetOutgoingAddresses_rotatesCorrectly() {
        rotatingSourceFactory.setOutgoingAddresses(Collections.singleton(address1));

        Optional<InetAddress> firstAddress = rotatingSourceFactory.getNextLocalAddress();
        assertTrue(firstAddress.isPresent());
        assertEquals(address1, firstAddress.get());

        Optional<InetAddress> secondAddress = rotatingSourceFactory.getNextLocalAddress();
        assertTrue(secondAddress.isPresent());
        assertEquals(address1, secondAddress.get());
    }

    @Test
    void testSetOutgoingAddresses_emptyCollection() {
        rotatingSourceFactory.setOutgoingAddresses(Collections.emptySet());

        Optional<InetAddress> address = rotatingSourceFactory.getNextLocalAddress();
        assertFalse(address.isPresent());
    }

    @Test
    void testCreateSocket_bindToNextAddress() throws IOException {
        Socket socket = mock(Socket.class);
        when(oldFactory.createSocket()).thenReturn(socket);
        rotatingSourceFactory.setOutgoingAddresses(Collections.singletonList(address1));

        Socket createdSocket = rotatingSourceFactory.createSocket();

        verify(socket).bind(any());
        assertEquals(socket, createdSocket);
    }

    @Test
    void testGetDefaultCipherSuites_delegates() {
        String[] cipherSuites = new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"};
        when(oldFactory.getDefaultCipherSuites()).thenReturn(cipherSuites);

        assertArrayEquals(cipherSuites, rotatingSourceFactory.getDefaultCipherSuites());
    }

    @Test
    void testGetSupportedCipherSuites_delegates() {
        String[] cipherSuites = new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"};
        when(oldFactory.getSupportedCipherSuites()).thenReturn(cipherSuites);

        assertArrayEquals(cipherSuites, rotatingSourceFactory.getSupportedCipherSuites());
    }
}