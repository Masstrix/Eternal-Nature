package me.masstrix.eternalnature.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LiquidFlow {

    public static final Direction SOURCE = new Direction("Source", "Source", 0, 0);
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    private Block block;
    private Direction direction;
    private int liquidHeight;

    private LiquidFlow(Block block, Direction direction, int height) {
        this.block = block;
        this.direction = direction;
        this.liquidHeight = height;
    }

    public Block getBlock() {
        return block;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getLiquidHeight() {
        return liquidHeight;
    }

    @Override
    public String toString() {
        return "LiquidFlow{" +
                "direction=" + direction +
                ", liquidHeight=" + liquidHeight +
                '}';
    }

    public static Direction getFlowDir(Block block, boolean randIfCenter) {
        if (block == null || !block.isLiquid()) return Direction.UNKNOWN;

        // Gets the blocks liquid data. Max is actually the lowest level
        // for the liquid in game. 0 is always a full source block.
        Levelled data = (Levelled) block.getBlockData();
        int lvl = data.getLevel();
        int max = data.getMaximumLevel();

        List<LiquidFlow> surroundingWater = new ArrayList<>();

        // Walk around block to find highest
        for (Direction d : Direction.getCompassDirections()) {
            if (!d.isCompass()) continue;
            Block rel = block.getRelative(d.getOffsetX(), 0, d.getOffsetZ());
            if (rel.getType() != block.getType()) continue;
            int l = ((Levelled) rel.getBlockData()).getLevel();
            surroundingWater.add(new LiquidFlow(block, d, l));
        }

        // This is a single block of water on its own.
        if (surroundingWater.size() == 0) {
            return Direction.NONE;
        }

        // Sort the list
        surroundingWater.sort(Comparator.comparingInt(LiquidFlow::getLiquidHeight));

        // Get the lowest and highest blocks
        LiquidFlow high = surroundingWater.get(0);
        LiquidFlow low = surroundingWater.get(surroundingWater.size() - 1);

        // Source block
        // FIXME this should not default if the source block has a direction (ie. not all sides)
        //       are water blocks
        if (lvl == 0)
            return randIfCenter ?  Direction.random() : SOURCE;

        return high.direction.opposite();
    }
}
