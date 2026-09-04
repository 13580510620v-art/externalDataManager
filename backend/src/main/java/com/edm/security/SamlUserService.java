package com.edm.security;

import java.util.Optional;

public interface SamlUserService {

    Optional<LoginUser> findByNameId(String nameId);
}
