# Home Office Settled Status Proxy

Backend proxy API to connect the Check Immigration Status web service which checks a customer's immigration status and rights to public funds to a Home Office API.

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the tests with coverage, scalafmt, scalastyle and dependency checks

    ./run_all_tests

## Running the app locally

    sbt run
    
or    

    sm --start HOSS

It should then be listening on port 10211

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
