name := "iris-reasoner"

version := "0.1"

scalaVersion := "2.12.8"

lazy val root = (project in file(".")).settings(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "iris-api/src/main",
  unmanagedSourceDirectories in Compile += baseDirectory.value / "iris-parser/src/main",
  unmanagedSourceDirectories in Compile += baseDirectory.value / "iris-impl/src/main",
  unmanagedSourceDirectories in Compile += baseDirectory.value / "iris-rdb/src/main",
  libraryDependencies += "org.jgrapht" % "jgrapht-core" % "0.9.1",
  libraryDependencies += "junit" % "junit" % "4.12" % Test,
  libraryDependencies += "commons-io" % "commons-io" % "2.6",
  libraryDependencies += "commons-cli" % "commons-cli" % "1.4",
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.26",
  mappings in (Compile, packageBin) += {
    (baseDirectory.value / "iris-parser" / "src" / "main" / "java" / "org" / "deri" / "iris" / "parser" / "parser" / "parser.dat") ->
    "org/deri/iris/parser/parser/parser.dat"
  },
  mappings in (Compile, packageBin) += {
    (baseDirectory.value / "iris-parser" / "src" / "main" / "java" / "org" / "deri" / "iris" / "parser" / "lexer" / "lexer.dat") ->
    "org/deri/iris/parser/lexer/lexer.dat"
  }
)
