package cmdai;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import cmdai.commands.Commands;
import cmdai.gui.CommandScreen;
import cmdai.gui.TaskExecutionOverlay;
import cmdai.gui.TaskOverlayManager;
import cmdai.gui.TaskReportOverlay;
import cmdai.task.TaskManager;

@Mod(Main.MODID)
public class Main {
	
	// The value here should match an entry in the META-INF/mods.toml file
	public static final String MODID = "cmdai";
	
    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }
    
    @SubscribeEvent
	public void clientSetup(FMLClientSetupEvent event) {
    	Commands.clientSetup();
    	CommandScreen.clientSetup();
    	TaskManager.clientSetup();
		TaskExecutionOverlay.clientSetup();
		TaskReportOverlay.clientSetup();
		TaskOverlayManager.clientSetup();
	}
    
    /* Game Thread: Minecraft.runTick(boolean !outofmemory) from Minecraft.run#663
     * 
     * { scheduledExecutables
     * 	 
     *   Minecraft.runAllTasks() from Minecraft.runTick#1013
     * 
     * }
     * 
     * { tick
     *   
     *   // up to 10 times, based on time since last execution of scheduledExecutables
     *   Minecraft.tick() from Minecraft.runTick#1019 
     *   
     *   # ForgeEventFactory.onPreClientTick() from Minecraft.tick#1614
     *   {
     *     # gui tick
     *     # set looking at block
     *     # game mode tick
     *     # texture manager tick
     *     # screen tick
     *     
     *     Minecraft.handleKeybinds() from Minecraft.tick#1663
     *     {
     *       # toggle perspective
     *       # smooth camera
     *       # hotbar slots (0 -> 8)
     *       # social interactions
     *       # inventory
     *       # advancements
     *       # swap offhand
     *       # drop
     *       # chat
     *       # chat (command)
     *       # attack
     *       # use
     *       # pick
     *     }
     *     
     *     # game renderer tick
     *     # level renderer tick
     *     # tick entities (including player)
     *     
     *     # ForgeEventFactory.onPlayerPreTick(...) from Player.tick#208
     *     {
     *     
     *     }
     *     # ForgeEventFactory.onPlayerPostTick(...) from Player.tick#279
     *     
     *     # music manager tick
     *     # sound manager tick
     *     
     *     ClientLevel.tick() form Minecraft.tick#1711
     *     {
     *       # world border tick
     *       # client chunk cache tick
     *     }
     *     
     *     # level animate tick
     *     # particle engine tick
     *     # pending connection tick
     * 	   # keyboard handler tick (manual debug crash - you just lost the game)
     *   }
     *   # ForgeEventFactory.onPostClientTick() from Minecraft.tick#1745
     *   
     * }
     * 
     * // updates the player's rotation with LocalPlayer.turn()
     * MouseHandler.turnPlayer() from Minecraft.runTick#1025
     * 
     * # ForgeEventFactory.onRenderTickStart(...) from Minecraft.runTick#1042
     * { gameRenderer
     * 
     *   GameRenderer.render(...) from Minecraft.runTick#1044
     *   {
     *   	Gui.render(...) from GameRenderer.render#868
     *   	{
     *   		DebugScreenOverlay.render(...) from Gui.render#266
     *   	}
     *   }
     *   
     * }
     * # ForgeEventFactory.onRenderTickEnd(...) from Minecraft.runTick#1048
     * 
     * # fps rendering and string
     * 
     */

}
