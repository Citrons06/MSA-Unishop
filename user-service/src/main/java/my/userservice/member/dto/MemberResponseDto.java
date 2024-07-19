package my.userservice.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.userservice.member.entity.Member;

@Getter @Setter
@NoArgsConstructor
public class MemberResponseDto {
    private Long id;

    private String username;
    private String memberTel;
    private String memberEmail;

    private String city;
    private String street;
    private String zipcode;

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.memberTel = member.getMemberTel();
        this.memberEmail = member.getMemberEmail();
        this.street = member.getMemberAddress().getStreet();
        this.city = member.getMemberAddress().getCity();
        this.zipcode = member.getMemberAddress().getZipcode();
    }

    public MemberResponseDto(String mail, String test, String city) {
        this.memberEmail = mail;
        this.username = test;
        this.city = city;
    }
}
