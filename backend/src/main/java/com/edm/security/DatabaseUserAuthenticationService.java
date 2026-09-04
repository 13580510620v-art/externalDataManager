package com.edm.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edm.system.entity.User;
import com.edm.system.mapper.UserMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class DatabaseUserAuthenticationService implements UserAuthenticationService, SamlUserService {

    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseUserAuthenticationService(UserMapper userMapper, JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<LoginCandidate> findByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(new LoginCandidate(toLoginUser(user), user.getPasswordHash()));
    }

    @Override
    public Optional<LoginUser> findByNameId(String nameId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getSamlNameId, nameId));
        return user == null ? Optional.empty() : Optional.of(toLoginUser(user));
    }

    private LoginUser toLoginUser(User user) {
        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                Boolean.TRUE.equals(user.getEnabled()),
                loadPermissions(user.getId())
        );
    }

    private Set<String> loadPermissions(Long userId) {
        String sql = """
                SELECT DISTINCT p.PERMISSION_CODE
                FROM SYS_USER_GROUP ug
                JOIN SYS_GROUP g ON g.ID = ug.GROUP_ID
                JOIN SYS_GROUP_PERMISSION gp ON gp.GROUP_ID = g.ID
                JOIN SYS_PERMISSION p ON p.ID = gp.PERMISSION_ID
                WHERE ug.USER_ID = ?
                  AND g.IS_ENABLE = 1
                  AND p.IS_ENABLE = 1
                ORDER BY p.PERMISSION_CODE
                """;
        return new HashSet<>(jdbcTemplate.queryForList(sql, String.class, userId));
    }
}
