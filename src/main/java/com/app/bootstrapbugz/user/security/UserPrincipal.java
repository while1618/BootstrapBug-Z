package com.app.bootstrapbugz.user.security;

import com.app.bootstrapbugz.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class UserPrincipal implements UserDetails {
  @Serial private static final long serialVersionUID = 5954870422841373076L;
  private final Long id;
  private final String username;
  @JsonIgnore private final String email;
  @JsonIgnore private final String password;
  private final LocalDateTime updatedAt;
  private final LocalDateTime lastLogout;
  private final boolean enabled;
  private final boolean accountNonLocked;
  private final Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(User user) {
    List<GrantedAuthority> authorities = getAuthorities(user);
    return new UserPrincipal(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        user.getUpdatedAt(),
        user.getLastLogout(),
        user.isActivated(),
        user.isNonLocked(),
        authorities);
  }

  private static List<GrantedAuthority> getAuthorities(User user) {
    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }
}
