package net.shyshkin.study.cqrs.user.storage.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationPasswordResponse {

    private boolean valid;

}

