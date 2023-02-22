package hibi.boathud;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

public class MenuInteg implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(TITLE);
			ConfigEntryBuilder entryBuilder = builder.entryBuilder();
			builder.getOrCreateCategory(CAT)

				.addEntry(entryBuilder.startBooleanToggle(ENABLED, Config.enabled)
					.setDefaultValue(true)
					.setSaveConsumer(newVal -> Config.enabled = newVal)
					.build())

				.addEntry(entryBuilder.startBooleanToggle(CENTERED, Config.centered)
					.setDefaultValue(false)
					.setTooltip(TIP_CENTERED)
					.setSaveConsumer(newVal -> Config.centered = newVal)
					.build())

				.addEntry(entryBuilder.startBooleanToggle(PLAYERNAME, Config.playername)
					.setDefaultValue(false)
					.setTooltip(TIP_PLAYERNAME)
					.setSaveConsumer(newVal -> Config.playername = newVal)
					.build())

				.addEntry(entryBuilder.startEnumSelector(SPEED_FORMAT, SpeedFormat.class, SpeedFormat.values()[Config.configSpeedType])
					.setDefaultValue(SpeedFormat.KMPH)
					.setSaveConsumer(newVal -> Config.setUnit(newVal.ordinal()))
					.setEnumNameProvider(value -> Text.translatable("boathud.option.speed_format." + value.toString()))
					.build())

				.addEntry(entryBuilder.startEnumSelector(BAR_TYPE, BarType.class, BarType.values()[Config.barType])
					.setDefaultValue(BarType.PACKED)
					.setTooltip(TIP_BAR, TIP_BAR_PACKED, TIP_BAR_MIXED, TIP_BAR_BLUE)
					.setSaveConsumer(newVal -> Config.barType = newVal.ordinal())
					.setEnumNameProvider(value -> Text.translatable("boathud.option.bar_type." + value.toString()))
					.build());

			builder.setSavingRunnable(() -> Config.save());
			return builder.build();
		};
	}

	public enum BarType {
		PACKED, MIXED, BLUE
	}
	public enum SpeedFormat {
		MS, KMPH, MPH, KT
	}

	private static final MutableText
		TITLE = Text.translatable("boathud.config.title"),
		CAT = Text.translatable("boathud.config.cat"),
		ENABLED = Text.translatable("boathud.option.enabled"),
		CENTERED = Text.translatable("boathud.option.centered"),
		PLAYERNAME = Text.translatable("boathud.option.playername"),
		BAR_TYPE = Text.translatable("boathud.option.bar_type"),
		SPEED_FORMAT = Text.translatable("boathud.option.speed_format"),
		TIP_CENTERED = Text.translatable("boathud.tooltip.centered"),
		TIP_PLAYERNAME = Text.translatable("boathud.tooltip.playername"),
		TIP_BAR = Text.translatable("boathud.tooltip.bar_type"),
		TIP_BAR_PACKED = Text.translatable("boathud.tooltip.bar_type.packed"),
		TIP_BAR_MIXED = Text.translatable("boathud.tooltip.bar_type.mixed"),
		TIP_BAR_BLUE = Text.translatable("boathud.tooltip.bar_type.blue");
}
