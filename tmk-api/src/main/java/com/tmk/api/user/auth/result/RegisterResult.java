package com.tmk.api.user.auth.result;

import com.tmk.core.user.entity.UserAccount;

public record RegisterResult(
        Long userId,
        String username
) {

    public static RegisterResult from(UserAccount userAccount) {
        return new RegisterResult(
                userAccount.getId(),
                userAccount.getUsername()
        );
    }
}
