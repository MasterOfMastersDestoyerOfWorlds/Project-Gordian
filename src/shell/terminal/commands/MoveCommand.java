package shell.terminal.commands;

import shell.exceptions.IdDoesNotExistException;
import shell.exceptions.TerminalParseException;
import shell.file.FileManagement;
import shell.shell.Range;
import shell.terminal.Terminal;
import shell.ui.main.Main;

public class MoveCommand extends TerminalCommand {

    public static String cmd = "mv";

    @Override
    public String fullName() {
        return "move";
    }

    @Override
    public String shortName() {
        return cmd;
    }

    @Override
    public String desc() {
        return "move point to coordinates";
    }

    @Override
    public String usage() {
        return "usage: mv|move [target point to move(id)] [coordinate dimension 1(double)] ... [coordinate dimension n(double)]";
    }

    @Override
    public int argLength() {
        return 2;
    }

    @Override
    public String[] run(String[] args, int startIdx, Terminal terminal) {
        try {
            Range idTarget = Range.parse(args[startIdx + 1]);
            int idDest = Integer.parseInt(args[startIdx]);
            Main.orgShell.moveBefore(idTarget, idDest);
            FileManagement.rewriteSolutionFile(Main.file, Main.orgShell);
            return new String[] { "mb " + idTarget + " " };

        } catch (NumberFormatException e) {
            terminal.error("arguments are not integers: " + this.usage());
        } catch (IdDoesNotExistException e) {
            terminal.error("no point with id " + e.ID + " exists");
        } catch (TerminalParseException e) {
            terminal.error("could not parse range: " + e.message);
        }
        return null;
    }
}