package bot.listeners;

import bot.Tools;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SpamControl extends ListenerAdapter {
    static ArrayList<TextChannel> blackListedChannels = new ArrayList<>();
    static ArrayList<Member> blackListedMembers = new ArrayList<>();
    static ArrayList<Role> blackListedRoles = new ArrayList<>();

    HashMap<Member, MessageHistory> messageTracking = new HashMap<>();

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        //5 messages in under 3 seconds then mute for 5 minutes and add a warning!
        if(blackListedChannels.contains(event.getChannel()) || blackListedMembers.contains(event.getMember())) {
            for(Role r : blackListedRoles) {
                if(Objects.requireNonNull(event.getMember()).getRoles().contains(r)) {
                    return;
                }
            }
            return;
        }
        int scmessages = 5;
        int scseconds = 3;
        if(!messageTracking.containsKey(event.getMember())) {
            messageTracking.put(event.getMember(), new MessageHistory(1, System.currentTimeMillis()));
        } else {
            int msgNum = messageTracking.get(event.getMember()).msgNum++;
            long lastTimeSent = messageTracking.get(event.getMember()).lastTimeSent;
            if(msgNum == scmessages && System.currentTimeMillis()-lastTimeSent <= scseconds*1000) {
                Tools.muteMember(event.getMember(), event.getGuild(), 300, "Spamming");
                Objects.requireNonNull(event.getMember()).getUser().openPrivateChannel().queue(c -> {
                    c.sendMessage("You have been muted for **5 minutes** for spamming in a channel! You have also been given **1 warning**!").queue();
                });
                event.getChannel().sendMessage("Please do not spam! You have been muted for `5 minutes`!").queue();
                if(Tools.memberToWarns.get(event.getMember()).size() >= 4) {
                    event.getChannel().sendMessage("Banned " + event.getMember().getAsMention() + " from the server because they exceeded `3 warnings`!").queue();
                    event.getMember().ban(7, "Exceeded 3 warnings!").queue();
                }
            } else if(System.currentTimeMillis()-lastTimeSent > scseconds*1000) {
                messageTracking.get(event.getMember()).msgNum = 1;
                messageTracking.get(event.getMember()).lastTimeSent = System.currentTimeMillis();
            }
            messageTracking.get(event.getMember()).lastTimeSent = System.currentTimeMillis();
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        //Add all of your blacklists here
        blackListedChannels.add(event.getJDA().getGuilds().get(0).getTextChannelById(768793442632990721L));
        blackListedChannels.add(event.getJDA().getGuilds().get(0).getTextChannelById(741785877944074251L));
        blackListedMembers.add(event.getJDA().getGuilds().get(0).getOwner());
        blackListedRoles.add(event.getJDA().getGuilds().get(0).getRoleById(773337238952083477L));
    }
}
class MessageHistory {
    public MessageHistory(int msgNum, long lastTimeSent) {
        this.msgNum = msgNum;
        this.lastTimeSent = lastTimeSent;
    }
    int msgNum = 0;
    long lastTimeSent = 0;
}