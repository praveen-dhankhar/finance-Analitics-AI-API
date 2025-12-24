package com.financeapp.security;

import com.financeapp.dto.UserResponseDto;
import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            return createUserPrincipal(user);
        } catch (Exception ex) {
            logger.error("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username, ex);
        }
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
            return createUserPrincipal(user);
        } catch (Exception ex) {
            logger.error("User not found with id: {}", id);
            throw new UsernameNotFoundException("User not found with id: " + id, ex);
        }
    }

    private UserDetails createUserPrincipal(User user) {
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // For now, all users have basic USER role
        // In the future, you can add role-based permissions here
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
