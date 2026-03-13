## SPEC-REFACTOR-001 Progress

- Started: 2026-03-12
- Completed: 2026-03-13
- Status: COMPLETE
- Development Mode: DDD (ANALYZE-PRESERVE-IMPROVE)
- Git Mode: personal (auto_branch: false)

## Summary

Pragmatic clean architecture refactoring (reduced scope: 29 files):

- Phase 1: JWT/Redis moved from tmk-core to tmk-api
- Phase A: JpaRepository → Port interface wiring (UserPort, ExamPort, EmailVerificationPort, DocumentPort, DocumentChunkPort, QuestionPort)
- Phase B: PasswordEncoderPort interface to decouple Spring Security from tmk-core
- Phase C: @Service + dependency injection on all domain services (TODO stubs for business logic)
- Phase D: UseCase TODO stubs with correct field injection
- Phase E: Batch job TODO stubs with wired dependencies
