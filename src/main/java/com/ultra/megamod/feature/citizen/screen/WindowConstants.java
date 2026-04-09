package com.ultra.megamod.feature.citizen.screen;

import net.minecraft.resources.Identifier;

/**
 * Class which contains all constants required for colony GUI windows.
 * Ported from MineColonies WindowConstants.
 */
public final class WindowConstants
{
    // ── General Buttons ──────────────────────────────────────────────────

    public static final String BUTTON_EXIT = "exit";
    public static final String BUTTON_INFO = "info";
    public static final String BUTTON_INFOPAGE = "infopage";
    public static final String BUTTON_ACTIONS = "actions";
    public static final String BUTTON_ALLIANCE = "alliances";
    public static final String BUTTON_SETTINGS = "settings";
    public static final String BUTTON_PERMISSIONS = "permissions";
    public static final String BUTTON_CITIZENS = "citizens";
    public static final String BUTTON_WORKORDER = "workOrder";
    public static final String BUTTON_STATS = "happiness";
    public static final String BUTTON_RECALL = "recall";
    public static final String BUTTON_CHANGE_SPEC = "changeSpec";
    public static final String BUTTON_RENAME = "rename";
    public static final String BUTTON_PATREON = "patreon";
    public static final String BUTTON_MERCENARY = "mercenaries";
    public static final String BUTTON_TOWNHALLMAP = "map";
    public static final String BUTTON_ADD_PLAYER = "addPlayer";
    public static final String BUTTON_TOGGLE_JOB = "toggleJob";
    public static final String BUTTON_TOGGLE_HOUSING = "toggleHousing";
    public static final String BUTTON_TOGGLE_MOVE_IN = "toggleMoveIn";
    public static final String BUTTON_TOGGLE_ENTER_LEAVE_MESSAGES = "toggleentermessages";
    public static final String BUTTON_TOGGLE_PRINT_PROGRESS = "togglePrintProgress";
    public static final String BUTTON_COLONY_SWITCH_STYLE = "colonyStylePicker";
    public static final String BUTTON_RESET_TEXTURE = "resettexture";
    public static final String BUTTON_BANNER_PICKER = "bannerPicker";
    public static final String BUTTON_REMOVE_PLAYER = "removePlayer";
    public static final String BUTTON_UP = "plus";
    public static final String BUTTON_TP = "tp";
    public static final String BUTTON_DOWN = "minus";
    public static final String BUTTON_DELETE = "delete";
    public static final String BUTTON_ABANDON = "abandon";
    public static final String BUTTON_CONFIRM_DELETE = "confirmdelete";
    public static final String BUTTON_CONFIRM_ABANDON = "confirmabandon";
    public static final String INPUT_ADDPLAYER_NAME = "addPlayerName";

    // ── Page & View IDs ──────────────────────────────────────────────────

    public static final String VIEW_PAGES = "pages";
    public static final String PAGE_INFO = "pageInfo";
    public static final String PAGE_ACTIONS = "pageActions";
    public static final String PAGE_SETTINGS = "pageSettings";
    public static final String PAGE_PERMISSIONS = "pagePermissions";
    public static final String PAGE_CITIZENS = "pageCitizens";
    public static final String PAGE_WORKORDER = "pageWorkOrder";
    public static final String PAGE_HAPPINESS = "pageHappiness";

    // ── List IDs ─────────────────────────────────────────────────────────

    public static final String LIST_USERS = "users";
    public static final String EVENTS_LIST = "eventsList";
    public static final String TOTAL_CITIZENS_LABEL = "totalCitizens";
    public static final String LIST_CITIZENS = "citizenList";
    public static final String LIST_HAPPINESS = "happinessList";
    public static final String LIST_ALLIES = "allies";
    public static final String LIST_FEUDS = "feuds";
    public static final String LIST_WORKORDER = "workOrderList";

    // ── Label IDs ────────────────────────────────────────────────────────

