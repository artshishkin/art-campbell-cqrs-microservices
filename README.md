[![GitHub issues](https://img.shields.io/github/issues/artshishkin/art-campbell-cqrs-microservices)](https://github.com/artshishkin/art-campbell-cqrs-microservices/issues)
![Spring Boot version][springver]
![Project licence][licence]
![Docker][docker]
![Testcontainers version][testcontainersver]


# art-campbell-cqrs-microservices
Master Spring Boot Microservices with CQRS &amp; Event Sourcing - Tutorial from Sean Campbell (Udemy) 

###  Migrating OAuth2.0 to Spring Security 5

####  15.1. Keycloak Authorization Server Configuration

1.  Add Keycloak Infrastructure Docker Compose file
2.  Start docker compose
3.  Log in as admin
4.  Add realm `katarinazart`
5.  Create client
    -  Client ID: `springbankClient`
    -  Client protocol: `openid-connect`
    -  Save
6.  Settings
    -  Direct Access Grants Enabled: `ON` (For simplicity of testing)
    -  Valid Redirect URIs:
        -  `http://localhost:8092/*` - for localhost testing
        -  `http://localhost:8091/*` - for localhost testing
        -  `http://user-query-api:8080/*` - for docker testing (compose and testcontainers)
        -  `http://user-cmd-api:8080/*` - for docker testing (compose and testcontainers)
    -  Access Type: `confidential`
    -  Save
7.  Get Credentials
    -  Secret: `674ae476-7591-4078-82e9-5eaea5e71cff`
8.  Add Roles
    -  `READ_PRIVILEGE`
    -  `WRITE_PRIVILEGE`
9.  Create Users
    -  User 1:
        -  Username: `shyshkin.art`
        -  Email: `d.art.shishkin@gmail.com`
        -  First Name: `Art`
        -  Last Name: `Shyshkin`
        -  Save
        -  Credentials:
            -  Password: `P@ssW0rd!`
            -  Temporary:  OFF
            -  Set password
        -  Role Mappings:
            -  Move READ_PRIVILEGE and WRITE_PRIVILEGE roles to Assigned Roles
    -  User 2:
        -  Username: `shyshkina.kate`
        -  Email: `kate.shishkina@gmail.com`
        -  First Name: `Kate`
        -  Last Name: `Shyshkina`
        -  Save
        -  Credentials:
            -  Password: `P@ssW0rd1`
            -  Temporary:  OFF
            -  Set password
        -  Role Mappings:
            -  Move READ_PRIVILEGE roles to Assigned Roles
 
####  15.2 Request Access Token - Password grant_type 

-  Get through Postman or curl

```shell script
curl --location --request POST 'http://localhost:8080/auth/realms/katarinazart/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Cookie: AUTH_SESSION_ID_LEGACY=fbdf5450-d269-4f8d-b0e5-3ff3972c492f.e36f563cf8d0' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=shyshkin.art' \
--data-urlencode 'password=P@ssW0rd!' \
--data-urlencode 'client_id=springbankClient' \
--data-urlencode 'client_secret=674ae476-7591-4078-82e9-5eaea5e71cff' \
--data-urlencode 'scope=openid profile'
```     
-  Response
```json
{
    "access_token": "eyJ...v8inQ",
    "expires_in": 300,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJ...cUMI",
    "token_type": "Bearer",
    "id_token": "eyJ...U1JA",
    "not-before-policy": 0,
    "session_state": "0932e5d5-7253-4472-bb5f-c6287aaff314",
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

####  15.3 Request Access Token - Authorization Code grant_type 

-  Use Browser to Get URI
    -  `http://localhost:8080/auth/realms/katarinazart/protocol/openid-connect/auth?response_type=code&client_id=springbankClient&scope=openid%20profile&state=jskd879sdkj&redirect_uri=http://localhost:8091/callback`
-  Will redirect to Keycloak login page
    -  enter username and password    
-  Will redirect to `http://localhost:8091/callback`    
    -  `http://localhost:8091/callback?state=jskd879sdkj&session_state=8c297d0e-2fa5-4725-8fae-139b067f8344&code=30f2783e-3f02-4a78-aa84-ddc5bf570389.8c297d0e-2fa5-4725-8fae-139b067f8344.d372983f-ba31-4ea7-852c-61aedd659529`         
-  Copy code and make POST request 
```shell script
 curl --location --request POST 'http://localhost:8080/auth/realms/katarinazart/protocol/openid-connect/token' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --header 'Cookie: AUTH_SESSION_ID_LEGACY=fbdf5450-d269-4f8d-b0e5-3ff3972c492f.e36f563cf8d0' \
 --data-urlencode 'grant_type=authorization_code' \
 --data-urlencode 'client_id=springbankClient' \
 --data-urlencode 'client_secret=674ae476-7591-4078-82e9-5eaea5e71cff' \
 --data-urlencode 'code=30f2783e-3f02-4a78-aa84-ddc5bf570389.8c297d0e-2fa5-4725-8fae-139b067f8344.d372983f-ba31-4ea7-852c-61aedd659529' \
 --data-urlencode 'redirect_uri=http://localhost:8091/callback'
```
-  Will receive response
```json
{
    "access_token": "eyJh...ofbA",
    "expires_in": 300,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJh...V_9fY",
    "token_type": "Bearer",
    "id_token": "eyJh...y5rw",
    "not-before-policy": 0,
    "session_state": "8c297d0e-2fa5-4725-8fae-139b067f8344",
    "scope": "openid profile email"
}
```

[springver]: https://img.shields.io/badge/dynamic/xml?label=Spring%20Boot&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27parent%27%5D%2F%2A%5Blocal-name%28%29%3D%27version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmaster%2Fpom.xml&logo=Spring&labelColor=white&color=grey
[licence]: https://img.shields.io/github/license/artshishkin/art-campbell-cqrs-microservices.svg
[testcontainersver]: https://img.shields.io/badge/dynamic/xml?label=Testcontainers&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27testcontainers.version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmaster%2Fpom.xml&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAjCAIAAAAMti2GAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAPCSURBVEhL7ZZNbBRlGMffj9nZabddtqW1yldDoSUSP1ICaoKlUJtw0BDTNCQcOBDjxSOnxpvGaLhw9KYSLxxAOGDCQa1iNBFogYCBdLds60KXfmw3W9idz/fDZ3bGsl1md6cYb/4Om5ln3uf/Pvt/n3nfwVJKFI6fHmXh952XNnm3DQklnbeso1fGby3n4Pq19o7zB4fao1HvUR0aS5+8fvWr5NQLmhYlBG4tIRZN84O+Xaf3vekNqEU96a9TybHJ682UxhQFY+xHEYKUEmM656f27juxs8+PPkOw9GQud/y3KwXLTKhRUiFaiZCyYFvtUe3bgcE9Gzv8aAXV0kXHOfbrL78vzIMDStmB+rCyP/u7Xjx74GBLJOJHy6yR/vjGxJf37nZomkapHwqHyXnOND96effne/b6oVXpszPpk9f+UAluUSKVtoYHdIrMsYU8/cZbx7b3QATPrKyMjP+YNQ3op1q2hgcWADp1U6z5wtAwzXx49Gbx8RYbI4yh/ucr2QPSCUbxaCSzbKfmS6QV00Jn83Rvm90UiTAJf8wfuG6kQhFz8ExG5PMypkbKPSAkRyi9pSXTHUeEECbWOYGEVsISZ+flbJZzKQmFf4/89gIXFC71KJ3q2bDUFaMCYR5mAgkuKgRDmdMZrpsCCl+19GnnQoBId4J8XE32thUTGly76xI0ARhXdgDrJZz6i+efCGhXAm1QsVTVLwU8oZAl5Fxnc7onwTTFnaBa3a1UMDz7UGRzHNToWlGP4PcNRilC2gTf39Y6tzUOacT3p2wrwguLMj3HGXcLf1bUI1jaA54pTBY1OrUzke+MwWQgVCi4tj4x1tgaSD1pAFJhASiTSwk1tXtjOsVyK4KSalsDaSDtARqUI0GQ4DLQ1kBCSftIt1vDsx7pdfK/dBXQWv8JsD0QXXDEGWwVfuxfA1LCcnTGyfkd/Z9s3mXZpsFZ4E4UHvcMc5he1D870H/uvYGnx+6R6clLy1kSgXMsaAFgj2oiyveLqCn4RLY4d4rG+6/0XDwy6EXWnOizlj6YvJYxS6qiwrbjRz1qS3MhDcPsbt/w8+jQ9kSrH62S9vgu/2g0fQsuNFrx0RQkDbkly4ED8dy7+0f7uv3oPwRIe4w9nDqVTSJF1bC7a1RJQxYslDSssbdf/2Kg30upoqY0AF9Gh6cnxgsLVImqmKxK21zYJWO4d+vlkUN1vrDqSXvc0R8PpyYWbUNt1ZRLSzpyuuKxH0YOvdrZ5o+oBUiH4ZulB+j2ZfTpmTN/3vdDjWhc9XOC0N95QCMLG07m0AAAAABJRU5ErkJggg==&labelColor=white&color=grey
[docker]: https://img.shields.io/static/v1?label=&message=Docker&labelColor=white&color=white&logo=docker



