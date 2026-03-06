package net.hallowed.oldways.content.item;

import net.hallowed.oldways.util.FastChunkScanner;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MapBuilderItem extends Item {

    public static final String NBT_STEP = "BuildStep";
    public static final String NBT_HAS_P1 = "HasPos1", NBT_HAS_P2 = "HasPos2";
    public static final String NBT_P1_X = "P1X", NBT_P1_Y = "P1Y", NBT_P1_Z = "P1Z";
    public static final String NBT_P2_X = "P2X", NBT_P2_Y = "P2Y", NBT_P2_Z = "P2Z";
    public static final String NBT_ZOOM = "MapZoom";
    public static final String NBT_FACING = "MapFacing";
    public static final String NBT_PLAYER_FACING = "PlayerFacing";

    public MapBuilderItem(Settings settings) {
        super(settings);
    }

    public interface MapGenerationTask {
        boolean process();
        float getProgress();
    }

    public static class MapGenerationQueue {
        private static final Queue<GenerationJob> JOBS = new LinkedList<>();

        public static void addJob(ServerPlayerEntity player, List<MapGenerationTask> tasks) {
            JOBS.add(new GenerationJob(player, tasks));
        }

        public static void tick() {
            if (JOBS.isEmpty()) return;

            long start = System.currentTimeMillis();
            GenerationJob currentJob = JOBS.peek();

            if (currentJob != null) {
                while (System.currentTimeMillis() - start < 15) {
                    boolean jobFinished = currentJob.processNext();
                    if (jobFinished) {
                        currentJob.finish();
                        JOBS.poll();
                        break;
                    }
                    if (currentJob.isThrottled()) break;
                }
            }
        }

        private static class GenerationJob {
            private final ServerPlayerEntity player;
            private final Queue<MapGenerationTask> queuedTasks;
            private final List<MapGenerationTask> activeTasks = new ArrayList<>();
            private final int totalTasks;
            private int completed = 0;
            private final ServerBossBar bossBar;

            public GenerationJob(ServerPlayerEntity player, List<MapGenerationTask> tasks) {
                this.player = player;
                this.queuedTasks = new LinkedList<>(tasks);
                this.totalTasks = tasks.size();
                this.bossBar = (ServerBossBar) new ServerBossBar(
                        Text.literal("Mapping Area (0%)"), BossBar.Color.BLUE, BossBar.Style.PROGRESS
                ).setDarkenSky(false);
                this.bossBar.addPlayer(player);
            }

            public boolean processNext() {
                // Keep exactly ONE map generation task active at a time
                while (activeTasks.isEmpty() && !queuedTasks.isEmpty()) {
                    activeTasks.add(queuedTasks.poll());
                }

                activeTasks.removeIf(task -> {
                    if (task.process()) {
                        completed++;
                        return true;
                    }
                    return false;
                });

                updateBossBar();

                return queuedTasks.isEmpty() && activeTasks.isEmpty();
            }

            public boolean isThrottled() {
                return !activeTasks.isEmpty();
            }

            private void updateBossBar() {
                if (totalTasks == 0) return;

                float activeProgress = 0;
                for (MapGenerationTask task : activeTasks) {
                    activeProgress += task.getProgress();
                }

                float percent = (completed + activeProgress) / totalTasks;
                percent = Math.min(1.0f, Math.max(0.0f, percent));
                bossBar.setPercent(percent);

                int displayPercent = (int)(percent * 100);
                bossBar.setName(Text.literal(String.format("Mapping Area (%d%%)", displayPercent)));
            }

            public void finish() {
                bossBar.removePlayer(player);
                player.sendMessage(Text.literal("Map generation complete!").formatted(Formatting.GREEN), false);
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        NbtCompound nbt = getCustomData(stack);
        int step = nbt.getInt(NBT_STEP, 0);
        BlockPos pos = context.getBlockPos();

        if (step == 2) {
            int p1X = nbt.getInt(NBT_P1_X, 0), p1Y = nbt.getInt(NBT_P1_Y, 0), p1Z = nbt.getInt(NBT_P1_Z, 0);
            int p2X = nbt.getInt(NBT_P2_X, 0), p2Y = nbt.getInt(NBT_P2_Y, 0), p2Z = nbt.getInt(NBT_P2_Z, 0);

            int minX = Math.min(p1X, p2X), maxX = Math.max(p1X, p2X);
            int minY = Math.min(p1Y, p2Y), maxY = Math.max(p1Y, p2Y);
            int minZ = Math.min(p1Z, p2Z), maxZ = Math.max(p1Z, p2Z);

            if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ) {
                if (buildMapWall((ServerWorld) world, player, stack)) {
                    nbt.putInt(NBT_STEP, 0);
                    nbt.putBoolean(NBT_HAS_P1, false);
                    nbt.putBoolean(NBT_HAS_P2, false);
                    saveCustomData(stack, nbt);
                }
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }

        nbt.putInt(NBT_P1_X, pos.getX());
        nbt.putInt(NBT_P1_Y, pos.getY());
        nbt.putInt(NBT_P1_Z, pos.getZ());
        nbt.putBoolean(NBT_HAS_P1, true);
        nbt.putInt(NBT_FACING, context.getSide().getIndex());
        if (player != null) {
            nbt.putInt(NBT_PLAYER_FACING, player.getHorizontalFacing().getIndex());
        }
        checkAdvanceToStep2(stack, nbt);

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            NbtCompound nbt = getCustomData(stack);
            if (user.isSneaking()) {
                int currentZoom = nbt.getInt(NBT_ZOOM, 0);
                int newZoom = (currentZoom + 1) % 5;
                nbt.putInt(NBT_ZOOM, newZoom);
                saveCustomData(stack, nbt);
                user.sendMessage(Text.literal("Map Zoom Level: " + newZoom).formatted(Formatting.YELLOW), true);
                return ActionResult.SUCCESS;
            }

            if (nbt.getInt(NBT_STEP, 0) == 0) {
                nbt.putInt(NBT_STEP, 1);
                saveCustomData(stack, nbt);
            }
        }
        return ActionResult.SUCCESS;
    }

    public void onLeftClickBlock(BlockPos pos, ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        int step = nbt.getInt(NBT_STEP, 0);

        if (step == 0 || step == 1) {
            nbt.putInt(NBT_P2_X, pos.getX());
            nbt.putInt(NBT_P2_Y, pos.getY());
            nbt.putInt(NBT_P2_Z, pos.getZ());
            nbt.putBoolean(NBT_HAS_P2, true);
            checkAdvanceToStep2(stack, nbt);
        }
    }

    private void checkAdvanceToStep2(ItemStack stack, NbtCompound nbt) {
        if (nbt.getBoolean(NBT_HAS_P1, false) && nbt.getBoolean(NBT_HAS_P2, false)) {
            nbt.putInt(NBT_STEP, 2);
            if (!nbt.contains(NBT_ZOOM)) nbt.putInt(NBT_ZOOM, 0);
        }
        saveCustomData(stack, nbt);
    }

    private record MapTarget(BlockPos framePos, int targetX, int targetZ, int rotation) {}

    private boolean buildMapWall(ServerWorld world, PlayerEntity player, ItemStack toolStack) {
        NbtCompound nbt = getCustomData(toolStack);
        int p1X = nbt.getInt(NBT_P1_X, 0), p1Y = nbt.getInt(NBT_P1_Y, 0), p1Z = nbt.getInt(NBT_P1_Z, 0);
        int p2X = nbt.getInt(NBT_P2_X, 0), p2Y = nbt.getInt(NBT_P2_Y, 0), p2Z = nbt.getInt(NBT_P2_Z, 0);

        Direction facing = Direction.byIndex(nbt.getInt(NBT_FACING, Direction.UP.getIndex()));
        if (facing == null) facing = Direction.UP;

        Direction pFacing = Direction.byIndex(nbt.getInt(NBT_PLAYER_FACING, Direction.NORTH.getIndex()));
        if (pFacing == null || pFacing.getAxis().isVertical()) pFacing = Direction.NORTH;

        int minX = Math.min(p1X, p2X), maxX = Math.max(p1X, p2X);
        int minY = Math.min(p1Y, p2Y), maxY = Math.max(p1Y, p2Y);
        int minZ = Math.min(p1Z, p2Z), maxZ = Math.max(p1Z, p2Z);

        BlockPos vecRight;
        BlockPos vecDown;
        int frameRotation = 0;

        if (facing == Direction.UP || facing == Direction.DOWN) {
            switch (pFacing) {
                case NORTH -> { vecRight = new BlockPos(1, 0, 0); vecDown = new BlockPos(0, 0, 1); frameRotation = 0; }
                case EAST -> { vecRight = new BlockPos(0, 0, 1); vecDown = new BlockPos(-1, 0, 0); frameRotation = 1; }
                case SOUTH -> { vecRight = new BlockPos(-1, 0, 0); vecDown = new BlockPos(0, 0, -1); frameRotation = 2; }
                case WEST -> { vecRight = new BlockPos(0, 0, -1); vecDown = new BlockPos(1, 0, 0); frameRotation = 3; }
                default -> { vecRight = new BlockPos(1, 0, 0); vecDown = new BlockPos(0, 0, 1); frameRotation = 0; }
            }
        } else {
            switch (facing) {
                case NORTH -> { vecRight = new BlockPos(-1, 0, 0); vecDown = new BlockPos(0, -1, 0); }
                case SOUTH -> { vecRight = new BlockPos(1, 0, 0); vecDown = new BlockPos(0, -1, 0); }
                case WEST -> { vecRight = new BlockPos(0, 0, 1); vecDown = new BlockPos(0, -1, 0); }
                case EAST -> { vecRight = new BlockPos(0, 0, -1); vecDown = new BlockPos(0, -1, 0); }
                default -> { vecRight = new BlockPos(1, 0, 0); vecDown = new BlockPos(0, -1, 0); }
            }
            frameRotation = 0;
        }

        int width = Math.max(1, Math.abs(vecRight.getX() * (maxX - minX) + vecRight.getY() * (maxY - minY) + vecRight.getZ() * (maxZ - minZ)) + 1);
        int height = Math.max(1, Math.abs(vecDown.getX() * (maxX - minX) + vecDown.getY() * (maxY - minY) + vecDown.getZ() * (maxZ - minZ)) + 1);

        int startX = p1X;
        if (vecRight.getX() > 0 || vecDown.getX() > 0) startX = minX;
        else if (vecRight.getX() < 0 || vecDown.getX() < 0) startX = maxX;

        int startY = p1Y;
        if (vecRight.getY() > 0 || vecDown.getY() > 0) startY = minY;
        else if (vecRight.getY() < 0 || vecDown.getY() < 0) startY = maxY;

        int startZ = p1Z;
        if (vecRight.getZ() > 0 || vecDown.getZ() > 0) startZ = minZ;
        else if (vecRight.getZ() < 0 || vecDown.getZ() < 0) startZ = maxZ;

        byte zoom = (byte) nbt.getInt(NBT_ZOOM, 0);
        int scaleMultiplier = 1 << zoom;
        int mapSize = 128 * scaleMultiplier;

        int gridX = MathHelper.floor((player.getX() + 64.0) / mapSize);
        int gridZ = MathHelper.floor((player.getZ() + 64.0) / mapSize);
        int centerMapTargetX = gridX * mapSize + mapSize / 2 - 64;
        int centerMapTargetZ = gridZ * mapSize + mapSize / 2 - 64;

        List<MapTarget> pendingMaps = new ArrayList<>();

        for (int v = 0; v < height; v++) {
            for (int u = 0; u < width; u++) {
                int x = startX + u * vecRight.getX() + v * vecDown.getX();
                int y = startY + u * vecRight.getY() + v * vecDown.getY();
                int z = startZ + u * vecRight.getZ() + v * vecDown.getZ();

                int mapTargetX = centerMapTargetX + (u - width / 2) * mapSize;
                int mapTargetZ = centerMapTargetZ + (v - height / 2) * mapSize;

                BlockPos framePos = new BlockPos(x, y, z).offset(facing);
                if (world.getBlockState(framePos).isReplaceable()) {
                    pendingMaps.add(new MapTarget(framePos, mapTargetX, mapTargetZ, frameRotation));
                }
            }
        }

        int requiredItems = pendingMaps.size();
        if (requiredItems == 0) return false;

        int normalToUse = requiredItems;
        int glowToUse = 0;

        if (!player.isCreative()) {
            int emptyMaps = player.getInventory().count(Items.MAP);
            int normalFrames = player.getInventory().count(Items.ITEM_FRAME);
            int glowFrames = player.getInventory().count(Items.GLOW_ITEM_FRAME);

            if (emptyMaps < requiredItems || (normalFrames + glowFrames) < requiredItems) {
                player.sendMessage(Text.literal("Not enough materials! Need " + requiredItems + "x Empty Map and Item Frames.").formatted(Formatting.RED), false);
                return false;
            }

            normalToUse = Math.min(normalFrames, requiredItems);
            glowToUse = requiredItems - normalToUse;

            consumeItems(player, Items.MAP, requiredItems);
            if (normalToUse > 0) consumeItems(player, Items.ITEM_FRAME, normalToUse);
            if (glowToUse > 0) consumeItems(player, Items.GLOW_ITEM_FRAME, glowToUse);
            toolStack.decrementUnlessCreative(1, player);
        }

        List<MapGenerationTask> unifiedPhase = new ArrayList<>();
        int normalSpawnsRemaining = normalToUse;

        for (MapTarget target : pendingMaps) {
            ItemStack mapStack = FilledMapItem.createMap(world, target.targetX, target.targetZ, zoom, false, false);

            boolean useGlow = normalSpawnsRemaining <= 0;
            if (!useGlow) normalSpawnsRemaining--;

            ItemFrameEntity itemFrame = useGlow ? new GlowItemFrameEntity(world, target.framePos, facing) : new ItemFrameEntity(world, target.framePos, facing);
            itemFrame.setHeldItemStack(mapStack);
            itemFrame.setRotation(target.rotation);
            world.spawnEntity(itemFrame);

            // Create ONE unified task per map frame (The boolean constructor variable is gone!)
            unifiedPhase.add(new FastChunkScanner(world, mapStack, target.targetX, target.targetZ, zoom));
        }

        MapGenerationQueue.addJob((ServerPlayerEntity) player, unifiedPhase);

        return true;
    }

    private void consumeItems(PlayerEntity player, Item item, int amount) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                int toTake = Math.min(amount, stack.getCount());
                stack.decrement(toTake);
                amount -= toTake;
                if (amount <= 0) break;
            }
        }
    }

    private NbtCompound getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private void saveCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}