    public static final String HAPPINESS_LABEL = "happinessLevel";
    public static final String NAME_LABEL = "name";
    public static final String SEARCH_INPUT = "search";
    public static final String POS_LABEL = "pos";
    public static final String ACTION_LABEL = "action";
    public static final String CITIZEN_INFO = "citizenInfoBox";
    public static final String RECALL_ONE = "recallone";
    public static final String JOB_LABEL = "job";
    public static final String HEALTH_SHORT_LABEL = "health";
    public static final String ENTITY_ICON = "entity";
    public static final String HAPPINESS_SHORT_LABEL = "happinessLevel";
    public static final String SATURATION_SHORT_LABEL = "saturation";
    public static final String DIST_LABEL = "dist";
    public static final String CITIZENS_AMOUNT_LABEL = "citizensAmount";
    public static final String HIDDEN_CITIZEN_ID = "hiddenCitizenId";
    public static final String ASSIGNEE_LABEL = "assignee";
    public static final String WORK_LABEL = "work";
    public static final String HIDDEN_WORKORDER_ID = "hiddenId";
    public static final String BUTTON_ADD_PLAYER_OR_FAKEPLAYER = "addfakeplayer";
    public static final String BUTTON_OPEN_ONLINE_PLAYER_LIST = "addOnlinePlayer";
    public static final String LIST_SELECT_PLAYER = "playerPicker";
    public static final String BUTTON_SELECT_PLAYER_LIST = "playerSelectButton";

    // ── Permissions ──────────────────────────────────────────────────────

    public static final String PERMISSION_VIEW = "managePermissions";
    public static final String BUTTON_TRIGGER = "trigger";
    public static final String LIST_FREE_BLOCKS = "blocks";
    public static final String KEY_TO_PERMISSIONS = "com.minecolonies.coremod.permission.";
    public static final String BUTTON_ADD_BLOCK = "addBlock";
    public static final String INPUT_BLOCK_NAME = "addBlockName";
    public static final String BUTTON_REMOVE_BLOCK = "removeBlock";
    public static final String BUTTON_BLOCK_TOOL = "blockTool";
    public static final String BUTTON_SELECT = "select";

    // ── Style Dropdown IDs ───────────────────────────────────────────────

    public static final String BUTTON_PREVIOUS_STYLE_ID = "previousStyle";
    public static final String DROPDOWN_STYLE_ID = "style";
    public static final String DROPDOWN_BUILDER_ID = "worker";
    public static final String BUTTON_NEXT_STYLE_ID = "nextStyle";
    public static final String BUTTON_PREVIOUS_COLOR_ID = "previousColor";
    public static final String DROPDOWN_INTERVAL_ID = "intervals";
    public static final String DROPDOWN_COLOR_ID = "colorPicker";
    public static final String DROPDOWN_TEXT_ID = "textureStylePicker";
    public static final String DROPDOWN_NAME_ID = "nameStylePicker";
    public static final String BUTTON_TEXTURE_SET = "switchstyle";
    public static final String BUTTON_NEXT_COLOR_ID = "nextColor";

    // ── Build / Navigation Buttons ───────────────────────────────────────

    public static final String BUTTON_CANCEL = "cancel";
    public static final String BUTTON_FORWARD = "up";
    public static final String BUTTON_BACKWARD = "down";
    public static final String BUTTON_TOGGLE = "toggle";

    // ── Resource List IDs ────────────────────────────────────────────────

    public static final String LIST_RESOURCES = "resources";
    public static final String LIST_WORK_ORDERS = "workOrders";
    public static final String RESOURCE_NAME = "resourceName";
    public static final String RESOURCE_AVAILABLE_NEEDED = "resourceAvailableNeeded";
    public static final String RESOURCE_MISSING = "resourceMissing";
    public static final String RESOURCE_ADD = "resourceAdd";
    public static final String RESOURCE_ID = "resourceId";
    public static final String RESOURCE_QUANTITY_MISSING = "resourceQuantity";
    public static final String RESOURCE_ICON = "resourceIcon";
    public static final String STAR_IMAGE = "star";

    public static final String STOCK_ADD = "addStock";
    public static final String STOCK_REMOVE = "removeStock";
    public static final String QUANTITY_LABEL = "resourceQty";
    public static final String IN_DELIVERY_ICON = "indeliveryicon";
    public static final String IN_DELIVERY_AMOUNT = "indeliveryamount";
    public static final String IN_WAREHOUSE_ICON = "inWarehouseIcon";
    public static final String IN_WAREHOUSE_AMOUNT = "inWarehouseAmount";
    public static final String WORK_ORDER_NAME = "buildingName";
    public static final String WORK_ORDER_POS = "buildingPos";
    public static final String WORK_ORDER_SELECT = "manage";

