scalaVersion := "2.12.11"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.1" % "provided"
libraryDependencies += "org.apache.kafka" % "kafka_2.12" % "0.10.2.2" % "provided"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.10.2.2"

mergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
  case x => (mergeStrategy in assembly).value(x)
}