# Contributing to Problem4J

Problem4J is released under the [Apache License, Version 2.0][apache-2.0]. By contributing to this project, you agree
that your contributions will be licensed under the same terms.

## How to Contribute

### Opening Issues or Discussions

1. Open an [issue][issues] to report bugs. Please include a clear description and, if possible, a minimal reproducible
   example.
2. Open an issue or start a [discussion][discussions] to ask questions or propose new features.
3. Feel free to participate in existing issues, discussions, or open reviews. Every insight is appreciated.

### Submitting a Pull Request

Before starting work on a feature, consider asking whether it is planned. This project aims to be minimalistic yet
extensible, so early coordination can help avoid unnecessary effort.

1. Rebase or update your fork/branch against the current `main` branch.
2. Squash or clean up your commits to reduce unnecessary noise.
3. If your PR addresses an existing issue, reference it in the description.
4. Include tests when possible and apply formatting using the `./gradlew spotlessApply` task.

Your contribution may be modified during review or merged into a different branch than the original PR. You will remain
the author of your Git commits. Additional changes may be requested before merging.

## Developer Certificate of Origin (DCO)

By submitting a Pull Request or commit, you certify that you have the right to contribute the code under the project's
license (Apache License, Version 2.0), as defined by the [Developer Certificate of Origin][dco].

The DCO sign-off confirms that:

1. You wrote the code or otherwise have permission to submit it.
2. You agree to license your contribution under the [Apache License, Version 2.0][apache-2.0].
3. Project maintainers can safely merge your work without further IP verification.

If a PR contains unsigned commits, you will be asked to amend them.

### How to Sign Off

Add the following line at the end of your commit message:

```txt
Signed-off-by: Your Name <your.email@example.com>
```

If your Git `user.name` and `user.email` are configured, you can add the sign-off automatically:

```bash
git commit -s -m "message"
```

If you prefer not to expose your personal email, you may use your GitHub nickname and/or GitHub-provided noreply
address, for example:

```txt
Signed-off-by: nickname <12345678+nickname@users.noreply.github.com>
```

Repository maintainers and code owners are not required to sign off their commits. For external contributors, every
commit in a PR must include a DCO sign-off.

## Code of Conduct

Please remain respectful, constructive, and considerate of others.

[dco]: https://developercertificate.org/

[discussions]: https://github.com/problem4j/problem4j-spring/discussions

[issues]: https://github.com/problem4j/problem4j-spring/issues

[apache-2.0]: https://www.apache.org/licenses/LICENSE-2.0
