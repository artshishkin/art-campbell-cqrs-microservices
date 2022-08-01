[![CircleCI](https://circleci.com/gh/artshishkin/art-campbell-cqrs-microservices.svg?style=svg)](https://circleci.com/gh/artshishkin/art-campbell-cqrs-microservices)
[![codecov](https://codecov.io/gh/artshishkin/art-campbell-cqrs-microservices/branch/main/graph/badge.svg)](https://codecov.io/gh/artshishkin/art-campbell-cqrs-microservices)
![Java CI with Maven](https://github.com/artshishkin/art-campbell-cqrs-microservices/workflows/Java%20CI%20with%20Maven/badge.svg)
[![GitHub issues](https://img.shields.io/github/issues/artshishkin/art-campbell-cqrs-microservices)](https://github.com/artshishkin/art-campbell-cqrs-microservices/issues)
![Spring Boot version][springver]
![Project licence][licence]
![Keycloak][keycloakver]
![Keycloak Container][keycloak-container-ver]
![MySQL Container][mysql-container-ver]
![MongoDB][mongo]
![Docker][docker]
![Axon version][axonver]
![Testcontainers version][testcontainersver]
![Spring Security][spring_security]
![JWT][jwt]

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
#### 30.5 Populate env file to `docker-compose up` command

1. Go to `/docker-compose/project_complete`
2. Run to start stack without previously saved data
   - `docker-compose --env-file ./../.env up -d`
   - to stop use `docker-compose down`
3. Run to start stack with import realm
   - `docker-compose --env-file ./../.env -f docker-compose-import-realm.yml up -d`

[springver]: https://img.shields.io/badge/dynamic/xml?label=Spring%20Boot&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27parent%27%5D%2F%2A%5Blocal-name%28%29%3D%27version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmain%2Fpom.xml&logo=Spring&labelColor=white&color=grey
[licence]: https://img.shields.io/github/license/artshishkin/art-campbell-cqrs-microservices.svg
[testcontainersver]: https://img.shields.io/badge/dynamic/xml?label=Testcontainers&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27testcontainers.version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmain%2Fpom.xml&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAjCAIAAAAMti2GAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAPCSURBVEhL7ZZNbBRlGMffj9nZabddtqW1yldDoSUSP1ICaoKlUJtw0BDTNCQcOBDjxSOnxpvGaLhw9KYSLxxAOGDCQa1iNBFogYCBdLds60KXfmw3W9idz/fDZ3bGsl1md6cYb/4Om5ln3uf/Pvt/n3nfwVJKFI6fHmXh952XNnm3DQklnbeso1fGby3n4Pq19o7zB4fao1HvUR0aS5+8fvWr5NQLmhYlBG4tIRZN84O+Xaf3vekNqEU96a9TybHJ682UxhQFY+xHEYKUEmM656f27juxs8+PPkOw9GQud/y3KwXLTKhRUiFaiZCyYFvtUe3bgcE9Gzv8aAXV0kXHOfbrL78vzIMDStmB+rCyP/u7Xjx74GBLJOJHy6yR/vjGxJf37nZomkapHwqHyXnOND96effne/b6oVXpszPpk9f+UAluUSKVtoYHdIrMsYU8/cZbx7b3QATPrKyMjP+YNQ3op1q2hgcWADp1U6z5wtAwzXx49Gbx8RYbI4yh/ucr2QPSCUbxaCSzbKfmS6QV00Jn83Rvm90UiTAJf8wfuG6kQhFz8ExG5PMypkbKPSAkRyi9pSXTHUeEECbWOYGEVsISZ+flbJZzKQmFf4/89gIXFC71KJ3q2bDUFaMCYR5mAgkuKgRDmdMZrpsCCl+19GnnQoBId4J8XE32thUTGly76xI0ARhXdgDrJZz6i+efCGhXAm1QsVTVLwU8oZAl5Fxnc7onwTTFnaBa3a1UMDz7UGRzHNToWlGP4PcNRilC2gTf39Y6tzUOacT3p2wrwguLMj3HGXcLf1bUI1jaA54pTBY1OrUzke+MwWQgVCi4tj4x1tgaSD1pAFJhASiTSwk1tXtjOsVyK4KSalsDaSDtARqUI0GQ4DLQ1kBCSftIt1vDsx7pdfK/dBXQWv8JsD0QXXDEGWwVfuxfA1LCcnTGyfkd/Z9s3mXZpsFZ4E4UHvcMc5he1D870H/uvYGnx+6R6clLy1kSgXMsaAFgj2oiyveLqCn4RLY4d4rG+6/0XDwy6EXWnOizlj6YvJYxS6qiwrbjRz1qS3MhDcPsbt/w8+jQ9kSrH62S9vgu/2g0fQsuNFrx0RQkDbkly4ED8dy7+0f7uv3oPwRIe4w9nDqVTSJF1bC7a1RJQxYslDSssbdf/2Kg30upoqY0AF9Gh6cnxgsLVImqmKxK21zYJWO4d+vlkUN1vrDqSXvc0R8PpyYWbUNt1ZRLSzpyuuKxH0YOvdrZ5o+oBUiH4ZulB+j2ZfTpmTN/3vdDjWhc9XOC0N95QCMLG07m0AAAAABJRU5ErkJggg==&labelColor=white&color=grey
[keycloakver]: https://img.shields.io/badge/dynamic/xml?label=Keycloak&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27keycloak.version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmain%2Fpom.xml&logo=data:image/png;base64,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&labelColor=white&color=grey
[keycloak-container-ver]: https://img.shields.io/badge/dynamic/yaml?label=Keycloak%20Container&query=$.KEYCLOAK_VERSION&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmain%2Fdocker-compose%2Fenv-versions.yaml&logoWidth=40&logo=data:image/png;base64,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&labelColor=white&color=grey
[docker]: https://img.shields.io/static/v1?label=&message=Docker&labelColor=white&color=white&logo=docker
[mysql-container-ver]: https://img.shields.io/badge/dynamic/yaml?label=MySQL%20Container&query=$.MYSQL_VERSION&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmain%2Fdocker-compose%2Fenv-versions.yaml&logoWidth=40&logo=data:image/png;base64,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&labelColor=white&color=grey
[mongo]: https://img.shields.io/static/v1?label=&message=MongoDB&labelColor=white&color=white&logo=mongodb
[axonver]: https://img.shields.io/badge/dynamic/xml?label=Axon%20Server&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27axon-spring-boot-starter.version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmain%2Fpom.xml&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAIAAAC0Ujn1AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAANMSURBVEhLrZZZT1NBFIAvBhRZwlJjkBgFlMSoD775zIMPPhJJfDMiGGLAqIBBRP0DGg0tXaB7C7WlNCyGELYCrUgrpRRCoexEVCpdQECrSSmecqdwS6f0kvhl0nTOmXydnDl3bokdekxYbdL6RpW6dWNzE4UiQUvtdLmZbIFErhJJ3wvEChSNBC31/MIijy+FXYOdxRGiaCToFoQvauDxZTVcUb/uEwpFgq4asE5Nf1n+hiY0OIL6qODVk7aZ9o4e2/QsmoehQL54q3ZuZf0vmgeDUUOfsbhCoUTB5ol1Hw0oGsKZCkvUg+HjJSYi37Do+IOiFDDqD+1d4IV+gMGtkw7oh1CCgnfbR9wZYpSZGeWjyaVmosA4a/egXAD8rjm1ElLtt/OlfQODKEchvcIS92gE1Mh+zzD1/TfK7YKvdY9WB622Z4fvff0H7ds+H1Qj4THVbpxY2a87Xg30aPV7drmi6fXbGpkc8xzG7NstKeVjRE61ZeYrmfKrrdZJZWMTOafS3avjCUDYxOaJKqteceokuLrv7v2JmfF0PPG2IC6XRdwfnfvhrzshb1ASUbGJKWnxSafJtVR6+/Rv3rHBC4+4rEENdtPIGMpRiC4ejs3jJ+SyTlVOk6e64fESV69dP5d1KTP7ShIjvZrFQWspKFVqvqgevFAZ+AGwo0QQ3sQ8buozG1n36GLT85ZlIufGzbSzWRkXL8ecTNY0t6KVFBSqZjCSRRfLlHD5oQQFjdl9rMSSWub3wiCKPjO1dmLV4Uw/n01Ex+cXFqGFFEAEg/TCD8D15HKvoVwAtclF3DWk7vY4jBMPTRdejkM8bIcAIBUGe51ON8oFCPVmvkCHEVYtkioPeB0OF8oF0JjDegG8GqRBXh54nSgXoG3MfYgXwKjhsq8T1h++XwA6LKU0rBfAqDu6tPAC3N+vE+MF4Hoit4z1Ahj12vpPeMnCU87kCKB/UDSEQvkCFIQoNGZUYbwAvtZer3dufgk+0TwMpqWtTus6moSAV/8X6Krt9tXmto7O7n6fz4dCkaCl9ng81TV8ePVA58Bzj6KRoKWemZ2H25XsGThhFI0ELTWcJ5MjFIj9/3I0Le0oGgm6td769Us/aBwx4/sMw87OPy1FLLbGY8wzAAAAAElFTkSuQmCC&labelColor=white&color=grey
[spring_security]: https://img.shields.io/static/v1?label=&message=Spring%20Security&labelColor=white&color=grey&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAMAAAAp4XiDAAAB5lBMVEVNryNOsCNPsSRRsidVsytWtC5WtC9XtC5XtDBYtC9ZtTFZtTJatTFatTJatTNatjJbtTNbtjNbtjRbtjVctjRctjVctjZctzZdtjVdtzVdtzZdtzdetzdetzheuDlftzhguDlguDpguTthuDphuDthuTxiuDtiuTxiuT5iuj1juT1juT9kuT5kuT9kuj5kuj9luj9lukBlukFmukBmukFmu0Fmu0Jnu0Jnu0Nou0NovERpvERpvEVqvEVqvEZqvEdqvUdrvEZrvEdrvUZrvUdrvUhuvk1vv01wv05wv093wlh4w1d4w1l8xV59xV+Ax2OCx2WDx2eEyGeFyWiHyWuJy26Ky26Ky2+Ky3CMzHGMzHKOzHOOzXSQznaRzniSzniW0H6X0X+Y0YGZ0oGa0oKa0oOa04Oc04Sd04ee1Iih1Yyi1Yyj1o6r2pit25uu25yw252w3J2w3J6x3Z+23qS33qa44qe74ay+4q+/4rLA4rLB47PD5bXF5bfH5brH5rrH5rvJ57zP6cTT68vWztnW7c3W7c7a79Lb79Pj8tzk897l9ODm8+Dn9ePo9OPo9eTp9OPp9eTq9ebq9uXr9ufu+urv9+zv+Ovw+O3x+e/y+e/y+fD0+vL2+vT4+/b7/fr8/vz////ESc4gAAABtUlEQVR42uzUg3b0QBgG4N82aqxt27Vt27bNU9vfnXaydRselW84eOLMK2CcF4KftQNGZL2nQPn9S1TZ0B4tMteZyf7JVdt8DouS9VVT3LtBSmaaUwL/8LR232WsKvYPTnbXPBHZeiXUO3x3YlOzPhCR/d8+/NiUD4g4HMyIQ/0v9H+IkQFxBOUvAcwm8OiTsFbwJ1dGSX45/DEmoULLAFqJbQ4sVikR2XkdgIXPGgEYzjPtAzRIIrCav58oPsvlN4sAx18SAaDvLcVneVhUiVJT+G0aFfrb0KojuKoaVVWUEpHddwosOlktKqzUo1W8UYXVSMKpnpjj5zbA6gTA6H8v3YdsZU1h5Z6fDvpv3/YvtrzEEOm4KMqJyN4l8XnMKrWVxjcGX9w+3FhdhERkwyf6VEKSocMnwiZC0svCJ18XCAn8dOAJczQQkzohHgkaISHwx35XGGOAjAx+vyMc7zdJCeQIbpM/3ZPkBOJUNwWrkHrkd94wrNMFElNZuKsj/CEVSlz9EiQKjR5bzhxiq6RCdkuQDkPeJuIrvl5XeTtrQb8FJNWVRVyqjaRWr/OoVomPagEA2YkR5ev8dhAAAAAASUVORK5CYII=
[jwt]: https://img.shields.io/static/v1?label=&message=JWT&labelColor=black&color=white&logo=data:image/png;base64,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

[//]: # ([jwt]: https://img.shields.io/static/v1?label=&message=JWT&labelColor=black&color=white&logo=jsonwebtokens)



