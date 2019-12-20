# Home Office Settled Status Proxy

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally

    sm --start AGENTS_STUBS HOME_OFFICE_SETTLED_STATUS_PROXY -f
    sm --stop HOME_OFFICE_SETTLED_STATUS_PROXY
    sbt run

It should then be listening on port 9388

    browse http://localhost:9388/home-office-settled-status-proxy

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
