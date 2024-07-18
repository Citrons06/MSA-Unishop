package my.payservice.pay.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.payservice.exception.CommonException;
import my.payservice.pay.dto.PayRequest;
import my.payservice.pay.dto.PayResponse;
import my.payservice.pay.dto.ProcessRequest;
import my.payservice.pay.entity.PayStatus;
import my.payservice.pay.service.PayService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @PostMapping("/enter")
    public String enterPayment(@ModelAttribute PayRequest payRequest, RedirectAttributes redirectAttributes) {
        ResponseEntity<?> responseEntity = payService.initiatePayment(payRequest);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("msg", "결제 화면에 진입하였습니다.");
            return "redirect:/pay-service/pay/form";
        } else {
            redirectAttributes.addFlashAttribute("msg", "결제 진입에 실패하였습니다.");
            return "redirect:/product-service/product/" + payRequest.getItemId();
        }
    }

    @GetMapping("/form")
    public String showPaymentForm(@RequestParam Long itemId,
                                  @RequestParam String itemName,
                                  @RequestParam int price,
                                  @RequestParam int count,
                                  HttpServletRequest request, Model model) {
        ProcessRequest processRequest = new ProcessRequest();
        processRequest.setItemId(itemId);
        processRequest.setQuantity(count);
        processRequest.setAmount(price * count);
        processRequest.setUsername(request.getHeader("X-User-Name"));

        model.addAttribute("processRequest", processRequest);
        model.addAttribute("itemName", itemName);
        model.addAttribute("price", price);

        return "pay/payment";
    }

    @PostMapping("/process")
    public String processPayment(@ModelAttribute ProcessRequest processRequest, RedirectAttributes redirectAttributes) {
        try {
            PayStatus payStatus = payService.processPayment(processRequest, processRequest.getUsername());
            if (payStatus == PayStatus.PAY_COMPLETE) {
                return "redirect:/pay/complete?username=" + processRequest.getUsername();
            } else {
                redirectAttributes.addFlashAttribute("error", "결제가 실패하였습니다.");
                return "redirect:/product-service/product/" + processRequest.getItemId();
            }
        } catch (CommonException e) {
            redirectAttributes.addFlashAttribute("error", "서버 오류입니다. 다시 시도해 주세요.");
            return "redirect:/product-service/product/" + processRequest.getItemId();
        }
    }

    @GetMapping("/complete")
    public String paymentComplete(Model model, @RequestParam String username) {
        try {
            PayResponse latestPayment = payService.getLatestPayment(username);
            model.addAttribute("payment", latestPayment);
            return "pay/complete";
        } catch (CommonException e) {
            model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
            return "payment/error";
        }
    }
}
