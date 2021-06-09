package net.shyshkin.study.cqrs.bankaccount.query.api.queries;

import lombok.Data;

import java.util.UUID;

@Data
public class FindAccountByIdQuery {
    private final UUID id;
}
