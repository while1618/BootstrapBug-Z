package org.bootstrapbugz.api.auth.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.bootstrapbugz.api.shared.constants.Regex;
import org.bootstrapbugz.api.shared.validator.EmailExist;
import org.bootstrapbugz.api.shared.validator.FieldMatch;
import org.bootstrapbugz.api.shared.validator.UsernameExist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@FieldMatch(first = "password", second = "confirmPassword", message = "{password.doNotMatch}")
public class SignUpRequest {
  @Pattern(regexp = Regex.FIRST_AND_LAST_NAME, message = "{firstName.invalid}")
  private String firstName;

  @Pattern(regexp = Regex.FIRST_AND_LAST_NAME, message = "{lastName.invalid}")
  private String lastName;

  @Pattern(regexp = Regex.USERNAME, message = "{username.invalid}")
  @UsernameExist(message = "{username.exists}")
  private String username;

  @NotEmpty(message = "{email.invalid}")
  @Email(message = "{email.invalid}")
  @EmailExist(message = "{email.exists}")
  private String email;

  @Pattern(regexp = Regex.PASSWORD, message = "{password.invalid}")
  private String password;

  @Pattern(regexp = Regex.PASSWORD, message = "{password.invalid}")
  private String confirmPassword;
}
