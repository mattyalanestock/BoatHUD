package hibi.boathud;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class HudRenderer
extends DrawableHelper {

	private static final Identifier WIDGETS_TEXTURE = new Identifier("boathud","textures/widgets.png");
	private final MinecraftClient client;
	private int scaledWidth;
	private int scaledHeight;

	// The index to be used in these scales is the bar type (stored internally as an integer, defined in Config)
	//                                       Pack  Mix Blue
	private static final double[] MIN_V =   {  0d, 10d, 40d}; // Minimum display speed (m/s)
	private static final double[] MAX_V =   { 40d, 70d, 70d}; // Maximum display speed (m/s)
	private static final double[] SCALE_V = {4.5d,  3d,  6d}; // Pixels for 1 unit of speed (px*s/m) (BarWidth / (VMax - VMin))
	// V coordinates for each bar type in the texture file
	//                                    Pk Mix Blu
	private static final int[] BAR_OFF = { 0, 10, 20};
	private static final int[] BAR_ON =  { 5, 15, 25};

	// Used for lerping
	private double displayedSpeed = 0.0d;

	public HudRenderer(MinecraftClient client) {
		this.client = client;
	}

	public void render(MatrixStack stack, float tickDelta) {
		this.scaledWidth = this.client.getWindow().getScaledWidth();
		if(Config.centered) {
			this.scaledHeight = (int) ((this.client.getWindow().getScaledHeight()/2) + 96);
		} else {
			this.scaledHeight = this.client.getWindow().getScaledHeight();
		}
		int i = this.scaledWidth / 2;

		// Render boilerplate
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Lerping the displayed speed with the actual speed against how far we are into the tick not only is mostly accurate,
		// but gives the impression that it's being updated faster than 20 hz (which it isn't)
		this.displayedSpeed = MathHelper.lerp(tickDelta, this.displayedSpeed, Common.hudData.speed);

		int colSpeed = this.getSpeedTextColor();
		int colDrift = this.getDriftTextColor();
		int angleInt = Math.max(-90, Math.min(90, Common.hudData.driftDir));
		int angleSpr = this.getDriftAngleUV();

		// Overlay texture and bar
		this.drawTexture(stack, i - 91, this.scaledHeight - 61, 0, 50, 182, 20);
		this.renderBar(stack, i - 91, this.scaledHeight - 61);

		// Sprites
		// Angle indicator
		if (Math.ceil(Common.hudData.driftAngle)<180) {
			this.drawTexture(stack, i + angleInt - 4, this.scaledHeight - 66, 112+angleSpr, 30, 8, 8);
		}
		// Left-right
		this.drawTexture(stack, i - 23, this.scaledHeight - 54, 96, this.client.options.leftKey.isPressed() ? 38 : 30, 8, 8);
		this.drawTexture(stack, i + 18, this.scaledHeight - 54, 104, this.client.options.rightKey.isPressed() ? 38 : 30, 8, 8);
		// Ping
		this.renderPing(stack, i + 30, this.scaledHeight - 54);
		// Brake-throttle bar
		this.drawTexture(stack, i, this.scaledHeight - 45, 0, this.client.options.forwardKey.isPressed() ? 45 : 40, 61, 5);
		this.drawTexture(stack, i - 61, this.scaledHeight - 45, 0, this.client.options.backKey.isPressed() ? 35 : 30, 61, 5);
		// Gain arrow
		this.drawTexture(stack, i + 80, this.scaledHeight - 54, 203, this.getGainArrowUV(), 7, 8);

		// Text
		// Player Name
		if(Config.playername) {
			int namePos = this.scaledHeight - 76;
			if(Config.centered) {
				namePos = this.scaledHeight - 38;
			}
			this.typeCentered(stack, Common.hudData.name, i, namePos, 0xFFFFFF);
		}

		// Speed and drift angle
		this.typeCentered(stack, String.format(Config.gFormat, Common.hudData.g), i + 60, this.scaledHeight - 54, colSpeed);
		this.typeCentered(stack, String.format(Config.speedFormat, this.displayedSpeed * Config.speedRate), i - 62, this.scaledHeight - 54, colSpeed);
		this.typeCentered(stack, String.format(Config.angleFormat, Common.hudData.driftAngle), i + 2, this.scaledHeight - 54, colDrift);

		RenderSystem.disableBlend();
	}
	
	/** Returns the vertical UV offset for the gain arrow sprite. */
	private Integer getGainArrowUV() {
		if (Math.abs(Common.hudData.g) < 0.05) {
			return 16; // -
		}
		if (Common.hudData.g > 0.0) {
			return 0; // up arrow
		}
		if (Common.hudData.g < 0.0) {
			return 8; // down arrow
		}
		return 16; // -
	}

	/** Returns a text color for the given acceleration. */
	private Integer getSpeedTextColor() {
		if (Math.abs(Common.hudData.g) < 0.05) {
			return 0xFFFFFF; // white
		}
		if (Common.hudData.g > 0.0) {
			return 0x63C74D; // green
		}
		if (Common.hudData.g < 0.0) {
			return 0xFF0044; // red
		}
		return 0xFFFFFF; // white
	}

	/** Returns a text color for the given drift angle. */
	private Integer getDriftTextColor() {
		if (Common.hudData.driftAngle < 22.5) {
			return 0xFFFFFF; // white
		} else if (Common.hudData.driftAngle < 45) {
			return 0xFEE761; // yellow
		} else {
			return 0xFF0044; // red
		}
	}

	/** Returns the horizontal UV offset for the drift angle sprite. */
	private Integer getDriftAngleUV() {
		if (Common.hudData.driftAngle < 22.5) {
			return 0; // white
		} else if (Common.hudData.driftAngle < 45) {
			return 8; // yellow
		} else {
			return 16; // red
		}
	}

	/** Renders the speed bar atop the HUD, uses displayedSpeed to, well, display the speed. */
	private void renderBar(MatrixStack stack, int x, int y) {
		this.drawTexture(stack, x, y, 0, BAR_OFF[Config.barType], 182, 5);
		if(Common.hudData.speed < MIN_V[Config.barType]) return;
		if(Common.hudData.speed > MAX_V[Config.barType]) {
			if(this.client.world.getTime() % 2 == 0) return;
			this.drawTexture(stack, x, y, 0, BAR_ON[Config.barType], 182, 5);
			return;
		}
		this.drawTexture(stack, x, y, 0, BAR_ON[Config.barType], (int)((this.displayedSpeed - MIN_V[Config.barType]) * SCALE_V[Config.barType]), 5);
	}

	/** Implementation is cloned from the notchian ping display in the tab player list.	 */
	private void renderPing(MatrixStack stack, int x, int y) {
		int offset = 0;
		if(Common.hudData.ping < 0) {
			offset = 40;
		}
		else if(Common.hudData.ping < 150) {
			offset = 0;
		}
		else if(Common.hudData.ping < 300) {
			offset = 8;
		}
		else if(Common.hudData.ping < 600) {
			offset = 16;
		}
		else if(Common.hudData.ping < 1000) {
			offset = 24;
		}
		else {
			offset = 32;
		}
		this.drawTexture(stack, x, y, 246, offset, 10, 8);
	}

	/** Renders a piece of text centered horizontally on an X coordinate. */
	private void typeCentered(MatrixStack stack, String text, int centerX, int y, int color) {
		this.client.textRenderer.drawWithShadow(stack, text, centerX - this.client.textRenderer.getWidth(text) / 2, y, color);
	}
}
