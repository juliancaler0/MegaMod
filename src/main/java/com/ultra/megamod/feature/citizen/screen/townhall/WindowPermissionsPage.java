package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.controls.TextField;
import com.ultra.megamod.feature.citizen.blockui.views.DropDownList;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Town hall permissions page.
 * Manages player ranks, permission actions, free interaction blocks.
 * Ported from MineColonies WindowPermissionsPage.
 *
 * @param <V> Town hall view type.
 */
public class WindowPermissionsPage<V> extends AbstractWindowTownHall<V>
{
    /**
     * Rank type translation keys.
     */
    private static final String RANK_TYPE_COLONY_MANAGER = "com.minecolonies.coremod.permission.ranktype.colonymanager";
    private static final String RANK_TYPE_HOSTILE = "com.minecolonies.coremod.permission.ranktype.hostile";
    private static final String RANK_TYPE_NONE = "com.minecolonies.coremod.permission.ranktype.none";

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList userList;

    /**
     * The ScrollingList of the permission actions.
     */
    private ScrollingList actionsList;

    /**
     * The ScrollingList of the free blocks.
     */
    private ScrollingList freeBlocksList;

    /**
     * The ScrollingList of all rank buttons.
     */
    private final ScrollingList rankButtonList;

    /**
     * A list of available rank types.
     */
    private Map<Integer, String> rankTypes = new HashMap<>();

    /**
     * Constructor for the town hall window.
     *
     * @param building the town hall view.
     */
    public WindowPermissionsPage(final V building)
    {
        super(building, "layoutpermissions.xml");

        rankTypes.put(0, RANK_TYPE_COLONY_MANAGER);
        rankTypes.put(1, RANK_TYPE_HOSTILE);
        rankTypes.put(2, RANK_TYPE_NONE);

        rankButtonList = findPaneOfTypeByID(TOWNHALL_RANK_BUTTON_LIST, ScrollingList.class);
        actionsList = findPaneOfTypeByID(TOWNHALL_RANK_LIST, ScrollingList.class);

        registerButton(BUTTON_ADD_PLAYER, this::addPlayerClicked);
        registerButton(BUTTON_REMOVE_PLAYER, this::removePlayerClicked);
        registerButton(BUTTON_TRIGGER, this::trigger);
        registerButton(BUTTON_ADD_BLOCK, this::addBlock);
        registerButton(BUTTON_REMOVE_BLOCK, this::removeBlock);
        registerButton(BUTTON_BLOCK_TOOL, this::giveBlockTool);
        registerButton(BUTTON_ADD_RANK, this::addRank);
        registerButton(TOWNHALL_RANK_BUTTON, this::onRankButtonClicked);
        registerButton(BUTTON_REMOVE_RANK, this::onRemoveRankButtonClicked);
        registerButton(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, this::addPlayerToColonyClicked);
        registerButton(BUTTON_OPEN_ONLINE_PLAYER_LIST, this::onPickPlayer);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        fillUserList();
        fillFreeBlockList();
        fillRanks();
        fillPermissionList();
    }

    private void addPlayerClicked()
    {
        final TextField input = findPaneOfTypeByID(INPUT_ADDPLAYER_NAME, TextField.class);
        if (input != null)
        {
            // TODO: Network.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(buildingView.getColony(), input.getText()));
            input.setText("");
        }
    }

    private void removePlayerClicked(final Button button)
    {
        // TODO: Get player from userList index and send PermissionsMessage.RemovePlayer
    }

    private void addPlayerToColonyClicked(@NotNull final Button button)
    {
        // TODO: Get permission event from eventList index and add player
    }

    private void onPickPlayer(final Button button)
    {
        // TODO: Show online player picker list
    }

    private void trigger(@NotNull final Button button)
    {
        // TODO: Toggle permission action for current rank
    }

    private void addBlock()
    {
        // TODO: Parse block/position from input and send ChangeFreeToInteractBlockMessage
    }

    private void removeBlock(final Button button)
    {
        // TODO: Remove free block/position and send message
    }

    private void giveBlockTool(final Button button)
    {
        // TODO: Network.getNetwork().sendToServer(new GiveToolMessage(buildingView, ModItems.permTool));
    }

    private void addRank()
    {
        final TextField input = findPaneOfTypeByID(INPUT_ADDRANK_NAME, TextField.class);
        if (input != null && !input.getText().isEmpty())
        {
            // TODO: Network.getNetwork().sendToServer(new PermissionsMessage.AddRank(buildingView.getColony(), input.getText()));
            input.setText("");
        }
    }

    private void onRankButtonClicked(@NotNull final Button button)
    {
        // TODO: Switch active rank for permissions editing
    }

    private void onRemoveRankButtonClicked(Button button)
    {
        // TODO: Send PermissionsMessage.RemoveRank
    }

    /**
     * Fill the rank button list in the GUI.
     */
    private void fillRanks()
    {
        if (rankButtonList == null)
        {
            return;
        }

        rankButtonList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 0; // TODO: return allRankList.size() from colony permissions
            }

            @Override
            public void updateElement(final int i, final Pane pane)
            {
                // TODO: Populate rank button text
            }
        });

        DropDownList dropdown = findPaneOfTypeByID(TOWNHALL_RANK_TYPE_PICKER, DropDownList.class);
        if (dropdown != null)
        {
            dropdown.setDataProvider(new DropDownList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return rankTypes.size();
                }

                @Override
                public MutableComponent getLabel(final int i)
                {
                    return Component.translatable(rankTypes.get(i));
                }
            });
        }
    }

    private void fillPermissionList()
    {
        if (actionsList == null)
        {
            return;
        }

        actionsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 0; // TODO: return actions.size()
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                // TODO: Populate action toggle buttons
            }
        });
    }

    private void fillUserList()
    {
        userList = findPaneOfTypeByID(LIST_USERS, ScrollingList.class);
        if (userList == null)
        {
            return;
        }

        userList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 0; // TODO: return users.size() from colony permissions
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                // TODO: Populate user name, rank dropdown
            }
        });
    }

    private void fillFreeBlockList()
    {
        freeBlocksList = findPaneOfTypeByID(LIST_FREE_BLOCKS, ScrollingList.class);
        if (freeBlocksList == null)
        {
            return;
        }

        freeBlocksList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 0; // TODO: return freeBlocks.size() + freePositions.size()
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                // TODO: Populate block/position names
            }
        });
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_PERMISSIONS;
    }
}
