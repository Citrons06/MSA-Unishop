package my.payservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserAdapter {

    @GetMapping("/api/user/internal/test/{username}")
    UserDto getMember(@PathVariable("username") String username);
}