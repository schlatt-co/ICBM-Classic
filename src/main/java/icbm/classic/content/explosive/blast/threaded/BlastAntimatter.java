package icbm.classic.content.explosive.blast.threaded;

import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.explosive.blast.BlastRedmatter;
import icbm.classic.content.explosive.thread2.IThreadWork;
import icbm.classic.content.explosive.thread2.ThreadWorkBlast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class BlastAntimatter extends BlastThreaded
{
    private boolean destroyBedrock;

    public BlastAntimatter(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastAntimatter(World world, Entity entity, double x, double y, double z, float size, boolean destroyBedrock)
    {
        this(world, entity, x, y, z, size);
        this.destroyBedrock = destroyBedrock;
    }

    /** Called before an explosion happens */
    @Override
    public void doPreExplode()
    {
        super.doPreExplode();
        ICBMSounds.ANTIMATTER.play(world, this.position.x(), this.position.y(), this.position.z(), 7F, (float) (this.world().rand.nextFloat() * 0.1 + 0.9F), true);
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    protected IThreadWork getWorkerTask()
    {
        return new ThreadWorkBlast((steps, edits) -> doRun(steps, edits), edits -> onWorkerThreadComplete(edits));
    }

    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        if (world instanceof WorldServer)
        {
            ((WorldServer) world).addScheduledTask(() -> {
                doExplode();
                destroyBlocks(edits); //TODO break up into chunks
                doPostExplode();
            });
        }
    }

    public void destroyBlock(BlockPos blockPos)
    {
        IBlockState blockState = world.getBlockState(blockPos);
        if (!blockState.getBlock().isAir(blockState, world, blockPos))
        {
            final double dist = position.distance(blockPos);
            if (dist < this.getBlastRadius() - 1 || world().rand.nextFloat() > 0.7)
            {
                if (blockState.getBlockHardness(this.world(), blockPos) >= 0 || destroyBedrock)
                {
                    world.setBlockToAir(blockPos);
                }
            }
        }
    }

    public boolean doRun(int loops, List<BlockPos> edits)
    {
        if (!this.world().isRemote)
        {
            for (int x = (int) -this.getBlastRadius(); x < this.getBlastRadius(); x++)
            {
                for (int y = (int) -this.getBlastRadius(); y < this.getBlastRadius(); y++)
                {
                    for (int z = (int) -this.getBlastRadius(); z < this.getBlastRadius(); z++)
                    {
                        final BlockPos blockPos = new BlockPos(position.xi() + x, position.yi() + y, position.zi() + z);
                        final double dist = position.distance(blockPos);

                        if (dist < this.getBlastRadius())
                        {
                            edits.add(blockPos);
                        }
                    }

                }
            }
        }
        return false;
    }

    @Override
    public void doExplode()
    {
        super.doExplode();

        // TODO: Render antimatter shockwave
        /*
         * else if (ZhuYao.proxy.isGaoQing()) { for (int x = -this.getRadius(); x <
         * this.getRadius(); x++) { for (int y = -this.getRadius(); y < this.getRadius(); y++) { for
         * (int z = -this.getRadius(); z < this.getRadius(); z++) { Vector3 targetPosition =
         * Vector3.add(position, new Vector3(x, y, z)); double distance =
         * position.distanceTo(targetPosition);
         * if (targetPosition.getBlockID(worldObj) == 0) { if (distance < this.getRadius() &&
         * distance > this.getRadius() - 1 && worldObj.rand.nextFloat() > 0.5) {
         * ParticleSpawner.spawnParticle("antimatter", worldObj, targetPosition); } } } } } }
         */
    }

    @Override
    public void doPostExplode()
    {
        super.doPostExplode();
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    protected boolean onDamageEntity(Entity entity)
    {
        if (entity instanceof EntityExplosion)
        {
            if (((EntityExplosion) entity).getBlast() instanceof BlastRedmatter)
            {
                entity.setDead();
                return true;
            }
        }

        return false;
    }
}