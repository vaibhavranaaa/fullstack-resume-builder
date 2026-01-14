package in.vaibhavrana.resumebuilderapi.service;


import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import in.vaibhavrana.resumebuilderapi.document.Payment;
import in.vaibhavrana.resumebuilderapi.document.User;
import in.vaibhavrana.resumebuilderapi.dto.AuthResponse;
import in.vaibhavrana.resumebuilderapi.repository.PaymentRepository;
import in.vaibhavrana.resumebuilderapi.repository.UserRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static in.vaibhavrana.resumebuilderapi.util.AppConstants.PREMIUM;

@RequiredArgsConstructor
@Slf4j
@Service

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final UserRespository userRespository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String getRazorpayKeySecret;

    public Payment createOrder(Object principal, String planType) throws RazorpayException {

        //Initial step
        AuthResponse authResponse=authService.getProfile(principal);

        //Step1: Initialize the razorpay client
        RazorpayClient razorpayClient=new RazorpayClient(razorpayKeyId,getRazorpayKeySecret);


        //Step 2: Prepare the JSON object to pass the razorpay
        int amount=99900; //Amount in paise
        String currency="INR";
        String receipt=PREMIUM+"_"+UUID.randomUUID().toString().substring(0,8);

        JSONObject orderRequest=new JSONObject();
        orderRequest.put("amount",amount);
        orderRequest.put("currency",currency);
        orderRequest.put("receipt",receipt);


        //Step3: Call the razorpay API to create order
        Order razorpayOrder=razorpayClient.orders.create(orderRequest);


        //Step 4: Save the order details into database
        Payment newPayment=Payment.builder()
                .userId(authResponse.getId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .currency(currency)
                .planType(planType)
                .status("created")
                .receipt(receipt)
                .build();

        //Step5: return rhe result
        return paymentRepository.save(newPayment);

    }

    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {
        try{
            JSONObject attributes=new JSONObject();
            attributes.put("razorpay_order_id",razorpayOrderId);
            attributes.put("razorpay_payment_id",razorpayPaymentId);
            attributes.put("razorpay_signature",razorpaySignature);

            boolean isValidSignature=Utils.verifyPaymentSignature(attributes,getRazorpayKeySecret);

            if(isValidSignature){

                //update the payment status
                Payment payment=paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(()-> new RuntimeException("Payment not found"));

                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setStatus("paid");

                paymentRepository.save(payment);

                //Upgrade the user subscription
                upgradeUserSubsciption(payment.getUserId(),payment.getPlanType());
                return true;

            }
            return false;

        }catch(Exception e){
            log.error("Error verifying the payment:",e);
            return false;
        }
    }

    private void upgradeUserSubsciption(String userId, String planType) {
       User existingUser= userRespository.findById(userId)
                        .orElseThrow(()-> new UsernameNotFoundException("User not found"));
       existingUser.setSubscriptionPlan(planType);
       userRespository.save(existingUser);
        log.info("User {} upgraded to {} plan",userId,planType);

    }


    public List<Payment> getUserPayments(Object principal) {
        //Step1: Get the currenty profile
        AuthResponse authResponse=authService.getProfile(principal);

        //Step2: Call the repo finder method

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(authResponse.getId());
    }

    public Payment getPaymentService(String orderId) {

        //Step1: Call the repo finder method
        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new RuntimeException("Payment not found"));
    }
}
