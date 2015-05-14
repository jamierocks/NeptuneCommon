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
package org.neptunepowered.common;

import net.canarymod.Canary;
import org.neptunepowered.common.wrapper.NeptuneTranslator;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandList;
import net.canarymod.commandsys.CommandManager;
import net.canarymod.commandsys.DuplicateCommandException;
import net.canarymod.database.DatabaseLoader;
import net.canarymod.hook.HookExecutor;
import net.canarymod.motd.CanaryMessageOfTheDayListener;
import net.canarymod.motd.MessageOfTheDay;
import net.canarymod.plugin.DefaultPluginManager;
import net.canarymod.plugin.PluginLangLoader;
import org.neptunepowered.common.wrapper.factory.NeptuneFactory;
import org.neptunepowered.common.wrapper.util.NeptuneJsonNBTUtility;

public class Neptune extends Canary {

    private boolean isInitialised = false;

    public Neptune() {
        Canary.setCanary(this);

        DatabaseLoader.load();
        PluginLangLoader.load();
        NeptuneTranslator.load();

        this.jsonNBT = new NeptuneJsonNBTUtility();
        this.motd = new MessageOfTheDay();
        this.commandManager = new CommandManager();
        this.hookExecutor = new HookExecutor();
        this.factory = new NeptuneFactory();
        this.pluginManager = new DefaultPluginManager();

        pluginManager.scanForPlugins(); // Scan for plugins
    }

    public void registerCanaryCommands() {
        try {
            this.commandManager.registerCommands(new CommandList(), getServer(), false);
        } catch (CommandDependencyException e) {
            log.error("Failed to set up system commands! Dependency resolution failed!", e);
        } catch (DuplicateCommandException f) {
            log.error("Failed to set up system commands! The command already exists!", f);
        }
    }

    public void initMOTDListener() {
        motd().registerMOTDListener(new CanaryMessageOfTheDayListener(), getServer(), false);
    }

    public void lateInitialisation() {
        if (isInitialised)
            return;

        this.registerCanaryCommands();
        this.initMOTDListener();
        isInitialised = true;
    }
}