resolvers ++= Seq(
        "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
        "Sonatype OSS Snapshots Repository" at "http://oss.sonatype.org/content/groups/public",
        "codebrew's maven" at "http://codebrew-io.github.io/maven/",
        Resolver.url(
                "bintray-sbt-plugin-releases",
                url("http://dl.bintray.com/content/sbt/sbt-plugin-releases")
        )(Resolver.ivyStylePatterns)
)

addSbtPlugin("com.untyped" %% "sbt-js" % "0.6")

addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "3.9.0")
