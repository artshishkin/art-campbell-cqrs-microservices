package net.shyshkin.study.cqrs.user.storage.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RemoteUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final UsersApiService usersService;

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {

        log.info("getUserById({},{})", id, realm);
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();

        return getUserByUsername(username, realm);

    }

    // We need to implement AT LEAST ONE of methods: getUserByUsername, getUserByEmail
    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {

        log.info("getUserByUsername({},{})", username, realm);

        User user = usersService.getUserDetailsByUsername(username);
        log.info("user: {}", user);
        if (user != null) {
            return createUserModel(user, realm);
        }
        return null;
    }

    private UserModel createUserModel(User user, RealmModel realm) {
        return new AbstractUserAdapter(session, realm, model) {
            @Override
            public String getUsername() {
                return user.getUsername();
            }

            @Override
            public String getEmail() {
                return user.getEmail();
            }

            @Override
            public String getFirstName() {
                return user.getFirstname();
            }

            @Override
            public String getLastName() {
                return user.getLastname();
            }

            @Override
            public String getFirstAttribute(String name) {
                switch (name) {
                    case UserModel.USERNAME:
                        return getUsername();
                    case UserModel.EMAIL:
                        return getEmail();
                    case UserModel.FIRST_NAME:
                        return getFirstName();
                    case UserModel.LAST_NAME:
                        return getLastName();
                    case "name":
                        return user.getUsername();
                }
                return super.getFirstAttribute(name);
            }

            @Override
            protected Set<RoleModel> getRoleMappingsInternal() {
                Set<RoleModel> roleModels = user.getRoles()
                        .stream()
                        .map(realm::getRole)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                log.info("User {} has Role Models {}", user, roleModels);

                return roleModels;
            }
        };
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {

        log.info("getUserByEmail({},{})", email, realm);

        User user = usersService.getUserDetailsByEmail(email);
        log.info("user: {}", user);
        if (user != null) {
            return createUserModel(user, realm);
        }
        return null;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {

        if (!supportsCredentialType(credentialType))
            return false;

        return getCredentialStore()
                .getStoredCredentialsByTypeStream(realm, user, credentialType)
                .findAny()
                .isPresent();
    }

    private UserCredentialStore getCredentialStore() {
        return session.userCredentialManager();
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {

        log.info("username: {}", user.getUsername());
        log.info("email: {}", user.getEmail());
        log.info("attributes: {}", user.getAttributes());
        System.out.println("Attributes: " + user.getAttributes());

        String userPassword = credentialInput.getChallengeResponse();
        VerificationPasswordResponse verificationPasswordResponse = usersService.verifyUserPasswordByUsername(user.getUsername(), userPassword);

        return verificationPasswordResponse != null && verificationPasswordResponse.isValid();
    }
}
