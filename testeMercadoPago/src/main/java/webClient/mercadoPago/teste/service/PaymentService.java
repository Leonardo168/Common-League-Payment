package webClient.mercadoPago.teste.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import webClient.mercadoPago.teste.dto.PaymentRecordDTO;

@Service
public class PaymentService {
    @Value("${MP.AccessToken}")
    private String mpAccessToken;
    
    private final WebClient webClient;
    
    public PaymentService(WebClient.Builder builder) {
	webClient = builder.baseUrl("https://api.mercadopago.com/v1/payments").build();
    }
    
    public Mono<PaymentRecordDTO> findById(String payment_id){
	return webClient.get()
		.uri("/{id}", payment_id)
		.headers(h -> h.setBearerAuth(mpAccessToken))
		.retrieve()
		.bodyToMono(PaymentRecordDTO.class);
    }

}