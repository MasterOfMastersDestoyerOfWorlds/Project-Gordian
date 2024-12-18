package shell.terminal.commands;

import java.io.File;

import shell.render.color.Color;
import shell.terminal.Terminal;
import shell.ui.input.KeyActions;

public class ListCommand extends TerminalCommand {

    public static String cmd = "ls";

    public static OptionList keyOptionAliases = new OptionList("keys", "keymap", "shortcuts", "bindings",
            "keybindings");

    public static OptionList commandOptionAliases = new OptionList("command", "cmds", "cmd", "commands",
            "commandlist");

    @Override
    public String fullName() {
        return "list";
    }

    @Override
    public String shortName() {
        return cmd;
    }

    @Override
    public String desc() {
        return "list information about an object";
    }

    @Override
    public String usage() {
        return "usage: ls|listfiles";
    }

    @Override
    public int argLength() {
        return -1;
    }

    @Override
    public String[] run(String[] args, int startIdx, Terminal terminal) {
        if (args.length == startIdx) {
            File[] solutions = new File(terminal.directory).listFiles();
            for (int i = 0; i < solutions.length; i++) {
                File f = solutions[i];
                terminal.history.addLine(f.getName(), f.isDirectory() ? Color.BLUE_WHITE : Color.IXDAR);
            }
            terminal.history.addLine("dir: " + terminal.directory, Color.GREEN);
            return new String[] { "cd " };
        } else {
            if (args[startIdx].equals("values")) {

            }
            if (args[startIdx].equals("questions")) {

            }
            if (keyOptionAliases.contains(args[startIdx])) {
                for (KeyActions k : KeyActions.values()) {
                    terminal.history.addLine(k.toString(), Color.GREEN);
                }
            }
            if (commandOptionAliases.contains(args[startIdx])) {
                for (TerminalCommand tc : Terminal.commandList) {
                    terminal.history.addWord(tc.shortName(), Color.COMMAND);
                    terminal.history.addLine(" - " + tc.desc(), Color.GREEN);
                }
            }
            return null;
        }

    }
}
