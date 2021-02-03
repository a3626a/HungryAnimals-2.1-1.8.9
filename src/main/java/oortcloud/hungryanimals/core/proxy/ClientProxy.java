package oortcloud.hungryanimals.core.proxy;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import oortcloud.hungryanimals.HungryAnimals;
import oortcloud.hungryanimals.blocks.ModBlocks;
import oortcloud.hungryanimals.blocks.render.RenderTileEntityTrough;
import oortcloud.hungryanimals.client.ClientRenderEventHandler;
import oortcloud.hungryanimals.core.lib.References;
import oortcloud.hungryanimals.core.network.HandlerClientSpawnParticle;
import oortcloud.hungryanimals.core.network.HandlerClientSyncHungry;
import oortcloud.hungryanimals.core.network.HandlerClientSyncProducingFluid;
import oortcloud.hungryanimals.core.network.HandlerClientSyncProducingInteraction;
import oortcloud.hungryanimals.core.network.HandlerClientSyncTamable;
import oortcloud.hungryanimals.core.network.HandlerServerDGEditDouble;
import oortcloud.hungryanimals.core.network.HandlerServerDGEditInt;
import oortcloud.hungryanimals.core.network.HandlerServerDGSet;
import oortcloud.hungryanimals.core.network.PacketClientSpawnParticle;
import oortcloud.hungryanimals.core.network.PacketClientSyncHungry;
import oortcloud.hungryanimals.core.network.PacketClientSyncProducingFluid;
import oortcloud.hungryanimals.core.network.PacketClientSyncProducingInteraction;
import oortcloud.hungryanimals.core.network.PacketClientSyncTamable;
import oortcloud.hungryanimals.core.network.PacketServerDGEditDouble;
import oortcloud.hungryanimals.core.network.PacketServerDGEditInt;
import oortcloud.hungryanimals.core.network.PacketServerDGSet;
import oortcloud.hungryanimals.entities.EntityBola;
import oortcloud.hungryanimals.entities.EntitySlingShotBall;
import oortcloud.hungryanimals.entities.handler.HungryAnimalManager;
import oortcloud.hungryanimals.entities.render.RenderEntityBola;
import oortcloud.hungryanimals.entities.render.RenderEntitySlingShotBall;
import oortcloud.hungryanimals.entities.render.RenderEntityWeight;
import oortcloud.hungryanimals.fluids.ModFluids;
import oortcloud.hungryanimals.items.ModItems;
import oortcloud.hungryanimals.items.gui.DebugOverlayHandler;
import oortcloud.hungryanimals.items.render.ModelItemBola;
import oortcloud.hungryanimals.items.render.ModelItemSlingshot;
import oortcloud.hungryanimals.keybindings.ModKeyBindings;
import oortcloud.hungryanimals.tileentities.TileEntityTrough;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	public void registerEntityRendering() {
		RenderingRegistry.registerEntityRenderingHandler(EntityBola.class, RenderEntityBola::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySlingShotBall.class, RenderEntitySlingShotBall::new);
	}

	@SuppressWarnings("unchecked")
	public void injectRender() {
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		for (Class<? extends MobEntity> i : HungryAnimalManager.getInstance().getRegisteredAnimal()) {
			if (HungryAnimalManager.getInstance().isModelGrowing(i)) {
				Render<MobEntity> render = (Render<MobEntity>) renderManager.entityRenderMap.get(i);
				renderManager.entityRenderMap.put(i, new RenderEntityWeight(render, Minecraft.getMinecraft().getRenderManager()));
			}
		}
		
	}
	
	public void registerTileEntityRendering() {
		ClientRegistry.<TileEntityTrough>bindTileEntitySpecialRenderer(TileEntityTrough.class, new RenderTileEntityTrough());
	}
	
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(ModBlocks.FLOOR_COVER_HAY.get()), 0,
				new ModelResourceLocation(ModBlocks.FLOOR_COVER_HAY.get().getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(ModBlocks.FLOOR_COVER_LEAF.get()), 0,
				new ModelResourceLocation(ModBlocks.FLOOR_COVER_LEAF.get().getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(ModBlocks.FLOOR_COVER_WOOL.get()), 0,
				new ModelResourceLocation(ModBlocks.FLOOR_COVER_WOOL.get().getRegistryName(), "inventory"));

		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(ModBlocks.EXCRETA.get()), 0,
				new ModelResourceLocation(ModBlocks.EXCRETA.get().getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(ModBlocks.NITER_BED.get()), 0,
				new ModelResourceLocation(ModBlocks.NITER_BED.get().getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(ModBlocks.TROUGH.get()), 0,
				new ModelResourceLocation(ModBlocks.TROUGH.get().getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(ModBlocks.TRAP_COVER.get()), 0,
				new ModelResourceLocation(ModBlocks.TRAP_COVER.get().getRegistryName(), "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(ModItems.bola, 0, ModelItemBola.modelresourcelocation_normal);
		ModelLoader.setCustomModelResourceLocation(ModItems.slingshot, 0, ModelItemSlingshot.modelresourcelocation_normal);
		ModelLoader.setCustomModelResourceLocation(ModItems.debugGlass, 0, new ModelResourceLocation(ModItems.debugGlass.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.trough, 0, new ModelResourceLocation(ModItems.trough.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.manure, 0, new ModelResourceLocation(ModItems.manure.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.woodash, 0, new ModelResourceLocation(ModItems.woodash.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.saltpeter, 0, new ModelResourceLocation(ModItems.saltpeter.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.tendon, 0, new ModelResourceLocation(ModItems.tendon.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.animalGlue, 0, new ModelResourceLocation(ModItems.animalGlue.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.compositeWood, 0, new ModelResourceLocation(ModItems.compositeWood.getRegistryName(), "inventory"));
		
		ModelBakery.registerItemVariants(ModItems.bola, ModelItemBola.modelresourcelocation_normal, ModelItemBola.modelresourcelocation_spin);
		ModelBakery.registerItemVariants(ModItems.slingshot, ModelItemSlingshot.modelresourcelocation_normal,
				ModelItemSlingshot.modelresourcelocation_shooting);
		
		registerFluidModels();
    }
	
    ////////////////////////////////
    ////CHOONSTER-MINECRAFT-MODS////
    ////////////////////////////////
    
	private static void registerFluidModels() {
		ModFluids.MOD_FLUID_BLOCKS.forEach(ClientProxy::registerFluidModel);
	}

	private static void registerFluidModel(IFluidBlock fluidBlock) {
		final Item item = Item.getItemFromBlock((Block) fluidBlock);
		assert item != null;

		ModelBakery.registerItemVariants(item);

		final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(References.MODID+":fluid", fluidBlock.getFluid().getName());

		ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return modelResourceLocation;
			}
		});

		ModelLoader.setCustomStateMapper((Block) fluidBlock, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(BlockState state) {
				return modelResourceLocation;
			}
		});
	}
	
    ////////////////////////////////
    ////CHOONSTER-MINECRAFT-MODS////
    ////////////////////////////////
    
    
    @SubscribeEvent
	public static void registerCustomBakedModel(ModelBakeEvent event) {
		IBakedModel bola_normal = event.getModelRegistry().getObject(ModelItemBola.modelresourcelocation_normal);
		IBakedModel bola_spin = event.getModelRegistry().getObject(ModelItemBola.modelresourcelocation_spin);
		ModelItemBola bolaModel = new ModelItemBola(bola_normal, bola_spin);
		event.getModelRegistry().putObject(ModelItemBola.modelresourcelocation_normal, bolaModel);

		IBakedModel slingshot_normal = event.getModelRegistry().getObject(ModelItemSlingshot.modelresourcelocation_normal);
		IBakedModel slingshot_shooting = event.getModelRegistry().getObject(ModelItemSlingshot.modelresourcelocation_shooting);
		TextureAtlasSprite texture = event.getModelManager().getTextureMap().getAtlasSprite(ModelItemSlingshot.textureresourcelocation.toString());
		ModelItemSlingshot slingshotModel = new ModelItemSlingshot(slingshot_normal, slingshot_shooting, texture);
		event.getModelRegistry().putObject(ModelItemSlingshot.modelresourcelocation_normal, slingshotModel);
	}

    @SubscribeEvent
	public static void registerSprite(TextureStitchEvent event) {
		event.getMap().registerSprite(ModelItemSlingshot.textureresourcelocation);
	}

	@Override
	public void registerEventHandler() {
		super.registerEventHandler();
		MinecraftForge.EVENT_BUS.register(new ClientRenderEventHandler());
		DebugOverlayHandler debugOverlay = new DebugOverlayHandler(Minecraft.getMinecraft());
		MinecraftForge.EVENT_BUS.register(debugOverlay);
	}

	@Override
	public void registerKeyBindings() {
		ModKeyBindings.init();
	}

	@Override
	public void registerColors() {
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			public int colorMultiplier(BlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
				return ColorizerFoliage.getFoliageColorBasic();
			}
		}, ModBlocks.FLOOR_COVER_LEAF.get());
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
				return ColorizerFoliage.getFoliageColorBasic();
			}
		}, ModBlocks.FLOOR_COVER_LEAF.get());
	}

	@Override
	public void initNEI() {
		// NEIHandler.init();
	}

	@Override
	public void registerPacketHandler() {
		HungryAnimals.simpleChannel.registerMessage(HandlerServerDGEditInt.class, PacketServerDGEditInt.class, 0, Side.SERVER);
		HungryAnimals.simpleChannel.registerMessage(HandlerServerDGEditDouble.class, PacketServerDGEditDouble.class, 1, Side.SERVER);
		HungryAnimals.simpleChannel.registerMessage(HandlerServerDGSet.class, PacketServerDGSet.class, 2, Side.SERVER);
		HungryAnimals.simpleChannel.registerMessage(HandlerClientSpawnParticle.class, PacketClientSpawnParticle.class, 3, Side.CLIENT);
		HungryAnimals.simpleChannel.registerMessage(HandlerClientSyncTamable.class, PacketClientSyncTamable.class, 4, Side.CLIENT);
		HungryAnimals.simpleChannel.registerMessage(HandlerClientSyncHungry.class, PacketClientSyncHungry.class, 5, Side.CLIENT);
		HungryAnimals.simpleChannel.registerMessage(HandlerClientSyncProducingFluid.class, PacketClientSyncProducingFluid.class, 6, Side.CLIENT);
		HungryAnimals.simpleChannel.registerMessage(HandlerClientSyncProducingInteraction.class, PacketClientSyncProducingInteraction.class, 7, Side.CLIENT);
	}
	
}
