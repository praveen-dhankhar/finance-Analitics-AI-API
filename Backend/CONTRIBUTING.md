# Contributing

We welcome contributions to the Finance Forecast Backend!

## Development Workflow

1. **Fork & Branch**: Create a feature branch (`feature/my-feature`) or bugfix branch (`fix/issue-description`).
2. **Code**: Write operations in `src/main/java`. Ensure standard Spring Boot conventions are followed.
3. **Test**: Add unit tests for new logic. Run `./mvnw test` before committing.
4. **Style**: Follow standard Java coding conventions (Google Java Style).
5. **PR**: Submit a Pull Request targeting `main`.

## Standards

- **Controller**: Keep thin. Delegate business logic to services.
- **Service**: Transactional boundaries. Handle exceptions explicitly.
- **DTOs**: Use Records or Lombok @Data classes for IO.
- **Security**: Never expose sensitive entities directly. Use `UserResponseDto`.

## License

MIT License
