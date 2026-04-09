package com.ultra.megamod.feature.citizen.blueprint.packs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata for a structure/style pack.
 * Parsed from a pack.json file found in each pack's directory.
 *
 * <p>Pack directory structure:
 * <pre>
 *   blueprints/megamod/{packId}/
 *       pack.json          -- this metadata file
 *       buildings/          -- blueprint files (.blueprint)
 *       decorations/
 *       ...
 * </pre>
 */
public class StructurePackMeta {

    private final String name;
    private final String id;
    private final Path path;
    private final String description;
    private final List<String> authors;
    private final String version;
    private final String icon;

    public StructurePackMeta(String name, String id, Path path, String description,
                             List<String> authors, String version, String icon) {
        this.name = name;
        this.id = id;
        this.path = path;
        this.description = description;
        this.authors = authors;
        this.version = version;
        this.icon = icon;
    }

    /**
     * Parses a pack.json file into StructurePackMeta.
     *
     * <p>Expected JSON format:
     * <pre>
     * {
     *   "name": "Medieval Style",
     *   "description": "Medieval-themed buildings",
     *   "authors": ["Author1", "Author2"],
     *   "version": "1.0.0",
     *   "icon": "icon.png"
     * }
     * </pre>
     *
     * @param json    the parsed JSON object from pack.json
     * @param id      the directory name used as the pack's unique identifier
     * @param packDir the path to the pack directory
     * @return the parsed StructurePackMeta
     */
    public static StructurePackMeta fromJson(JsonObject json, String id, Path packDir) {
        String name = json.has("name") ? json.get("name").getAsString() : id;
        String description = json.has("description") ? json.get("description").getAsString() : "";
        String version = json.has("version") ? json.get("version").getAsString() : "1.0.0";
        String icon = json.has("icon") ? json.get("icon").getAsString() : "";

        List<String> authors = new ArrayList<>();
        if (json.has("authors") && json.get("authors").isJsonArray()) {
            JsonArray authorsArray = json.getAsJsonArray("authors");
            for (int i = 0; i < authorsArray.size(); i++) {
                authors.add(authorsArray.get(i).getAsString());
            }
        } else if (json.has("author")) {
            authors.add(json.get("author").getAsString());
        }

        return new StructurePackMeta(name, id, packDir, description, authors, version, icon);
    }

    /** Returns the display name of this pack. */
    public String getName() {
        return name;
    }

    /** Returns the unique identifier (directory name) of this pack. */
    public String getId() {
        return id;
    }

    /** Returns the filesystem path to this pack's directory. */
    public Path getPath() {
        return path;
    }

    /** Returns the pack description text. */
    public String getDescription() {
        return description;
    }

    /** Returns the list of pack authors. */
    public List<String> getAuthors() {
        return authors;
    }

    /** Returns the pack version string. */
    public String getVersion() {
        return version;
    }

    /** Returns the icon filename relative to the pack directory, or empty string if none. */
    public String getIcon() {
        return icon;
    }

    /** Returns the authors as a comma-separated string. */
    public String getAuthorsString() {
        return String.join(", ", authors);
    }

    @Override
    public String toString() {
        return "StructurePackMeta{name='" + name + "', id='" + id + "', version='" + version + "'}";
    }
}
