package com.nisovin.shopkeepers.commands.lib;

import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Implements the {@link CommandExecutor} and {@link TabCompleter} for a Bukkit {@link PluginCommand} by invoking our
 * command handling.
 */
public abstract class BaseCommand extends Command implements CommandExecutor, TabCompleter {

	private static PluginCommand getPluginCommand(JavaPlugin plugin, String commandName) {
		Validate.notNull(plugin, "plugin is null");
		Validate.notEmpty(commandName, "commandName is null or empty");
		PluginCommand bukkitCommand = plugin.getCommand(commandName);
		return Validate.State.notNull(
				bukkitCommand,
				() -> "Could not find command: " + commandName
		);
	}

	/**
	 * Creates a new {@link BaseCommand} that configures and binds itself to the specified {@link PluginCommand}.
	 * 
	 * @param plugin
	 *            the plugin
	 * @param commandName
	 *            the command name
	 * @see #BaseCommand(PluginCommand)
	 */
	public BaseCommand(JavaPlugin plugin, String commandName) {
		this(getPluginCommand(plugin, commandName));
	}

	/**
	 * Creates a new {@link BaseCommand} that configures and binds itself to the given {@link PluginCommand}.
	 * <p>
	 * This adopts the command's name, aliases, description, and permission, and assigns itself as the command's
	 * {@link CommandExecutor} and {@link TabCompleter}.
	 * 
	 * @param bukkitCommand
	 *            the corresponding Bukkit {@link PluginCommand}
	 */
	public BaseCommand(PluginCommand bukkitCommand) {
		super(
				Validate.notNull(bukkitCommand, "bukkitCommand is null").getName(),
				bukkitCommand.getAliases()
		);
		String desc = bukkitCommand.getDescription();
		if (desc != null && !desc.isEmpty()) {
			this.setDescription(Text.of(desc));
		}
		String permission = bukkitCommand.getPermission();
		if (permission != null && !permission.isEmpty()) {
			this.setPermission(permission);
		}

		// Register command executor:
		bukkitCommand.setExecutor(this);
		// Register tab completer:
		bukkitCommand.setTabCompleter(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command bukkitCommand, String commandAlias, String[] args) {
		CommandInput input = new CommandInput(sender, this, commandAlias, args);
		this.handleCommand(input);
		// We completely handle the command, including printing usage or help on syntax failure:
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command bukkitCommand, String commandAlias, String[] args) {
		CommandInput input = new CommandInput(sender, this, commandAlias, args);
		return this.handleTabCompletion(input);
	}
}
