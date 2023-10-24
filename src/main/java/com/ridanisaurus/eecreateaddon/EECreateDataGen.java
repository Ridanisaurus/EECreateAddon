package com.ridanisaurus.eecreateaddon;

/*
 *  MIT License
 *
 *  Copyright (c) 2020 Ridanisaurus
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import com.google.common.collect.Lists;
import com.ridanisaurus.emendatusenigmatica.api.EmendatusDataRegistry;
import com.ridanisaurus.emendatusenigmatica.datagen.base.*;
import com.ridanisaurus.emendatusenigmatica.loader.parser.model.CompatModel;
import com.ridanisaurus.emendatusenigmatica.loader.parser.model.MaterialModel;
import com.ridanisaurus.emendatusenigmatica.registries.EERegistrar;
import com.ridanisaurus.emendatusenigmatica.registries.EETags;
import com.ridanisaurus.emendatusenigmatica.util.Reference;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Consumer;

public class EECreateDataGen {

	public static class VanillaRecipes extends RecipeProvider {

		private final EmendatusDataRegistry registry;

		public VanillaRecipes(DataGenerator gen, EmendatusDataRegistry registry) {
			super(gen);
			this.registry = registry;
		}

		@Override
		protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
			for (MaterialModel material : registry.getMaterials()) {
				List<String> processedType = material.getProcessedTypes();
				if (processedType.contains("crushed_ore") && processedType.contains("ingot")) {
					// Ingot from Smelting Crushed Ore
					SimpleCookingRecipeBuilder.smelting(Ingredient.of(EECreateTags.MATERIAL_CRUSHED_ORE.apply(material.getId())),
									EERegistrar.ingotMap.get(material.getId()).get(), 0.1F, 200)
							.unlockedBy("cobblestone", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.COBBLESTONE))
							.save(consumer, new ResourceLocation(Reference.MOD_ID, "ingot/from_crushed_ore/smelting/" + material.getId()));
					// Ingot from Blasting Crushed Ore
					SimpleCookingRecipeBuilder.blasting(Ingredient.of(EECreateTags.MATERIAL_CRUSHED_ORE.apply(material.getId())),
									EERegistrar.ingotMap.get(material.getId()).get(), 0.1F, 100)
							.unlockedBy("cobblestone", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.COBBLESTONE))
							.save(consumer, new ResourceLocation(Reference.MOD_ID, "ingot/from_crushed_ore/blasting/" + material.getId()));
				}
			}
		}
	}

	public static class CreateRecipes extends EERecipeProvider {

		private final EmendatusDataRegistry registry;

		public CreateRecipes(DataGenerator gen, EmendatusDataRegistry registry) {
			super(gen);
			this.registry = registry;
		}

		@Override
		protected void buildRecipes(Consumer<IFinishedGenericRecipe> consumer) {
			for (MaterialModel material : registry.getMaterials()) {
				if (material.getCompat().getCreateCompat()) {
					List<String> processedType = material.getProcessedTypes();
					// CRUSHING WHEEL
					for (CompatModel compat : registry.getCompat()) {
						if (compat.getId().equals(material.getId()) && material.isModded()) {
							for (CompatModel.CompatRecipesModel recipe : compat.getRecipes()) {
								if (recipe.getMod().equals("create") && recipe.getMachine().equals("crushing_wheels")) {
									for (CompatModel.CompatValuesModel value : recipe.getValues()) {
										if (value.getType().equals("ore") && processedType.contains("ore")) {
											// Metal Ore to Crushing Ore
											if (processedType.contains("crushed_ore") && material.getProperties().getMaterialType().equals("metal")) {
												new RecipeBuilder("results")
														.type("create:crushing")
														.group("emendatusenigmatica:compat_recipe")
														.fieldJson("ingredients", new RecipeBuilder.JsonItemBuilder(true)
																.tag(EETags.MATERIAL_ORE.apply(material.getId()))
														)
														.fieldInt("processingTime", 250)
														.addOutput(builder -> builder
																.stack(EECreateRegistrar.crushedOreMap.get(material.getId()).get())
																.stacks(value.getOutput())
														)
														.save(consumer, new ResourceLocation(Reference.MOD_ID, "crushed/from_ore_crushing/" + material.getId()));
											}
											// Gem Ore to Gem
											if (material.getProperties().getMaterialType().equals("gem")) {
												new RecipeBuilder("results")
														.type("create:crushing")
														.group("emendatusenigmatica:compat_recipe")
														.fieldJson("ingredients", new RecipeBuilder.JsonItemBuilder(true)
																.tag(EETags.MATERIAL_ORE.apply(material.getId()))
														)
														.fieldInt("processingTime", 350)
														.addOutput(builder -> builder
																.stack((processedType.contains("gem") ? EERegistrar.gemMap.get(material.getId()).get() : material.getOreDrop().getDefaultItemDropAsItem()))
																.stacks(value.getOutput())
														)
														.save(consumer, new ResourceLocation(Reference.MOD_ID, "gem/from_ore_crushing/" + material.getId()));
											}

										}
									}
								}
							}
						}
					}
					if (processedType.contains("crushed_ore") && material.isModded() && processedType.contains("raw")) {
						// Raw Material to Crushed Ore
						new RecipeBuilder("results")
								.type("create:crushing")
								.group("emendatusenigmatica:compat_recipe")
								.fieldJson("ingredients", new RecipeBuilder.JsonItemBuilder(true)
										.stack(EERegistrar.rawMap.get(material.getId()).get()))
								.fieldInt("processingTime", 400)
								.addOutput(builder -> builder
										.stackWithCount(EECreateRegistrar.crushedOreMap.get(material.getId()).get(), 1)
										.stackWithChance(ForgeRegistries.ITEMS.getValue(new ResourceLocation(EECreateAddon.CREATE, "experience_nugget")), 1, 0.75))
								.save(consumer, new ResourceLocation(Reference.MOD_ID, "crushed/from_raw_crushing/" + material.getId()));
						// Raw Block to Crushed Ore
						new RecipeBuilder("results")
								.type("create:crushing")
								.group("emendatusenigmatica:compat_recipe")
								.fieldJson("ingredients", new RecipeBuilder.JsonItemBuilder(true)
										.stack(EERegistrar.rawBlockItemMap.get(material.getId()).get()))
								.fieldInt("processingTime", 400)
								.addOutput(builder -> builder
										.stackWithCount(EECreateRegistrar.crushedOreMap.get(material.getId()).get(), 9)
										.stackWithChance(ForgeRegistries.ITEMS.getValue(new ResourceLocation(EECreateAddon.CREATE, "experience_nugget")), 9, 0.75))
								.save(consumer, new ResourceLocation(Reference.MOD_ID, "crushed/from_raw_block_crushing/" + material.getId()));
					}
					// SPLASHING
					for (CompatModel compat : registry.getCompat()) {
						if (compat.getId().equals(material.getId()) && material.isModded()) {
							for (CompatModel.CompatRecipesModel recipe : compat.getRecipes()) {
								if (recipe.getMod().equals("create") && recipe.getMachine().equals("fan_washing")) {
									for (CompatModel.CompatValuesModel value : recipe.getValues()) {
										// Crushed Ore to Nugget
										if (value.getType().equals("crushed_ore") && processedType.contains("crushed_ore") && processedType.contains("nugget")) {
											new RecipeBuilder("results")
													.type("create:splashing")
													.group("emendatusenigmatica:compat_recipe")
													.fieldJson("ingredients", new RecipeBuilder.JsonItemBuilder(true)
															.stack(EECreateRegistrar.crushedOreMap.get(material.getId()).get()))
													.addOutput(builder -> builder
															.stackWithCount(EERegistrar.nuggetMap.get(material.getId()).get(), 9)
															.stacks(value.getOutput())
													)
													.save(consumer, new ResourceLocation(Reference.MOD_ID, "nugget/from_crushed_splashing/" + material.getId()));
										}
									}
								}
							}
						}
					}
				}
			}
		}

		@Override
		public String getName() {
			return "EE Create Addon Recipes";
		}
	}

	public static class ItemModels extends EEItemModelProvider {

		private final EmendatusDataRegistry registry;

		public ItemModels(DataGenerator generator, EmendatusDataRegistry registry) {
			super(generator);
			this.registry = registry;
		}

		@Override
		protected void buildItemModels(Consumer<IFinishedGenericJSON> consumer) {
			for (MaterialModel material : registry.getMaterials()) {
				List<String> processedType = material.getProcessedTypes();
				// Crushed Ore
				if (processedType.contains("crushed_ore")) {
					ItemModelBuilder crushedBuilder = new ItemModelBuilder("minecraft:item/generated");
					if (material.getColors().getMaterialColor() == -1) {
						crushedBuilder.texture("layer0", new ResourceLocation(Reference.MOD_ID, "items/" + material.getId() + "_crushed").toString());
					} else {
						crushedBuilder.texture("layer0", new ResourceLocation(Reference.MOD_ID, "items/templates/crushed_ore/00").toString())
								.texture("layer1", new ResourceLocation(Reference.MOD_ID, "items/templates/crushed_ore/01").toString())
								.texture("layer2", new ResourceLocation(Reference.MOD_ID, "items/templates/crushed_ore/02").toString())
								.texture("layer3", new ResourceLocation(Reference.MOD_ID, "items/templates/crushed_ore/03").toString())
								.texture("layer4", new ResourceLocation(Reference.MOD_ID, "items/templates/crushed_ore/04").toString());
					}
					crushedBuilder.save(consumer, new ResourceLocation(Reference.MOD_ID, "crushed_" + material.getId() + "_ore"));
				}
			}
		}

		@Override
		public String getName() {
			return "EE Create Addon Item Models";
		}
	}

	public static class ItemTags extends EETagProvider {

		private final EmendatusDataRegistry registry;

		public ItemTags(DataGenerator gen, EmendatusDataRegistry registry) {
			super(gen);
			this.registry = registry;
		}

		private final List<String> createCrushedOres = Lists.newArrayList();

		@Override
		protected void buildTags(Consumer<IFinishedGenericJSON> consumer) {
			for (MaterialModel material : registry.getMaterials()) {
				List<String> processedType = material.getProcessedTypes();
				// Crushed Ores
				if (processedType.contains("crushed_ore")) {
					ResourceLocation crushedOre = EECreateRegistrar.crushedOreMap.get(material.getId()).getId();
					if (!createCrushedOres.contains("#create:crushed_ores/" + material.getId())) createCrushedOres.add("#create:crushed_ores/" + material.getId());
					new TagBuilder().tag(crushedOre.toString()).save(consumer, new ResourceLocation(EECreateAddon.CREATE, "/items/crushed_ores/" + material.getId()));
				}
			}
			if (!createCrushedOres.isEmpty()) new TagBuilder().tags(createCrushedOres).save(consumer, new ResourceLocation(EECreateAddon.CREATE, "/items/crushed_ores"));
		}
		@Override
		public String getName() {
			return "EE Create Addon Item Tags";
		}
	}

	public static class Lang extends EELangProvider {

		private final EmendatusDataRegistry registry;

		public Lang(DataGenerator gen, EmendatusDataRegistry registry) {
			super(gen, EECreateAddon.MOD_ID, "en_us");
			this.registry = registry;
		}

		@Override
		protected void addTranslations() {
			for (MaterialModel material : registry.getMaterials()) {
				List<String> processedType = material.getProcessedTypes();
				// Crushed Ore
				if (processedType.contains("crushed_ore")) {
					StringBuilder sb = new StringBuilder();
					sb.append("Crushed ");
					sb.append(material.getLocalizedName());
					sb.append(" Ore");
					add(EECreateRegistrar.crushedOreMap.get(material.getId()).get(), sb.toString());
				}
			}
		}

		@Override
		public String getName() {
			return "EE Create Addon Languages: en_us";
		}
	}
}