package net.shyshkin.study.cqrs.bankaccount.core.dto;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAccountResponseTest {

    private UUID id;
    private String message;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        message = Faker.instance().lorem().sentence();
    }

    @Test
    void builderAndGettersTest() {

        //when
        var openAccountResponse = OpenAccountResponse.builder()
                .id(id)
                .message(message)
                .build();

        //then
        assertThat(openAccountResponse.getId()).isEqualTo(id);
        assertThat(openAccountResponse.getMessage()).isEqualTo(message);
    }

    @Test
    void settersTest() {

        //when
        var openAccountResponse = new OpenAccountResponse();
        openAccountResponse.setId(id);
        openAccountResponse.setMessage(message);

        //then
        assertThat(openAccountResponse.getId()).isEqualTo(id);
        assertThat(openAccountResponse.getMessage()).isEqualTo(message);
    }


}