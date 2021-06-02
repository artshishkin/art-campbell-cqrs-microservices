package net.shyshkin.study.cqrs.user.query.api.repositories;

import net.shyshkin.study.cqrs.user.core.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
