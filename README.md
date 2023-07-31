# tuples-space-server

[![Build status](https://github.com/cake-lier/tuples-space-server/actions/workflows/release.yml/badge.svg)](https://github.com/cake-lier/tuples-space-server/actions/workflows/release.yml)
[![semantic-release: conventional-commits](https://img.shields.io/badge/semantic--release-conventional_commits-e10098?logo=semantic-release)](https://github.com/semantic-release/semantic-release)
[![GitHub](https://img.shields.io/github/license/cake-lier/tuples-space-server)](https://github.com/cake-lier/tuples-space-server/blob/main/LICENSE.md)
[![Docker Image Version](https://img.shields.io/docker/v/matteocastellucci3/tuples-space-server?label=docker%20hub)](https://hub.docker.com/r/matteocastellucci3/tuples-space-server)
[![Scaladoc](https://img.shields.io/github/v/release/cake-lier/tuples-space-server?label=scaladoc)](https://cake-lier.github.io/tuples-space-server/io/github/cake-lier)
[![Issues](https://img.shields.io/github/issues/cake-lier/tuples-space-server)](https://github.com/cake-lier/tuples-space-server/issues)
[![Pull requests](https://img.shields.io/github/issues-pr/cake-lier/tuples-space-server)](https://github.com/cake-lier/tuples-space-server/pulls)
[![Codecov](https://codecov.io/gh/cake-lier/tuples-space-server/branch/main/graph/badge.svg?token=UX36N6CU78)](https://codecov.io/gh/cake-lier/tuples-space-server)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=cake-lier_tuples-space-server&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=cake-lier_tuples-space-server)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=cake-lier_tuples-space-server&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=cake-lier_tuples-space-server)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=cake-lier_tuples-space-server&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=cake-lier_tuples-space-server)

## How to use

Build a new container from the latest version of the Docker image for this server using:

```bash
docker run -p 80:80 matteocastellucci3/tuples-space-server
```

When the server has booted up, it will be available at `ws://localhost/tuplespace`.

You can change the port on which the server is listening and the path at which the service is available using:

```bash
docker run -p 8080:8080 -e TUPLES_SPACE_PORT_NUMBER=8080 -e TUPLES_SPACE_SERVICE_PATH= matteocastellucci3/tuples-space-server
```

In this second example, the server will be available at `ws://localhost:8080`.

## What is this?

This application is the server of a bigger project which allows to create tuple spaces easily and reliably. A tuple
space is a mean to exchange pieces of information between parties while at the same time coordinating the actions of the parties that
need those pieces of information. For example, an entity could suspend its job while waiting for the information to be available,
not differently from how a future-based computation works. When it is available, it can carry on from where it left off. The idea of
a tuple space, which very aptly is a "coordination medium", is to bring this onto distributed systems, which are by definition
more challenging to model, coordinate and in general make work. If you are familiar with some message-oriented middleware, such
as RabbitMQ, this is not different, with the added twist that not only we can send and receive messages, but also wait
for them, decide whether remove them from the pool of messages, probe for their presence or absence etc. A tuple space is just a
big pool of messages, waiting to be read from someone or probed or whatever. Differently from RabbitMQ, we just don't subscribe to
topics, because every receive operation is intended to receive one and only one message.

This repo contains only the server part of this project: the server to which your apps communicate. The actual operations are
discussed in the repo which hosts the client for interacting with the tuple space, you can find it [here](https://github.com/cake-lier/tuples-space-client). Another repo exists which contains the core elements, such as tuples and templates, and it's [here](https://github.com/cake-lier/tuples-space-core).

## What should I do with this server?

Well, you should install it. If you are using some kind of automation tool for deploying your system, such as Docker Compose, you can specify in your `docker-compose.yml` to install it while deploying. Then, you must simply include the client in the app you are developing and you can start using it!

## Can I use it?

Of course, the MIT license is applied to this whole project.
