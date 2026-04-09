/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.economy.shop;

public record ShopItem(String itemId, String displayName, int buyPrice, int sellPrice) {
    public String toJson() {
        return "{\"id\":\"" + this.itemId + "\",\"name\":\"" + this.displayName + "\",\"buy\":" + this.buyPrice + ",\"sell\":" + this.sellPrice + "}";
    }

    public static ShopItem fromJson(String json) {
        String id = ShopItem.extractString(json, "id");
        String name = ShopItem.extractString(json, "name");
        int buy = ShopItem.extractInt(json, "buy");
        int sell = ShopItem.extractInt(json, "sell");
        return new ShopItem(id, name, buy, sell);
    }

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            return "";
        }
        int end = json.indexOf("\"", start += search.length());
        if (end == -1) {
            return "";
        }
        return json.substring(start, end);
    }

    private static int extractInt(String json, String key) {
        int end;
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) {
            return 0;
        }
        for (end = start += search.length(); end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-'); ++end) {
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }
}

