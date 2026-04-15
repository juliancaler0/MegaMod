I have all the information I need. I'll now deliver the full plan.

---

# MegaMod EMF/ETF Full-Parity Port — Implementation Plan

## Executive Summary Table

| Phase | Scope | Risk | Key Output |
|---|---|---|---|
| 0 — Pre-Port Audit | 2–4 h | L | Mixin overlap map, salvage list |
| 1 — Layout Strategy | 1 h | L | Package/path decision doc |
| 2 — Delete Partial Port | 30 m | **M** (destructive) | Clean `lib/emf`, `lib/etf` trees |
| 3 — Bulk Copy & Preprocess | 4–8 h | **H** (preprocessor stripping) | ~200 ported .java + 2 mixin configs |
| 4 — Self-Identification Cleanup | 1–2 h | L | mod-id refs replaced/stripped |
| 5 — Mixin Config Rebuild | 1 h | L | 57+40 entries registered |
| 6 — Mixin Conflict Resolution | 2–3 h | **M** | Priority annotations + merges |
| 7 — Entry-Point Wiring | 1–2 h | L | init calls in MegaModClient |
| 8 — Config UI Integration | 2 h (keep standalone) | L | Keybind-opens-screen |
| 9 — Build & Dependencies | 2 h | **M** (tconfig, accesswidener→AT) | build.gradle updates |
| 10 — Compile & Smoke Test | 2–6 h | **H** | runClient + FA pack loads |
| 11 — Risks & Rollback | — | — | Branch strategy |

**Total:** ~20–35 h of focused work, with two-thirds of the risk concentrated in Phase 3 (preprocessor stripping) and Phase 10 (first-boot correctness).

---

## Decisions required from user BEFORE execution

These block other work — resolve first:

1. **D1. `tconfig` dependency policy.** Source's `EMFConfig`, `ETFConfig`, `EMFConfigScreen`, `ETFConfigScreenMain` all extend `traben.tconfig.TConfig` and use `traben.tconfig.gui.entries.*`. Pick one:
   - **D1a (recommended):** Port `tconfig` as a third `lib/` tree (`com.ultra.megamod.lib.tconfig`) — cleanest, fully offline, ~15–25 classes. **+4–6 h work.**
   - **D1b:** Rewrite EMFConfig/ETFConfig as plain POJOs backed by NeoForge's `ModConfig` system; rebuild screens with `ConfigurationScreen`. **+8–12 h, larger deviation, harder to merge future upstream fixes.**
   - **D1c:** Add Traben's tconfig as a maven dependency. Fastest if available, but no known public maven for it (source ships it via Essential Gradle Toolkit / Jitpack).
2. **D2. Config-screen integration policy** (Phase 8). Standalone keybind (recommended) vs. nested inside MegaMod settings.
3. **D3. Mod-id surface policy** (Phase 4). Strip all `entity_model_features`/`entity_texture_features` id references and serve as `megamod`, OR keep legacy id strings for pack-author muscle memory (resource-pack paths `optifine/cem/` are untouched regardless — this is only about log tags and `ModList.isLoaded` calls).
4. **D4. Branch policy.** Work on `feature/emf-etf-full-port` branch with a pinned pre-port commit tagged `pre-emf-etf-port`.

---

## Phase 0 — Pre-Port Audit

### Goal
Produce a complete inventory of the current state: (a) every mixin target touched by existing MegaMod code, (b) every source-file-level dependency from non-EMF/ETF code into the partial port, (c) every overlap between source-port's mixin targets and MegaMod's existing ones.

### Concrete actions

**0.1. Enumerate existing MegaMod mixins and their targets.**
Run Grep across `src/main/java/com/ultra/megamod/mixin/` for `@Mixin\(`. Output a table with columns `{mixin file, @Mixin target class, methods injected}`. The Grep output I already produced confirms the classes below are targeted by MegaMod mixins and will **overlap** with ported EMF/ETF:

| Target class | MegaMod mixin | EMF source touches? | ETF source touches? | Resolution class (Phase 6) |
|---|---|---|---|---|
| `GameRenderer` | `GameRendererMixin` (cancels `bobView`) | YES (`MixinGameRenderer`) | NO | A — coexist, different methods |
| `EntityRenderer` | `EntityRendererMixin` (forces nametag) | NO | YES (`MixinEntityRenderer`) | A — coexist, different methods |
| `LightTexture` | `LightTextureMixin` (fullbright) | NO | NO | A — no-op |
| `LivingEntity` | `LivingEntityMixin` (BC knockback) | NO | NO (ETF touches `MixinEntity` parent) | A — no-op |
| `ItemEntityRenderer` | `ItemEntityRendererMixin` (item physics) | NO | NO | A — no-op |
| `Minecraft` | `MinecraftMixin` (BC attack controller) | NO (ETF has `MixinMinecraftClient` for reload) | YES | **B — coexist, different methods; document order** |
| `LevelRenderer` | `LevelRendererMixin` | NO | NO | A — no-op |
| `Entity` | — | YES (`MixinEntity`) | YES (`MixinEntity`) | **C — EMF and ETF both target this; keep both, different methods** |
| `BlockEntity` | — | YES (`MixinBlockEntity`) | YES (`MixinBlockEntity`) | **C — coexist, document** |
| `EntityRenderDispatcher` | — | YES | YES | **C — coexist, different methods; document** |

All A-cases: **no action needed** beyond adding `@Mixin(priority = 1500)` defensively on MegaMod's mixins (default is 1000 — nudging higher makes MegaMod's cancel-returns fire first).
B-case: verify `MinecraftMixin` in both targets the `Minecraft.setScreen`/`tick` vs. ETF's `reload` — they should be disjoint method sets. Document ordering.
C-cases: same-class-different-method overlaps; they coexist cleanly.

**0.2. Enumerate external callsites into the partial port.**
Run Grep for `com.ultra.megamod.lib.emf`, `com.ultra.megamod.lib.etf`, `EmfModelManager`, `EmfActiveModel`, `ETFManager` outside `lib/emf/` and `lib/etf/`. The grep I ran shows **only `MegaModClient.java` and two files inside the port itself (`ETFEmissiveFeatureLayer`, which is also inside lib/etf)** reference the partial API. So the external attack surface is:

- `MegaModClient.java` lines 180–185 (reload listener registration for `etf`, `emf`)
- `MegaModClient.java` lines 191–192 (ETFManager eager init)
- `MegaModClient.java` lines 198–207 (EMF eager init, config, texture redirect, API register, keybind, debug HUD)
- `MegaModClient.java` line 210–213 (ETF keybind, emissive layer, debug HUD)

No other feature module depends on the partial port. Migration is isolated to `MegaModClient.java`.

**0.3. Catalogue the current partial-port files.**
From the Glob I ran there are:
- EMF: 65 .java files under `lib/emf/` — all salvage=NO (will be replaced with the full 90-class upstream tree). Keep exactly zero file, delete entire directory.
- ETF: 125 .java files under `lib/etf/` — same verdict, all delete.

