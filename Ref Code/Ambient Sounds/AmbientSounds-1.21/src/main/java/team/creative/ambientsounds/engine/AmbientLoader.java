package team.creative.ambientsounds.engine;

import com.google.gson.JsonElement;

@FunctionalInterface
public interface AmbientLoader<T> {
    
    public void setNameAndLoad(T t, String name, JsonElement element) throws AmbientEngineLoadException;
    
}
