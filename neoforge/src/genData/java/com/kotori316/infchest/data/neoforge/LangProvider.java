package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LangProvider implements DataProvider {
    private final List<LanguageProvider> providers;

    public LangProvider(PackOutput output) {
        this.providers = List.of(
            new EnUs(output),
            new JaJp(output),
            new ZhCn(output)
        );
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return CompletableFuture.allOf(
            this.providers.stream().map(languageProvider -> languageProvider.run(cachedOutput)).toArray(CompletableFuture[]::new)
        );
    }

    @Override
    public String getName() {
        return "InfChest Lang";
    }

    private static final class EnUs extends LanguageProvider {
        public EnUs(PackOutput output) {
            super(output, InfChest.modID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(InfChest.accessor.CHEST(), "Inf Chest");
            add(InfChest.accessor.DEQUE(), "Deque");
            add("config.jade.plugin_infchest.jade_plugin", "InfChest Plugin");
        }
    }

    private static final class JaJp extends LanguageProvider {
        public JaJp(PackOutput output) {
            super(output, InfChest.modID, "ja_jp");
        }

        @Override
        protected void addTranslations() {
            add(InfChest.accessor.CHEST(), "無限チェスト");
            add(InfChest.accessor.DEQUE(), "Deque");
            add("config.jade.plugin_infchest.jade_plugin", "InfChest Plugin");
        }
    }

    private static final class ZhCn extends LanguageProvider {
        public ZhCn(PackOutput output) {
            super(output, InfChest.modID, "zh_cn");
        }

        @Override
        protected void addTranslations() {
            add(InfChest.accessor.CHEST(), "无限箱子");
            add(InfChest.accessor.DEQUE(), "Deque");
            add("config.jade.plugin_infchest.jade_plugin", "InfChest Plugin");
        }
    }
}
