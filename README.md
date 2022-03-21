# 2021-ClimateArchive

Overview:

    The goal of this project is to give researchers easy access to complex climate data with the climatearchive.org web
    application as a front end and our software solution as the backend to provide access to the data. The website
    currently visualises the research data in 3D, however quantitative access to the underlying data is needed to improve
    interdisciplinary research. We will provide a solution which filters the database and produces smaller data subsets
    which could then be downloaded/visualised from the browser.

Stakeholders:

    The typical stakeholders for this project will be researchers, or other people who may be interested in the data,
    who may not be particularly tech savy.

    Researchers
    Individulas interested in the data

    Sebastian
    Coders (us)

  User Stories :

    A researcher / individual interested in the data, I should be able to:
        -select a point on the globe on the alreadyprovided climate change model in order to extract point data for a selected location and time period.
        -view and compare graphs generated from comparing these data points.
        -download datasets generated from certain parameters
        -share datasets/graphs generated at specific points with other individuals
    
Note: we are mainly working on our client's private repository to add features to climatearchive.org and on a backend server to provide access to underlying climate data.


Usage
=====

The server can be downloaded from the releases tab or built from source.

Building from source
--------------------

Download the source code using

```
git clone https://github.com/spe-uob/2021-ClimateArchive.git
```

Compile into a jar file using

```
cd 2021-ClimateArchive

mvn package -DskipTests
```

This will save ```climateArchive.jar``` in the ```target``` directory

Running
-------

First place ```climateArchive.jar``` into your chosen directory.

Create a config folder using in the same directory with ```mkdir config```

Create the file ```application.properties``` in the ```config``` file

In this file enter the config

```
data_location=<path to climate data>
```

The server can now be started by running ```java -jar climateArchive.jar```


Running as a service
--------------------

To have the program running in the background on a linux machine you can use a service. These instructions
are only tested on ```ubuntu``` but should work on other distributions, maybe with some small modifications

Create the file ```/lib/systemd/system/climate_archive.service``` and  paste the following

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



Adding models to the server
---------------------------
The server can only access models which have been added to the database. They can be added through the commandline
using the command

```
java -jar climateArchive.jar --add_models --models=<models to add seperated by ",">
```

The separator used can be changed by setting the ```model_sep``` property in ```application.properties```