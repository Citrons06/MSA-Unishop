package my.orderservice.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserAdapter {

    @GetMapping("/user/api/internal/test/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);
}