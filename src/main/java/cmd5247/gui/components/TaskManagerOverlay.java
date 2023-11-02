package cmd5247.gui.components;

import java.util.List;

import com.google.common.base.Strings;

import cmd5247.CMD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class TaskManagerOverlay implements IGuiOverlay {

  private final Font font = Minecraft.getInstance().font;

  @Override
  public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
    if (CMD.getInstance().taskManager.isBusy()) {
      Minecraft.getInstance().getProfiler().push(getClass().getSimpleName());
      var task = CMD.getInstance().taskManager.getAactiveTask();
      renderLines(guiGraphics, task.getScript(), true);
      renderLines(guiGraphics, task.getSummary(), false);
      Minecraft.getInstance().getProfiler().pop();
    }
  }

  public static record DebugLine(String text, int indentation, boolean active) {
    
    public static DebugLine simple(String text) {
      return new DebugLine(text, 0, true);
    }
    
  }

  /** {@link DebugScreenOverlay#renderLines} */
  private void renderLines(GuiGraphics guiGraphics, List<DebugLine> lines, boolean leftSide) {
    int indentWidth = this.font.width("--");

    for(int i = 0; i < lines.size(); ++i) {
       DebugLine line = lines.get(i);
       if (!Strings.isNullOrEmpty(line.text)) {
          int w = this.font.width(line.text);
          int h = this.font.lineHeight;
          int x = leftSide ? (2 + line.indentation * indentWidth) : (guiGraphics.guiWidth() - 2 - w - line.indentation * indentWidth);
          int y = 2 + h * i;
          guiGraphics.fill(x - 1, y - 1, x + w + 1, y + h - 1, 0x90505050);
          guiGraphics.drawString(this.font, line.text, x, y, line.active ? 0xE0E0E0 : 0xB0B0B0, false);
       }
    }
  }

}