    public static final String GUIDE_CONFIRM = "confirm";
    public static final String GUIDE_CLOSE = "close";

    // ── Sorting ──────────────────────────────────────────────────────────

    public static final int NO_SORT = 0;
    public static final int ASC_SORT = 1;
    public static final int DESC_SORT = 2;
    public static final int COUNT_ASC_SORT = 3;
    public static final int COUNT_DESC_SORT = 4;
    public static final String LIST_ALLINVENTORY = "allinventorylist";
    public static final String BUTTON_SORT = "sortStorageFilter";

    // ── Building Buttons ─────────────────────────────────────────────────

    public static final String BUTTON_BUILD = "build";
    public static final String BUTTON_REPAIR = "repair";
    public static final String BUTTON_INVENTORY = "inventory";
    public static final String BUTTON_ALLINVENTORY = "allinventory";
    public static final String LABEL_BUILDING_NAME = "name";
    public static final String BUTTON_PREVPAGE = "prevPage";
    public static final String BUTTON_NEXTPAGE = "nextPage";
    public static final String LABEL_NO_UPGRADE = "infotextnoupgrade";
    public static final String UNI_INV_RESEARCH = "invresearch";
    public static final String BUTTON_PRESTIGE = "prestige";
    public static final String BUTTON_PRESTIGE_ICON = "prestigeicon";
    public static final String BUTTON_MAP = "map";
    public static final String BUTTON_MAP_ICON = "mapicon";

    // ── Guard Tower ──────────────────────────────────────────────────────

    public static final String GUI_LIST_ELEMENT_NAME = "name";
    public static final String GUI_LIST_BUTTON_SWITCH = "switch";
    public static final String LEVEL_LABEL = "level";

    // ── Misc Buttons ─────────────────────────────────────────────────────

    public static final String BUTTON_REMOVE = "remove";
    public static final String BUTTON_DECONSTRUCT_BUILDING = "deconstruct";
    public static final String BUTTON_PICKUP_BUILDING = "pickup";

    // ── Happiness Icons ──────────────────────────────────────────────────

    public static final String UNHAPPY_ICON = "megamod:textures/gui/citizen/unhappy_icon.png";
    public static final String UNSATISFIED_ICON = "megamod:textures/gui/citizen/unsatisfied_icon.png";
    public static final String HAPPY_ICON = "megamod:textures/gui/citizen/happy_icon.png";
    public static final String SATISFIED_ICON = "megamod:textures/gui/citizen/satisfied_icon.png";

    public static final String LABEL_CONSTRUCTION_NAME = "constructionName";
    public static final String LABEL_PROGRESS = "progress";
    public static final String STEP_PROGRESS = "stepprogress";
    public static final String LABEL_WORKERNAME = "workerName";
    public static final String LABEL_PAGE_NUMBER = "pageNum";

    // ── Request IDs ──────────────────────────────────────────────────────

    public static final String REQUEST_FULFILL = "fulfill";
    public static final String REQUEST_CANCEL = "cancel";

    // ── Heart / Health Constants ──────────────────────────────────────────

    public static final int XP_HEIGHT = 5;
    public static final int LEFT_BORDER_X = 10;
    public static final int LEFT_BORDER_Y = 10;
    public static final int XP_BAR_ICON_COLUMN = 0;
    public static final int XP_BAR_ICON_COLUMN_END = 172;
    public static final int XP_BAR_ICON_COLUMN_END_WIDTH = 10;
    public static final int XP_BAR_ICON_END_OFFSET = 90;
    public static final int XP_BAR_WIDTH = 182 / 2;
    public static final int HAPPINESS_BAR_EMPTY_ROW = 0;
    public static final int HAPPINESS_BAR_FULL_ROW = 0;

    public static final int EMPTY_HEART_ICON_X = 16;
    public static final int RED_HEART_ICON_X = 52;
    public static final int HALF_RED_HEART_ICON_X = 61;
    public static final int GOLD_HEART_ICON_X = 160;
    public static final int HALF_GOLD_HEART_ICON_X = 169;
    public static final int HEART_ICON_MC_Y = 0;

    public static final Identifier GREEN_BLUE_ICON = Identifier.fromNamespaceAndPath("megamod", "textures/gui/citizen/green_bluehearts.png");