Exception — **only** `ETFEmissiveFeatureLayer.java` (under `lib/etf/features/`) is an integration glue class that was hand-written by us (not upstream). Check it:
- If it mirrors upstream's `ETFElytraFeatureRenderer` layer-add logic: delete and use upstream's.
- If it adds MegaMod-specific emissive hook (e.g., a player-layer wiring not in upstream): **salvage** — move its contents into a new `com.ultra.megamod.feature.emfetf.ETFEmissiveFeatureLayer` (so the file lives *outside* `lib/etf/` and cannot be clobbered by the bulk copy).

**0.4. `megamod.mixins.json` loading-order decision.**
`neoforge.mods.toml` currently lists configs in this order:
```
megamod.mixins.json → playeranim → azurelib → accessories → spellengine → megamod-etf → megamod-emf
```
EMF depends on ETF at runtime (`EMF.java` imports `traben.entity_texture_features.ETF`, and `EMFConfig` imports ETF config). **Retain current order: ETF before EMF.** Also retain `megamod.mixins.json` first — so MegaMod's own high-priority renderer mixins get class-loaded before EMF/ETF try to inject into the same classes.

### Files touched
Documentation-only in this phase — no file writes. Record audit findings in a Phase-0 notebook/wiki section.

### Risks
- Undercounting external callsites. Mitigation: before Phase 2's deletions, re-run the grep with the patterns `EmfActiveModel|EmfModelManager|EMFManager|EMFConfig|ETFManager|ETFConfig|ETFRenderContext|EMFApi|ETFApi` restricted to paths that do **not** start with `lib/emf` or `lib/etf`.

### Verification
After the audit, answer: "If I delete `lib/emf/` and `lib/etf/` entirely right now, what breaks in MegaMod?" Expected answer: `MegaModClient.java` (4 identified blocks) — nothing else.

---

## Phase 1 — Directory & Package Layout Strategy

### Goal
Lock the final directory and package structure before any copy happens.

### Decisions

**1.1. Source-tree structure.** Upstream is a `gg.essential.multi-version` multi-version project. The `src/main/java/traben/entity_model_features/` tree is the **canonical source**. The `versions/*/` directories contain only `gradle.properties` — they receive generated code at build time, not authored code. Conclusion: **copy from `src/main/java/traben/...` only**. There is no `mainProject`-vs-`1.21.11-neoforge` merge to perform.

**1.2. Package remap (EMF).** Flatten to a single tree:
```
traben.entity_model_features               →  com.ultra.megamod.lib.emf
traben.entity_model_features.mixin         →  com.ultra.megamod.lib.emf.mixin
traben.entity_model_features.mixin.mixins  →  com.ultra.megamod.lib.emf.mixin.mixins
traben.entity_model_features.mixin.Plugin  →  com.ultra.megamod.lib.emf.mixin.Plugin
traben.entity_model_features.models        →  com.ultra.megamod.lib.emf.models
traben.entity_model_features.config        →  com.ultra.megamod.lib.emf.config
traben.entity_model_features.utils         →  com.ultra.megamod.lib.emf.utils
traben.entity_model_features.mod_compat    →  com.ultra.megamod.lib.emf.mod_compat
```
Do **not** flatten `mixin.mixins` to just `mixin` — keeping the nesting makes the mixin-config `package` field (`com.ultra.megamod.lib.emf.mixin.mixins`) cleanly distinct from the Plugin class (`com.ultra.megamod.lib.emf.mixin.Plugin`), matching source's topology exactly.

**1.3. Package remap (ETF).** Same pattern:
```
traben.entity_texture_features             →  com.ultra.megamod.lib.etf
traben.entity_texture_features.mixin       →  com.ultra.megamod.lib.etf.mixin
traben.entity_texture_features.mixin.mixins→  com.ultra.megamod.lib.etf.mixin.mixins
traben.entity_texture_features.mixin.Plugin→  com.ultra.megamod.lib.etf.mixin.Plugin
(all feature/config/utils subpackages likewise under com.ultra.megamod.lib.etf)
```

**1.4. `tconfig` package decision** (per D1a). If porting as source:
```
traben.tconfig → com.ultra.megamod.lib.tconfig
```
Place under `src/main/java/com/ultra/megamod/lib/tconfig/`. EMF + ETF config code will then import from there.

**1.5. Mixin Plugin class locations.**
- `com.ultra.megamod.lib.emf.mixin.Plugin`
- `com.ultra.megamod.lib.etf.mixin.Plugin`

Both Plugins must:
1. Call `MixinExtrasBootstrap.init()` in `onLoad`.
2. Gate `shouldApplyMixin` on target-class existence for:
   - Sodium compat → detect `net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer` (1.21.11 class). Source's check is stale (`me.jellysquid...`) — **update** to Caffeine Mods path.
   - Iris compat → detect `net.irisshaders.iris.renderers.WrappedRenderType` (1.21.11 path). Source has both "new" (`mods.iris.*`) and "old" (`mods.iris.old.*`) variants; gate them so only the matching set applies given the installed Iris version. In practice for 1.21.11 the "new" variant is active.
   - Skin Layers 3D compat → detect `dev.tr7zw.skinlayers3d.render.CustomizableModelPart`.
   - ImmediatelyFast compat → detect `net.raphimc.immediatelyfast.feature.core.BatchableBufferSource`.
3. In EMF's Plugin: detect Iris shadow-pass (used by `IrisShadowPassDetection`) — actually that's a runtime class lookup, not a mixin gate. No action in Plugin, runtime class does its own `ModList.isLoaded` check.

**1.6. Resource file layout.**
- **PRESERVE AS-IS (pack-facing — do not move):** Not in mod resources. Resource packs write to `<pack>/assets/<mod>/optifine/cem/*.jem`, `<pack>/assets/<mod>/optifine/random/entity/*.properties`, etc. EMF's `EMFDirectoryHandler` scans **all** mod namespaces. No mod-side change needed.
- **Move under `assets/megamod/`:** upstream lang files, icons.
  - `assets/entity_model_features/lang/en_us.json` → `assets/megamod/lang/en_us.json` **merged with existing**. Prefix all keys that don't already conflict: `emf.<original_key>` and `etf.<original_key>`. This avoids name collisions and makes `megamod.mixins.json` localisation self-contained.
  - `assets/entity_model_features/textures/gui/icon.png` → `assets/megamod/textures/gui/emf/icon.png`.
  - `assets/entity_texture_features/textures/gui/*.png` → `assets/megamod/textures/gui/etf/`.
  - `assets/entity_features/textures/gui/*.png` (ETF auxiliary) → `assets/megamod/textures/gui/etf/`.
- **Access wideners:** upstream ships `entity_model_features_13.accesswidener` and `entity_texture_features_13.accesswidener` for MC 1.21.11. NeoForge does not use access wideners directly — source auto-converts AW → AT. We will **hand-translate** these two AW files into new lines appended to `src/main/resources/META-INF/accesstransformer.cfg`. See Phase 9 for the conversion mechanics.

### Files touched
Same: no writes — just lock the spec.

### Risks
- Upstream adds an access requirement we forget to translate → runtime `IllegalAccessError`. Mitigation: checklist-walk both `_13.accesswidener` files line-by-line against our existing AT.

