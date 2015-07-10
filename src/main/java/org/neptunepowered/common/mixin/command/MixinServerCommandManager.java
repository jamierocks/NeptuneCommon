/*
 * This file is part of NeptuneCommon, licensed under the MIT License (MIT).
 *
 * Copyright (c) Jamie Mansfield <https://github.com/jamierocks>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.neptunepowered.common.mixin.command;

import com.google.common.collect.Lists;
import net.canarymod.Canary;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.CommandDependencyException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.util.BlockPos;
import org.neptunepowered.common.Neptune;
import org.neptunepowered.common.interfaces.command.IMixinServerCommandManager;
import org.neptunepowered.common.wrapper.commandsys.NeptuneCommand;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Iterator;
import java.util.List;

@Mixin(ServerCommandManager.class)
public class MixinServerCommandManager extends CommandHandler implements IMixinServerCommandManager {

    private List<NeptuneCommand> earlyRegisterCommands = Lists.newArrayList();

    @Override
    public int executeCommand(ICommandSender sender, String command) {
        command = command.trim();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        String[] args = command.split(" ");
        String commandName = args[0];

        return Canary.commands().parseCommand((MessageReceiver) sender, commandName, args) ? 1 : 0;
    }

    @Override
    public ICommand registerCommand(ICommand command) {
        NeptuneCommand cmd = new NeptuneCommand(command);
        if (Canary.instance() != null) {
            try {
                Canary.commands().registerCommand(cmd, Neptune.minecraftCommandOwner, false);
            } catch (CommandDependencyException e) {
                Canary.log.error("Eh, something has broken :(", e);
            }
        } else {
            earlyRegisterCommands.add(cmd);
        }

        return super.registerCommand(command);
    }

    @Override
    public List getPossibleCommands(ICommandSender sender) {
        return Canary.commands().matchCommandNames((MessageReceiver) sender, "", false);
    }

    @Override
    public List getTabCompletionOptions(ICommandSender sender, String command, BlockPos pos) {
        command = command.trim();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        String[] args = command.split(" ");
        String commandName = args[0];
        return Canary.commands().tabComplete((MessageReceiver) sender, commandName, args);
    }

    @Override
    public void registerEarlyCommands() {
        for (Iterator<NeptuneCommand> it = earlyRegisterCommands.iterator(); it.hasNext(); ) {
            NeptuneCommand cmd = it.next();
            it.remove();
            try {
                Canary.commands().registerCommand(cmd, Neptune.minecraftCommandOwner, true);
            } catch (CommandDependencyException e) {
                Canary.log.error("Eh, something has broken :(", e);
            }
        }
    }
}
