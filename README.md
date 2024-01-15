# Home Office Settled Status Proxy

Backend proxy API to connect the Check Immigration Status web service which checks a customer's immigration status and rights to public funds to a Home Office API.

## Endpoints

| Method | Endpoint                           | Note                                                                               |
|--------|------------------------------------|------------------------------------------------------------------------------------|
| POST   | /status/public-funds/nino          | Protected by stride authentication, requires a STRIDE "privileged" application     |
| POST   | /status/public-funds/nino/:service | Protected by internal-auth, requires service to be configured and a token provided |
| POST   | /status/public-funds/mrz           | Protected by stride authentication, requires a STRIDE "privileged" application     |

## Running the tests

**Note**: The integration tests `sbt it/test` require that you are running `sm2 --start INTERNAL_AUTH` in order to generate a token to use to authenticate with as the client.

```bash
sbt test it/test
```

## Running the tests with coverage

```bash
sbt clean coverageOn test it/test coverageReport
```

## Running the tests with coverage, scalafmt, scalastyle and dependency checks

```bash
./run_all_tests
```

## Running the app locally

```bash
sbt run
```

or

```bash
sm2 --start HOME_OFFICE_IMMIGRATION_STATUS_ALL
```

It should then be listening on port 10211

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
