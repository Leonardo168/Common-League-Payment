package com.CLPayment.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CLPayment.dto.MP.BackUrlRecordDTO;
import com.CLPayment.dto.MP.ItemRecordDTO;
import com.CLPayment.dto.MP.PreferenceRecordDTO;
import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.enums.TransactionType;
import com.CLPayment.model.TransactionModel;
import com.CLPayment.service.PreferenceService;
import com.CLPayment.service.TransactionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/buychips")
public class PreferenceController {

    @Autowired
    PreferenceService preferenceService;

    @Autowired
    TransactionService transactionService;

    @Value("${MP.webhook}")
    String webhook;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    ZoneId brTimeZone = ZoneId.of("America/Sao_Paulo");

    @PostMapping
    public ResponseEntity<Mono<Object>> createPreference(@RequestBody Map<String, Object> data) {

	LocalDateTime creation_date = LocalDateTime.now();
	LocalDateTime expiration_date = LocalDateTime.now().plusDays(7);

	UUID transaction_id = UUID.randomUUID();

	String inventory_id = (String) data.get("inventory_id");

	ItemRecordDTO item = new ItemRecordDTO(transaction_id.toString(),
					       (String) data.get("title"),
					       (Integer) data.get("chips_qty"),
					       "BRL",
					       (Integer) data.get("unit_price"));

	PreferenceRecordDTO preferenceObj = new PreferenceRecordDTO(
								    new ItemRecordDTO[] {
											  item
								    },
								    new BackUrlRecordDTO("https://www.dicio.com.br/sucesso/",
											 "https://www.dicio.com.br/pendente/",
											 "https://www.dicio.com.br/falha/"),
								    webhook + "?source_news=webhooks",
								    inventory_id,
								    creation_date.atZone(brTimeZone).format(formatter),
								    expiration_date.atZone(brTimeZone)
										   .format(formatter));

	Mono<PreferenceRecordDTO> preferenceMono = preferenceService.create(preferenceObj)
								    .flatMap(pr -> {
									TransactionModel transaction = new TransactionModel(
															    transaction_id,
															    TransactionType.BUY_CHIPS,
															    TransactionStatus.pending,
															    UUID.fromString(inventory_id),
															    item.quantity(),
															    pr.id(),
															    creation_date,
															    creation_date,
															    expiration_date);

									return Mono.fromRunnable(() -> transactionService.save(transaction))
										   .thenReturn(pr);
								    })
								    .onErrorResume(e -> {
									return Mono.just(new PreferenceRecordDTO("Erro ao criar a preferência"));
								    });

	return ResponseEntity.ok(preferenceMono
					       .map(preference -> {
						   if (preference.id() == null) {
						       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
									    .body(preference);
						   }
						   return ResponseEntity.ok(preference);
					       }));
    }
}
