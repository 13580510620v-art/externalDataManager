package com.edm.security;

public record LoginCandidate(LoginUser user, String passwordHash) {
}
