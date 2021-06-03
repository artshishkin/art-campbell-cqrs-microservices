package net.shyshkin.study.cqrs.user.query.api.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchUsersQuery {

    private String filter;

}
