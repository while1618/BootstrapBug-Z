package org.bootstrapbugz.api.user.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.bootstrapbugz.api.auth.security.UserPrincipal;
import org.bootstrapbugz.api.auth.service.JwtService;
import org.bootstrapbugz.api.auth.util.JwtUtil;
import org.bootstrapbugz.api.auth.util.JwtUtil.JwtPurpose;
import org.bootstrapbugz.api.shared.error.exception.BadRequestException;
import org.bootstrapbugz.api.shared.error.exception.ResourceNotFound;
import org.bootstrapbugz.api.shared.message.service.MessageService;
import org.bootstrapbugz.api.user.mapper.UserMapperImpl;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.model.User;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.bootstrapbugz.api.user.request.ChangePasswordRequest;
import org.bootstrapbugz.api.user.request.UpdateUserRequest;
import org.bootstrapbugz.api.user.response.UserResponse;
import org.bootstrapbugz.api.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private MessageService messageService;
  @Spy private UserMapperImpl userMapper;
  @Spy private BCryptPasswordEncoder bCryptPasswordEncoder;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private JwtService jwtService;
  @Mock private Authentication auth;
  @Mock private SecurityContext securityContext;

  @InjectMocks private UserServiceImpl userService;

  @Captor private ArgumentCaptor<User> userArgumentCaptor;

  private String password;
  private Set<Role> roles;
  private User user;

  @BeforeEach
  void setUp(TestInfo info) {
    if (info.getTags().contains("skipBeforeEach")) return;

    password = bCryptPasswordEncoder.encode("qwerty123");
    roles = Collections.singleton(new Role(RoleName.USER));
    user = new User(1L, "Test", "Test", "test", "test@test.com", password, true, true, roles);
    setAuth(user);
  }

  private void setAuth(User user) {
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);
    when(auth.getPrincipal()).thenReturn(UserPrincipal.create(user));
  }

  @Test
  void itShouldFindUserByUsername_showEmail() {
    UserResponse expectedUserResponse =
        new UserResponse(1L, "Test", "Test", "test", "test@test.com", true, true, null);
    when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
    UserResponse actualUserResponse = userService.findByUsername("test");
    assertThat(actualUserResponse).isEqualTo(expectedUserResponse);
  }

  @Test
  void itShouldFindUserByUsername_hideEmail() {
    UserResponse expectedUserResponse =
        new UserResponse(1L, "Test", "Test", "test", null, true, true, null);
    User loggedUser =
        new User(2L, "User", "User", "user", "user@user.com", password, true, true, roles);
    setAuth(loggedUser);
    when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
    UserResponse actualUserResponse = userService.findByUsername("test");
    assertThat(actualUserResponse).isEqualTo(expectedUserResponse);
  }

  @Test
  @Tag("skipBeforeEach")
  void findUserByUsernameShouldThrowResourceNotFound() {
    when(messageService.getMessage("user.notFound")).thenReturn("User not found.");
    assertThatThrownBy(() -> userService.findByUsername("test"))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage("User not found.");
  }

  @Test
  void itShouldUpdateUser_newUsernameAndEmail() {
    User expectedUser =
        new User(1L, "User", "User", "user", "user@user.com", password, false, true, roles);
    UpdateUserRequest updateUserRequest =
        new UpdateUserRequest("User", "User", "user", "user@user.com");
    when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(updateUserRequest.getEmail())).thenReturn(false);
    when(jwtService.createToken(updateUserRequest.getUsername(), JwtPurpose.CONFIRM_REGISTRATION))
        .thenReturn(JwtUtil.TOKEN_TYPE + "token");
    userService.update(updateUserRequest);
    verify(userRepository, times(1)).save(userArgumentCaptor.capture());
    assertThat(userArgumentCaptor.getValue()).isEqualTo(expectedUser);
  }

  @Test
  void itShouldUpdateUser_sameUsernameAndEmail() {
    User expectedUser =
      new User(1L, "User", "User", "test", "test@test.com", password, true, true, roles);
    UpdateUserRequest updateUserRequest =
      new UpdateUserRequest("User", "User", "test", "test@test.com");
    userService.update(updateUserRequest);
    verify(userRepository, times(1)).save(userArgumentCaptor.capture());
    assertThat(userArgumentCaptor.getValue()).isEqualTo(expectedUser);
  }

  @Test
  void updateUserShouldThrowBadRequest_usernameExists() {
    UpdateUserRequest updateUserRequest =
        new UpdateUserRequest("User", "User", "user", "user@user.com");
    when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(true);
    when(messageService.getMessage("username.exists")).thenReturn("Username already exists.");
    assertThatThrownBy(() -> userService.update(updateUserRequest))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Username already exists.");
  }

  @Test
  void updateUserShouldThrowBadRequest_emailExists() {
    UpdateUserRequest updateUserRequest =
        new UpdateUserRequest("User", "User", "user", "user@user.com");
    when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(updateUserRequest.getEmail())).thenReturn(true);
    when(messageService.getMessage("email.exists")).thenReturn("Email already exists.");
    assertThatThrownBy(() -> userService.update(updateUserRequest))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Email already exists.");
  }

  @Test
  void itShouldChangePassword() {
    ChangePasswordRequest changePasswordRequest =
        new ChangePasswordRequest("qwerty123", "qwerty1234", "qwerty1234");
    userService.changePassword(changePasswordRequest);
    verify(userRepository, times(1)).save(userArgumentCaptor.capture());
    assertThat(
            bCryptPasswordEncoder.matches(
                changePasswordRequest.getNewPassword(),
                userArgumentCaptor.getValue().getPassword()))
        .isTrue();
  }

  @Test
  void changePasswordShouldThrownBadRequest_wrongOldPassword() {
    when(messageService.getMessage("oldPassword.invalid")).thenReturn("Wrong old password.");
    ChangePasswordRequest changePasswordRequest =
        new ChangePasswordRequest("qwerty123456", "qwerty1234", "qwerty1234");
    assertThatThrownBy(() -> userService.changePassword(changePasswordRequest))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Wrong old password.");
  }
}