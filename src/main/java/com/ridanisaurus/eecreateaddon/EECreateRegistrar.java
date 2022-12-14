package com.ridanisaurus.eecreateaddon;

import com.ridanisaurus.emendatusenigmatica.items.BasicBurnableItem;
import com.ridanisaurus.emendatusenigmatica.items.BasicItem;
import com.ridanisaurus.emendatusenigmatica.loader.parser.model.MaterialModel;
import com.ridanisaurus.emendatusenigmatica.util.Reference;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class EECreateRegistrar {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

	public static Map<String, RegistryObject<Item>> crushedOreMap = new HashMap<>();

	public static void registerCrushedOres(MaterialModel material) {
		String itemName = "crushed_" + material.getId() + "_ore";
		if (material.getProperties().isBurnable()) {
			crushedOreMap.put(material.getId(), ITEMS.register(itemName, () -> new BasicBurnableItem(
					material.getProperties().getBurnTime(),
					material.getColors().getHighlightColor(3),
					material.getColors().getHighlightColor(1),
					material.getColors().getMaterialColor(),
					material.getColors().getShadowColor(1),
					material.getColors().getShadowColor(2)
			)));
		} else {
			crushedOreMap.put(material.getId(), ITEMS.register(itemName, () -> new BasicItem(
					material.getColors().getHighlightColor(3),
					material.getColors().getHighlightColor(1),
					material.getColors().getMaterialColor(),
					material.getColors().getShadowColor(1),
					material.getColors().getShadowColor(2)
			)));
		}
	}

	public static void finalize(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}
