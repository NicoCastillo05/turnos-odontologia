# turnos-odontologia — Claude Instructions

## Project Context

- **Repo:** https://github.com/NicoCastillo05/turnos-odontologia
- **Product:** Dental appointment/scheduling management SaaS for the Argentine market.
- **Stack:** Java 17+ / Spring Boot 3 + H2 (embedded, file mode) + React/Vite PWA.
- **Structure:** `knowledge-base/` (10 files), `CHANGES.md` (roadmap, 14 changes), `.atl/skill-registry.md`.
- **Installed skills:** `java-springboot`, `springboot-patterns`, `springboot-security`.
- **Multi-tenant:** every query/repository must be scoped by `tenant_id`.

## References

- Knowledge base: `knowledge-base/` (10 canonical `.md` files).
- Roadmap / change index: `CHANGES.md`.
- Skill registry: `.atl/skill-registry.md`.

## Hard Rules (10 — non-negotiable)

1. **No build/commit without explicit request.** Only build or commit when the user explicitly asks.
2. **Conventional commits, no AI co-authorship.** Use `feat:`, `fix:`, etc. Never add `Co-Authored-By` lines for the AI.
3. **Never expose secrets** (JWT_SECRET, MercadoPago/WhatsApp credentials) in code. Only via environment variables / `application.yml` with placeholders.
4. **Never use `System.out.println`.** Use SLF4J (`Logger`).
5. **Never mix JPA entities with DTOs in the API.** Separate DTOs per layer; thin controllers, logic in services.
6. **Never omit `tenant_id` in queries/repositories.** Multi-tenant isolation is mandatory.
7. **Never commit schema changes without a numbered migration.** Use Flyway/Liquibase with numbered scripts.
8. **Never use `any` in TypeScript / untyped components.** Strict typing; components in PascalCase.
9. **Never store real patient data in the repo or examples.** All test data must be fictitious. (Patient data is sensitive — handle with extra care.)
10. **Never log clinical or personal patient data.** Logs must be free of PII.

## Patient Data (prominent)

Patient records are sensitive personal/health data. Rules 9 and 10 apply to every change: no real patient data anywhere in the repository (code, tests, fixtures, examples, docs), and no PII in logs. Use clearly fictitious test data only.
