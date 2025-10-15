package net.hallowed.oldways.content.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.particle.DragonBreathParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DragonBurstTrailEntity extends Entity {
    private int targetEntityId;
    private int lifeTime;

    public DragonBurstTrailEntity(EntityType<? extends DragonBurstTrailEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public DragonBurstTrailEntity(EntityType<? extends DragonBurstTrailEntity> type,
                                  ServerWorld world, int targetEntityId, int lifeTime) {
        this(type, world);
        this.targetEntityId = targetEntityId;
        this.lifeTime = lifeTime;
        this.setPos(0, 0, 0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) { }

    @Override
    public void tick() {
        super.tick();

        if (this.age > this.lifeTime) {
            this.discard();
            return;
        }

        Entity target = this.getEntityWorld().getEntityById(this.targetEntityId);
        if (target == null || !target.isAlive()) return;

        // Follow the player
        this.setPosition(target.getX(), target.getBodyY(0.5), target.getZ());

        // Strong, server-broadcast dragon-breath particles that dominate vanilla sparks
        if (this.getEntityWorld() instanceof ServerWorld sw) {
            Vec3d v = target.getVelocity();
            // a small backwards streak (behind the player’s motion)
            double backX = -v.x * 0.25;
            double backY = -v.y * 0.25;
            double backZ = -v.z * 0.25;

            var effect = new DragonBreathParticleEffect(ParticleTypes.DRAGON_BREATH, 1.0F);

            // Heavier count + modest spread to visually “win” over the firework trail
            sw.spawnParticles(effect,
                    this.getX() + backX, this.getY() + backY, this.getZ() + backZ,
                    8,                      // count per tick
                    0.25, 0.08, 0.25,        // spread (dx, dy, dz)
                    0.00);                   // speed
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putInt("DBR_TargetId", this.targetEntityId);
        view.putInt("DBR_LifeTime", this.lifeTime);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.targetEntityId = view.getInt("DBR_TargetId", 0);
        this.lifeTime = view.getInt("DBR_LifeTime", 0);
    }

    @Override public boolean isAttackable() { return false; }
}
