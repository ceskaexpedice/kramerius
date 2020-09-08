For build project, we use gradle build tool (version 5.+). For more information, see  http://www.gradle.org/.
You should always use embedded version of gradle, i.e. ./gradlew task NOT gradle task. Because gradle's behaviour can differ between it's versions,
which can result in failure to successfully execute some of the tasks.

All actions should be run from top-level module directory

To build jar with dependencies (i.e. executable with java -jar):
./gradlew search-index:clean  search-index:build

To run:
./gradlew search-index:run --args "ARGUMENTS"
For example:
./gradlew search-index:run --args "build_solr_passord_hash myLogin myPassword"

To run tests:
./gradlew search-index:test

