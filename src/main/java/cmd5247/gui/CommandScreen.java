package cmd5247.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cmd5247.CMD;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.Mth;

public class CommandScreen extends ChatScreen {

  private static List<String> history = new ArrayList<>();
  private static int historyPos = -1;
  private static String historyBuffer = "";

  public CommandScreen(String p_95579_) {
    super(p_95579_);
  }

  @Override
  protected void init() {
    super.init();
    // change the CommandSuggestions to the local version
    this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10,
        true, -805306368);
    this.commandSuggestions.updateCommandInfo();
    historyPos = history.size();
  }

  /** Called by {@link ChatScreen#keyPressed}. */
  @Override
  @SuppressWarnings("resource")
  public boolean handleChatInput(String input, boolean p_242161_) {
    var command = normalizeChatMessage(input);
    if (command.isEmpty()) {
      return true;
    } else {
      /* Adapted from {@link net.minecraft.commands.Commands#performCommand}. */
      if (command.startsWith("$")) {
        var source = Minecraft.getInstance().player.createCommandSourceStack();
        if (history.isEmpty() || !history.get(history.size() - 1).equals(command))
          history.add(command);
        try {
          CMD.instance.dispatcher.execute(command.substring(1), source);
        } catch (CommandRuntimeException cre) {
          source.sendFailure(cre.getComponent());
        } catch (CommandSyntaxException cse) {
          source.sendFailure(ComponentUtils.fromMessage(cse.getRawMessage()));
          if (cse.getInput() != null && cse.getCursor() >= 0) {
            var failure = Component.empty()
                .withStyle(ChatFormatting.GRAY)
                .withStyle(style -> style.withClickEvent(
                    new ClickEvent(Action.SUGGEST_COMMAND, command)));
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
      return minecraft.screen == this; // FORGE: Prevent closing the screen if another screen has been opened.
    }
  }

  /** Overridden to keep a separate history for mod commands */
  @Override
  public void moveInHistory(int delta) {
    int i = historyPos + delta;
    int j = history.size();
    i = Mth.clamp(i, 0, j);
    if (i != historyPos) {
      if (i == j) {
        historyPos = j;
        this.input.setValue(historyBuffer);
      } else {
        if (historyPos == j) {
          historyBuffer = this.input.getValue();
        }

        this.input.setValue(history.get(i));
        this.commandSuggestions.setAllowSuggestions(false);
        historyPos = i;
      }
    }
  }

}
