package cmd5247.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class Commands {

  // Client side, mod-only command line
  private final CommandDispatcher<CommandSourceStack> dispatcher;

  // replaces recent
  private List<String> history = new ArrayList<>();

  public Commands() {
    this.dispatcher = new CommandDispatcher<>();
    FishCommand.register(this.dispatcher);
    StopCommand.register(this.dispatcher);
    ClickCommand.register(this.dispatcher);
  }

  public CommandDispatcher<CommandSourceStack> getDispatcher() {
    return this.dispatcher;
  }

  // replaces minecraft.gui.getChat().getRecentChat()
  public List<String> getRecentCommands() {
    return history;
  }

  // replaces minecraft.gui.getChat().addRecentChat();
  /** {@link ChatComponent#addRecentChat} */
  public void addRecentCommand(String command) {
      if (this.history.isEmpty() || !this.history.get(this.history.size() - 1).equals(command)) {
         this.history.add(command);
      }
   }

  /** {@link net.minecraft.commands.Commands#performCommand} */
  @SuppressWarnings("resource")
  public void performCommand(String command) {
    if (command.startsWith("$")) {
      var source = Minecraft.getInstance().player.createCommandSourceStack();
      try {
        this.dispatcher.execute(command.substring(1), source);
      } catch (CommandRuntimeException cre) {
        source.sendFailure(cre.getComponent());
      } catch (CommandSyntaxException cse) {
        source.sendFailure(ComponentUtils.fromMessage(cse.getRawMessage()));
        if (cse.getInput() != null && cse.getCursor() >= 0) {
          var failure = Component.empty()
              .withStyle(ChatFormatting.GRAY)
              .withStyle(style -> style.withClickEvent(
                new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
          int j = Math.min(cse.getInput().length(), cse.getCursor());

          if (j > 10)
            failure.append("...");
          failure.append(cse.getInput().substring(Math.max(0, j - 10), j));
          if (j < cse.getInput().length())
            failure.append(
              Component.literal(cse.getInput().substring(j))
                  .withStyle(new ChatFormatting[] {
                    ChatFormatting.RED, ChatFormatting.UNDERLINE }));
          failure.append(Component.translatable("command.context.here")
              .withStyle(new ChatFormatting[] {
                ChatFormatting.RED, ChatFormatting.ITALIC }));

          source.sendFailure(failure);
        }
      }
    }
  }

}
