#!/usr/bin/env bash

sbt clean scalafmtAll coverage compile Test/test it/test coverageOff dependencyUpdates coverageReport
