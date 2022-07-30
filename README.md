[![GitHub issues](https://img.shields.io/github/issues/artshishkin/art-campbell-cqrs-microservices)](https://github.com/artshishkin/art-campbell-cqrs-microservices/issues)
![Spring Boot version][springver]
![Project licence][licence]
![Keycloak][keycloak]
![MySQL][mysql]
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

[springver]: https://img.shields.io/badge/dynamic/xml?label=Spring%20Boot&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27parent%27%5D%2F%2A%5Blocal-name%28%29%3D%27version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmaster%2Fpom.xml&logo=Spring&labelColor=white&color=grey
[licence]: https://img.shields.io/github/license/artshishkin/art-campbell-cqrs-microservices.svg
[testcontainersver]: https://img.shields.io/badge/dynamic/xml?label=Testcontainers&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27testcontainers.version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmaster%2Fpom.xml&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAjCAIAAAAMti2GAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAPCSURBVEhL7ZZNbBRlGMffj9nZabddtqW1yldDoSUSP1ICaoKlUJtw0BDTNCQcOBDjxSOnxpvGaLhw9KYSLxxAOGDCQa1iNBFogYCBdLds60KXfmw3W9idz/fDZ3bGsl1md6cYb/4Om5ln3uf/Pvt/n3nfwVJKFI6fHmXh952XNnm3DQklnbeso1fGby3n4Pq19o7zB4fao1HvUR0aS5+8fvWr5NQLmhYlBG4tIRZN84O+Xaf3vekNqEU96a9TybHJ682UxhQFY+xHEYKUEmM656f27juxs8+PPkOw9GQud/y3KwXLTKhRUiFaiZCyYFvtUe3bgcE9Gzv8aAXV0kXHOfbrL78vzIMDStmB+rCyP/u7Xjx74GBLJOJHy6yR/vjGxJf37nZomkapHwqHyXnOND96effne/b6oVXpszPpk9f+UAluUSKVtoYHdIrMsYU8/cZbx7b3QATPrKyMjP+YNQ3op1q2hgcWADp1U6z5wtAwzXx49Gbx8RYbI4yh/ucr2QPSCUbxaCSzbKfmS6QV00Jn83Rvm90UiTAJf8wfuG6kQhFz8ExG5PMypkbKPSAkRyi9pSXTHUeEECbWOYGEVsISZ+flbJZzKQmFf4/89gIXFC71KJ3q2bDUFaMCYR5mAgkuKgRDmdMZrpsCCl+19GnnQoBId4J8XE32thUTGly76xI0ARhXdgDrJZz6i+efCGhXAm1QsVTVLwU8oZAl5Fxnc7onwTTFnaBa3a1UMDz7UGRzHNToWlGP4PcNRilC2gTf39Y6tzUOacT3p2wrwguLMj3HGXcLf1bUI1jaA54pTBY1OrUzke+MwWQgVCi4tj4x1tgaSD1pAFJhASiTSwk1tXtjOsVyK4KSalsDaSDtARqUI0GQ4DLQ1kBCSftIt1vDsx7pdfK/dBXQWv8JsD0QXXDEGWwVfuxfA1LCcnTGyfkd/Z9s3mXZpsFZ4E4UHvcMc5he1D870H/uvYGnx+6R6clLy1kSgXMsaAFgj2oiyveLqCn4RLY4d4rG+6/0XDwy6EXWnOizlj6YvJYxS6qiwrbjRz1qS3MhDcPsbt/w8+jQ9kSrH62S9vgu/2g0fQsuNFrx0RQkDbkly4ED8dy7+0f7uv3oPwRIe4w9nDqVTSJF1bC7a1RJQxYslDSssbdf/2Kg30upoqY0AF9Gh6cnxgsLVImqmKxK21zYJWO4d+vlkUN1vrDqSXvc0R8PpyYWbUNt1ZRLSzpyuuKxH0YOvdrZ5o+oBUiH4ZulB+j2ZfTpmTN/3vdDjWhc9XOC0N95QCMLG07m0AAAAABJRU5ErkJggg==&labelColor=white&color=grey
[keycloak]: https://img.shields.io/static/v1?label=&message=Keycloak&logo=data:image/png;base64,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&labelColor=white&color=grey
[docker]: https://img.shields.io/static/v1?label=&message=Docker&labelColor=white&color=white&logo=docker
[mysql]: https://img.shields.io/static/v1?label=&message=MySQL&labelColor=white&color=grey&logo=mysql
[mongo]: https://img.shields.io/static/v1?label=&message=MongoDB&labelColor=white&color=white&logo=mongodb
[axonver]: https://img.shields.io/badge/dynamic/xml?label=Axon%20Server&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27properties%27%5D%2F%2A%5Blocal-name%28%29%3D%27axon-spring-boot-starter.version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2Fartshishkin%2Fart-campbell-cqrs-microservices%2Fmaster%2Fpom.xml&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAIAAAC0Ujn1AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAANMSURBVEhLrZZZT1NBFIAvBhRZwlJjkBgFlMSoD775zIMPPhJJfDMiGGLAqIBBRP0DGg0tXaB7C7WlNCyGELYCrUgrpRRCoexEVCpdQECrSSmecqdwS6f0kvhl0nTOmXydnDl3bokdekxYbdL6RpW6dWNzE4UiQUvtdLmZbIFErhJJ3wvEChSNBC31/MIijy+FXYOdxRGiaCToFoQvauDxZTVcUb/uEwpFgq4asE5Nf1n+hiY0OIL6qODVk7aZ9o4e2/QsmoehQL54q3ZuZf0vmgeDUUOfsbhCoUTB5ol1Hw0oGsKZCkvUg+HjJSYi37Do+IOiFDDqD+1d4IV+gMGtkw7oh1CCgnfbR9wZYpSZGeWjyaVmosA4a/egXAD8rjm1ElLtt/OlfQODKEchvcIS92gE1Mh+zzD1/TfK7YKvdY9WB622Z4fvff0H7ds+H1Qj4THVbpxY2a87Xg30aPV7drmi6fXbGpkc8xzG7NstKeVjRE61ZeYrmfKrrdZJZWMTOafS3avjCUDYxOaJKqteceokuLrv7v2JmfF0PPG2IC6XRdwfnfvhrzshb1ASUbGJKWnxSafJtVR6+/Rv3rHBC4+4rEENdtPIGMpRiC4ejs3jJ+SyTlVOk6e64fESV69dP5d1KTP7ShIjvZrFQWspKFVqvqgevFAZ+AGwo0QQ3sQ8buozG1n36GLT85ZlIufGzbSzWRkXL8ecTNY0t6KVFBSqZjCSRRfLlHD5oQQFjdl9rMSSWub3wiCKPjO1dmLV4Uw/n01Ex+cXFqGFFEAEg/TCD8D15HKvoVwAtclF3DWk7vY4jBMPTRdejkM8bIcAIBUGe51ON8oFCPVmvkCHEVYtkioPeB0OF8oF0JjDegG8GqRBXh54nSgXoG3MfYgXwKjhsq8T1h++XwA6LKU0rBfAqDu6tPAC3N+vE+MF4Hoit4z1Ahj12vpPeMnCU87kCKB/UDSEQvkCFIQoNGZUYbwAvtZer3dufgk+0TwMpqWtTus6moSAV/8X6Krt9tXmto7O7n6fz4dCkaCl9ng81TV8ePVA58Bzj6KRoKWemZ2H25XsGThhFI0ELTWcJ5MjFIj9/3I0Le0oGgm6td769Us/aBwx4/sMw87OPy1FLLbGY8wzAAAAAElFTkSuQmCC&labelColor=white&color=grey
[spring_security]: https://img.shields.io/static/v1?label=&message=Spring%20Security&labelColor=white&color=grey&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAMAAAAp4XiDAAAB5lBMVEVNryNOsCNPsSRRsidVsytWtC5WtC9XtC5XtDBYtC9ZtTFZtTJatTFatTJatTNatjJbtTNbtjNbtjRbtjVctjRctjVctjZctzZdtjVdtzVdtzZdtzdetzdetzheuDlftzhguDlguDpguTthuDphuDthuTxiuDtiuTxiuT5iuj1juT1juT9kuT5kuT9kuj5kuj9luj9lukBlukFmukBmukFmu0Fmu0Jnu0Jnu0Nou0NovERpvERpvEVqvEVqvEZqvEdqvUdrvEZrvEdrvUZrvUdrvUhuvk1vv01wv05wv093wlh4w1d4w1l8xV59xV+Ax2OCx2WDx2eEyGeFyWiHyWuJy26Ky26Ky2+Ky3CMzHGMzHKOzHOOzXSQznaRzniSzniW0H6X0X+Y0YGZ0oGa0oKa0oOa04Oc04Sd04ee1Iih1Yyi1Yyj1o6r2pit25uu25yw252w3J2w3J6x3Z+23qS33qa44qe74ay+4q+/4rLA4rLB47PD5bXF5bfH5brH5rrH5rvJ57zP6cTT68vWztnW7c3W7c7a79Lb79Pj8tzk897l9ODm8+Dn9ePo9OPo9eTp9OPp9eTq9ebq9uXr9ufu+urv9+zv+Ovw+O3x+e/y+e/y+fD0+vL2+vT4+/b7/fr8/vz////ESc4gAAABtUlEQVR42uzUg3b0QBgG4N82aqxt27Vt27bNU9vfnXaydRselW84eOLMK2CcF4KftQNGZL2nQPn9S1TZ0B4tMteZyf7JVdt8DouS9VVT3LtBSmaaUwL/8LR232WsKvYPTnbXPBHZeiXUO3x3YlOzPhCR/d8+/NiUD4g4HMyIQ/0v9H+IkQFxBOUvAcwm8OiTsFbwJ1dGSX45/DEmoULLAFqJbQ4sVikR2XkdgIXPGgEYzjPtAzRIIrCav58oPsvlN4sAx18SAaDvLcVneVhUiVJT+G0aFfrb0KojuKoaVVWUEpHddwosOlktKqzUo1W8UYXVSMKpnpjj5zbA6gTA6H8v3YdsZU1h5Z6fDvpv3/YvtrzEEOm4KMqJyN4l8XnMKrWVxjcGX9w+3FhdhERkwyf6VEKSocMnwiZC0svCJ18XCAn8dOAJczQQkzohHgkaISHwx35XGGOAjAx+vyMc7zdJCeQIbpM/3ZPkBOJUNwWrkHrkd94wrNMFElNZuKsj/CEVSlz9EiQKjR5bzhxiq6RCdkuQDkPeJuIrvl5XeTtrQb8FJNWVRVyqjaRWr/OoVomPagEA2YkR5ev8dhAAAAAASUVORK5CYII=
[jwt]: https://img.shields.io/static/v1?label=&message=JWT&labelColor=black&color=white&logo=data:image/png;base64,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

[//]: # ([jwt]: https://img.shields.io/static/v1?label=&message=JWT&labelColor=black&color=white&logo=jsonwebtokens)



