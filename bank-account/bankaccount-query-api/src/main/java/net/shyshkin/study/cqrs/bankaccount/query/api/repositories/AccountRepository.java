package net.shyshkin.study.cqrs.bankaccount.query.api.repositories;

import net.shyshkin.study.cqrs.bankaccount.core.models.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<BankAccount, UUID> {
}
