import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import java.awt.Color;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.eclipse.jetty.util.HttpCookieStore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import java.io.IOException;
import java.util.*;

public class Messages extends ListenerAdapter {
    public Messages() {
        System.out.println("MorseBot is online!");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        // Get Command Message
        String[] brokeString = e.getMessage().getContentRaw().split(" ");
        String opener = brokeString[0];
        String username = e.getAuthor().getAsTag();
        EmbedBuilder eb = new EmbedBuilder();

        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("MMM dd yyyy");

        int day;
        int wordleNumber = 0;

        try {
            BufferedReader read = new BufferedReader(new FileReader("doy.txt"));

            String doy = read.readLine();
            String[] doyArr = null;

            if (doy != null) {
                doyArr = doy.split(" ");
            }

            day = Integer.parseInt(doyArr[0]);
            wordleNumber = Integer.parseInt(doyArr[1]);

            if (date.getDayOfYear() != day) {
                wordleNumber += date.getDayOfYear() - day;
                day = date.getDayOfYear();
                FileWriter writer = new FileWriter("doy.txt", false);
                writer.write(day + " " + wordleNumber);
                writer.close();
            }

        } catch (IOException ex) {
            System.out.println("IO Exception");
        }

        //Embedded Builder Settings
        eb.setTitle("DEFAULT TITLE");
        eb.setColor(Color.red);
        eb.setDescription("TEST DESCRIPTION");
        eb.addField("INLINE FIELD", "IN FIELD TEXT", true);
        eb.addBlankField(true);
        eb.addField("OUTLINE FIELD", "OUT FIELD TEXT", false);
        eb.setImage("https://preview.redd.it/buzyn25jzr761.png?width=1000&format=png&auto=webp&s=c8a55973b52a27e003269914ed1a883849ce4bdc");

        // Commands
        if (opener.equals(".help")) {
            eb.clear();
            eb.setAuthor("Wordle Bot Commands");
            eb.setColor(new Color(245, 59, 83));
            eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
            eb.addField("To add your score to the leaderboard: ", "``!wordle [WORDLE NUMBER] [WORDLE SCORE]``", false);
            eb.addField("To view today's wordle score: ", "``!tordle``", false);
            eb.addField("To view a past wordle leaderboard: ", "``!oldle [WORDLE NUMBER]``", false);
            e.getChannel().sendMessage(eb.build()).queue();
        }

        if (opener.equalsIgnoreCase("!wordle")) {
            String scoreString = brokeString[2];
            try {
                String score = scoreString.substring(0,1);
                String fileName = wordleNumber + ".txt";

                FileWriter writer = new FileWriter(fileName, true);
                writer.write(username + " " + score + "\n");
                writer.close();
            } catch (IOException ex) {
                System.out.println("IO Exception");
            }

            eb.clear();
            eb.setAuthor("Success! Wordle #" + wordleNumber + " score for " + username + " added.");
            eb.setColor(new Color(0, 255, 0));
            eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
            e.getChannel().sendMessage(eb.build()).queue();
        }

        if (opener.equals("!tordle")) {
            eb.clear();
            int score;
            String user;
            String[] brokenLine;
            StringBuilder embedUsernames = new StringBuilder("");
            StringBuilder embedScores = new StringBuilder("");

            try {
                String fileName = wordleNumber + ".txt";
                BufferedReader read = new BufferedReader(new FileReader(fileName));
                TreeMap<Integer, TreeSet<String>> rankTree = new TreeMap<>();
                eb.clear();

                String line = read.readLine();
                if (line == null) {
                    throw new EmptyLeaderboardException();
                }
                while (line != null) {
                    brokenLine = line.split(" ");

                    user = brokenLine[0];
                    score = Integer.parseInt(brokenLine[1]);

                    if (rankTree.containsKey(score)) {
                        rankTree.get(score).add(user);
                    } else {
                        rankTree.put(score, new TreeSet<String>());
                        rankTree.get(score).add(user);
                    }

                    line = read.readLine();
                }
                read.close();

                int counter = 1;
                for(Integer key : rankTree.keySet()) {
                    for(String name: rankTree.get(key)) {
                        embedUsernames.append(counter);
                        embedUsernames.append(". ");
                        embedUsernames.append(name);


                        if (counter == 1) {
                            embedUsernames.append(" <:crowny:845241528771149854>");
                        } else if (rankTree.lastKey() == key && rankTree.get(key).last() == name) {
                            embedUsernames.append(" <:poopy:845240577679425558>");
                        }
                        embedUsernames.append("\n");

                        embedScores.append(key + "/6");
                        embedScores.append("\n");
                    }
                    counter++;
                }

                eb.setAuthor("TODAY'S SCORDLE on " + date.format(myFormatObj) + " (#" + wordleNumber + ")");
                eb.setColor(new Color(255, 215, 0));
                eb.addField("Name:", embedUsernames.toString(), true);
                eb.addField("", "           ", true);
                eb.addField("Score:", embedScores.toString(), true);
                eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
                e.getChannel().sendMessage(eb.build()).queue();
            } catch (IOException ex) {
                eb.clear();
                eb.setAuthor("Wordle " + wordleNumber + " Leaderboard Does Not Have Any Entries!");
                eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
                eb.setColor(new Color(245, 0, 0));
                e.getChannel().sendMessage(eb.build()).queue();
            } catch (EmptyLeaderboardException ex) {
                eb.clear();
                eb.setAuthor("Wordle " + wordleNumber + " Leaderboard Does Not Have Any Entries!");
                eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
                eb.setColor(new Color(245, 0, 0));
                e.getChannel().sendMessage(eb.build()).queue();
            }
        }

        if (opener.equals("!oldle")) {
            int score;
            String user;
            String[] brokenLine;
            StringBuilder embedUsernames = new StringBuilder("");
            StringBuilder embedScores = new StringBuilder("");
            String oldleNum = null;

            try {
                oldleNum = brokeString[1];
                String fileName = oldleNum + ".txt";
                BufferedReader read = new BufferedReader(new FileReader(fileName));
                TreeMap<Integer, TreeSet<String>> rankTree = new TreeMap<>();
                eb.clear();

                String line = read.readLine();
                if (line == null) {
                    throw new EmptyLeaderboardException();
                }

                while (line != null) {
                    brokenLine = line.split(" ");

                    user = brokenLine[0];
                    score = Integer.parseInt(brokenLine[1]);

                    if (rankTree.containsKey(score)) {
                        rankTree.get(score).add(user);
                    } else {
                        rankTree.put(score, new TreeSet<String>());
                        rankTree.get(score).add(user);
                    }
                    line = read.readLine();
                }
                read.close();

                int counter = 1;
                for(Integer key : rankTree.keySet()) {
                    for(String name: rankTree.get(key)) {
                        embedUsernames.append(counter);
                        embedUsernames.append(". ");
                        embedUsernames.append(name);


                        if (counter == 1) {
                            embedUsernames.append(" <:crowny:845241528771149854>");
                        } else if (rankTree.lastKey() == key && rankTree.get(key).last() == name) {
                            embedUsernames.append(" <:poopy:845240577679425558>");
                        }
                        embedUsernames.append("\n");

                        embedScores.append(key + "/6");
                        embedScores.append("\n");
                    }
                    counter++;
                }

                eb.setAuthor("SCORDLE on " + date.format(myFormatObj) + " (#" + oldleNum + ")");
                eb.setColor(new Color(255, 215, 0));
                eb.addField("Name:", embedUsernames.toString(), true);
                eb.addField("", "           ", true);
                eb.addField("Score:", embedScores.toString(), true);
                eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
                e.getChannel().sendMessage(eb.build()).queue();
            } catch (IOException ex) {
                eb.clear();
                eb.setAuthor("Wordle " + oldleNum + " Leaderboard Does Not Have Any Entries!");
                eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
                eb.setColor(new Color(245, 0, 0));
                e.getChannel().sendMessage(eb.build()).queue();
            } catch (EmptyLeaderboardException ex) {
                eb.clear();
                eb.setAuthor("Wordle " + oldleNum + " Leaderboard Does Not Have Any Entries!");
                eb.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
                eb.setColor(new Color(245, 0, 0));
                e.getChannel().sendMessage(eb.build()).queue();
            }
        }

    }

}
