# AsmJit Readiness Plan — android-widget-bar

Status: PREPARED_ONLY
Branch: `prep/asmjit-readiness-2026-09-19`
Decision: DO NOT INTEGRATE under current scope.

## Rationale
Android widget/UI/application behavior has no demonstrated need for runtime native machine-code generation.

## Preferred optimization path
Profile UI composition, Android lifecycle, IPC, startup, rendering, allocations, and platform APIs. Use ordinary Kotlin/Java/Rust/native code only where justified.

## Revisit trigger
A separate native compute subsystem with dynamic code generation and benchmark evidence.

No implementation, dependency addition, PR, or merge on this branch.
