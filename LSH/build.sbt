name := "LSHandRMSE"

version := "1.0"

scalaVersion := "2.10.6"

resolvers++=Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "http://oss.sonatype.org/service/local/staging/deploy/maven2/")

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.spark" % "spark-mllib_2.10" % "2.1.0")

//libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.1"

// https://mvnrepository.com/artifact/com.github.scopt/scopt_2.10
//libraryDependencies += "com.github.scopt" % "scopt_2.10" % "2.1.0"

// https://mvnrepository.com/artifact/com.github.scopt/scopt_2.12
libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"

libraryDependencies += "com.github.karlhigley" %% "spark-neighbors" % "0.2.2"

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

dependencyOverrides += "org.scala-lang" % "scala-reflect" % scalaVersion.value