### Verification
Print the spec as a dedicated `docs/EMF_ETF_PORT_LAYOUT.md` (outside planning — this is for the executor to follow), but **do not write it as part of this planning deliverable**.

---

## Phase 2 — Deletion of Existing Partial Port

### Goal
Clear the slate so the bulk copy lands on empty directories.

### Concrete actions

**2.1. Final pre-deletion grep** (safety check). Run:
```
Grep pattern="com\.ultra\.megamod\.lib\.(emf|etf)\." excluding paths lib/emf and lib/etf
```
Confirm only `MegaModClient.java` matches. If anything new appears since Phase 0, halt and re-plan.

**2.2. Delete (via git rm, on the branch):**
- Entire tree `src/main/java/com/ultra/megamod/lib/emf/`
- Entire tree `src/main/java/com/ultra/megamod/lib/etf/`
- File `src/main/resources/megamod-emf.mixins.json`
- File `src/main/resources/megamod-etf.mixins.json`

**2.3. In `MegaModClient.java`, comment out** (do not delete yet — we re-wire in Phase 7) the 4 blocks identified in Phase 0.2:
- Lines ~180–185: `event.addListener(... etf ...)` and `event.addListener(... emf ...)` reload listeners
- Lines ~189–192: `ETFManager.getInstance()` eager init
- Lines ~197–207: EMF eager init block (model manager, config, texture redirect, API, keybind, debug HUD)
- Lines ~210–213: ETF keybind / emissive layer / debug HUD

Adding `// TODO EMF-ETF-PORT: re-wire in Phase 7` comments to each block — this preserves intent and makes the Phase-7 re-wire trivial.

**2.4. Attempt a compile** (`./gradlew compileJava`). Should succeed — only `MegaModClient.java` changes and all callsites are commented. If it fails, the Phase-0 grep missed a callsite: restore, re-audit, retry.

**2.5. Salvage `ETFEmissiveFeatureLayer.java`** per Phase 0.3 decision. Move its contents to `com.ultra.megamod.feature.emfetf.ETFEmissiveFeatureLayer` if it is not pure upstream mirror.

### Files touched
- All of `lib/emf/` (deleted)
- All of `lib/etf/` (deleted)
- `src/main/resources/megamod-emf.mixins.json` (deleted)
- `src/main/resources/megamod-etf.mixins.json` (deleted)
- `src/main/java/com/ultra/megamod/MegaModClient.java` (4 blocks commented)
- Possibly `src/main/java/com/ultra/megamod/feature/emfetf/ETFEmissiveFeatureLayer.java` (new, salvaged)

### Risks
- Forgetting to commit between Phase 1 and Phase 2 → 190-file deletion lives in one commit with the re-copy, making blame useless. **Commit the deletion as its own atomic commit: `emf-etf: delete partial port ahead of full-parity re-port`.**
- Accidental deletion of non-port files if the tree move-vs-delete gets confused. Mitigation: use `git rm -r lib/emf` / `git rm -r lib/etf` (exact paths only).

### Verification
`./gradlew compileJava` passes. `git status` shows only the expected deletions plus the `MegaModClient.java` comment-out.

---

## Phase 3 — Bulk Copy, Preprocessor Stripping & Package Rename

This is the highest-risk phase. Three concerns stack: (a) the source uses an **Essential Gradle Toolkit preprocessor** with `//#if MC >= …` directives, (b) package names must be rewritten, (c) some files are version-specific branches inside `//#if` blocks that must be selected and kept.

### Goal
Land ~90 EMF + ~90 ETF + (15–25 tconfig if D1a) Java files under their new package paths with preprocessor markers resolved for MC 1.21.11 NeoForge.

### Preprocessor semantics (critical)

