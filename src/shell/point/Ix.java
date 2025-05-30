package shell.point;

import java.io.File;
import java.util.ArrayList;

import shell.exceptions.TerminalParseException;
import shell.file.FileManagement;
import shell.file.PointSetPath;
import shell.terminal.commands.OptionList;

public class Ix extends PointCollection {
    public static String cmd = "ix";
    public static OptionList opts = new OptionList("i", "ix", "ixdar", "load", "ld");

    public static ArrayList<PointND> parse(String[] args, int startIdx) throws TerminalParseException {
        PointSetPath retTup = parseFull(args, startIdx);
        return retTup.ps;
    }

    public static PointSetPath parseFull(String[] args, int startIdx) throws TerminalParseException {
        File loadFile = FileManagement.getTestFile(args[startIdx]);
        PointSetPath retTup = FileManagement.importFromFile(loadFile);
        return retTup;
    }

    public static Ix parseIx(String[] args, int startIdx) throws TerminalParseException {
        PointSetPath retTup = parseFull(args, startIdx);
        Ix ix = new Ix(args[startIdx], retTup.ps);
        return ix;
    }

    @Override
    public PointCollection parseCollection(String[] args, int startIdx) throws TerminalParseException {
        PointCollection c = parseIx(args, startIdx);
        return c;
    }

    @Override
    public int minArgLength() {
        return 1;
    }

    @Override
    public ArrayList<PointND> realizePoints() {
        return points;
    }

    @Override
    public String desc() {
        return "all of the points contained in another ix file";
    }

    @Override
    public String usage() {
        return "usage: add ix [name of ix file(filename)]";
    }

    @Override
    public int argLength() {
        return 1;
    }

    @Override
    public OptionList options() {
        return opts;
    }

    String fileName;
    ArrayList<PointND> points;

    public Ix() {
        fileName = "djbouti.ix";
    }

    public Ix(String fileName, ArrayList<PointND> points) {
        this.fileName = fileName;
        this.points = points;
    }

    @Override
    public String toFileString() {
        return "IX " + fileName;
    }

    @Override
    public String fullName() {
        return "ixdar";
    }

    @Override
    public String shortName() {
        return "ix";
    }

}
