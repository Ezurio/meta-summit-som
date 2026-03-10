# Copilot Rules for meta-summit-som

This repository is a Yocto/OpenEmbedded layer. Prefer minimal, reviewable changes that follow existing layer patterns.

## Scope and style

- Keep changes scoped to the requested feature or fix.
- Follow existing naming, formatting, and directory conventions in this layer.
- Avoid large refactors unless explicitly requested.
- Preserve backward compatibility for existing machines and distro configs.

## Yocto and BitBake rules

- Use `.bbappend` when extending upstream recipes; avoid copying full recipes unless required.
- Keep recipe metadata deterministic and reproducible. Do not add network-dependent build steps.
- Prefer overrides and `PACKAGECONFIG`-based toggles instead of hardcoded machine-specific logic.
- Use `${}` variable expansion and standard BitBake patterns used in neighboring recipes.
- Keep `SRC_URI` additions explicit and checksum-pinned when applicable.
- When modifying install/package behavior, verify `FILES`, `RDEPENDS`, `RRECOMMENDS`, and `SYSTEMD_*` consistency.

## Patches

- Put patches in the recipe-specific files directory next to the corresponding `.bb`/`.bbappend`.
- Keep patches minimal and upstreamable when possible.
- Include clear patch subjects and rationale in patch headers.
- Do not reformat unrelated code in patches.

## Images and packagegroups

- For image changes, prefer packagegroups over direct package lists when that pattern already exists.
- Keep image feature changes intentional and documented in the recipe commit message context.
- Avoid introducing debug or development packages into production images unless requested.

## Machine and distro config

- Preserve current behavior across NXP and TI paths unless the request is platform-specific.
- For `conf/machine` and `conf/distro` edits, keep secure and non-secure variants aligned unless divergence is required.
- Avoid changing defaults that impact all builds without explicit user request.

## Security and update flow

- Be conservative in `swupdate`, provisioning, and secure boot related recipes and configs.
- Do not weaken security settings, key handling, or verification flows unless explicitly requested.
- Call out any change that affects boot chain, signing, OTA behavior, or partition layout.

## Validation expectations

When making code changes, prefer validating with the smallest relevant scope first:

- `bitbake-layers show-layers`
- `bitbake <affected-recipe> -c fetch`
- `bitbake <affected-recipe>`
- Image build only when required by the change

If validation cannot be run, state what was not run and why.
