package net.shyshkin.study.cqrs.user.query.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationPasswordResponse {

    private boolean valid;

}
