# Home Office Settled Status Proxy

Backend proxy API to connect the Check Immigration Status web service which checks a customer's immigration status and rights to public funds to a Home Office API.

## Running the tests

```bash
sbt test IntegrationTest/test
```

## Running the tests with coverage

```bash
sbt clean coverageOn Test/test IntegrationTest/test coverageReport
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
sm --start HOSS
```

It should then be listening on port 10211

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
