# Clean Code - Web Crawler

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TannerGabriel_622.060-Clean-Code&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=TannerGabriel_622.060-Clean-Code)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=TannerGabriel_622.060-Clean-Code&metric=coverage)](https://sonarcloud.io/summary/new_code?id=TannerGabriel_622.060-Clean-Code)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=TannerGabriel_622.060-Clean-Code&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=TannerGabriel_622.060-Clean-Code)

This repository features an implementation of the Web Crawler assignment from the course 622.060 (24S) Clean-Code.

## Getting started

### Environment variables

To use the translation feature, you need to provide a `TRANSLATION_API_KEY` from the [Google Translate RapidAPI](https://rapidapi.com/IRCTCAPI/api/google-translator9/) as an environment variable.

```
TRANSLATION_API_KEY=<API_KEY>
```

The translation and source language detection steps will be skipped if you do not provide the environment variable.

### Running the application

The application requires four starting parameters that can be provided in two different ways, as explained below.

- Starting URLs: The URLs that the crawler starts with (Split using `,` when providing them as a command line argument)
- Depth limit: How deep the crawler should search for (0 means only the provided starting URL will be crawled)
- Domain filter: Regex definition of the domains that should be crawled (Use `[\s\S]*` if all domains should be considered valid)
- Target Language: Language the headings should be translated to (list of available languages can be found using the GET call [here](https://rapidapi.com/IRCTCAPI/api/google-translator9/))

**Using arguments:**

The Main class can be started using arguments by either passing when running in the command line or by adding them to the "Run Configuration" in the IDE:

```
java webcrawler <Starting URLs> <Depth limit> <Domain filter> <Target language>
```

**Reading input from the command line:**

If the application is started without arguments, the user will be asked to enter the arguments in the command line.

### Executing tests

The unit test can be executed manually using `./gradlew test`, which will run all tests and build test coverage using Jacoco. The tests also run automatically in the pipeline, and the coverage is displayed in Sonarcloud.