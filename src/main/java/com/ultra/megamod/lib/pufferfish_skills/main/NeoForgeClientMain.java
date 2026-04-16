package com.ultra.megamod.lib.pufferfish_skills.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.client.SkillsClientMod;
import com.ultra.megamod.lib.pufferfish_skills.client.event.ClientEventListener;
import com.ultra.megamod.lib.pufferfish_skills.client.event.ClientEventReceiver;
import com.ultra.megamod.lib.pufferfish_skills.client.keybinding.KeyBindingHandler;
import com.ultra.megamod.lib.pufferfish_skills.client.keybinding.KeyBindingReceiver;
import com.ultra.megamod.lib.pufferfish_skills.client.network.ClientPacketHandler;
import com.ultra.megamod.lib.pufferfish_skills.client.network.ClientPacketSender;
import com.ultra.megamod.lib.pufferfish_skills.client.setup.ClientRegistrar;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;
import com.ultra.megamod.lib.pufferfish_skills.network.OutPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class NeoForgeClientMain {
	private final List<ClientEventListener> clientListeners = new ArrayList<>();
	private final List<KeyMappingWithHandler> keyBindings = new ArrayList<>();
	private final Map<Identifier, CustomPacketPayload.Type<NeoForgeMain.InOutPayload<?>>> outPackets = new HashMap<>();
	private final List<Consumer<PayloadRegistrar>> payloadRegistrations = new ArrayList<>();

	public NeoForgeClientMain(IEventBus modEventBus) {
		SkillsClientMod.setup(
				new ClientRegistrarImpl(),
				new ClientEventReceiverImpl(),
				new KeyBindingReceiverImpl(),
				new ClientPacketSenderImpl()
		);

		modEventBus.addListener(this::onRegisterPayloadHandler);
		modEventBus.addListener(this::onRegisterKeyMappings);

		var neoForgeEventBus = NeoForge.EVENT_BUS;
		neoForgeEventBus.addListener(this::onPlayerLoggedIn);
		neoForgeEventBus.addListener(this::onInputKey);
	}

	private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		for (var keyBinding : keyBindings) {
			event.register(keyBinding.keyBinding);
		}
	}

	private void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
		for (var listener : clientListeners) {
			listener.onPlayerJoin();
		}
	}

	private void onInputKey(InputEvent.Key event) {
		for (var keyBinding : keyBindings) {
			if (keyBinding.keyBinding.consumeClick()) {
				keyBinding.handler.handle();
			}
		}
	}

	private void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
		var registrar = event.registrar(SkillsAPI.MOD_ID);
		for (var payloadRegistration : payloadRegistrations) {
			payloadRegistration.accept(registrar);
		}
	}

	private record KeyMappingWithHandler(KeyMapping keyBinding, KeyBindingHandler handler) { }

	private class ClientRegistrarImpl implements ClientRegistrar {
		@Override
		public <T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ClientPacketHandler<T> handler) {
			var pId = new CustomPacketPayload.Type<NeoForgeMain.InOutPayload<T>>(id);
			payloadRegistrations.add(registrar -> registrar.playToClient(pId, CustomPacketPayload.codecOf(
					(value, buf) -> value.outPacket().write(buf),
					buf -> new NeoForgeMain.InOutPayload<>(pId, reader.apply(buf), null)
			), (payload, context) -> handler.handle(payload.inValue())));
		}
		@Override
		public void registerOutPacket(Identifier id) {
			outPackets.put(id, new CustomPacketPayload.Type<>(id));
		}
	}

	private class ClientEventReceiverImpl implements ClientEventReceiver {
		@Override
		public void registerListener(ClientEventListener eventListener) {
			clientListeners.add(eventListener);
		}
	}

	private class KeyBindingReceiverImpl implements KeyBindingReceiver {
		@Override
		public void registerKeyMapping(KeyMapping keyBinding, KeyBindingHandler handler) {
			keyBindings.add(new KeyMappingWithHandler(keyBinding, handler));
		}
	}

	private class ClientPacketSenderImpl implements ClientPacketSender {
		@Override
		public void send(OutPacket packet) {
			Objects.requireNonNull(Minecraft.getInstance().getNetworkHandler())
					.send(new ServerboundCustomPayloadPacket(
							new NeoForgeMain.InOutPayload<>(outPackets.get(packet.getId()), null, packet)
					));
		}
	}
}
