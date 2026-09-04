package com.edm.auth;

import com.edm.auth.dto.CurrentUserResponse;
import com.edm.auth.dto.LoginRequest;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.CurrentUser;
import com.edm.security.LoginCandidate;
import com.edm.security.LoginUser;
import com.edm.security.UserAuthenticationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserAuthenticationService userAuthenticationService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAuthenticationService userAuthenticationService, PasswordEncoder passwordEncoder) {
        this.userAuthenticationService = userAuthenticationService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginUser login(LoginRequest request) {
        Optional<LoginCandidate> candidate = userAuthenticationService.findByUsername(request.username().trim());
        if (candidate.isEmpty()) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        LoginCandidate loginCandidate = candidate.get();
        if (!loginCandidate.user().enabled()
                || loginCandidate.passwordHash() == null
                || !passwordEncoder.matches(request.password(), loginCandidate.passwordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return loginCandidate.user();
    }

    public CurrentUserResponse currentUser() {
        LoginUser user = CurrentUser.required();
        return new CurrentUserResponse(user.id(), user.username(), user.fullName(), user.permissions());
    }
}
