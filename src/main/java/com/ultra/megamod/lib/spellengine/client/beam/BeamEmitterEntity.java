package com.ultra.megamod.lib.spellengine.client.beam;

import com.ultra.megamod.lib.spellengine.internals.delivery.Beam;

public interface BeamEmitterEntity {
    void setLastRenderedBeam(Beam.Rendered beam);
}