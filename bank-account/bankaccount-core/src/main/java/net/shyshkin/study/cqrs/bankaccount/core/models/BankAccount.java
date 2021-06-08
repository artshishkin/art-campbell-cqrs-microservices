package net.shyshkin.study.cqrs.bankaccount.core.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankAccount {

    @Id
    @EqualsAndHashCode.Include
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private String accountHolderId; //userId from user microservices

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creationDate;

    @Enumerated(value = EnumType.STRING)
    private AccountType accountType;

    private BigDecimal balance;
}
