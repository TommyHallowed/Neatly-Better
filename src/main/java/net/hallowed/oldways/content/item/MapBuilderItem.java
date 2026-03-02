package net.hallowed.oldways.content.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;

public class MapBuilderItem extends Item {

    public static final String NBT_STEP = "BuildStep";
    public static final String NBT_HAS_P1 = "HasPos1";
    public static final String NBT_HAS_P2 = "HasPos2";
    public static final String NBT_P1_X = "P1X", NBT_P1_Y = "P1Y", NBT_P1_Z = "P1Z";
    public static final String NBT_P2_X = "P2X", NBT_P2_Y = "P2Y", NBT_P2_Z = "P2Z";
    public static final String NBT_ZOOM = "MapZoom";
    public static final String NBT_FACING = "MapFacing";

    public MapBuilderItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        NbtCompound nbt = getCustomData(stack);
        int step = nbt.getInt(NBT_STEP, 0);

        if (step == 0 || step == 1) {
            BlockPos pos = context.getBlockPos();
            nbt.putInt(NBT_P1_X, pos.getX());
            nbt.putInt(NBT_P1_Y, pos.getY());
            nbt.putInt(NBT_P1_Z, pos.getZ());
            nbt.putBoolean(NBT_HAS_P1, true);
            nbt.putInt(NBT_FACING, context.getSide().getIndex());

            if (player != null) {
                player.sendMessage(Text.literal("Corner 1 set at " + pos.toShortString()).formatted(Formatting.GREEN), true);
                checkAdvanceToStep2(stack, nbt, player);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return ActionResult.SUCCESS;

        NbtCompound nbt = getCustomData(stack);
        int step = nbt.getInt(NBT_STEP, 0);

        if (step == 0) {
            nbt.putInt(NBT_STEP, 1);
            saveCustomData(stack, nbt);
            user.sendMessage(Text.literal("Right Click a block to set corner 1\nLeft Click a block to set corner 2").formatted(Formatting.YELLOW), false);
        } else if (step == 2 && user.isSneaking()) {
            buildMapWall((ServerWorld) world, user, stack);

            nbt.putInt(NBT_STEP, 0);
            nbt.putBoolean(NBT_HAS_P1, false);
            nbt.putBoolean(NBT_HAS_P2, false);
            saveCustomData(stack, nbt);
        }

        return ActionResult.SUCCESS;
    }

    public void onLeftClickBlock(PlayerEntity player, BlockPos pos, ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        int step = nbt.getInt(NBT_STEP, 0);

        if (step == 0 || step == 1) {
            nbt.putInt(NBT_P2_X, pos.getX());
            nbt.putInt(NBT_P2_Y, pos.getY());
            nbt.putInt(NBT_P2_Z, pos.getZ());
            nbt.putBoolean(NBT_HAS_P2, true);

            if (player != null) {
                player.sendMessage(Text.literal("Corner 2 set at " + pos.toShortString()).formatted(Formatting.GREEN), true);
                checkAdvanceToStep2(stack, nbt, player);
            }
        }
    }

    private void checkAdvanceToStep2(ItemStack stack, NbtCompound nbt, PlayerEntity player) {
        if (nbt.getBoolean(NBT_HAS_P1, false) && nbt.getBoolean(NBT_HAS_P2, false)) {
            nbt.putInt(NBT_STEP, 2);
            if (!nbt.contains(NBT_ZOOM)) nbt.putInt(NBT_ZOOM, 0); // Default to Zoom 0

            int zoom = nbt.getInt(NBT_ZOOM, 0);
            Direction dir = Direction.byIndex(nbt.getInt(NBT_FACING, Direction.UP.getIndex()));
            String surface = dir.getAxis().isVertical() ? (dir == Direction.UP ? "Floor" : "Ceiling") : "Wall (" + dir.asString() + ")";

            player.sendMessage(Text.literal("Surface detected: " + surface + "\nMap zoom: " + zoom + "x (Shift+Z to change)\nShift+Right Click air to Build!").formatted(Formatting.LIGHT_PURPLE), false);
        }
        saveCustomData(stack, nbt);
    }

    private record MapTarget(BlockPos framePos, int targetX, int targetZ) {}

    private void buildMapWall(ServerWorld world, PlayerEntity player, ItemStack toolStack) {
        NbtCompound nbt = getCustomData(toolStack);

        int p1X = nbt.getInt(NBT_P1_X, 0), p1Y = nbt.getInt(NBT_P1_Y, 0), p1Z = nbt.getInt(NBT_P1_Z, 0);
        int p2X = nbt.getInt(NBT_P2_X, 0), p2Y = nbt.getInt(NBT_P2_Y, 0), p2Z = nbt.getInt(NBT_P2_Z, 0);

        Direction facing = Direction.byIndex(nbt.getInt(NBT_FACING, Direction.UP.getIndex()));
        if (facing == null) facing = Direction.UP;

        int minX = Math.min(p1X, p2X), maxX = Math.max(p1X, p2X);
        int minY = Math.min(p1Y, p2Y), maxY = Math.max(p1Y, p2Y);
        int minZ = Math.min(p1Z, p2Z), maxZ = Math.max(p1Z, p2Z);

        if (facing.getAxis() == Direction.Axis.X) { minX = p1X; maxX = p1X; }
        if (facing.getAxis() == Direction.Axis.Y) { minY = p1Y; maxY = p1Y; }
        if (facing.getAxis() == Direction.Axis.Z) { minZ = p1Z; maxZ = p1Z; }

        int width = (facing.getAxis() == Direction.Axis.X) ? maxZ - minZ + 1 : maxX - minX + 1;
        int height = (facing.getAxis() == Direction.Axis.Y) ? maxZ - minZ + 1 : maxY - minY + 1;

        int centerCol = width / 2;
        int centerRow = height / 2;

        byte zoom = (byte) nbt.getInt(NBT_ZOOM, 0);
        int scaleMultiplier = 1 << zoom;
        int mapSize = 128 * scaleMultiplier;

        // Snaps the center map exactly to the vanilla global grid based on the chunk the player is standing in
        int gridX = MathHelper.floor((player.getBlockX() + 64.0) / mapSize);
        int gridZ = MathHelper.floor((player.getBlockZ() + 64.0) / mapSize);
        int centerMapTargetX = gridX * mapSize + mapSize / 2 - 64;
        int centerMapTargetZ = gridZ * mapSize + mapSize / 2 - 64;

        List<MapTarget> pendingMaps = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    int col = 0, row = 0;

                    switch (facing) {
                        case NORTH -> { col = x - minX; row = maxY - y; }
                        case SOUTH -> { col = maxX - x; row = maxY - y; }
                        case WEST  -> { col = z - minZ; row = maxY - y; }
                        case EAST  -> { col = maxZ - z; row = maxY - y; }
                        case UP    -> { col = x - minX; row = z - minZ; }
                        case DOWN  -> { col = x - minX; row = maxZ - z; }
                    }

                    int mapTargetX = centerMapTargetX + (col - centerCol) * mapSize;
                    int mapTargetZ = centerMapTargetZ + (row - centerRow) * mapSize;

                    BlockPos framePos = new BlockPos(x, y, z).offset(facing);
                    pendingMaps.add(new MapTarget(framePos, mapTargetX, mapTargetZ));
                }
            }
        }

        int placed = 0;

        for (MapTarget target : pendingMaps) {
            if (!world.getBlockState(target.framePos).isReplaceable()) {
                continue;
            }

            ItemStack mapStack = FilledMapItem.createMap(world, target.targetX, target.targetZ, zoom, true, true);
            fillMapChunkByChunk(world, mapStack, target.targetX, target.targetZ, scaleMultiplier);

            ItemFrameEntity itemFrame = new ItemFrameEntity(world, target.framePos, facing);
            itemFrame.setHeldItemStack(mapStack);

            if (world.spawnEntity(itemFrame)) {
                placed++;
            }
        }

        player.sendMessage(Text.literal("Placed and generated " + placed + " filled maps!").formatted(Formatting.GREEN), true);

        if (placed > 0 && !player.isCreative()) {
            toolStack.decrement(1);
        }
    }

    private void fillMapChunkByChunk(ServerWorld world, ItemStack mapStack, int centerX, int centerZ, int scale) {
        MapIdComponent mapId = mapStack.get(DataComponentTypes.MAP_ID);
        if (mapId == null) return;
        MapState state = world.getMapState(mapId);
        if (state == null) return;

        int startX = centerX - 64 * scale;
        int startZ = centerZ - 64 * scale;

        Chunk lastChunk = null;
        int lastChunkX = Integer.MAX_VALUE;
        int lastChunkZ = Integer.MAX_VALUE;

        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                int worldX = startX + i * scale;
                int worldZ = startZ + j * scale;
                int chunkX = worldX >> 4;
                int chunkZ = worldZ >> 4;

                if (chunkX != lastChunkX || chunkZ != lastChunkZ) {
                    // FULL, true -> Forces the chunk to load or generate immediately!
                    lastChunk = world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
                    lastChunkX = chunkX;
                    lastChunkZ = chunkZ;
                }

                if (lastChunk != null) {
                    int localX = worldX & 15;
                    int localZ = worldZ & 15;

                    int topY = lastChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, localX, localZ) + 1;
                    if (topY <= world.getBottomY()) continue;

                    BlockPos.Mutable pos = new BlockPos.Mutable(worldX, topY, worldZ);
                    BlockState bState = Blocks.AIR.getDefaultState();

                    do {
                        pos.move(Direction.DOWN);
                        bState = lastChunk.getBlockState(pos);
                    } while (bState.getMapColor(world, pos) == MapColor.CLEAR && pos.getY() > world.getBottomY());

                    MapColor color = bState.getMapColor(world, pos);
                    state.putColor(i, j, color.getRenderColorByte(MapColor.Brightness.NORMAL));
                }
            }
        }
        state.markDirty();
    }

    private NbtCompound getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private void saveCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}