package com.tmk.core.auth.service;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.RefreshTokenPort;
import com.tmk.core.port.out.UserPort;
import com.tmk.core.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReissueTokenService {

    private final RefreshTokenPort refreshTokenPort;
    private final UserPort userPort;

    public User validateRefreshTokenAndGetUser(Long userId, String refreshToken) {
        String stored = refreshTokenPort.find(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        if (!stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        return userPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
    }
}
