package my.orderservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "userClient", url = "${user-service.url}")
public interface UserAdapter {

    @GetMapping("/api/internal/user/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);
}
