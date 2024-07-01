package my.productservice.resilience4j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorfulService {

    private final ErrorfulServiceClient errorfulServiceClient;

    @Scheduled(fixedRate = 1000) // 1초에 한 번씩 호출
    public void callErrorfulService() {
        try {
            String response1 = errorfulServiceClient.case1();
            log.info("Response from case1: {}", response1);

            String response2 = errorfulServiceClient.case2();
            log.info("Response from case2: {}", response2);

            String response3 = errorfulServiceClient.case3();
            log.info("Response from case3: {}", response3);
        } catch (Exception e) {
            log.error("Error calling errorful service", e);
        }
    }
}
