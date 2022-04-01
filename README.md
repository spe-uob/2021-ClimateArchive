# 2021-ClimateArchive

## Overview

    The goal of this project is to give researchers easy access to complex climate data with the climatearchive.org web
    application as a front end and our software solution as the backend to provide access to the data. The website
    currently visualises the research data in 3D, however quantitative access to the underlying data is needed to improve
    interdisciplinary research. We will provide a solution which filters the database and produces smaller data subsets
    which could then be downloaded/visualised from the browser.

## Stakeholders

    The typical stakeholders for this project will be researchers, or other people who may be interested in the data,
    who may not be particularly tech savy.

    Researchers
    Individulas interested in the data

    Sebastian
    Coders (us)

## User Stories

    A researcher / individual interested in the data, I should be able to:
        -select a point on the globe on the alreadyprovided climate change model in order to extract point data for a selected location and time period.
        -view and compare graphs generated from comparing these data points.
        -download datasets generated from certain parameters
        -share datasets/graphs generated at specific points with other individuals

Note: we are mainly working on our client's private repository to add features to climatearchive.org and on a backend server to provide access to underlying climate data.

# Usage

The server can be downloaded from the releases tab or built from source.

## Building from source

Download the source code using

```
git clone https://github.com/spe-uob/2021-ClimateArchive.git
```

Compile into a jar file using

```
cd 2021-ClimateArchive

mvn package -DskipTests
```

This will save `climateArchive.jar` in the `target` directory

## Running

First place `climateArchive.jar` into your chosen directory.

Create a config folder using in the same directory with `mkdir config`

Create the file `application.properties` in the `config` file

In this file enter the config

```
data_location=<path to climate data>
```

[Add additional config](#config)

The server can now be started by running `java -jar climateArchive.jar`

# Running as a service

To have the program running in the background on a linux machine you can use a service. These instructions
are only tested on `ubuntu` but should work on other distributions, maybe with some small modifications

Create the file `/lib/systemd/system/climate_archive.service` and paste the following

```
[Unit]
Description=climate archive data api

[Service]
WorkingDirectory=<path to climateArchive.jar>
ExecStart=java -jar climateArchive.jar
User=<user>

[Install]
WantedBy=multi-user.target
```

Now the service can be started by running

```
systemctl daemon-reload
systemctl start climate_archive.service
```

## Adding models to the server

The server can only access models which have been added to the database. They can be added through the commandline
using the command

```
java -jar climateArchive.jar --add_models --models=<models to add seperated by ",">
```

The separator used can be changed by setting the `model_sep` property in `application.properties`

# Config

The following properties can be specified in the config file.

| Property               | Description                                                                                                                                                                                                                                                     | Default Value |
| ---------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- |
| data_location          | The path to the folder containing all models                                                                                                                                                                                                                    | ./data        |
| model_separator        | The separator used for splitting the list of models                                                                                                                                                                                                             | ,             |
| allowed_cors           | A list of origins which can call the api. They must be seperated by the allowed_cors_separator and formatted according to the [spring documentation](https://spring.getdocs.org/en-US/spring-framework-docs/docs/spring-web-reactive/webflux/webflux-cors.html) | \*            |
| allowed_cors_separator | The separator used for splitting the list of origins in allowed_cors                                                                                                                                                                                            | ,             |
| server.port            | The port that will be used by the api                                                                                                                                                                                                                           | 8080          |

## HTTPS

To enable HTTPS on the server a security certificate must be created.
This must be saved in a keystore in the same directory as the `.jar` file.

Then in the `application.properties` file set the following properties:

| Property                      | Description                                         |
| ----------------------------- | --------------------------------------------------- |
| server.ssl.key-store-type     | The format of the keystore                          |
| server.ssl.key-store          | The path to the keystore containing the certificate |
| server.ssl.key-store-password | The password for the keystore                       |
| server.ssl.key-alias          | The alias mapped to the certificate                 |
| server.ssl.key-password       | The password used to generate the certificate       |
| server.ssl.enabled            | Whether to enable https. Set this to `true`         |

This will now block all requests using HTTP and require HTTPS