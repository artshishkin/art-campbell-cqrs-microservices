package net.shyshkin.study.cqrs.bankaccount.core.dto;

import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class OpenAccountResponse extends BaseResponse {

    private UUID id;

    @Builder
    public OpenAccountResponse(UUID id, String message) {
        super(message);
        this.id = id;
    }
}
