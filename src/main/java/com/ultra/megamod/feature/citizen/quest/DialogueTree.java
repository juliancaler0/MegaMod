package com.ultra.megamod.feature.citizen.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a parsed dialogue tree from quest JSON.
 * <p>
 * Mirrors MineColonies' {@code DialogueElement / AnswerElement} system.
 * Each dialogue element has a text line spoken by the NPC, and a list
 * of answer options. Each answer can lead to:
 * <ul>
 *   <li>Another {@code DialogueElement} (nested dialogue)</li>
 *   <li>An {@code AdvanceObjective} result (moves to next/specific objective)</li>
 *   <li>A {@code CancelQuest} result (abandons the quest)</li>
 *   <li>A {@code Return} result (closes UI without progressing, quest stays)</li>
 * </ul>
 */
public class DialogueTree {

    // ========================= Result Types =========================

    /**
     * Base type for all answer results.
     */
    public sealed interface AnswerResult permits
            DialogueResult, AdvanceObjectiveResult, CancelResult, ReturnResult {
    }

    /**
     * Continues to another dialogue element.
     */
    public record DialogueResult(DialogueElement next) implements AnswerResult {}

    /**
     * Advances the quest to a specific objective index.
     * A value of -1 or any index >= objectiveCount means "complete the quest".
     */
    public record AdvanceObjectiveResult(int goTo) implements AnswerResult {}

    /**
     * Cancels/abandons the quest.
     */
    public record CancelResult() implements AnswerResult {}

    /**
     * Closes the dialogue UI without any state change (player can return later).
     */
    public record ReturnResult() implements AnswerResult {}

    // ========================= Elements =========================

    /**
     * A single dialogue element: NPC text + player answer options.
     */
    public static class DialogueElement {
        private final String text;
        private final List<AnswerOption> options;

        public DialogueElement(String text, List<AnswerOption> options) {
            this.text = text;
            this.options = Collections.unmodifiableList(options);
        }

        public String getText() { return text; }
        public List<AnswerOption> getOptions() { return options; }

        /**
         * Gets the result for a given answer index.
         *
         * @return the result, or null if index is out of bounds
         */
        public AnswerResult getResult(int answerIndex) {
            if (answerIndex < 0 || answerIndex >= options.size()) return null;
            return options.get(answerIndex).result();
        }
    }

    /**
     * A single answer option: player text + result.
     */
    public record AnswerOption(String text, AnswerResult result) {}

    // ========================= Parsing =========================

    /**
     * Parses a dialogue tree from an objective JSON object.
     * Expects fields: "text", "options" (array of {answer, result}).
     *
     * @param json the objective JSON with text/options
     * @return the parsed dialogue element, or null if not a dialogue type
     */
    public static DialogueElement parse(JsonObject json) {
        if (!json.has("text") || !json.has("options")) return null;

        String text = json.get("text").getAsString();
        List<AnswerOption> options = new ArrayList<>();

        JsonArray optionsArray = json.getAsJsonArray("options");
        for (JsonElement optEl : optionsArray) {
            if (!optEl.isJsonObject()) continue;
            JsonObject optObj = optEl.getAsJsonObject();

            String answerText = optObj.has("answer") ? optObj.get("answer").getAsString() : "";
            AnswerResult result = parseResult(optObj.has("result") ? optObj.getAsJsonObject("result") : null);
            options.add(new AnswerOption(answerText, result));
        }

        return new DialogueElement(text, options);
    }

    /**
     * Parses a result object from JSON. The "type" field determines the result type:
     * <ul>
     *   <li>"megamod:dialogue" - nested dialogue (has text/options)</li>
     *   <li>"megamod:advanceobjective" - advance to objective index (has go-to)</li>
     *   <li>"megamod:cancel" - cancel the quest</li>
     *   <li>"megamod:return" - close UI without change</li>
     * </ul>
     */
    private static AnswerResult parseResult(JsonObject result) {
        if (result == null) return new ReturnResult();

        String type = result.has("type") ? result.get("type").getAsString() : "";

        return switch (type) {
            case "megamod:dialogue" -> {
                DialogueElement nested = parse(result);
                yield nested != null ? new DialogueResult(nested) : new ReturnResult();
            }
            case "megamod:advanceobjective" -> {
                int goTo = result.has("go-to") ? result.get("go-to").getAsInt() : -1;
                yield new AdvanceObjectiveResult(goTo);
            }
            case "megamod:cancel" -> new CancelResult();
            case "megamod:return" -> new ReturnResult();
            default -> new ReturnResult();
        };
    }

    /**
     * Resolves participant name placeholders ($0, $1, $2, ...) in dialogue text.
     *
     * @param text           the raw dialogue text
     * @param participantNames ordered list of participant display names
     *                         (index 0 = quest giver, 1+ = participants)
     * @return text with placeholders replaced
     */
    public static String resolveParticipants(String text, List<String> participantNames) {
        if (text == null || participantNames == null) return text;
        String resolved = text;
        for (int i = 0; i < participantNames.size(); i++) {
            resolved = resolved.replace("$" + i, participantNames.get(i));
        }
        return resolved;
    }
}
