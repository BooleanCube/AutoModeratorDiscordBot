package bot;

import bot.commands.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;
import java.util.regex.Pattern;

public class Manager {
    private final Map<String, Command> commands = new HashMap<>();

    Manager() {
        //commands
        addCommand(new Ping());
        addCommand(new Help(this));
        addCommand(new Warn());
        addCommand(new Kick());
        addCommand(new Ban());
        addCommand(new Mute());
        addCommand(new Unmute());
    }

    private void addCommand(Command c) {
        if (!commands.containsKey(c.getCommand())) {
            commands.put(c.getCommand(), c);
            System.out.println("Added " + c.getCommand());
        }
    }

    public Collection<Command> getCommands() {
        return commands.values();
    }

    public Command getCommand(String commandName) {
        if (commandName == null) {
            return null;
        }
        return commands.get(commandName);
    }

    void run(GuildMessageReceivedEvent event) {
        final String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(Constants.PREFIX)) {
            return;
        }
        final String[] split = msg.replaceFirst("(?i)" + Pattern.quote(Constants.PREFIX), "").split("\\s+");
        final String command = split[0].toLowerCase();
        if (commands.containsKey(command)) {
            final List<String> args = Arrays.asList(split).subList(1, split.length);
            commands.get(command).run(args, event);
        }
    }
}
