name := "scala-grpc-playground"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "io.monix" %% "monix-execution" % "3.3.0"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
)
