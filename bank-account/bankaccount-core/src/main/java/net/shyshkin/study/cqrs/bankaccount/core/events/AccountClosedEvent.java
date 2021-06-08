package net.shyshkin.study.cqrs.bankaccount.core.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AccountClosedEvent {

    private final UUID id;

}
