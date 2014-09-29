name := "fisheries-play"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.1",
  "org.webjars" % "angularjs" % "1.1.5-1",
  "com.google.guava" % "guava" % "14.0.1",
  "com.google.code.findbugs" % "jsr305" % "1.3.+", //http://stackoverflow.com/questions/13162671/missing-dependency-class-javax-annotation-nullable
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings

routesImport += "controllers.ModelConfigUI._,controllers.ModelConfigUI"

templatesImport += "model.HelpText"