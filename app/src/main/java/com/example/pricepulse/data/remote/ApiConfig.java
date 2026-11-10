package com.example.pricepulse.data.remote;

/**
 * Network configuration constants.
 *
 * NOTE ON ANDROID EMULATOR NETWORKING:
 * On the Android Emulator, 'localhost' refers to the emulator device itself.
 * The special loopback address '10.0.2.2' points to the development host computer's '10.0.2.2' / 'localhost'.
 * On a physical device on the same local Wi-Fi, this should be the development machine's LAN IP (e.g. 192.168.1.X).
 */
public class ApiConfig {
    // Emulator host IP mapping
    public static final String EMULATOR_BASE_URL = "http://10.0.2.2:8000/api/v1/";
    
    // Default base URL for current debug development
    public static final String BASE_URL = EMULATOR_BASE_URL;
}
