# Configure Keycloak Authorization Server

### 1. Configure Keycloak

1. Start Keycloak server 
   - From folder ./docker-compose run
   - `docker-compose -f auth-server/keycloak-postgres.yml --env-file .env up -d`
2. Log in 
   - [http://localhost:8080/admin](http://localhost:8080/admin)
   - Username: `admin`
   - Password: `Pa55w0rd`
3. Create realm `katarinazart`
4. Create client
   - Client ID: `springbankClient`
   - Client protocol: `openid-connect`
   - Save
5. Settings
   - Client authentication: `On`
   - Authorization: `Off`
   - Authentication flow:
     - Standard flow - `ON`
     - Direct Access Grants Enabled: `ON` (For simplicity of testing)
   - Valid Redirect URIs:
      - `http://localhost:*` - for localhost testing
      - `http://user-query-api:*` - for docker testing (compose and testcontainers)
      - `http://user-cmd-api:*` - for docker testing (compose and testcontainers)
      - `http://host.testcontainers.internal:*` - for docker testing
6. Get Credentials
   - Credentials ->
   - Client Authenticator: Client Id and Secret
   - Secret: `BJ3cbXk16VWdeevUSYrtVyT2XWurOmey`
7. Add Roles
   - Realm Roles ->
   - `READ_PRIVILEGE`
   - `WRITE_PRIVILEGE`
8. Create Users
   - User 1:
      - Username: `shyshkin.art`
      - Email: `d.art.shishkin@gmail.com`
      - First Name: `Art`
      - Last Name: `Shyshkin`
      - Create
      - Credentials:
         -  Password: `P@ssW0rd!`
         -  Temporary:  OFF
         -  Set password
      - Role Mappings:
        - Assign Role
        - Choose READ_PRIVILEGE and WRITE_PRIVILEGE
   - User 2:
      - Username: `shyshkina.kate`
      - Email: `kate.shishkina@gmail.com`
      - First Name: `Kate`
      - Last Name: `Shyshkina`
      - Create
      - Credentials:
         -  Password: `P@ssW0rd1`
         -  Temporary:  OFF
         -  Set password
      - Role Mappings:
        - Assign Role
        - Choose READ_PRIVILEGE
9. Export client
   - Clients -> `springbankClient` -> 3 dots
   - Export client
10. Export Realm
    - Realm Settings
    - Actions -> Partial Export
      - Include groups and roles
      - Include clients

### 2. Request Access Token - Password grant_type 

1. Through IntelliJ IDEA HttpClient
   - use [1 GET Access Token - Password grant_type](../docker-compose/requests.http)
2. Through Postman or curl  

```shell script
curl --location --request POST 'http://localhost:8080/realms/katarinazart/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=shyshkin.art' \
--data-urlencode 'password=P@ssW0rd!' \
--data-urlencode 'client_id=springbankClient' \
--data-urlencode 'client_secret=BJ3cbXk16VWdeevUSYrtVyT2XWurOmey' \
--data-urlencode 'scope=openid profile'
```

- Response

```json
{
  "access_token": "eyJ..._Yy_Xg",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJh...cGWPAPY",
  "token_type": "Bearer",
  "id_token": "eyJh...HU8A",
  "not-before-policy": 0,
  "session_state": "cd29d7a4-3c7e-417b-b742-1c69cdcf8763",
  "scope": "openid profile email"
}
```
-  View Token - [https://jwt.io](https://jwt.io)
    -  "scope": "openid profile email",
    -  "email_verified": false,
    -  "name": "Art Shyshkin",
    -  "preferred_username": "shyshkin.art",
    -  "given_name": "Art",
    -  "family_name": "Shyshkin",
    -  "email": "d.art.shishkin@gmail.com"
```json
{
  "exp": 1660657551,
  "iat": 1660657251,
  "jti": "6576483b-5594-4123-9642-8a28f2cb619f",
  "iss": "http://localhost:8080/realms/katarinazart",
  "aud": "account",
  "sub": "56c98ff6-dac5-4713-a720-2921ffa9bebf",
  "typ": "Bearer",
  "azp": "springbankClient",
  "session_state": "cd29d7a4-3c7e-417b-b742-1c69cdcf8763",
  "acr": "1",
  "realm_access": {
    "roles": [
      "default-roles-katarinazart",
      "READ_PRIVILEGE",
      "offline_access",
      "uma_authorization",
      "WRITE_PRIVILEGE"
    ]
  },
  "resource_access": {
    "account": {
      "roles": [
        "manage-account",
        "manage-account-links",
        "view-profile"
      ]
    }
  },
  "scope": "openid profile email",
  "sid": "cd29d7a4-3c7e-417b-b742-1c69cdcf8763",
  "email_verified": false,
  "name": "Art Shyshkin",
  "preferred_username": "shyshkin.art",
  "given_name": "Art",
  "family_name": "Shyshkin",
  "email": "d.art.shishkin@gmail.com"
}
```

### 3. Request Access Token - Authorization Code grant_type

- Use Browser to Get URI
    -  `http://localhost:8080/realms/katarinazart/protocol/openid-connect/auth?response_type=code&client_id=springbankClient&scope=openid profile&state=jskd879sdkj&redirect_uri=http://localhost:8083/no_matter_callback`
- Will redirect to Keycloak login page
    -  enter username and password
- Will redirect to `http://localhost:8083/no_matter_callback`
    -  `http://localhost:8083/no_matter_callback?state=jskd879sdkj&session_state=44b74481-cf7d-443a-a138-9efdcd9c4d95&code=8f6f6c61-db23-4418-9db0-7069ff07e8ff.44b74481-cf7d-443a-a138-9efdcd9c4d95.c10b3c5f-fcc4-40fd-890e-dcbd7b18e6b2`
- Copy code and make POST request
  - using IntelliJ IDEA HttpClient
    - [2 Get Access token (Authorization code Grant Type)](../docker-compose/requests.http)
  - using Postman or curl
```shell script
 curl --location --request POST 'http://localhost:8080/realms/katarinazart/protocol/openid-connect/token' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --data-urlencode 'grant_type=authorization_code' \
 --data-urlencode 'client_id=springbankClient' \
 --data-urlencode 'client_secret=BJ3cbXk16VWdeevUSYrtVyT2XWurOmey' \
 --data-urlencode 'code=8f6f6c61-db23-4418-9db0-7069ff07e8ff.44b74481-cf7d-443a-a138-9efdcd9c4d95.c10b3c5f-fcc4-40fd-890e-dcbd7b18e6b2' \
 --data-urlencode 'redirect_uri=http://localhost:8083/no_matter_callback'
```
-  Will receive response
```json
{
   "access_token":"eyJhb...E7VQ",
   "expires_in":300,
   "refresh_expires_in":1800,
   "refresh_token":"eyJhb...g7Hx0",
   "token_type":"Bearer",
   "id_token":"eyJhb...j5VQg",
   "not-before-policy":0,
   "session_state":"44b74481-cf7d-443a-a138-9efdcd9c4d95",
   "scope":"openid profile email"
}
```

### 4. Export Realm

- Start exporting with compose file [keycloak-postgres-export.yml](/docker-compose/auth-server/keycloak-postgres-export.yml)
  - From folder ./docker-compose run
  - `docker-compose -f auth-server/keycloak-postgres-export.yml --env-file .env up -d`

### 5. Import realm

- Start import with compose file [keycloak-postgres-import.yml](/docker-compose/auth-server/keycloak-postgres-import.yml)
    - From folder ./docker-compose run
    - `docker-compose -f auth-server/keycloak-postgres-import.yml --env-file .env up -d`



