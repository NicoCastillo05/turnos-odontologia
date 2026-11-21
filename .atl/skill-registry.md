# Skill Registry

**Delegator use only.** Any agent that launches sub-agents reads this registry to resolve compact rules, then injects them directly into sub-agent prompts. Sub-agents do NOT read this registry or individual SKILL.md files.

## User Skills

| Trigger | Skill | Path |
|---------|-------|------|
| /active-orchestrator:init or start/resume foundation | active-orchestrator | C:\Users\MegaTecnologia\.config\opencode\skills\active-orchestrator\SKILL.md |
| create/update AGENTS.md or CLAUDE.md | agent-instruction | C:\Users\MegaTecnologia\.config\opencode\skills\agent-instruction\SKILL.md |
| define or audit visual identity during foundation | design-system | C:\Users\MegaTecnologia\.config\opencode\skills\design-system\SKILL.md |
| original v1 taste-skill exact backward compat | design-taste-frontend-v1 | C:\Users\MegaTecnologia\.config\opencode\skills\design-taste-frontend-v1\SKILL.md |
| discovery/investigation before KB | discovery-research | C:\Users\MegaTecnologia\.config\opencode\skills\discovery-research\SKILL.md |
| UI polish, component design, animation details | emil-design-eng | C:\Users\MegaTecnologia\.config\opencode\skills\emil-design-eng\SKILL.md |
| find/install skills | find-skills | C:\Users\MegaTecnologia\.config\opencode\skills\find-skill\SKILL.md |
| build project knowledge base | kb-creator | C:\Users\MegaTecnologia\.config\opencode\skills\kb-creator\SKILL.md |
| create/update CHANGES.md roadmap | roadmap-generator | C:\Users\MegaTecnologia\.config\opencode\skills\roadmap-generator\SKILL.md |
| create/edit/optimize skills | skill-creator | C:\Users\MegaTecnologia\.config\opencode\skills\skill-creator\SKILL.md |
| scrape competitor from URL | web-scraper | C:\Users\MegaTecnologia\.config\opencode\skills\web-scraper\SKILL.md |

## Project Skills

| Trigger | Skill | Path |
|---------|-------|------|
| Write/review Spring Boot 3/4 application code | java-springboot | C:\Users\MegaTecnologia\Desktop\UTN\Desarrollo de Software\Global Desarrollo\.agents\skills\java-springboot\SKILL.md |
| Spring Boot backend architecture, REST, data access, caching, async | springboot-patterns | C:\Users\MegaTecnologia\Desktop\UTN\Desarrollo de Software\Global Desarrollo\.agents\skills\springboot-patterns\SKILL.md |
| Spring Security authn/authz, validation, CSRF, secrets, headers | springboot-security | C:\Users\MegaTecnologia\Desktop\UTN\Desarrollo de Software\Global Desarrollo\.agents\skills\springboot-security\SKILL.md |

## Compact Rules

### java-springboot
- Detect Spring Boot version, Java version, starters, persistence, package layout from pom/build; never assume.
- Never upgrade Spring Boot/Java/deps implicitly; Boot 3->4 is a deliberate task.
- Ask before adding infrastructure (broker, cache, DB, scheduler); add only what requested with Boot-managed versions.
- Verify every change with project build (./mvnw verify or ./gradlew build).
- Prefer ProblemDetail for errors, validation annotations, Spring Data JPA/JDBC, RestClient, config properties.

### springboot-patterns
- Layered architecture: controllers thin, services hold logic, repositories data access.
- REST design with proper HTTP verbs/status codes; DTOs over entities.
- Caching via @Cacheable with explicit cache names; async via @Async with executor config.
- Logging with SLF4J, structured, no sensitive data.

### springboot-security
- Enforce authn/authz with method security; validate all input.
- CSRF protection for state-changing requests; secure headers; secrets via env/config not code.
- Rate limiting on sensitive endpoints; keep dependencies updated.

### active-orchestrator
- Coordinate project foundation phases; reuse existing artifacts; delegate phases and resume safely.

### design-system
- Check visual_design.mode in .active-orchestrator-state.json and design-system/README.md Mode before mockups.

### kb-creator
- Build 10 canonical .md files at knowledge-base/; silent from docs/ or interactive.

### roadmap-generator
- Generate CHANGES.md operational index with dependency tree and per-change scope.

## Project Conventions

| File | Path | Notes |
|------|------|-------|
| (none found) | | No AGENTS.md/CLAUDE.md/.cursorrules at project root |

Read the convention files listed above for project-specific patterns and rules.
