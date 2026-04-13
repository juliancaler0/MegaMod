package com.ultra.megamod.lib.combatroll.config;

public class ServerConfig {
    /** The number of game ticks players become invulnerable upon rolling */
    public int invulnerable_ticks_upon_roll = 0;
    /** The duration of the roll ability, expressed as a number of ticks, during which the player cannot use item/attack/jump etc... */
    public int roll_duration = 13;
    /** Allows Vanilla Minecraft auto jump feature to work while rolling */
    public boolean allow_auto_jump_while_rolling = true;
    /** Allows jumping while rolling. WARNING! Setting this to true breaks roll distance attribute and enchantment */
    public boolean allow_jump_while_rolling = false;
    /** Allows combat roll while the player has its weapon on cooldown */
    public boolean allow_rolling_while_weapon_cooldown = true;
    /** Allows combat roll while the player is in the air. WARNING! Setting this to true breaks roll distance attribute and enchantment */
    public boolean allow_rolling_while_airborn = false;
    /** The amount of exhaust (hunger) to be added to the player on every roll */
    public float exhaust_on_roll = 0.1F;
    /** The amount of food level above which players can do a roll */
    public float food_level_required = 6;
    /** The cooldown duration of the combat roll ability expressed in seconds */
    public float roll_cooldown = 4F;
    /** Default roll distance attribute is 3. Settings this to 1 will make it 4. Warning! Attribute based scaling does not effect this. */
    public float additional_roll_distance = 0;
}
