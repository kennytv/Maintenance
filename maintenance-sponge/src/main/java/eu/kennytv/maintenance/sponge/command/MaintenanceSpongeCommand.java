package eu.kennytv.maintenance.sponge.command;

import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import eu.kennytv.maintenance.sponge.SettingsSponge;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@NonnullByDefault
public final class MaintenanceSpongeCommand extends MaintenanceCommand implements CommandCallable {
    private static final String[] EMPTY = new String[0];

    public MaintenanceSpongeCommand(final MaintenanceSpongePlugin plugin, final SettingsSponge settings) {
        super(plugin, settings, "MaintenanceSponge");
    }

    @Override
    public CommandResult process(final CommandSource source, final String arguments) throws CommandException {
        execute(new SpongeSenderInfo(source), arguments.isEmpty() ? EMPTY : arguments.split(" "));
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(final CommandSource source, final String arguments, @Nullable final Location<World> targetPosition) throws CommandException {
        return null;
    }

    @Override
    public boolean testPermission(final CommandSource source) {
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(final CommandSource source) {
        return Optional.of(Text.of("Maintenance main-command"));
    }

    @Override
    public Optional<Text> getHelp(final CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(final CommandSource source) {
        return null;
    }

    @Override
    protected void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(name);
        if (!optionalPlayer.isPresent()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        final Player player = optionalPlayer.get();
        if (settings.addWhitelistedPlayer(player.getUniqueId(), player.getName()))
            sender.sendMessage(settings.getMessage("whitelistAdded").replace("%PLAYER%", player.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistAlreadyAdded").replace("%PLAYER%", player.getName()));
    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(name);
        if (!optionalPlayer.isPresent()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        final Player player = optionalPlayer.get();
        if (settings.removeWhitelistedPlayer(player.getUniqueId()))
            sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", player.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistNotFound"));
    }
}