Source uses these marker styles (derived from `com.replaymod.preprocess` + Essential's extensions):

```java
//#if MC >= 12102
active Java code here
//#else
//$$ inactive branch (commented with //$$)
//#endif
```

Rules for MC=121111, Loader=NEOFORGE:
- **`//#if MC >= 12109`** → **active** for 1.21.11 (12111 ≥ 12109). Keep as plain code.
- **`//#if MC >= 12102`** → **active** (12111 ≥ 12102). Keep.
- **`//#if MC >= 12111`** → **active**. Keep.
- **`//#if MC < 12109`** → inactive. **Strip entire block**.
- **`//#if FABRIC`** → inactive. Strip.
- **`//#if FORGE`** → inactive. Strip.
- **`//#if NEOFORGE`** → active. Keep, uncommenting any `//$$` inside.
- **`//#else`** / **`//#elseif`** → resolve per above.
- **`//$$` prefix lines** inside an **active** branch → uncomment (strip the `//$$ ` prefix).
- **`//$$` prefix lines** inside an **inactive** branch → delete the line entirely.
- **Non-marker lines** always kept.
- Remove `//#if`, `//#else`, `//#elseif`, `//#endif` marker lines themselves after resolution.

### Concrete actions

**3.1. Copy + preprocess — script approach.** Write a throwaway Python or Node script (placed in `scripts/emf_etf_port_preprocess.py` — this is a *script*, not a source file, and does not violate the no-writes rule for the planning conversation; it is created during execution). Inputs:
  - Source tree paths
  - `MC_VERSION=12111`, `LOADER=NEOFORGE` conditions
  - Output dir

The script should:
  1. Walk the source tree, reading each `.java` file as text.
  2. Resolve `//#if`/`//#elseif`/`//#else`/`//#endif` per the rules above. **Respect nesting** — tokens can nest 2–3 deep (seen in `EMFInit.java` with `MC >= 12006` outer and `MC >= 12100` inner). Use a stack.
  3. For active branches, strip leading `//$$ ` or `//$$`.
  4. For inactive branches, drop all lines including `//$$` lines.
  5. After preprocessor resolution, run a package-name regex substitution:
     - `traben.entity_model_features` → `com.ultra.megamod.lib.emf` (all occurrences, import statements and fully-qualified class names)
     - `traben.entity_texture_features` → `com.ultra.megamod.lib.etf`
     - `traben.tconfig` → `com.ultra.megamod.lib.tconfig` (per D1a)
     - Fix the package-statement line to match the new directory path
  6. Write to target path — computed by transforming the source-relative path:
     `{src}/traben/entity_model_features/foo/Bar.java` → `{tgt}/com/ultra/megamod/lib/emf/foo/Bar.java`

**3.2. Run the script twice — once for EMF, once for ETF.** Also once for tconfig (if D1a).

**3.3. Manual fixups (after script).** The script cannot handle every edge case. Expect to hand-fix:
  - `MixinLivingEntityRenderer.java` — generic parameter lists that differ between `MC >= 12102` and older; script keeps the `MC >= 12102` branch, but the `//#else` generic `<T extends LivingEntity, M extends EntityModel<T>>` block will collide with a duplicate public-abstract-class declaration if our conditional parsing misses the embedded `public abstract class` line. Hand-verify this file after preprocessing.
  - `EMFInit.java` / `ETFInit.java` — source wraps the whole mod class in `//#if … //#else` chain across Fabric/Forge/NeoForge. After preprocessing, the NeoForge branch should emerge as the only `EMFInit`. **We then DELETE these files entirely** — our port doesn't need them; `MegaModClient.onClientSetup()` does the init (Phase 7). Add `EMFInit.java` and `ETFInit.java` to a "delete after preprocessing" list.
  - `EMFModMenu.java`, `ETFModMenu.java` — Fabric-only ModMenu compat. **Delete entirely**.
  - `EMFConfig.java` uses `traben.tconfig.TConfig` base class — resolves to `com.ultra.megamod.lib.tconfig.TConfig` after rename. Only works if tconfig is ported (D1a).
  - Any file with remaining `//#if` or `//$$` markers after the script pass → manually finish.

**3.4. Resource-file copy (with rewrites).**
  - Source: `src/main/resources/entity_model_features.mixins.json` — NOT copied; we author a replacement in Phase 5.
  - Source: `entity_model_features_13.accesswidener` — NOT copied; we translate to AT in Phase 9.
  - Source: `assets/entity_model_features/lang/*.json` — copy to `src/main/resources/assets/megamod/lang/` after **key-prefixing** (programmatically wrap each top-level key with `emf.` prefix, merge with existing `en_us.json`). If merge would conflict on same key, bail with a warning and manual-resolve.
  - Same for ETF lang.
  - Source: `assets/entity_model_features/textures/gui/icon.png` → `assets/megamod/textures/gui/emf/icon.png`.
  - Same for ETF textures.
  - Source: `fabric.mod.json`, `pack.mcmeta`, `META-INF/mods.toml`, `META-INF/neoforge.mods.toml`, upstream `icon.png` (root) — **NOT copied**. Our `neoforge.mods.toml` already declares `megamod-emf.mixins.json` and `megamod-etf.mixins.json`.

**3.5. Package-rename of mixin inner sub-packages.** After the script, every `@Mixin(…)` annotation already names Minecraft classes — no rename needed there. But `Shadow` and `Accessor` references inside mixins that reference other port classes DO need rename (script handles via the global `traben.entity_*` regex).

**3.6. Rename the Minecraft class references that changed between MC versions.**
Source uses mojmap where available but has some yarn-style names in comments. With Parchment `2025.12.20`, the real class names are what the source-tree uses. No rename needed — but verify by spot-checking `MixinLivingEntityRenderer.java` line 65's `INVOKE` target descriptor matches the 1.21.11 mapping: `Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V`. If Parchment mapped `setupAnim` to a different name, the `@Inject` target lookup fails. Parchment `2025.12.20` keeps `setupAnim` — confirmed in existing MegaMod mixins.

### Files touched
- New: ~90 files under `src/main/java/com/ultra/megamod/lib/emf/`
- New: ~90 files under `src/main/java/com/ultra/megamod/lib/etf/`
- New: ~20 files under `src/main/java/com/ultra/megamod/lib/tconfig/` (if D1a)
- New: assets under `src/main/resources/assets/megamod/lang/` and `assets/megamod/textures/gui/emf/`, `.../etf/`
- Deleted post-preprocess: `EMFInit.java`, `ETFInit.java`, `EMFModMenu.java`, `ETFModMenu.java`
- Script (transient, in `scripts/`): `emf_etf_port_preprocess.py`

### Risks

| Risk | Severity | Mitigation |
|---|---|---|
| Preprocessor nesting bug drops or keeps wrong lines | High | Unit-test the script against 5 representative files (1 simple, 1 nested, 1 Fabric-only, 1 NeoForge-version-forked, 1 entirely-commented-out) before running on the full tree. |
| Package-rename regex mismatches a string literal containing `traben.entity_model_features` | Med | Grep the output tree for `traben\.` after the script runs — 0 matches means clean. If any remain, inspect case-by-case (most common: user-facing error messages embedding package names). |
| Parchment name drift between source (yarn/mojmap) and our target | Med | Spot-check the P0 mixins' `@Inject(method=…)` descriptors against what NeoForge dev environment sees in `run/logs/debug.log` on first apply failure. |
| Files with mojmap that source compiles against but Parchment renamed | Low | Rare on MC 1.21.11 — Parchment is mostly param-name additive. Compile errors will surface immediately. |
| Script leaves stray `//#if` markers, causing compile errors | Med | Post-pass: grep the output tree for `//#if|//#else|//#endif|//\$\$` and fail if any remain. |

### Verification
- Grep output tree for `traben\.` → **0 matches**.
- Grep output tree for `//#if|//#else|//#elseif|//#endif|//\$\$` → **0 matches**.
- `./gradlew compileJava` — **expect many errors at first pass** (mostly around MegaModClient still commented out, plus any renamed-during-MC-version mojmap symbols). Fix each in Phase 3.3.
- Every `@Mixin(X.class)` in the output tree points at a Minecraft 1.21.11 class that actually exists. Fastest verification: open each mixin file, compile-check will fail if the target doesn't exist.

---

## Phase 4 — Self-Identification & Mod-ID Cleanup

### Goal
Replace or strip the `entity_model_features` / `entity_texture_features` self-identification strings now that our code lives under `megamod`. Per D3, pick one of two policies.

### Concrete actions

**4.1. Search-and-review list (all of these need a decision):**

| Location | Pattern | Current | D3a: "strip, we are megamod" | D3b: "keep legacy for pack authors" |
|---|---|---|---|---|
| Log tags | `[EMF]`, `[ETF]`, `[Entity Model Features]`, `[Entity Texture Features]` | used in ~30 `System.out.println` / logger calls | replace with `[MegaMod/EMF]`, `[MegaMod/ETF]` | keep as-is |
| `ModList.isLoaded("entity_model_features")` calls | in compat code (e.g. ETF might check for EMF presence) | yes, `EMFManager` imports `ETF` directly, but IrisShadowPassDetection etc. use ModList | replace with `true` literal (we ARE both) | replace the ETF→EMF check with `true` (we are both); keep the iris/sodium/etc. checks |
| `FabricLoader.getInstance().isModLoaded(...)` | Some compat files may still reference | rare post-preprocess | replace with `ModList.get().isLoaded(...)` | same |
| Config file paths | `config/entity_model_features.json`, `config/entity_texture_features.json` | EMFConfig / ETFConfig write here | change to `config/megamod/emf.json`, `config/megamod/etf.json` | same (conflicts with other installs of source EMF/ETF if kept) |
| Debug HUD self-id strings | "Entity Model Features vX.Y.Z" | shown in F3 / debug HUD | change to "MegaMod/EMF" | keep |
| `@Mod("entity_model_features")` / `@Mod("entity_texture_features")` | In `EMFInit.java`, `ETFInit.java` | these files get deleted in Phase 3; no action | — | — |
| `showAsResourcePack = false` | source neoforge.mods.toml — we aren't copying | — | — | — |
| Resource-pack **sensing** paths (`optifine/cem/`, `optifine/random/entity/`, `optifine/mob/`, `optifine/armor/`, `textures/entity/`) | hardcoded in `EMFDirectoryHandler`, ETF texture resolver | **MUST NEVER CHANGE** — Fresh Animations contract | leave | leave |
| Mixin-package resource-pack id checks | e.g., `Random.properties` file reading may use `entity_features` namespace | Yes — `assets/entity_features/textures/gui/*.png` reference from ETF config screen | the pack itself uses `entity_features` as a namespace — keep the code using this name, we'll also ship the assets under `assets/megamod/textures/gui/etf/` and rewrite the hardcoded identifier calls in ETF UI classes (only ~5 call sites) | same |

**Recommendation: D3a (strip, we are megamod).** Rationale: pack authors only interact with `optifine/cem/*.jem` paths (untouched). All log tags, config filenames, and debug HUD text are mod-private.

**4.2. Execution checklist (under D3a):**
- Run Grep for `"entity_model_features"` (string literal, with quotes) across `lib/emf/`. Expect ~30–50 hits. Review each:
  - Config path string → replace with `"megamod/emf"` (directory-prefixed).
  - Log tag string → replace with `"MegaMod/EMF"`.
  - Mod-id in a `ModList.isLoaded()` check → replace with `true` (we are always present).
  - Translation key (starts with `"entity_model_features.config...."`) → **keep**, but make sure the corresponding lang file keys use the same prefix. In Phase 3.4 we already merged lang files without prefixing EMF keys (i.e., upstream keys keep `entity_model_features.config.options` etc. — the user-facing key names stay, only the **ownership** changes).
  - Resource-pack **asset identifier** (e.g., `ResourceLocation("entity_features", "textures/gui/settings.png")`) → replace namespace with `"megamod"` and copy the texture file under `assets/megamod/textures/gui/etf/settings.png`.
- Same for `"entity_texture_features"` across `lib/etf/`.
- Do NOT touch any string containing `optifine/` or `textures/entity/`.

**4.3. Version-string policy.** Source's `EMF.java` has `public static final String EMF_VERSION = "..."` used for debug HUD header. Choose: hardcode `"ported-from-upstream-commit-<shortsha>"` or wire to `ModList.get().getModContainerById("megamod").get().getModInfo().getVersion()`. Recommend the latter — single source of truth.

### Files touched
Approximately 30–50 `lib/emf/*.java` and `lib/etf/*.java` files will see string-literal edits. No structural changes.

### Risks
- Accidentally touching an `optifine/` string. Mitigation: scripted replacement with a **blocklist** — if a candidate line matches `optifine/` or `textures/entity/` or `cem/` or `random/entity/`, skip and report for manual review.

### Verification
Grep for `"entity_model_features"` and `"entity_texture_features"` in `lib/` — should only match translation-key literals of the form `"entity_model_features.config..."` (all UI keys). Zero matches that look like mod IDs or log tags.

---

## Phase 5 — Mixin Config Rebuild

### Goal
Produce the full 57-entry `megamod-emf.mixins.json` and 40-entry `megamod-etf.mixins.json` matching upstream's topology exactly.

### Concrete actions

**5.1. Author `src/main/resources/megamod-emf.mixins.json`** — structure mirrors source's `entity_model_features.mixins.json` from Phase-0 read:
```
{
  "required": true,
  "package": "com.ultra.megamod.lib.emf.mixin.mixins",
  "plugin": "com.ultra.megamod.lib.emf.mixin.Plugin",
  "compatibilityLevel": "JAVA_21",
  "minVersion": "0.8",
  "client": [ ...same 57 entries from source ],
  "mixins": [ ],
  "injectors": {
    "defaultRequire": 1
  }
}
```
The 57 client entries are copied exactly from upstream (I read them in Phase 0). Note `compatibilityLevel` bumped from `JAVA_17` (source) to `JAVA_21` to match MegaMod's existing policy.

**5.2. Author `src/main/resources/megamod-etf.mixins.json`** — structure mirrors upstream's `entity_texture_features.mixins.json`:
```
{
  "required": true,
  "package": "com.ultra.megamod.lib.etf.mixin.mixins",
  "plugin": "com.ultra.megamod.lib.etf.mixin.Plugin",
  "compatibilityLevel": "JAVA_21",
  "minVersion": "0.8",
  "mixins": [ ...server/common 17 entries ],
  "client": [ ...33 entries ],
  "injectors": {
    "maxShiftBy": 2,
    "defaultRequire": 1
  }
}
```
Full content from Phase-0 read.

**5.3. Decision: which iris-compat entries to register.** Source lists both `mods.iris.*` (new) and `mods.iris.old.*` (old) — the Plugin gates each with class-existence checks. Keep **both** in the config and rely on Plugin gating. Same for sodium `me.jellysquid.*` vs `net.caffeinemc.*` — in 1.21.11 Iris/Sodium, `net.caffeinemc.mods.sodium.*` is the current namespace. Update the Plugin's class-existence check, but keep the mixin file (class-gated at load time).

**5.4. Confirm `neoforge.mods.toml` registration already includes both configs.** From Phase 0 read: yes, lines 67–72 of `src/main/templates/META-INF/neoforge.mods.toml` already declare `megamod-etf.mixins.json` and `megamod-emf.mixins.json`. No change needed.

**5.5. Set load-order via `neoforge.mods.toml` mixin block ordering.** The current order (Phase 0.4 analysis) already has ETF before EMF. Maintain that.

### Files touched
- `src/main/resources/megamod-emf.mixins.json` (rewritten; was 6 entries, now 57)
- `src/main/resources/megamod-etf.mixins.json` (rewritten; was 14 entries, now 50)

### Risks
- Path mismatch: if `package` field points to `com.ultra.megamod.lib.emf.mixin.mixins` but actual mixins are at `com.ultra.megamod.lib.emf.mixin.` (flattened wrong), FML throws at load. Double-check that the Phase-3 rename preserves the `mixin/mixins` nesting.
- Typos in the 57-entry list. Mitigation: generate the JSON programmatically from the upstream config (read, transform package path, write) rather than hand-type.

### Verification
Run `./gradlew build`. If Mixin can't find a listed class, the build log will say `Failed to find mixin <name>`. Each error maps 1:1 to a typo in the JSON.

---

## Phase 6 — Existing-Mixin Conflict Resolution

### Goal
Given the Phase-0 overlap table, annotate existing MegaMod mixins with explicit priorities and document the resolution for each case.

### Concrete actions

**6.1. MegaMod `GameRendererMixin` vs. EMF `MixinGameRenderer`.** Methods: `bobView` (ours) vs. whatever EMF injects (a `setupLevelCamera`-adjacent method, likely `render()`). **Different methods, no conflict.** Add `@Mixin(value = GameRenderer.class, priority = 1100)` to ours; leave EMF at default (1000).

**6.2. MegaMod `EntityRendererMixin` vs. ETF `entity.renderer.MixinEntityRenderer`.** Ours: `shouldShowName`. ETF's: likely `render` + `getTextureLocation`. **Different methods, no conflict.** Add priority 1100 to ours.

**6.3. MegaMod `LivingEntityMixin` vs. ETF `entity.misc.MixinEntity` (parent-class injection) and EMF `MixinPlayerEntity` / `MixinEntity`.** Ours injects `knockback`. ETF/EMF inject different methods (ETF: NBT-related; EMF: entity model getters). **No conflict.** No action.

**6.4. MegaMod `ItemEntityRendererMixin` vs. anything from EMF.** EMF doesn't touch `ItemEntityRenderer`. No action.

**6.5. MegaMod `MinecraftMixin` vs. ETF `reloading.MixinMinecraftClient`.** Ours: BetterCombat attack controller (injects into `Minecraft.tick` / interact methods). ETF: likely `reloadResources` / `setOverlay`. **Different methods.** Add `@Mixin(value = Minecraft.class, priority = 1100)` to ours to be safe.

**6.6. MegaMod `LevelRendererMixin` vs. EMF.** EMF doesn't touch `LevelRenderer`. No action.

**6.7. ETF `MixinEntity` vs. EMF `MixinEntity`.** Different sub-packages, both register their own mixin. They run in separate mixin configs. No conflict.

**6.8. Shared `MixinBlockEntity` between EMF and ETF.** Both upstream mods register the same class name. Since they're in separate mixin-config files (each with its own `package`), they resolve to different full classes. No conflict.

**6.9. MegaMod `AvatarRendererMixin` vs. any EMF PlayerEntityRenderer work.** `AvatarRenderer` is NeoForge's 1.21.11 player-renderer shell; EMF's `MixinPlayerEntityRenderer` targets the **inner** model-carrying renderer class. Separate classes. No conflict.

**6.10. Add defensive priority bumps.** On all of these MegaMod mixins (regardless of observed conflict), add explicit priority:
- `GameRendererMixin` → 1100
- `EntityRendererMixin` → 1100
- `MinecraftMixin` → 1100
- `LevelRendererMixin` → 1100
- `ItemEntityRendererMixin` → 1100

This makes MegaMod's cancel-style injections fire before EMF's data-gathering injections if they ever co-apply.

**6.11. Document in code comments.** On each of the 5 bumped mixins, add a `// NOTE: priority 1100 to run before EMF/ETF mixins on same class (different methods).`

### Files touched
- `src/main/java/com/ultra/megamod/mixin/GameRendererMixin.java`
- `src/main/java/com/ultra/megamod/mixin/EntityRendererMixin.java`
- `src/main/java/com/ultra/megamod/mixin/MinecraftMixin.java`
- `src/main/java/com/ultra/megamod/mixin/LevelRendererMixin.java`
- `src/main/java/com/ultra/megamod/mixin/ItemEntityRendererMixin.java`

### Risks
- Missed overlap discovered only at runtime via `InvalidInjectionException`. Mitigation: first runClient attempt in Phase 10 will surface these — fix iteratively.

### Verification
`./gradlew runClient` — mixin apply log in `run/logs/latest.log` should show no `WARN` / `ERROR` at mixin application for any of the 5 annotated classes.

---

## Phase 7 — Entry-Point Wiring

### Goal
Restore the 4 initialization blocks commented out in Phase 2.3, re-pointing them at the NEW upstream classes (`EMFManager`, `ETFManager`) instead of the deleted partial-port (`EmfModelManager`, old `ETFManager`).

### Concrete actions

**7.1. Reload listener registration** (was Phase-2 block 1). Replace with:
```java
event.addListener(
    Identifier.fromNamespaceAndPath("megamod", "etf"),
    new com.ultra.megamod.lib.etf.ETFReloadListener());
event.addListener(
    Identifier.fromNamespaceAndPath("megamod", "emf"),
    new com.ultra.megamod.lib.emf.EmfReloadListener());
```
Upstream `EmfReloadListener` and `ETFReloadListener` classes come across intact in the Phase-3 copy. Check their constructors — if they're singleton-only (private ctor + `INSTANCE` field), use `.INSTANCE` instead of `new`.

**7.2. Singleton-manager eager init** (was Phase-2 blocks 2+3). Replace with:
```java
modEventBus.addListener((FMLClientSetupEvent e) -> {
    e.enqueueWork(() -> {
        com.ultra.megamod.lib.etf.features.ETFManager.getInstance();
        com.ultra.megamod.lib.emf.EMFManager.getInstance();
        // EMF init() method handles config load, compat detection, mappings registration
        com.ultra.megamod.lib.emf.EMF.init();
        com.ultra.megamod.lib.etf.ETF.start();
    });
});
```
Check `EMF.init()` — if it registers its own reload listener (Fabric style), that's a duplicate of 7.1. Inspect `EMF.init()` after preprocessing; if it contains a Fabric-only `ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(...)` block, the preprocessor should have stripped it (Fabric-only). Confirm zero reload-listener registrations happen inside `init()` for the NeoForge build.

**7.3. Config keybind + debug HUD** (was Phase-2 block 4). **Policy decision per D2.** If keeping standalone keybinds:
```java
modEventBus.addListener(com.ultra.megamod.lib.emf.config.screen.EMFConfigKeybind::onRegisterKeyMappings);
modEventBus.addListener(com.ultra.megamod.lib.etf.config.screens.ETFConfigKeybind::onRegisterKeyMappings); // note source uses 'screens' (plural) — verify post-rename
modEventBus.addListener(com.ultra.megamod.lib.emf.debug.EMFDebugHud::onRegisterGuiLayers);
modEventBus.addListener(com.ultra.megamod.lib.etf.debug.ETFDebugHud::onRegisterGuiLayers);
```
The classes `EMFConfigKeybind` and `ETFConfigKeybind` are not present upstream — they are MegaMod's glue that the upstream doesn't ship (ModMenu ships in Fabric). Since these were only in the partial port and are now deleted, we author them fresh. Skeleton:
- `EMFConfigKeybind`: registers a KeyMapping "key.megamod.emf_config" default Unbound, on press opens `EMFConfig.getConfigScreen()` (source's TConfig provides this).
- `ETFConfigKeybind`: same for ETF.
- `EMFDebugHud` / `ETFDebugHud`: register a Gui layer that reads `EMF.config().debug_render_level` and shows debug text when enabled.

These 4 classes are **new MegaMod-authored glue**, not from upstream. Lives at `src/main/java/com/ultra/megamod/lib/emf/config/EMFConfigKeybind.java`, `.../lib/emf/debug/EMFDebugHud.java`, and ETF equivalents.

**7.4. EntityRenderersEvent.AddLayers for emissive feature layer.** Per Phase 2.5 salvage: re-wire the (possibly-salvaged) `ETFEmissiveFeatureLayer` if it provides MegaMod-specific player wiring. If upstream ships `ETFPlayerFeatureRenderer` as the proper analog (yes — Phase-0 glob shows it), use that instead:
```java
modEventBus.addListener((EntityRenderersEvent.AddLayers e) ->
    com.ultra.megamod.lib.etf.features.player.ETFPlayerFeatureRenderer.addLayersToPlayer(e));
```
(Method name `addLayersToPlayer` is a placeholder — inspect the actual upstream signature during execution.)

**7.5. Remove the salvaged `ETFEmissiveFeatureLayer` if upstream renders it redundant.** If upstream `ETFPlayerFeatureRenderer` covers every emissive case we wired manually, delete the salvaged file from `feature/emfetf/`.

**7.6. Networking.** Neither EMF nor ETF ships networking (they're client-render mods). Confirm by grep for `register.*Payload` / `sendToPlayer` / `ServerboundPacket` in upstream. **Expected: zero hits.** No action.

### Files touched
- `src/main/java/com/ultra/megamod/MegaModClient.java` (uncomments blocks, points at new classes)
- New: `src/main/java/com/ultra/megamod/lib/emf/config/EMFConfigKeybind.java`
- New: `src/main/java/com/ultra/megamod/lib/etf/config/ETFConfigKeybind.java`
- New: `src/main/java/com/ultra/megamod/lib/emf/debug/EMFDebugHud.java`
- New: `src/main/java/com/ultra/megamod/lib/etf/debug/ETFDebugHud.java`
- Potentially deleted: `src/main/java/com/ultra/megamod/feature/emfetf/ETFEmissiveFeatureLayer.java` (if redundant)

### Risks
- `EMF.init()` or `ETFManager.getInstance()` has a Fabric-only side effect the preprocessor didn't clean up — surfaces as `NoSuchMethodError` on first run. Mitigation: read each init method manually after Phase-3 preprocessing and eyeball for leftover fabric-loader imports.

### Verification
`./gradlew runClient` boots to main menu without crash. `run/logs/debug.log` should show:
- "EMFManager initialised" (or equivalent from upstream `EMFManager` ctor)
- "ETFManager initialised"
- Both reload listeners fire on first world load

---

## Phase 8 — Config UI Integration (optional)

Per D2 — recommend **standalone keybinds** (fastest path to a working port). Mark for future integration into `ConfigurationScreen` once the standalone UI is proven.

### Concrete actions

**8.1. Hook both `EMFConfigKeybind` / `ETFConfigKeybind` onto default-Unbound keys.** Users must explicitly bind.
**8.2. Menu entry points:** None — standalone keybinds only. If the user later wants the NeoForge `ConfigurationScreen` entry to expose EMF/ETF, wire it in Phase 11 (future work).

### Risks
- `TConfig.getConfigScreen()` returns a fabric-specific Screen subclass. Confirm after preprocessing that the returned type is a plain `net.minecraft.client.gui.screens.Screen`. If it's a ModMenu-specific class (`com.terraformersmc.modmenu.ModMenuScreen`-adjacent), rip out.

### Verification
Bind the EMF key in-game, press it, config screen opens, you can toggle `debug_render_level` and see the HUD react.

---

## Phase 9 — Build & Dependencies

### Goal
Update `build.gradle` to satisfy upstream's implicit dependencies.

### Concrete actions

**9.1. Dependencies added to `build.gradle`:**
- **MixinExtras** — `io.github.llamalad7:mixinextras:0.4.1` (or whatever NeoForge 21.11.42 ships — **check first**, NeoForge bundles mixinextras since 20.2.84). `MixinExtrasBootstrap.init()` in the Plugin classes requires the API on classpath at mixin-apply time. Check `run/logs/debug.log` from a current `runClient` — if `MixinExtrasBootstrap` resolves already, no dep needed.
- **mcdev annotations** — `com.demonwav.mcdev:annotations:2.1.0` (used by upstream for nullability). Compile-only.
- **fastutil** — already on classpath via Minecraft; no explicit dep.
- **Apache Commons Lang 3** — `ImmutablePair`, `MutableTriple` used by upstream `EMFManager`. Minecraft bundles Commons Lang 3; no explicit dep.

**9.2. Access Transformer merge.** Translate upstream's AW files to AT format and append to `src/main/resources/META-INF/accesstransformer.cfg`:

Source → AT translation rules (per source's `build.gradle.kts` `awToAt` function, lines 364–380):
- `accessible class X` → `public X`
- `extendable class X` → `public-f X`  (same for `mutable class`)
- `accessible method X name desc` → `public X name desc`
- `extendable method X name desc` → `public-f X name desc` (same for mutable)
- `accessible field X name type` → `public X name`  (type is dropped in AT — ATs use field-name-only)
- `mutable field X name type` → `public-f X name`

Apply this translation to every non-comment, non-empty line of:
- `entity_model_features_13.accesswidener` (contents captured in Phase-0 read)
- `entity_texture_features_13.accesswidener` (contents captured in Phase-0 read)

**Before appending, cross-check against existing `accesstransformer.cfg` (Phase-0 read, lines 14–17):**
- `public-f net.minecraft.client.model.geom.ModelPart cubes` — already present ✓
- `public-f net.minecraft.client.model.geom.ModelPart children` — already present ✓
- `public net.minecraft.client.model.geom.ModelPart$Cube` — already present ✓
- `public-f net.minecraft.client.model.geom.ModelPart$Cube polygons` — already present ✓

So roughly 40 new AT lines need adding (the rest from upstream's `.accesswidener` files are new).

**9.3. Parchment compatibility.** `gradle.properties` pins `parchment_mappings_version=2025.12.20` for MC 1.21.11. Upstream is authored against "mojmap" per source's `root.gradle.kts` line 39 (`neoforgeNode = createNode(..., "mojmap")`). Parchment adds parameter names on top of mojmap — class and method names match. **No mapping translation needed.**

**9.4. Gradle tasks to keep/remove.**
- Keep: `runClient`, `compileJava`, `build`.
- No new tasks needed — we rejected Essential's preprocessor toolkit.

### Files touched
- `build.gradle` — add 2 dependency lines (MixinExtras conditional, mcdev annotations)
- `src/main/resources/META-INF/accesstransformer.cfg` — ~40 new lines appended

### Risks
- Access-transformer formatting error → FML refuses to load. Mitigation: validate the file by comparing to existing entries line-by-line. NeoForge tolerates blank lines and comments prefixed with `#`.
- MixinExtras already bundled by NeoForge 21.11.42, adding it again → classpath dup. Mitigation: try first WITHOUT adding; only add if `MixinExtrasBootstrap` class-not-found occurs at boot.

### Verification
`./gradlew build` compiles. Run an empty `runClient` (no world) — accessTransformer errors surface at mod-load.

---

## Phase 10 — Compilation & Runtime Smoke Test

### Goal
Milestone-gated validation.

### Milestones

**M1. Clean compile.** `./gradlew build` finishes without errors. Expected duration: 3–8 min on a cold build. **Tolerance for intermediate failures:** Java compile errors due to mojmap drift — fix each by spot-checking the Parchment-mapped symbol names.

**M2. Client starts.** `./gradlew runClient` reaches the main menu. No `FATAL` or mixin-apply exception in `run/logs/latest.log`. Expected common failures:
- `InvalidInjectionException: Failed on LiteralInjector` — target method signature drift; fix `@Inject(method=…)` descriptor.
- `IllegalAccessError` — missing AT line; add to `accesstransformer.cfg`.
- `ClassNotFoundException` in Plugin — mixin-config typo or package-rename missed.

**M3. Fresh Animations pack loads.** Drop a Fresh Animations v1.9+ resource pack into `run/resourcepacks/`, enable it, load a creative world, spawn a zombie. Zombie should idle-breathe. If static → `EMFDirectoryHandler` isn't reading the pack's `assets/minecraft/optifine/cem/zombie.jem`, or `EMFManager.getLayerRootFromModelId` isn't returning the CEM-patched model.

**M4. Pack-swap invalidates cache.** With pack A loaded and models animating, swap to pack B (different animations). Models should update within one reload cycle. If stale → `EmfReloadListener` isn't actually clearing `EMFManager.cache_JemDataByFileName`.

**M5. Per-model mixins verified.** Spawn one of each: ender dragon, wolf, armadillo, villager, creeper (charged), parrot, warden. Each must honor the pack's `.jem` for that mob. If any is broken, the corresponding `rendering.model.Mixin*` or `rendering.feature.Mixin*` likely failed to apply — check the mixin log.

**M6. Compat mods retest.** If user has Iris and/or Sodium installed, re-run M3–M5. Iris shadow pass is a known edge (EMF's `IrisShadowPassDetection` may need path updates). Skin Layers 3D compat mixin should class-gate correctly via Plugin.

### Risks
- A single incompatible mixin crashes the whole mod. NeoForge's mixin service does NOT auto-disable failing mixins — the whole config fails. Mitigation: isolate by temporarily removing one mixin at a time from `megamod-emf.mixins.json` to identify the offender, then fix and re-enable.

### Verification
Each milestone produces a binary pass/fail. Document the first-failure point so execution can resume.

---

## Phase 11 — Known Risks & Rollback

### Risks

**R1. Partial compile failure mid-Phase-3.** A preprocessor bug leaves one file unparseable. Mitigation: do the copy in a dedicated branch; fix incrementally; the `emf-etf-full-port` branch doesn't merge until M3 passes.

**R2. Mixin conflict bricks an unrelated feature.** Example: upstream's `MixinEntityRenderDispatcher` uses `@Redirect` on a method MegaMod's AvatarRenderer depends on. Mitigation: Phase-6 priority bumps; if needed, convert the offending upstream `@Redirect` to `@WrapOperation` or `@ModifyExpressionValue` (MixinExtras-style) with a passthrough when MegaMod's AvatarRenderer is in play.

**R3. Parchment 2025.12.20 vs upstream source drift.** Upstream targets mojmap exactly; Parchment is a Mojmap superset (adds param names only). No class/method renames should occur. But some `@Inject(method=…)` descriptors use intermediary name. If these fail, fallback is method-index-based injection. Mitigation: replace any problematic `method="foo"` with `method="foo*"` (glob) as a last resort.

**R4. Access-widener fields that are also final in 1.21.11** (AW `mutable field X` translates to AT `public-f X` — removes `final`). Making a field non-final in a record class or sealed class may not compile under ASM. Mitigation: verify each `mutable field` translation against current mojmap source — skip the `-f` suffix if field is already non-final.

**R5. Iris 1.21.11 mod namespace drift.** Source's iris-compat targets `net.irisshaders.iris.renderers.WrappedRenderType` (new) or legacy Fabric-path. If 1.21.11 Iris changed the class path, all Iris mixins fail to class-gate and apply too eagerly → crash. Mitigation: Plugin's class-existence check defends against this; if Iris is not present, mixins are skipped entirely.

**R6. `tconfig` port missing pieces.** EMF references obscure tconfig classes (`TConfigScreenList`, `TConfigEntryMathVariable`, etc.) that require full-parity port. Mitigation: D1a path — port the entire `traben.tconfig` tree. Confirmed ~25 classes based on `TConfigEntry*` imports across EMFConfig.

### Rollback plan

**Pre-work:** create and tag:
- Branch: `feature/emf-etf-full-port` from `main` HEAD.
- Tag: `pre-emf-etf-port` on the last pre-port commit (on `main`).

**Commit strategy on the branch:**
- Commit 1: `emf-etf: delete partial port` (Phase 2)
- Commit 2: `emf-etf: bulk copy upstream with preprocessing` (Phase 3)
- Commit 3: `emf-etf: mod-id cleanup` (Phase 4)
- Commit 4: `emf-etf: mixin configs rebuilt` (Phase 5)
- Commit 5: `emf-etf: conflict-priority annotations on existing mixins` (Phase 6)
- Commit 6: `emf-etf: wire entry points in MegaModClient` (Phase 7)
- Commit 7: `emf-etf: config keybinds + debug HUD glue` (Phase 7)
- Commit 8: `emf-etf: AT additions + build.gradle deps` (Phase 9)
- Commits 9+: iterative fixes from M1–M6.

**Rollback options:**
- Full abandon: `git switch main`, delete branch. Nothing lost since `main` was untouched.
- Partial rollback: git revert specific commit(s) on the branch. Commit boundaries are designed to each be independently revertable (mostly).

### Verification
`git log --oneline pre-emf-etf-port..HEAD` shows a clean, semantic history that could be cherry-picked selectively if a future 1.21.12 port wants just Phase-3 copy without Phase-6 conflict decisions.

---

## Cross-Cutting Notes

**N1. Do not touch resource-pack-facing paths.** `optifine/cem/*.jem`, `optifine/random/entity/*.properties`, `optifine/mob/`, `optifine/armor/`, `textures/entity/` — these are the OptiFine-compat contract Fresh Animations relies on. No Phase modifies any of these paths.

**N2. `megamod.mixins.json` is untouched.** All work happens in `megamod-emf.mixins.json` and `megamod-etf.mixins.json`.

**N3. Upstream's `//$$` preprocessor lines are safe to delete only after the preprocessor runs.** Grep-checks in Phase 3.6 ensure no stragglers.

**N4. Upstream imports of other upstream files (e.g., EMFManager importing ETF) survive package rename cleanly because the regex handles both EMF's and ETF's traben packages in one pass.**

**N5. `showAsResourcePack = false` in upstream `neoforge.mods.toml` is not a thing we replicate — MegaMod is a mod, not a pack, and our existing `neoforge.mods.toml` already declares the mod correctly.**

**N6. Source uses `compatibilityLevel=JAVA_17` and `JAVA_16`.** Bump both to `JAVA_21` in the new configs to match MegaMod's java.toolchain setting.

**N7. The `IrisShadowPassDetection` class relies on reflection into Iris; it will only fire if Iris is actually loaded. Safe to ship unconditionally.**

**N8. Config file location.** Per D3a + Phase 4, `config/megamod/emf.json` and `config/megamod/etf.json` on disk. Existing users (if any) with `config/entity_model_features.json` from prior partial port can copy manually — no migration scaffolding needed.

---

### Critical Files for Implementation

- `C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\java\com\ultra\megamod\MegaModClient.java`
- `C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\megamod-emf.mixins.json`
- `C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\megamod-etf.mixins.json`
- `C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\META-INF\accesstransformer.cfg`
- `C:\Users\JulianCalero\Desktop\Projects\MegaMod\build.gradle`