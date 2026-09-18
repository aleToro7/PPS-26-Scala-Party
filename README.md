# 🚀 ScalaParty

Hi there!
Here you have **✨ScalaParty✨**: a distributed multiplayer space-arcade game built purely in Scala 3.

Basically, _ScalaParty_ is a remake of the legendary _Astro Party_, completely written in Scala3.
This game puts you in the cockpit of a spaceship with a catch: **you can't hit the brakes!**
Your ship moves forward constantly.
You can only control your rotation to dodge walls, blast your friends and be the last ship flying in the arena!

This game is developed as a simple academic project for the _Paradigmi di Programmazione e Sviluppo (PPS)_ course at the University of Bologna.

## 🎮 How to Play (Current Status)

We are currently at the end of **Sprint 1**, meaning the core infrastructure is laid out, but we are still warming up the engines!

To peek into the current state of the game, you can follow these steps:

1. Download the [latest release](https://github.com/aleToro7/PPS-26-Scala-Party/releases)
2. Download the latest [jar file](scalaparty.jar) from the releases page.
3. Run the [jar file](scalaparty.jar) using:
   ```bash
   $ scala -jar scalaparty.jar
   ```
   or, if you don't have Scala installed:
   ```bash
   $ java -jar scalaparty.jar
   ```
4. Open your browser and navigate to [http://localhost:8081](http://localhost:8081) to verify the server is up and running. You should see a simple message saying that ScalaParty server is up and running.
5. Head over to [http://localhost:8081/scalaparty](http://localhost:8081/scalaparty) to connect to the game.

> [!NOTE]
> At this current version, you will be able move your ship around the arena, but you won't be able to see other players or interact with them yet.
> Any match will end after 60 and the next player in line will be able to start a new match.
> It is not possible to play with more than one player or more than one match at the same time at this stage.
> Multiplayer and multi-match support will be implemented in the upcoming sprints :)

![ScalaParty Demo](./doc/img/demo.png)

## Authors

This project is proudly developed by:

- **Alessandro Martini**: @AlleMartins
- **Alessandro Torelli**: @aleToro7
- **Federico Diotallevi**: @DiottaNax
