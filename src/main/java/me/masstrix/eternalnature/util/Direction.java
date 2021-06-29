package me.masstrix.eternalnature.util;

import org.bukkit.util.Vector;

import java.util.*;

public class Direction {

    // Cardinal
    public static final Direction NORTH = new Direction("N", "North", 0, -1, true);
    public static final Direction EAST = new Direction("E", "East", 1, 0, true);
    public static final Direction SOUTH = new Direction("S", "South", 0, 1, true);
    public static final Direction WEST = new Direction("W", "West", -1, 0, true);

    // Ordinal
    public static final Direction NORTH_EAST = new Direction("NE", "North East", 1, -1, true);
    public static final Direction SOUTH_EAST = new Direction("SE", "South East", 1, 1, true);
    public static final Direction SOUTH_WEST = new Direction("SW", "South West", -1, 1, true);
    public static final Direction NORTH_WEST = new Direction("NW", "North West", -1, -1, true);

    // Misc
    public static final Direction UNKNOWN = new Direction("?", "Unknown", 0, 0);
    public static final Direction NONE = new Direction("None", "None", 0, 0);

    private static final List<Direction> CARDINAL_DIRECTIONS = new ArrayList<>();
    private static final List<Direction> ORDINAL_DIRECTIONS = new ArrayList<>();
    private static final List<Direction> COMPASS_DIRECTIONS = new ArrayList<>();

    static {
        // Setup opposite values.
        NORTH.opposite = SOUTH;
        SOUTH.opposite = NORTH;
        EAST.opposite = WEST;
        WEST.opposite = EAST;
        NORTH_EAST.opposite = SOUTH_WEST;
        SOUTH_EAST.opposite = NORTH_WEST;
        SOUTH_WEST.opposite = NORTH_EAST;
        NORTH_WEST.opposite = SOUTH_EAST;

        // Set cardinal's
        NORTH.cardinal = true;
        EAST.cardinal = true;
        SOUTH.cardinal = true;
        WEST.cardinal = true;

        CARDINAL_DIRECTIONS.addAll(Arrays.asList(NORTH, EAST, SOUTH, WEST));
        ORDINAL_DIRECTIONS.addAll(Arrays.asList(NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST));
        COMPASS_DIRECTIONS.addAll(Arrays.asList(
                NORTH, EAST, SOUTH, WEST,
                NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
        ));
    }

    public static List<Direction> getCompassDirections() {
        return Collections.unmodifiableList(COMPASS_DIRECTIONS);
    }

    public static List<Direction> getCardinalDirections() {
        return Collections.unmodifiableList(CARDINAL_DIRECTIONS);
    }

    public static List<Direction> getOrdinalDirections() {
        return Collections.unmodifiableList(ORDINAL_DIRECTIONS);
    }

    /**
     * Returns the compass/cardinal direction from a 2d direction. This works by using
     * the offset of x and z to get its rotation on a 2d plane.
     *
     * @param x x position.
     * @param z z position.
     * @return closest direction enum.
     */
    public static Direction compass(double x, double z) {
        int compass = (((int) Math.round(Math.atan2(x, z) / (2 * Math.PI / 8))) + 8) % 8;

        return switch (compass) {
            case 0 -> EAST;
            case 1 -> NORTH_EAST;
            case 2 -> NORTH;
            case 3 -> NORTH_WEST;
            case 4 -> WEST;
            case 5 -> SOUTH_WEST;
            case 6 -> SOUTH;
            case 7 -> SOUTH_EAST;
            default -> NORTH;
        };
    }

    /**
     * @return a random direction.
     */
    public static Direction random() {
        return COMPASS_DIRECTIONS.get(MathUtil.randomInt(8));
    }

    private final String NAME;
    private final String SH;
    private final int OFFSET_X;
    private final int OFFSET_Z;
    private final boolean COMPASS;
    private boolean cardinal;
    private Direction opposite;

    public Direction(String sh, String name, int x, int z) {
        this(sh, name, x, z, false);
    }

    public Direction(String sh, String name, int x, int z, boolean compass) {
        this.SH = sh;
        this.NAME = name;
        this.OFFSET_X = x;
        this.OFFSET_Z = z;
        this.COMPASS = compass;
        this.opposite = this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Direction direction)) return false;
        return OFFSET_X == direction.OFFSET_X
                && OFFSET_Z == direction.OFFSET_Z
                && COMPASS == direction.COMPASS && cardinal == direction.cardinal
                && Objects.equals(NAME, direction.NAME) && Objects.equals(opposite, direction.opposite);
    }

    @Override
    public String toString() {
        return NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME, OFFSET_X, OFFSET_Z, opposite);
    }

    public String getShortHand() {
        return SH;
    }

    public String getNiceName() {
        return NAME;
    }

    public int getOffsetX() {
        return OFFSET_X;
    }

    public int getOffsetZ() {
        return OFFSET_Z;
    }

    public Vector asVector() {
        return new Vector(OFFSET_X, 0, OFFSET_Z);
    }

    public boolean hasVector() {
        return OFFSET_Z != 0 && OFFSET_X != 0;
    }

    /**
     * @return the opposite direction.
     */
    public Direction opposite() {
        return this.opposite;
    }

    /**
     * Returns if this value is a compass direction, either N, NE, SE, S, SW or W.
     *
     * @return if this direction is a compass value.
     */
    public boolean isCompass() {
        return COMPASS;
    }

    public boolean isCardinal() {
        return cardinal;
    }
}
