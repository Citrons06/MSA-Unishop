package my.userservice.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.userservice.member.entity.Member;
import my.userservice.member.entity.UserRole;

@Getter @Setter
@NoArgsConstructor
public class MemberRequestDto {
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String memberTel;
    private String memberEmail;

    @NotNull
    private String city;
    @NotNull
    private String street;
    @NotNull
    private String zipcode;

    private UserRole role = UserRole.USER;

    public MemberRequestDto(String username, String password, String tel, String email, String city, String street, String zipcode, UserRole role) {
        this.username = username;
        this.password = password;
        this.memberTel = tel;
        this.memberEmail = email;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.role = role;
    }

    public Member toEntity() {
        return new Member(this);
    }
}
