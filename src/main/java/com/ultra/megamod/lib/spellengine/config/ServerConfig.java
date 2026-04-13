package com.ultra.megamod.lib.spellengine.config;

import com.ultra.megamod.lib.spellengine.internals.target.EntityRelation;

import java.util.LinkedHashMap;

/**
 * Server-side SpellEngine configuration.
 * Ported from Fabric's AutoConfig to simple POJO.
 */
public class ServerConfig {
    public ServerConfig() {}

    public float movement_multiplier_speed_while_casting = 1F;
    public boolean bypass_iframes = true;
    public boolean haste_affects_cooldown = true;
    public float spell_cost_exhaust_multiplier = 1F;
    public boolean spell_cost_item_allowed = true;
    public boolean spell_cost_durability_allowed = true;
    public int spell_instant_cast_global_cooldown = 0;
    public boolean spell_item_cooldown_lock = true;
    public float spell_book_additional_cooldown = 10F;
    public boolean spell_book_creation_enabled = true;
    public int spell_book_creation_cost = 1;
    public int spell_book_creation_requirement = 1;
    public int spell_binding_level_cost_multiplier = 1;
    public int spell_binding_level_cost_offset = 0;
    public int spell_binding_level_cost_min = 1;
    public int spell_binding_level_requirement_multiplier = 1;
    public int spell_binding_level_requirement_offset = 0;
    public int spell_binding_level_requirement_min = 1;
    public int spell_binding_lapis_cost_multiplier = 1;
    public boolean spell_binding_allow_unbinding = true;
    public int spell_scroll_level_cost_per_tier = 0;
    public int spell_scroll_apply_cost_base = 1;
    public boolean spell_container_caching = true;
    public boolean spell_container_from_offhand_any = false;
    public boolean projectiles_pass_thru_irrelevant_targets = true;
    public int auto_swap_cooldown = 5;
    public float attribute_evasion_angle = 120F;
    public boolean attribute_evasion_allowed_while_spell_casting = false;
    public boolean attribute_evasion_allowed_while_item_usage = false;
    public boolean melee_skills_area_focus_mode = true;

    public LinkedHashMap<String, EntityRelation> player_relations = new LinkedHashMap<>() {{
        put("minecraft:player", EntityRelation.FRIENDLY);
        put("minecraft:villager", EntityRelation.FRIENDLY);
        put("minecraft:allay", EntityRelation.FRIENDLY);
        put("minecraft:iron_golem", EntityRelation.FRIENDLY);
        put("minecraft:cat", EntityRelation.FRIENDLY);
        put("minecraft:minecart", EntityRelation.FRIENDLY);
    }};
    public LinkedHashMap<String, EntityRelation> player_relation_tags = new LinkedHashMap<>();
    public EntityRelation player_relation_to_owned_pets = EntityRelation.FRIENDLY;
    public EntityRelation player_relation_to_teammates = EntityRelation.FRIENDLY;
    public EntityRelation player_relation_to_passives = EntityRelation.HOSTILE;
    public EntityRelation player_relation_to_hostiles = EntityRelation.HOSTILE;
    public EntityRelation player_relation_to_other = EntityRelation.HOSTILE;
}
