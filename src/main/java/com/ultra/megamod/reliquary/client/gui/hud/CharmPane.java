package com.ultra.megamod.reliquary.client.gui.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.reliquary.client.gui.components.Box;
import com.ultra.megamod.reliquary.client.gui.components.Component;
import com.ultra.megamod.reliquary.client.gui.components.ItemStackPane;
import com.ultra.megamod.reliquary.reference.Config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CharmPane extends Component {
	private static Box mainPane = Box.createVertical();
	private static final Lock LOCK = new ReentrantLock();

	@Override
	public int getHeightInternal() {
		return mainPane.getHeight();
	}

	@Override
	public int getWidthInternal() {
		return mainPane.getWidth();
	}

	@Override
	public int getPadding() {
		return 0;
	}

	@Override
	public boolean shouldRender() {
		removeExpiredMobCharms();
		boolean isEmpty;
		LOCK.lock();
		try {
			isEmpty = charmsToDraw.isEmpty();
		} finally {
			LOCK.unlock();
		}
		return !isEmpty;
	}

	private static void updateCharmsPane() {
		LOCK.lock();
		Component[] components;
		try {
			components = new Component[charmsToDraw.size()];
			AtomicInteger i = new AtomicInteger(0);
			charmsToDraw.forEach((slot, charmToDraw) -> {
				int index = i.getAndIncrement();
				components[index] = new ItemStackPane(charmToDraw.getCharm(), true);
			});
		} finally {
			LOCK.unlock();
		}
		mainPane = Box.createVertical(components);
	}

	@Override
	public void renderInternal(GuiGraphics guiGraphics, int x, int y) {
		mainPane.render(guiGraphics, x, y);
	}

	private static final Map<Integer, CharmToDraw> charmsToDraw = new HashMap<>();

	private static class CharmToDraw {
		CharmToDraw(ItemStack charm, long time) {
			this.charm = charm;
			this.time = time;
		}

		final ItemStack charm;
		final long time;

		ItemStack getCharm() {
			return charm;
		}
	}

	public static void addCharmToDraw(ItemStack charm, int slot) {
		int maxMobCharmsToDisplay = Config.COMMON.items.mobCharm.maxCharmsToDisplay.get();
		LOCK.lock();
		try {
			if (charmsToDraw.size() == maxMobCharmsToDisplay) {
				charmsToDraw.remove(0);
			}

			if (charm.isEmpty()) {
				charmsToDraw.remove(slot);
			} else {
				charmsToDraw.put(slot, new CharmToDraw(charm, System.currentTimeMillis()));
			}
		} finally {
			LOCK.unlock();
		}
		updateCharmsPane();
	}

	private static void removeExpiredMobCharms() {
		int secondsToExpire = 4;
		boolean changed = false;
		LOCK.lock();
		try {
			for (Iterator<Map.Entry<Integer, CharmToDraw>> iterator = charmsToDraw.entrySet().iterator(); iterator.hasNext(); ) {
				Map.Entry<Integer, CharmToDraw> entry = iterator.next();
				CharmToDraw charmToDraw = entry.getValue();
				float percentToMaxDamage = 1 - (float) charmToDraw.getCharm().getDamageValue() / charmToDraw.getCharm().getMaxDamage();

				int expirationDuration = secondsToExpire * 1000;
				if (percentToMaxDamage < 0.1f) {
					expirationDuration = (int) (expirationDuration + (expirationDuration * 2 * (1 - percentToMaxDamage * 10)));
				}

				if (charmToDraw.time + expirationDuration < System.currentTimeMillis()) {
					iterator.remove();
					changed = true;
				}
			}
		} finally {
			LOCK.unlock();
		}

		if (changed) {
			updateCharmsPane();
		}
	}
}
