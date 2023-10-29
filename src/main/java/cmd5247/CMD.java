package cmd5247;

import cmd5247.commands.Commands;
import cmd5247.gui.TaskOverlayManager;
import cmd5247.gui.components.TaskExecutionOverlay;
import cmd5247.gui.components.TaskReportOverlay;
import cmd5247.gui.screens.CommandScreen;
import cmd5247.task.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CMD.MODID)
public class CMD {

  private static CMD instance;

  // The value here should match an entry in the META-INF/mods.toml file
  public static final String MODID = "cmd5247";

  public final Options options = new Options();
  public Commands commands = new Commands();

  public CMD() {
    instance = this;
    
    var modBus = FMLJavaModLoadingContext.get().getModEventBus();
    modBus.addListener(this::clientSetup);
    modBus.addListener(this::registerGuiOverlays);

    var eventBus = MinecraftForge.EVENT_BUS;
    eventBus.addListener(this::tick);
    eventBus.addListener(this::onRegisterCommandsEvent);
  }

  public static CMD getInstance() {
    return instance;
  }

  public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
    this.commands = new Commands();
  }

  public void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
    TaskExecutionOverlay.registerGuiOverlays(event);
    TaskReportOverlay.registerGuiOverlays(event);
  }

  public void clientSetup(FMLClientSetupEvent event) {
    TaskManager.clientSetup();
    TaskOverlayManager.clientSetup();
  }

  /** A direct extension of the vanilla {@link Minecraft#tick}. */
  public void tick(ClientTickEvent event) {
    var game = Minecraft.getInstance();

    if (event.phase == Phase.END) {

      // opens command screen - see Minecraft.handleKeybinds
      if (game.screen == null && game.getOverlay() == null && this.options.keyCommand.consumeClick())
        if (game.getChatStatus().isChatAllowed(game.isLocalServer()))
          game.setScreen(new CommandScreen("$"));

    }
  }

  /*
   * Game Thread: Minecraft.runTick(boolean !outofmemory) from Minecraft.run#663
   * 
   * { scheduledExecutables
   * 
   * Minecraft.runAllTasks() from Minecraft.runTick#1013
   * 
   * }
   * 
   * { tick
   * 
   * // up to 10 times, based on time since last execution of scheduledExecutables
   * Minecraft.tick() from Minecraft.runTick#1019
   * 
   * # ForgeEventFactory.onPreClientTick() from Minecraft.tick#1614
   * {
   * # gui tick
   * # set looking at block
   * # game mode tick
   * # texture manager tick
   * # screen tick
   * 
   * Minecraft.handleKeybinds() from Minecraft.tick#1663
   * {
   * # toggle perspective
   * # smooth camera
   * # hotbar slots (0 -> 8)
   * # social interactions
   * # inventory
   * # advancements
   * # swap offhand
   * # drop
   * # chat
   * # chat (command)
   * # attack
   * # use
   * # pick
   * }
   * 
   * # game renderer tick
   * # level renderer tick
   * # tick entities (including player)
   * 
   * # ForgeEventFactory.onPlayerPreTick(...) from Player.tick#208
   * {
   * 
   * }
   * # ForgeEventFactory.onPlayerPostTick(...) from Player.tick#279
   * 
   * # music manager tick
   * # sound manager tick
   * 
   * ClientLevel.tick() form Minecraft.tick#1711
   * {
   * # world border tick
   * # client chunk cache tick
   * }
   * 
   * # level animate tick
   * # particle engine tick
   * # pending connection tick
   * # keyboard handler tick (manual debug crash - you just lost the game)
   * }
   * # ForgeEventFactory.onPostClientTick() from Minecraft.tick#1745
   * 
   * }
   * 
   * // updates the player's rotation with LocalPlayer.turn()
   * MouseHandler.turnPlayer() from Minecraft.runTick#1025
   * 
   * # ForgeEventFactory.onRenderTickStart(...) from Minecraft.runTick#1042
   * { gameRenderer
   * 
   * GameRenderer.render(...) from Minecraft.runTick#1044
   * {
   * Gui.render(...) from GameRenderer.render#868
   * {
   * DebugScreenOverlay.render(...) from Gui.render#266
   * }
   * }
   * 
   * }
   * # ForgeEventFactory.onRenderTickEnd(...) from Minecraft.runTick#1048
   * 
   * # fps rendering and string
   * 
   */

}
