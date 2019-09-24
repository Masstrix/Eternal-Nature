/*
 * Copyright 2019 Matthew Denton
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

package me.masstrix.eternalnature.command;

import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

public abstract class EternalCommand implements CommandExecutor, TabCompleter {

    private String name;
    private String label;
    private CommandSender sender;
    private boolean playerOnly;

    public EternalCommand(String name) {
        this.name = name;
        if (getClass().isAnnotationPresent(PlayerOnly.class)) {
            playerOnly = true;
        }
    }

    public void msg(CommandSender sender, String msg) {
        sender.sendMessage(StringUtil.color(msg));
    }

    public void msg(String msg) {
        sender.sendMessage(StringUtil.color(msg));
    }

    @Override
    public final List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command cm, @Nonnull String label, @Nonnull String[] args) {
        return tabComplete(args);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        this.sender = sender;
        this.label = label;
        if (playerOnly && wasPlayer())
            execute(args);
        else if (!playerOnly)
            execute(args);
        return false;
    }

    public String getName() {
        return name;
    }

    public String getLabelUsed() {
        return label;
    }

    public boolean wasPlayer() {
        return sender instanceof Player;
    }

    public CommandSender getSender() {
        return sender;
    }

    public abstract void execute(String[] args);

    public List<String> tabComplete(String[] args) {
        return Collections.emptyList();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface PlayerOnly {

    }
}
