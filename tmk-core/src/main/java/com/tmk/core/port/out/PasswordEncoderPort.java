package com.tmk.core.port.out;

/**
 * Port for password encoding operations.
 * Implemented in tmk-api using BCryptPasswordEncoder.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
