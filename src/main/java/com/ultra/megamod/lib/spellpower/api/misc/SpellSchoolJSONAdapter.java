package com.ultra.megamod.lib.spellpower.api.misc;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ultra.megamod.lib.spellpower.api.SpellSchool;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;

import java.io.IOException;

public class SpellSchoolJSONAdapter extends TypeAdapter<SpellSchool> {
    @Override
    public void write(JsonWriter jsonWriter, SpellSchool school) throws IOException {
        jsonWriter.value(school.id.toString());
    }

    @Override
    public SpellSchool read(JsonReader jsonReader) throws IOException {
        return SpellSchools.getSchool(jsonReader.nextString());
    }
}
