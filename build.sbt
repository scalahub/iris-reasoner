// externalPom()

name := "iris-reasoner"

version := "0.1"

scalaVersion := "2.12.8"

lazy val api = (project in file("iris-api")).settings(
  libraryDependencies += "org.jgrapht" % "jgrapht-core" % "0.9.1",
  libraryDependencies += "junit"             %  "junit"       % "4.12"        % Test

)

lazy val parser = (project in file("iris-parser")).settings(
  //  libraryDependencies += "junit"             %  "junit"       % "4.12"        % Test,
  //  includeFilter in (Compile, unmanagedSources) := "parser.dat",
  //  includeFilter in (Compile, unmanagedSources) := "lexer.dat",
  mappings in (Compile, packageBin) += {
    (baseDirectory.value / "src" / "main" / "java" / "org" / "deri" / "iris" / "parser" / "parser" / "parser.dat") ->
    "org/deri/iris/parser/parser/parser.dat"
  },
  mappings in (Compile, packageBin) += {
    (baseDirectory.value / "src" / "main" / "java" / "org" / "deri" / "iris" / "parser" / "lexer" / "lexer.dat") ->
    "org/deri/iris/parser/lexer/lexer.dat"
  }

)

lazy val impl = (project in file("iris-impl")).dependsOn(api, parser).settings(
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.26",
  libraryDependencies += "junit"             %  "junit"       % "4.12"        % Test
)
lazy val rdb = (project in file("iris-rdb")).settings(
  libraryDependencies += "commons-io" % "commons-io" % "2.6",
  libraryDependencies += "commons-cli" % "commons-cli" % "1.4",
  libraryDependencies += "junit"             %  "junit"       % "4.12"        % Test

).dependsOn(api, impl)

// aggregate: running a task on the aggregate project will also run it on the aggregated projects.
// dependsOn: a project depends on code in another project.
lazy val root = (project in file(".")).aggregate(
  api, 
  impl,
  parser,
  rdb
).dependsOn(impl, parser, rdb).settings(
  libraryDependencies += "junit"             %  "junit"       % "4.12"        % Test
)