    public static final int GREEN_HEART_ICON_X = 0;
    public static final int GREEN_HALF_HEART_ICON_X = 8;
    public static final int GREEN_HEARTS_ICON_Y = 0;
    public static final int BLUE_HEART_ICON_X = 0;
    public static final int BLUE_HALF_HEART_ICON_X = 8;
    public static final int BLUE_HEARTS_ICON_Y = 8;

    public static final int HEART_ICON_HEIGHT_WIDTH = 9;
    public static final int HEART_ICON_POS_X = 10;
    public static final int HEART_ICON_OFFSET_X = 10;
    public static final int HEART_ICON_POS_Y = 10;
    public static final int MAX_HEART_ICONS = 10;

    public static final int EMPTY_HEART_VALUE = 0;
    public static final int RED_HEART_VALUE = 2;
    public static final int GOLDEN_HEART_VALUE = 4;
    public static final int GREEN_HEART_VALUE = 6;
    public static final int BLUE_HEART_VALUE = 8;

    // ── Saturation Constants ─────────────────────────────────────────────

    public static final int SATURATION_ICON_POS_Y = 10;
    public static final int SATURATION_ICON_COLUMN = 27;
    public static final int SATURATION_ICON_HEIGHT_WIDTH = 9;
    public static final int SATURATION_ICON_POS_X = 10;
    public static final int SATURATION_ICON_OFFSET_X = 10;

    // ── Citizen Window IDs ───────────────────────────────────────────────

    public static final String WINDOW_ID_NAME = "name";
    public static final String STATUS_ICON = "statusicon";
    public static final String WINDOW_ID_HAPPINESS = "happinessLabel";
    public static final String WINDOW_ID_HEALTHBAR = "healthBar";
    public static final String WINDOW_ID_HEALTHLABEL = "healthLabel";

    public static final int EMPTY_SATURATION_ITEM_ROW_POS = 16;
    public static final int FULL_SATURATION_ITEM_ROW_POS = 16 + 36;
    public static final int HALF_SATURATION_ITEM_ROW_POS = 16 + 45;

    public static final String WINDOW_ID_SATURATION_BAR = "saturationBar";
    public static final String WINDOW_ID_HAPPINESS_BAR = "happinessBar";
    public static final String WINDOW_ID_GENDER = "gender";
    public static final String WINDOW_ID_LIST_REQUESTS = "requests";
    public static final String LIST_ELEMENT_ID_REQUEST_STACK = "requestStack";
    public static final String DELIVERY_IMAGE = "deliveryImage";
    public static final String BUTTON_BACK = "back";

    public static final String FEMALE_SOURCE = "megamod:textures/gui/citizen/colonist_wax_female_smaller.png";
    public static final String MALE_SOURCE = "megamod:textures/gui/citizen/colonist_wax_male_smaller.png";

    // ── Request Detail ───────────────────────────────────────────────────

    public static final String REQUEST_DETAIL = "detail";
    public static final String REQUEST_SHORT_DETAIL = "shortDetail";
    public static final String REQUEST_PRIORITY = "priority";
    public static final String REQUESTER = "requester";
    public static final String PARENT = "parent";
    public static final int LIFE_COUNT_DIVIDER = 30;

    // ── Postbox ──────────────────────────────────────────────────────────

    public static final String INPUT_NAME = "name";
    public static final String INPUT_QTY = "qty";
    public static final String BUTTON_REQUEST = "request";
    public static final String BUTTON_DONE = "done";
    public static final String BUTTON_HIRE = "hire";

    // ── Citizen Assignment ───────────────────────────────────────────────

    public static final String CITIZEN_LABEL = "citizen";
    public static final String UNASSIGNED_CITIZEN_LIST = "unassigned";
    public static final String ASSIGNED_CITIZEN_LIST = "assigned";
    public static final String CITIZEN_DONE = "done";
    public static final String CITIZEN_JOB = "job";
    public static final String CITIZEN_LIVING = "living";
    public static final String BUTTON_EDIT_NAME = "editName";
    public static final String BUTTON_MODE = "mode";
    public static final String BUTTON_JOB = "job";
    public static final String TOGGLE_SHOW_EMPLOYED = "showEmployed";
    public static final String CITIZEN_LIST_UNEMP = "unemployed";
    public static final String JOB_LIST = "jobs";
    public static final String ATTRIBUTES_LABEL = "attributes";
    public static final String DISTANCE_LABEL = "distance";

