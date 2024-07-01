package my.productservice.resilience4j;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "errorful-service")
public interface ErrorfulServiceClient {

    @GetMapping("/errorful/case1")
    String case1();

    @GetMapping("/errorful/case2")
    String case2();

    @GetMapping("/errorful/case3")
    String case3();
}
