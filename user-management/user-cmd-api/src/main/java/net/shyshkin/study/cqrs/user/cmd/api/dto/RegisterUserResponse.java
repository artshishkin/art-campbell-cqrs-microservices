package net.shyshkin.study.cqrs.user.cmd.api.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RegisterUserResponse extends BaseResponse {

    private final String id;

    public RegisterUserResponse(String id, String message) {
        super(message);
        this.id = id;
    }
}
