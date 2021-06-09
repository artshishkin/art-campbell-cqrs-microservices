package net.shyshkin.study.cqrs.bankaccount.query.api.queries;

import lombok.Data;

@Data
public class FindAccountByHolderIdQuery {
    private final String accountHolderId;
}
