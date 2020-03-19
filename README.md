# Webhooks4j

Small, simple and extendable Java library for messaging using webhooks.

## Status

[![Build Status](https://travis-ci.org/jensborch/webhooks4j.svg?branch=master)](https://travis-ci.org/jensborch/webhooks4j) [![codecov](https://codecov.io/gh/jensborch/webhooks4j/branch/master/graph/badge.svg)](https://codecov.io/gh/jensborch/webhooks4j)

[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.jensborch.webhooks4j%3Awebhooks4j&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.jensborch.webhooks4j%3Awebhooks4j)

Webhooks4j is currently under development.

## Building

The Webhooks4j is build using Maven.

To build the application run the following command:

```sh
./mvnw package
```

Start the test application using:

```sh
./mvnw compile -pl test quarkus:dev
```

Run mutation tests:

```sh
./mvnw eu.stamp-project:pitmp-maven-plugin:run
```

Release to Maven central:

```sh
./mvnw release:clean release:prepare -Prelease
./mvnw release:perform -Prelease
````