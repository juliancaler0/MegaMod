# EMF coexistence notes

The EMF port writes directly to vanilla `ModelPart` transform fields
(`xRot`, `yRot`, `zRot`, `x`, `y`, `z`, `xScale`, `yScale`, `zScale`,
`visible`). Two other bone animators live inside MegaMod and can also write
to those same fields:

- **PlayerAnim library** (`com.ultra.megamod.lib.playeranim`) — used by the
  player-sitting, path-sprinting, and emote-style features. Applies
  per-animation modifiers on top of vanilla poses.
- **AzureLib** (`com.ultra.megamod.lib.azurelib`) — geckolib-style bone
  animator used by Archers / Paladins armor, some dungeon mobs, the
  spellengine, and several feature systems.

## Call-order contract

Upstream EMF documents a strict order: **vanilla default pose →
EMF pack animations → mod-provided geckolib / PlayerAnim modifiers**. We
preserve that order by attaching EMF's `setupAnim` TAIL hook at a lower
priority than anything that runs after setupAnim in the render pipeline:

1. Vanilla `EntityModel.setupAnim(state)` runs first. Sets the base pose.
2. `MixinHumanoidModel#emf$applyCompiledAnimations` (TAIL) and
   `MixinEntityModelSetup#emf$applyCompiledAnimations` (TAIL) run next —
   this is where `EmfBoneApplier.apply(...)` writes `.jem` transforms on
   top of the vanilla pose.
3. `MixinModelFeatureRenderer#emf$applyAfterSetupAnim` fires for feature
   layers — runs the same applier once per feature pass so per-layer
   bone-level transforms are refreshed.
4. **PlayerAnim** modifiers run later, in the feature-layer submit path.
   They see EMF's pose as their "input" and composite on top.
5. **AzureLib** bone animators run in their own renderer layer (Archers,
   Paladins, etc.) and replace rather than composite — they draw their
   own geckolib models with their own pose data and do not interact with
   `ModelPart` transforms at all, so they do not conflict with EMF.

Net effect: a pack with an EMF `.jem` for a player sees its transforms
applied first; PlayerAnim modifiers then composite emotes / sitting on
top. When neither system has anything to say for a bone, vanilla's base
pose wins.

## When writes collide

If a `.jem` expression writes `head.rx` for a player and a PlayerAnim
modifier also writes `head.rx` on the same frame, **PlayerAnim wins**.
That matches upstream EMF's behaviour (EMF applies in its own TAIL, and
external animators that run afterwards overwrite on top).

If you need EMF to win the conflict, the EMFApi-level way is to register
an `EMFApi.BoneApplyListener` that stashes the EMF value for the
conflicting bone and re-applies it in a post-modifier hook. Upstream
does not provide such a hook directly; MegaMod's listener is the escape
hatch.

## Pausing EMF for specific entities

`EMFApi.pauseEntity(uuid)` and `EMFApi.pausePartsOfEntity(uuid, parts)`
drop EMF out of the pipeline for a frame entirely. Use this when another
system is driving the whole entity pose (e.g. an emote that must not be
overwritten by the `.jem`). `EMFApi.lockEntityToVanillaModel(uuid)` does
the stronger thing — EMF won't even try to bind a `.jem`, so no pack
transforms or texture overrides apply.

## AzureLib specifics

AzureLib models bring their own vertex data and ignore `ModelPart`.
There is no interaction with EMF here — both systems can render on the
same entity without conflict as long as the AzureLib renderer is bound
to a different `ModelLayerLocation` than the one EMF's `.jem` targets
(which is the standard geckolib setup).

## Texture redirect

EMF's `.jem texture` override is wired through
`ETFUtils2.baseTextureRedirector` on client setup. When a pack declares a
custom texture on the `.jem`, the redirector substitutes it *before* the
ETF variator picks a numbered variant. This means ETF's
`name2.png`/`name3.png` scheme still works, but relative to the
EMF-provided base — exactly the upstream behaviour.
