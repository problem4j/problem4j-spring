# Security Policy

## Supported Versions

The following version lines are actively supported:

- `v2.2.x` (`main` branch) - supported versions line for Spring Boot 4,
- `v1.2.x` (`release-v1.2.x` branch) - supported versions line for Spring Boot 3.

## Dependencies

This library does **not** include transitively fixed versions of external dependencies such as:

- Spring Framework / Spring Boot,
- Jackson (`ObjectMapper` and friends).

It is the responsibility of the application using this library to:

- regularly update Spring Boot, Jackson, and other libraries to the latest patched releases,
- ensure that known CVEs are resolved by upgrading their chosen stack.

The maintainers cannot guarantee security if the consuming application uses outdated upstream dependencies.

Because this library does not manage transitive versions for Spring, Jackson, and other dependencies, please do not open
PRs that update those upstream libs. Such updates belong in the consuming application, not here. This project is
designed to remain dependency-light and avoid dictating the user's Spring/Jackson version. This helps to ensure maximum
compatibility and avoids conflicts with application BOMs.

## Reporting a Vulnerability

If you believe you have found a security issue in scope of **Problem4J**, please **do not open a public GitHub Issue**.
Instead, please report the problem via **GitHub Security Advisories**.

Please include:

- version of the library,
- affected dependency versions (if relevant),
- sample code or minimal reproduction,
- details explaining the vulnerability.

## What is *not* considered a security issue

- Misconfiguration in user applications.
- Outdated versions of Spring Boot, Jackson, or other dependencies used by the consuming app.
- Vulnerabilities in upstream libraries not directly caused by this project.
