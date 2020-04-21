package com.app.webapp.security;

import com.app.webapp.error.ErrorDomains;
import com.app.webapp.error.exception.ResourceNotFound;
import com.app.webapp.model.User;
import com.app.webapp.repository.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public UserDetailsServiceImpl(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws ResourceNotFound {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFound(messageSource.getMessage("user.notFound", null, LocaleContextHolder.getLocale()), ErrorDomains.AUTH)
        );
        return UserPrincipal.create(user);
    }
}
