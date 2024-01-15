#!/usr/bin/env bash

sbt scalafmtAll scalastyleAll clean coverage compile Test/test it/test coverageOff dependencyUpdates coverageReport