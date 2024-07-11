package my.userservice.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.userservice.member.entity.Member;
import my.userservice.member.entity.UserRole;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestMemberSignup {

    private String username;

    private String password;

    private UserRole role = UserRole.USER;

    public TestMemberSignup(Member member) {
        this.username = member.getUsername();
        this.password = member.getPassword();
    }
}
