# OpenLMIS Fulfillment Service
This repository holds the files for the OpenLMIS Fulfillment Independent Service.

## Prerequisites
* Docker 1.11+
* Docker Compose 1.6+

## <a name="quickstart">Quick Start</a>
1. Fork/clone this repository from GitHub.

 ```shell
 git clone https://github.com/OpenLMIS/openlmis-fulfillment.git
 ```
2. Add an environment file called `.env` to the root folder of the project, with the required 
project settings and credentials. For a starter environment file, you can use [this 
one](https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env). e.g.

 ```shell
 cd openlmis-fulfillment
 curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env
 ```
3. Develop w/ Docker by running `docker-compose run --service-ports fulfillment`.
See [Developing w/ Docker](#devdocker). You should now be in an interactive shell inside
the newly created development environment.
4. Run `gradle build` to build. After the build steps finish, you should see 'Build Successful'.
5. Start the service with `gradle bootRun`. Once it is running, you should see
'Started Application in NN seconds'. Your console will not return to a prompt as long as
the service is running. The service may write errors and other output to your console.
6. You must authenticate to get a valid `access_token` before you can use the service.
Follow the [Security](https://github.com/OpenLMIS/openlmis-example/blob/master/README.md#security)
instructions to generate a POST request to the authorization server at `http://localhost:8081/`.
You can use a tool like [Postman](https://www.getpostman.com/) to generate the POST.
The authorization server will return an `access_token` which you must save for use on requests to
this OpenLMIS service. The token will expire with age, so be ready to do this step often.
7. Go to `http://localhost:8080/?access_token=<yourAccessToken>` to see the service name and version.
Note: If localhost does not work, the docker container with the service running might not be
bridged to your host workstation. In that case, you can determine your Docker IP address by
running `docker-machine ip` and then visit `http://<yourDockerIPAddress>:8080/`.
8. Go to `http://localhost:8080/index.html?access_token=<yourAccessToken>` to see the Swagger UI showing the API endpoints.
(Click 'default' to expand the list.)
9. Use URLs of the form `http://localhost:8080/api/*?access_token=<yourAccessToken>` to hit
the APIs directly.

## Stopping the Service
To stop the service (when it is running with `gradle bootRun`) use Control-C.

To clean up unwanted Docker containers, see the [Docker Cheat Sheet](https://openlmis.atlassian.net/wiki/display/OP/Docker+Cheat+Sheet#DockerCheatSheet-Cleaningup:).

## API Definition and Testing
See the API Definition and Testing section in the Example Service README at
https://github.com/OpenLMIS/openlmis-example/blob/master/README.md#api.

## Building & Testing
See the Building & Testing section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#building.

## Security
See the Security section in the Example Service README at
https://github.com/OpenLMIS/openlmis-example/blob/master/README.md#security.

## <a name="devdocker">Developing with Docker</a>
See the Developing with Docker section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#devdocker.

### Development Environment
See the Development Environment section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#devenv.

### Build Deployment Image
See the Build Deployment Image section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#buildimage.

### Publish to Docker Repository
TODO

### Docker's file details
See the Docker's file details section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#dockerfiles.

### Running complete application with nginx proxy
See the Running complete application with nginx proxy section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#nginx.

### Logging
See the Logging section in the Service Template README at 
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#logging.

### Internationalization (i18n)
See the Internationalization section in the Service Template README at 
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#internationalization.

### Debugging
See the Debugging section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#debugging.

### Demo Data
You can use a standard data set for demonstration purposes. To do so, first follow the Quick Start
until step 3 is done: https://github.com/OpenLMIS/openlmis-fulfillment/blob/master/README.md#quickstart.
Then, before `gradle bootRun`, use `gradle demoDataSeed`. This will generate a sql input file under
`./demo-data` directory.

To insert this data into the database, finish the Quick Start steps,
and then outside of container's interactive shell, run:
`docker exec -i openlmisfulfillment_db_1 psql -Upostgres open_lmis < demo-data/input.sql`

## Production by Spring Profile

By default when this service is started, it will clean its schema in the database before migrating
it.  This is meant for use during the normal development cycle.  For production data, this obviously
is not desired as it would remove all of the production data.  To change the default clean & migrate
behavior to just be a migrate behavior (which is still desired for production use), we use a Spring
Profile named `production`.  To use this profile, it must be marked as Active.  The easiest way to
do so is to add to the .env file:

```java
spring_profiles_active=production
```

This will set the similarly named environment variable and limit the profile in use.  The
expected use-case for this is when this service is deployed through the
[Reference Distribution](https://github.com/openlmis/openlmis-ref-distro).

### Demo Data
A basic set of demo data is included with this service, defined under `./demo-data/`.  This data may
be optionally loaded by using the `demo-data` Spring Profile.  Setting this profile may be done by
setting the `spring.profiles.active` environment variable.

When building locally from the development environment, you may run:

```shell
$ export spring_profiles_active=demo-data
$ gradle bootRun
```

To see how to set environment variables through Docker Compose, see the 
[Reference Distribution](https://github.com/openlmis/openlmis-ref-distro)


## Environment variables

Environment variables common to all services are listed here: https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#environment-variables

* **TRANSFER_IN_REASON_ID** - the ID of the reason that will be used to make receive entries in the Stock Management physical inventory  

If you are using the [hauptmedia/proftpd](https://hub.docker.com/r/hauptmedia/proftpd/) Docker image for testing fulfillment (like we do in our reference distribution), you can use the following variables to set the username and password for that server:

* **FTP_USERNAME** - the username of the FTP user. 
* **FTP_PASSWORD** - the password of the FTP user.

Note: the fulfillment service does not use the variables above for setting up any connections - the configuration is kept in the database and managed through the appropriate endpoints.

* **SHIPMENT_POLLING_RATE_MILLISECONDS** - Rate in milliseconds how often shipment service should check for files over FTP
, default `10000`
/SFTP, ...
* **SHIPMENT_SHIPPED_BY_ID** - OLMIS user to send shipment files as, default admin, `a337ec45-31a0-4f2b-9b2e-a105c4b669bb`

* **ALLOW_FTP_TRANSFER_ON_REQUISITION_TO_ORDER** - whether order file should be send over FTP, default `true`
* **SEND_EMAIL_ON_REQUISITION_TO_ORDER** - whether order file should be send over email, default `true`

* **ALLOW_EMPTY_SHIPMENT** - whether empty shipment (and consequently empty Proof of Delivery) should be accepted by
 validation, default `false`