    // ── Worker Buttons ───────────────────────────────────────────────────

    public static final String BUTTON_FIRE = "fire";
    public static final String BUTTON_PAUSE = "pause";
    public static final String BUTTON_RESTART = "restart";
    public static final String BUTTON_BUILDTOOL = "buildtool";
    public static final String BUTTON_DIRECT = "direct";
    public static final String BUTTON_REACTIVATE = "reactivate";

    // ── Interaction Window ───────────────────────────────────────────────

    public static final String CHAT_LABEL_ID = "chat";
    public static final int SLIGHTLY_BLUE = 100;
    public static final int BUTTON_HEIGHT = 17;
    public static final int BUTTON_LENGTH = 129;
    public static final int BUTTON_Y_BUFFER = 3;
    public static final int BUTTON_X_BUFFER = 10;
    public static final String RESPONSE_BOX_ID = "responseOptions";
    public static final String MEDIUM_SIZED_BUTTON_RES = "textures/gui/builderhut/builder_button_medium_large.png";
    public static final String MEDIUM_SIZED_BUTTON_DIS = "textures/gui/builderhut/builder_button_medium_large_disabled.png";

    // ── Job/Skill Page ───────────────────────────────────────────────────

    public static final String JOB_TITLE_LABEL = "jobLabel";
    public static final String JOB_DESC_LABEL = "skillexplanation";
    public static final String PRIMARY_SKILL_LABEL = "primary";
    public static final String PRIMARY_SKILL_COM = "comp1";
    public static final String PRIMARY_SKILL_ADV = "adverse1";
    public static final String SECONDARY_SKILL_LABEL = "secondary";
    public static final String SECONDARY_SKILL_COM = "comp2";
    public static final String SECONDARY_SKILL_ADV = "adverse2";
    public static final String IMAGE_APPENDIX = "img";
    public static final String BASE_IMG_SRC = "megamod:textures/entity/skills/small/";

    // ── Button Prefixes ──────────────────────────────────────────────────

    public static final String PLUS_PREFIX = "plus_";
    public static final String MINUS_PREFIX = "minus_";
    public static final String LOCATE = "locate";
    public static final String CLIPBOARD_TOGGLE = "important";
    public static final String BUTTON_ASSIGN = "assign";
    public static final String LABEL_NAME = "name";
    public static final String LABEL_DIST = "dist";
    public static final String LIST_CITIZEN = "assignedCitizen";
    public static final String UPGRADE_PROGRESS_LABEL = "upgradeProgress";

    // ── Town Hall Rank System ────────────────────────────────────────────

    public static final String TOWNHALL_RANK_BUTTON_LIST = "rankButtonList";
    public static final String TOWNHALL_RANK_BUTTON = "rankButton";
    public static final String BUTTON_ADD_RANK = "buttonAddRank";
    public static final String INPUT_ADDRANK_NAME = "addRankName";
    public static final String BUTTON_REMOVE_RANK = "removeRank";
    public static final String TOWNHALL_RANK_LIST = "rankList";
    public static final String TOWNHALL_ADD_RANK_ERROR = "rankNameError";
    public static final String TOWNHALL_RANK_PICKER = "rankPicker";
    public static final String TOWNHALL_PERM_MANAGEMENT = "permissionsManagement";
    public static final String TOWNHALL_PERM_LIST = "permissionsList";
    public static final String TOWNHALL_PERM_SETTINGS = "permissionsSettings";
    public static final String TOWNHALL_PERM_MODE_TOGGLE = "permissionsModeToggle";
    public static final String TOWNHALL_RANK_TYPE_PICKER = "rankTypePicker";

    // ── Misc ─────────────────────────────────────────────────────────────

    public static final String INPUT_FILTER = "input";
    public static final String DESC_LABEL = "desc";
    public static final String BUTTON_SWITCH = "switch";
    public static final String BUTTON_RESET_DEFAULT = "resetDefault";
    public static final String LIST_SETTINGS = "settingslist";
    public static final String BUTTON_PLACE = "place";
    public static final String TITLE_LABEL = "title";

    private WindowConstants()
    {
        // Intentionally left empty.
    }
}
