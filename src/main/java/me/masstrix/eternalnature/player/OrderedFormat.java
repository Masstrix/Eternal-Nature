/*
 * Copyright 2020 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature.player;

import me.masstrix.eternalnature.util.Pair;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the parsing and updating of formatted text. This allows for you to create
 * formatted text such as {@code The time is %time% on %day%} and have it parsed into
 * it's separate components and tags. Ig a tag is not registered it will be ignored and
 * read as standard text otherwise it will be replaced with it's registered instance.
 * <p>
 * Tags are defined in formatted text by encasing the name of the tag in percent symbols.
 * There can not be any spaces in a tag, if there are spaces it will be invalid, it's
 * recommended for better reading to instead use underscores.
 *
 * @author Masstrix
 */
public class OrderedFormat {

    private final static char TAG_ENTER = '%';
    private final Map<String, TagFormatter> TAGS = new HashMap<>();
    private final List<TagFormatter> ORDERED = new ArrayList<>();
    private BaseComponent[] formatted = new BaseComponent[] {new TextComponent()};
    private String legacy = "";

    /**
     * Adds a new tag for the formatter. Any tag that is registered will be replaced in
     * the parsed text with it.
     *
     * @param name      name of the tag that is used as a placeholder in the text for example
     *                  if we have a tag named <i>test</i>, any occurrence of <i>%test%</i> in
     *                  the parsed text will be replaced with it.
     * @param formatter formatter that operates for this tag.
     * @return an instance of this ordered format.
     */
    public OrderedFormat registerTag(String name, TagFormatter formatter) {
        TAGS.put(name.toLowerCase(), formatter);
        return this;
    }

    /**
     * Parse a format from a string value and breaks it down into base components
     * to be used as either {@code BaseComponent[]} or a legacy string later.
     *
     * @param format format to parse.
     * @return an instance of this ordered format.
     */
    public OrderedFormat praseFormat(String format) {
        if (format == null) return this;
        ORDERED.clear();
        char[] chars = format.toCharArray();
        ComponentBuilder builder = new ComponentBuilder();
        boolean done = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '&' || c == '\u00A7' && i < chars.length - 1) {
                char next = Character.toLowerCase(chars[i + 1]);
                ChatColor color = ChatColor.getByChar(next);
                if (color != null) {
                    switch (next) {
                        case 'k': {
                            builder.obfuscated(true);
                        }
                        case 'l': {
                            builder.bold(true);
                        }
                        case 'm': {
                            builder.strikethrough(true);
                        }
                        case 'n': {
                            builder.underlined(true);
                        }
                        case 'o': {
                            builder.italic(true);
                        }
                        case 'r': {
                            builder.reset();
                        }
                        default: {
                            builder.color(color);
                        }
                    }
                    i++;
                    continue;
                }
            }

            if (c == TAG_ENTER) {
                int next = format.indexOf('%', i + 1);
                int nextSpace = format.indexOf(' ', i + 1);
                if (next != -1 && (nextSpace == -1 || next < nextSpace)) {
                    String tag = format.substring(i + 1, next);
                    TagFormatter formatter = TAGS.get(tag.toLowerCase());
                    if (formatter != null) {
                        addText(builder);
                        builder = new ComponentBuilder();
                        BaseComponent[] json = formatter.asJson();
                        if (json != null && json.length != 0) {
                            ORDERED.add(formatter);
                        }
                        done = true;
                        i = next;
                        continue;
                    }
                }
            }
            builder.append(String.valueOf(c));
            done = false;
        }
        if (!done) addText(builder);
        this.formatted = getFormatted(true);
        this.legacy = getLegacy(true);
        return this;
    }

    /**
     * Adds a new text element to the order. This is only used by {@link #praseFormat(String)}
     * to add text in between each tag.
     *
     * @param builder the builder being added.
     */
    private void addText(ComponentBuilder builder) {
        final BaseComponent[] text = builder.create();
        final StringBuilder legacy = new StringBuilder();
        for (BaseComponent b : text) {
            legacy.append(b.toLegacyText());
        }
        ORDERED.add(() -> new Pair<>(text, legacy.toString()));
    }

    /**
     * Returns the last version of the formatted text. This will never call for an update
     * and always return the cached version of the format.
     *
     * @return the formatted text as an array of components.
     */
    public BaseComponent[] getFormatted() {
        return getFormatted(false);
    }

    /**
     * Returns the formatted text. If update is enabled it will rebuild the text calling
     * each tag for a refresh, otherwise it will return the last updated version.
     *
     * @param update should all tags be updated.
     * @return the newly formatted text as an array of components.
     */
    public BaseComponent[] getFormatted(boolean update) {
        if (update) {
            ComponentBuilder builder = new ComponentBuilder();
            builder.color(ChatColor.WHITE);
            boolean copyFromLast = false;
            for (TagFormatter f : ORDERED) {
                BaseComponent[] comp = f.asJson();
                if (comp == null || comp.length == 0) continue;
                for (BaseComponent b : comp) {
                    b = b.duplicate();
                    if (copyFromLast) {
                        b.copyFormatting(builder.getCurrentComponent(), true);
                    }
                    builder.append(b);
                }
                copyFromLast = f.copyFormattingToNextComponent();
            }
            formatted = builder.create();
        }
        return formatted;
    }

    /**
     * Returns the last version of the formatted text. This will never call for an update
     * and always return the cached version of the format.
     *
     * @return the formatted text as legacy text.
     */
    public String getLegacy() {
        return getLegacy(false);
    }

    /**
     * Returns the formatted text in legacy formatting. Legacy formatted is non-json formatting
     * and may not be fully supported on all areas of the spigot api so using {@link #getFormatted(boolean)}
     * is recommended instead when possible. If update is enabled it will rebuild the text calling
     * each tag for a refresh, otherwise it will return the last updated version.
     *
     * @param update should all tags be updated.
     * @return the newly formatted text as a legacy string.
     */
    public String getLegacy(boolean update) {
        if (update) {
            return TextComponent.toLegacyText(getFormatted(update));
        }
        return legacy;
    }

    /**
     * This is a basic utility helper for {@link OrderedFormat} for storing tags. When a format
     * is being created the {@link #getText()} is called for getting either the modern or legacy
     * text for the tag.
     */
    public interface TagFormatter {
        /**
         * Returns a combination of both modern components for json text and legacy text. Both
         * should be utilized is using both but null is also accepted in either of the elements.
         *
         * @return the text elements for this tag.
         */
        Pair<BaseComponent[], String> getText();

        /**
         * @return the modern components for this tag.
         */
        default BaseComponent[] asJson() {
            return getText().getFirst();
        }

        /**
         * @return the legacy text for this tag.
         */
        default String asLegacy() {
            return getText().getSecond();
        }

        /**
         * Sets if the tags formatting should be copied to the following component. If this
         * is enabled, the next following element will share the same formatting such as
         * color, if it's bold, italic etc.
         *
         * @return if the following component should share the same formatting.
         */
        default boolean copyFormattingToNextComponent() {
            return false;
        }
    }
}
