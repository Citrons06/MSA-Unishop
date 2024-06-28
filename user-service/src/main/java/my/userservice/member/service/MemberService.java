package my.userservice.member.service;


import my.userservice.adapter.OrderDto;
import my.userservice.member.dto.MemberRequestDto;
import my.userservice.member.dto.MemberResponseDto;

public interface MemberService {
    void signup(MemberRequestDto memberRequestDto);

    boolean verifyEmail(String email, String token);

    MemberResponseDto updateMember(String username, MemberRequestDto memberRequestDto);

    MemberResponseDto findMember(String username);

    MemberResponseDto getMember(String username);

    OrderDto getOrder(String username);
}
