# Real-Time Chat System

> Scalable WebSocket chat with STOMP + Redis pub/sub — supports horizontal scaling across multiple server instances with live presence tracking and persistent message history.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue)](https://stomp.github.io/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)

---

## Live Demo

**[chat.eswarr-dasi.dev](#)** — try it live (link updated after deployment)

---

## Problem

Real-time chat systems are deceptively hard to scale. A naive WebSocket server holds every connection in memory — the moment you run more than one server instance (for load balancing or high availability), users on different instances can no longer see each other's messages.

## Solution

This system uses **Redis Pub/Sub** as a message broker between server instances. Any server instance can broadcast a message, and Redis fans it out to every other instance, which then delivers it to its own locally-connected WebSocket clients. This means the system scales horizontally — add more instances behind a load balancer with zero code changes.

## Architecture

```
 Client A ──WS──┐                      ┌── WS── Client C
 Client B ──WS──┤                      ├── WS── Client D
                │                      │
          [Server Instance 1]    [Server Instance 2]
                │                      │
                └──────► Redis Pub/Sub ◄──────┘
                         (message relay)
                              │
                              ▼
                        PostgreSQL
                    (message persistence)
```

---

## Features

- [x] WebSocket + STOMP protocol for real-time bidirectional messaging
- [x] Redis Pub/Sub message relay for horizontal scaling across instances
- [x] Chat rooms / channels with join/leave events
- [x] Live presence tracking (online, offline, typing indicators)
- [x] Message persistence with cursor-based pagination (load older history)
- [x] JWT-based authentication on WebSocket handshake
- [x] Rate limiting per connection to prevent spam/flooding
- [x] REST API for room management and message history
- [x] Simple web client (vanilla JS) for live demo

---

## Tech Stack

- **Java 17** + **Spring Boot 3.2** — core application
- **Spring WebSocket + STOMP** — real-time messaging protocol
- **Redis** — pub/sub message broker + presence store
- **PostgreSQL** — message and room persistence
- **JWT** — WebSocket handshake authentication
- **Docker Compose** — full local multi-instance stack
- **Render Blueprint** — one-click cloud deployment

---

## Quick Start (Local)

```bash
git clone https://github.com/eswarr-dasi/realtime-chat-system.git
cd realtime-chat-system
docker-compose up -d
```

Then open `http://localhost:8080` in two different browser tabs and start chatting.

## Testing Horizontal Scaling Locally

```bash
docker-compose up -d --scale chat-server=3
```

Connect to different instances (via the load balancer on port 80) and confirm messages relay correctly between them through Redis.

---

## REST API

```
POST /api/rooms                      # create a chat room
GET  /api/rooms/{roomId}/messages    # paginated message history
GET  /api/rooms/{roomId}/presence    # who's currently online
POST /api/auth/login                 # get JWT for WebSocket handshake
```

## WebSocket Endpoints (STOMP)

```
CONNECT   /ws                        # WebSocket handshake (JWT in header)
SUBSCRIBE /topic/room.{roomId}       # receive messages for a room
SEND      /app/chat.send.{roomId}    # send a message to a room
SEND      /app/chat.typing.{roomId}  # broadcast typing indicator
```

---

## Author

**Eswarr Dasi** — Software Engineer II | Distributed Systems · Java · Spring Boot · AWS

[Portfolio](https://eswarr-dasi.github.io) · [LinkedIn](https://linkedin.com/in/eswarr-dasi) · [GitHub](https://github.com/eswarr-dasi)
