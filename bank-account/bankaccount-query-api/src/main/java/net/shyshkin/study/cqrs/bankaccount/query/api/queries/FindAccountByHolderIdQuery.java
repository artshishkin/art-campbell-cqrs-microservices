package net.shyshkin.study.cqrs.bankaccount.query.api.queries;

import lombok.Data;

import java.util.UUID;

@Data
public class FindAccountByHolderIdQuery {
    private final UUID accountHolderId;
}
