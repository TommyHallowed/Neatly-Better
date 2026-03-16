package net.hallowed.neatlybetter.content.item;

import net.hallowed.neatlybetter.util.FastChunkScanner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

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

    public MapBuilderItem(Properties settings) {
        super(settings);
    }

    public interface MapGenerationTask {

        boolean process();

        float getProgress();

        default boolean isWaiting() { return false; }
    }

    public static class MapGenerationQueue {
        private static final Queue<GenerationJob> JOBS = new LinkedList<>();

        public static void addJob(ServerPlayer player, List<MapGenerationTask> tasks) {
            JOBS.add(new GenerationJob(player, tasks));
        }

        public static void tick() {
            if (JOBS.isEmpty()) return;

            long deadline = System.nanoTime() + 15_000_000L; // 15 ms budget
            GenerationJob currentJob = JOBS.peek();

            while (currentJob != null && System.nanoTime() < deadline) {
                boolean jobFinished = currentJob.processNext();
                if (jobFinished) {
                    currentJob.finish();
                    JOBS.poll();
                    currentJob = JOBS.peek();
                    continue;
                }
                if (currentJob.isThrottled()) break;
            }
        }

        private static class GenerationJob {
            private final ServerPlayer player;
            private final Queue<MapGenerationTask> queuedTasks;
            private final List<MapGenerationTask> activeTasks = new ArrayList<>();
            private final int totalTasks;
            private int completed = 0;
            private final ServerBossEvent bossBar;

            public GenerationJob(ServerPlayer player, List<MapGenerationTask> tasks) {
                this.player = player;
                this.queuedTasks = new LinkedList<>(tasks);
                this.totalTasks = tasks.size();
                this.bossBar = (ServerBossEvent) new ServerBossEvent(
                        Component.literal("Mapping Area (0%)"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS
                ).setDarkenScreen(false);
                this.bossBar.addPlayer(player);
            }

            public boolean processNext() {
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
                for (MapGenerationTask task : activeTasks) {
                    if (task.isWaiting()) return true;
                }
                return false;
            }

            private void updateBossBar() {
                if (totalTasks == 0) return;

                float activeProgress = 0;
                for (MapGenerationTask task : activeTasks) {
                    activeProgress += task.getProgress();
                }

                float percent = (completed + activeProgress) / totalTasks;
                percent = Math.min(1.0f, Math.max(0.0f, percent));
                bossBar.setProgress(percent);

                int displayPercent = (int)(percent * 100);
                bossBar.setName(Component.literal(String.format("Mapping Area (%d%%)", displayPercent)));
            }

            public void finish() {
                bossBar.removePlayer(player);
                player.displayClientMessage(Component.literal("Map generation complete!").withStyle(ChatFormatting.GREEN), false);
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        CompoundTag nbt = getCustomData(stack);
        int step = nbt.getIntOr(NBT_STEP, 0);
        BlockPos pos = context.getClickedPos();

        if (step == 2) {
            int p1X = nbt.getIntOr(NBT_P1_X, 0), p1Y = nbt.getIntOr(NBT_P1_Y, 0), p1Z = nbt.getIntOr(NBT_P1_Z, 0);
            int p2X = nbt.getIntOr(NBT_P2_X, 0), p2Y = nbt.getIntOr(NBT_P2_Y, 0), p2Z = nbt.getIntOr(NBT_P2_Z, 0);

            int minX = Math.min(p1X, p2X), maxX = Math.max(p1X, p2X);
            int minY = Math.min(p1Y, p2Y), maxY = Math.max(p1Y, p2Y);
            int minZ = Math.min(p1Z, p2Z), maxZ = Math.max(p1Z, p2Z);

            if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ) {
                if (buildMapWall((ServerLevel) world, player, stack)) {
                    nbt.putInt(NBT_STEP, 0);
                    nbt.putBoolean(NBT_HAS_P1, false);
                    nbt.putBoolean(NBT_HAS_P2, false);
                    saveCustomData(stack, nbt);
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }

        nbt.putInt(NBT_P1_X, pos.getX());
        nbt.putInt(NBT_P1_Y, pos.getY());
        nbt.putInt(NBT_P1_Z, pos.getZ());
        nbt.putBoolean(NBT_HAS_P1, true);
        nbt.putInt(NBT_FACING, context.getClickedFace().get3DDataValue());
        if (player != null) {
            nbt.putInt(NBT_PLAYER_FACING, player.getDirection().get3DDataValue());
        }
        checkAdvanceToStep2(stack, nbt);

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult use(Level world, Player user, @NotNull InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide()) {
            CompoundTag nbt = getCustomData(stack);
            if (user.isShiftKeyDown()) {
                int currentZoom = nbt.getIntOr(NBT_ZOOM, 0);
                int newZoom = (currentZoom + 1) % 5;
                nbt.putInt(NBT_ZOOM, newZoom);
                saveCustomData(stack, nbt);
                user.displayClientMessage(Component.literal("Map Zoom Level: " + newZoom).withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.SUCCESS;
            }

            if (nbt.getIntOr(NBT_STEP, 0) == 0) {
                nbt.putInt(NBT_STEP, 1);
                saveCustomData(stack, nbt);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void onLeftClickBlock(BlockPos pos, ItemStack stack) {
        CompoundTag nbt = getCustomData(stack);
        int step = nbt.getIntOr(NBT_STEP, 0);

        if (step == 0 || step == 1) {
            nbt.putInt(NBT_P2_X, pos.getX());
            nbt.putInt(NBT_P2_Y, pos.getY());
            nbt.putInt(NBT_P2_Z, pos.getZ());
            nbt.putBoolean(NBT_HAS_P2, true);
            checkAdvanceToStep2(stack, nbt);
        }
    }

    private void checkAdvanceToStep2(ItemStack stack, CompoundTag nbt) {
        if (nbt.getBooleanOr(NBT_HAS_P1, false) && nbt.getBooleanOr(NBT_HAS_P2, false)) {
            nbt.putInt(NBT_STEP, 2);
            if (!nbt.contains(NBT_ZOOM)) nbt.putInt(NBT_ZOOM, 0);
        }
        saveCustomData(stack, nbt);
    }

    private record MapTarget(BlockPos framePos, int targetX, int targetZ, int rotation) {}

    private boolean buildMapWall(ServerLevel world, Player player, ItemStack toolStack) {
        CompoundTag nbt = getCustomData(toolStack);
        int p1X = nbt.getIntOr(NBT_P1_X, 0), p1Y = nbt.getIntOr(NBT_P1_Y, 0), p1Z = nbt.getIntOr(NBT_P1_Z, 0);
        int p2X = nbt.getIntOr(NBT_P2_X, 0), p2Y = nbt.getIntOr(NBT_P2_Y, 0), p2Z = nbt.getIntOr(NBT_P2_Z, 0);

        Direction facing = Direction.from3DDataValue(nbt.getIntOr(NBT_FACING, Direction.UP.get3DDataValue()));

        Direction pFacing = Direction.from3DDataValue(nbt.getIntOr(NBT_PLAYER_FACING, Direction.NORTH.get3DDataValue()));
        if (pFacing.getAxis().isVertical()) pFacing = Direction.NORTH;

        int minX = Math.min(p1X, p2X), maxX = Math.max(p1X, p2X);
        int minY = Math.min(p1Y, p2Y), maxY = Math.max(p1Y, p2Y);
        int minZ = Math.min(p1Z, p2Z), maxZ = Math.max(p1Z, p2Z);

        BlockPos vecRight;
        BlockPos vecDown;
        int frameRotation;

        if (facing == Direction.UP || facing == Direction.DOWN) {
            switch (pFacing) {
                case EAST -> { vecRight = new BlockPos(0, 0, 1); vecDown = new BlockPos(-1, 0, 0); frameRotation = 1; }
                case SOUTH -> { vecRight = new BlockPos(-1, 0, 0); vecDown = new BlockPos(0, 0, -1); frameRotation = 2; }
                case WEST -> { vecRight = new BlockPos(0, 0, -1); vecDown = new BlockPos(1, 0, 0); frameRotation = 3; }
                default -> { vecRight = new BlockPos(1, 0, 0); vecDown = new BlockPos(0, 0, 1); frameRotation = 0; }
            }
        } else {
            switch (facing) {
                case NORTH -> { vecRight = new BlockPos(-1, 0, 0); vecDown = new BlockPos(0, -1, 0); }
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

        byte zoom = (byte) nbt.getIntOr(NBT_ZOOM, 0);
        int scaleMultiplier = 1 << zoom;
        int mapSize = 128 * scaleMultiplier;

        int gridX = Mth.floor((player.getX() + 64.0) / mapSize);
        int gridZ = Mth.floor((player.getZ() + 64.0) / mapSize);
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

                BlockPos framePos = new BlockPos(x, y, z).relative(facing);
                if (world.getBlockState(framePos).canBeReplaced()) {
                    pendingMaps.add(new MapTarget(framePos, mapTargetX, mapTargetZ, frameRotation));
                }
            }
        }

        int requiredItems = pendingMaps.size();
        if (requiredItems == 0) return false;

        int normalToUse = requiredItems;
        int glowToUse;

        if (!player.isCreative()) {
            int emptyMaps = player.getInventory().countItem(Items.MAP);
            int normalFrames = player.getInventory().countItem(Items.ITEM_FRAME);
            int glowFrames = player.getInventory().countItem(Items.GLOW_ITEM_FRAME);

            if (emptyMaps < requiredItems || (normalFrames + glowFrames) < requiredItems) {
                player.displayClientMessage(Component.literal("Not enough materials! Need " + requiredItems + "x Empty Map and Item Frames.").withStyle(ChatFormatting.RED), false);
                return false;
            }

            normalToUse = Math.min(normalFrames, requiredItems);
            glowToUse = requiredItems - normalToUse;

            consumeItems(player, Items.MAP, requiredItems);
            if (normalToUse > 0) consumeItems(player, Items.ITEM_FRAME, normalToUse);
            if (glowToUse > 0) consumeItems(player, Items.GLOW_ITEM_FRAME, glowToUse);
            toolStack.consume(1, player);
        }

        List<MapGenerationTask> unifiedPhase = new ArrayList<>();
        int normalSpawnsRemaining = normalToUse;

        for (MapTarget target : pendingMaps) {
            ItemStack mapStack = MapItem.create(world, target.targetX, target.targetZ, zoom, false, false);

            boolean useGlow = normalSpawnsRemaining <= 0;
            if (!useGlow) normalSpawnsRemaining--;

            ItemFrame itemFrame = useGlow ? new GlowItemFrame(world, target.framePos, facing) : new ItemFrame(world, target.framePos, facing);
            itemFrame.setItem(mapStack);
            itemFrame.setRotation(target.rotation);
            world.addFreshEntity(itemFrame);

            unifiedPhase.add(new FastChunkScanner(world, mapStack, target.targetX, target.targetZ, zoom));
        }

        MapGenerationQueue.addJob((ServerPlayer) player, unifiedPhase);

        return true;
    }

    private void consumeItems(Player player, Item item, int amount) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toTake = Math.min(amount, stack.getCount());
                stack.shrink(toTake);
                amount -= toTake;
                if (amount <= 0) break;
            }
        }
    }

    private CompoundTag getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private void saveCustomData(ItemStack stack, CompoundTag nbt) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
}