# Home Office Settled Status Proxy

Backend proxy API to connect the Check Immigration Status web service which checks a customer's immigration status and
rights to public funds to a Home Office API.

## Note on terminology
The Machine Readable Zone (MRZ) is a standardized section located at the bottom of a passport's identity page, designed for machine reading using Optical Character Recognition (OCR) technology. The MRZ contains essential personal information about the passport holder in a compressed, predefined format

## Endpoints

| Method | Endpoint                           | Note                                                                               |
|--------|------------------------------------|------------------------------------------------------------------------------------|
| POST   | /status/public-funds/nino          | Protected by stride authentication, requires a STRIDE "privileged" application     |
| POST   | /status/public-funds/nino/:service | Protected by internal-auth, requires service to be configured and a token provided |
| POST   | /status/public-funds/mrz           | Protected by stride authentication, requires a STRIDE "privileged" application     |

## Running the tests

```bash
sbt test it/test
```

## Running the tests with coverage

```bash
sbt clean coverageOn test it/test coverageReport
```

## Running the tests with coverage, scalafmt and dependency checks

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

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
