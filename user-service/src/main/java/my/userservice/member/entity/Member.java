package my.userservice.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.userservice.common.entity.BaseEntity;
import my.userservice.member.dto.MemberRequestDto;

@Getter
@NoArgsConstructor
@Entity(name = "users")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;
    private String password;
    private String memberTel;
    private String memberEmail;

    @Embedded
    private Address memberAddress;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public Member(MemberRequestDto requestDto) {
        this.username = requestDto.getUsername();
        this.password = requestDto.getPassword();
        this.memberTel = requestDto.getMemberTel();
        this.memberEmail = requestDto.getMemberEmail();
        this.memberAddress = new Address(requestDto.getCity(), requestDto.getStreet(), requestDto.getZipcode());
        this.role = requestDto.getRole();
    }

    public void updateMember(MemberRequestDto memberRequestDto) {
        this.password = memberRequestDto.getPassword();
        this.memberTel = memberRequestDto.getMemberTel();
        this.memberAddress = new Address(memberRequestDto.getCity(), memberRequestDto.getStreet(), memberRequestDto.getZipcode());
    }
}

