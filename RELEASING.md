# Releasing

1. Update `CHANGELOG.md` - change `[Unreleased]` to `[X.Y.Z] - YYYY-MM-DD` and add a new `[Unreleased]` section on top.
2. Update `version` property in `gradle.properties` to the new version.
3. Commit the changes from (1) and (2) with a message `"Release X.Y.Z"` and push to GitHub.
4. Create an annotated git tag named `vX.Y.Z` with the message `"Release X.Y.Z"` and push it to GitHub. This will
   trigger the release workflow, which will build and publish the artifacts to Sonatype repository. You can use the
   [`./tools/tagrelease`](./tools/tagrelease) script to ensure the tag is correctly formatted and prevent mistakes.
5. Update `version` property in `gradle.properties` to the next snapshot version, for example `1.3.1-SNAPSHOT`.
6. Commit the change from (5) with a message `"Update snapshot version"` and push to GitHub.
7. If the release was made on a maintenance branch, make sure to `merge` or `cherry-pick` the `CHANGELOG.md` entry to
   the `main` branch as well.

## Maven Central

[![Publish Release Status](https://github.com/problem4j/problem4j-spring/actions/workflows/gradle-publish-release.yml/badge.svg)][gradle-publish-release]
[![Sonatype](https://img.shields.io/maven-central/v/io.github.problem4j/problem4j-spring-bom)][maven-central]

1. Keep Git tags with `vX.Y.Z-suffix` format. GitHub Actions job will only trigger on such tags and will remove `v`
   prefix.
2. The releasing procedure only uploads the artifacts to Sonatype repository. You need to manually log in to Sonatype to
   push the artifacts to Maven Central.

See [`gradle-publish-release.yml`][gradle-publish-release.yml] for publishing release versions instructions.

Set the following environment variables in your CI/CD (GitHub Actions, etc.):

```txt
# generated tokens on Sonatype account, do not use real username/password
PUBLISHING_USERNAME=<username>
PUBLISHING_PASSWORD=<password>

# generated PGP key for signing artifacts
SIGNING_KEY=<PGP key>
SIGNING_PASSWORD=<PGP password>
```

Artifacts are published to Maven Central via Sonatype, using following Gradle task.

```bash
./gradlew -Pversion=<version> -Psign publishAggregationToCentralPortal
```

This command uses `nmcp` Gradle plugin - [link](https://github.com/GradleUp/nmcp).

[gradle-publish-release]: https://github.com/problem4j/problem4j-spring/actions/workflows/gradle-publish-release.yml

[gradle-publish-release.yml]: .github/workflows/gradle-publish-release.yml

[maven-central]: https://central.sonatype.com/namespace/io.github.problem4j
