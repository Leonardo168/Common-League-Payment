package webClient.mercadoPago.teste.dto;

public record PreferenciaRecordDTO(ItemRecordDTO[] items, BackUrlRecordDTO back_urls, String notification_url,
				   String external_reference, Boolean expires, String expiration_date_from, String expiration_date_to,
				   String id, String sandbox_init_point) {

    public PreferenciaRecordDTO(ItemRecordDTO[] itemRecordDTOs,
				BackUrlRecordDTO backUrlRecordDTO, String notification_url, String external_reference,
				String expiration_date_from, String expiration_date_to) {
	this(itemRecordDTOs, backUrlRecordDTO, notification_url, external_reference, true, expiration_date_from,
	     expiration_date_to, null, null);
    }
}