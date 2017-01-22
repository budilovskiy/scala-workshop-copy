name := "eslc"

version := "1.0"

scalaVersion := "2.12.0"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
resolvers += "Eventuate Releases" at "https://dl.bintray.com/rbmhtechnology/maven"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.14"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.0"

libraryDependencies += "com.rbmhtechnology" %% "eventuate-core" % "0.8.1"
libraryDependencies += "com.rbmhtechnology" %% "eventuate-log-leveldb" % "0.8.1"
libraryDependencies += "com.rbmhtechnology" %% "eventuate-log-cassandra" % "0.8.1"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"