package cmd5247;

import cmd5247.commands.Commands;
import cmd5247.gui.screens.CommandScreen;
import cmd5247.task.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CMD.MODID)
public class CMD {

  private static CMD instance;

  // The value here should match an entry in the META-INF/mods.toml file
  public static final String MODID = "cmd5247";

  public final Options options = new Options();
  public Commands commands = new Commands();
  public final TaskManager taskManager = new TaskManager();

  public CMD() {
    instance = this;
    
    var modBus = FMLJavaModLoadingContext.get().getModEventBus();
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
    this.taskManager.onRegisterGuiOverlaysEvent(event);
  }

  /** A direct extension of the vanilla {@link Minecraft#tick}. */
  public void tick(ClientTickEvent event) {
    var game = Minecraft.getInstance();

    if (event.phase == Phase.END) {

      // opens command screen - see Minecraft.handleKeybinds
      if (game.screen == null && game.getOverlay() == null && this.options.keyCommand.consumeClick())
        if (game.getChatStatus().isChatAllowed(game.isLocalServer()))
          game.setScreen(new CommandScreen("$"));

      taskManager.tick();

    }
  }

}
