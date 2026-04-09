package shiroroku.theaurorian.Config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec config;

    public static final ForgeConfigSpec.ConfigValue<Boolean> enable_auroras;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        enable_auroras = builder.define("enable_auroras", true);
        config = builder.build();
    }
}
