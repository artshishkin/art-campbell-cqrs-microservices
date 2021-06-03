package net.shyshkin.study.cqrs.user.oauth2_0.repositories;

import net.shyshkin.study.cqrs.user.core.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    @Query("{'account.username': ?0}")
    Optional<User> finaByUsername(String username);

}
