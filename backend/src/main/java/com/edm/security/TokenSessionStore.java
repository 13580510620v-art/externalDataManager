package com.edm.security;

import java.util.Optional;

public interface TokenSessionStore {

    void save(TokenSession session, LoginUser user);

    Optional<LoginUser> find(String tokenHash);

    void delete(String tokenHash);
}
