package com.edm.security;

import java.util.Optional;

public interface UserAuthenticationService {

    Optional<LoginCandidate> findByUsername(String username);
}
