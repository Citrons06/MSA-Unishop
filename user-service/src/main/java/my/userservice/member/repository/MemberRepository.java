package my.userservice.member.repository;


import my.userservice.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByMemberEmail(String email);
}