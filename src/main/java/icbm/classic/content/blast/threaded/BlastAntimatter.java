package icbm.classic.content.blast.threaded;

import icbm.classic.api.events.BlastCancelEvent;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigBlast;
import icbm.classic.content.blast.BlastHelpers;
import icbm.classic.content.blast.BlastRedmatter;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.lib.transform.BlockEditHandler;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlastAntimatter extends BlastThreaded
{
    private final IBlockState replaceState = Blocks.AIR.getDefaultState();

    private boolean antimatterDoBlockUpdates = false;
    private static final int antimatterWaterCleanupRange = 5; // antimatter water cleanup antimatterWaterCleanupRange
    private boolean makeHoles = false;

    public BlastAntimatter()
    {
    }

    /**
     * Called before an explosion happens
     */
    @Override
    public void setupBlast()
    {
        super.setupBlast();
        antimatterDoBlockUpdates = ConfigBlast.BLAST_DO_BLOCKUPDATES;
        ICBMSounds.ANTIMATTER.play(world, this.location.x(), this.location.y(), this.location.z(), 7F, (float) (this.world().rand.nextFloat() * 0.1 + 0.9F), true);
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    public void destroyBlock(BlockPos blockPos)
    {
        final IBlockState blockState = world.getBlockState(blockPos);
        if (!blockState.getBlock().isAir(blockState, world, blockPos))
        {
            if (blockState.getBlockHardness(world, blockPos) >= 0 || ConfigBlast.ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS)
            {
                world.setBlockState(blockPos, replaceState, antimatterDoBlockUpdates ? 3 : 2);
            }
        }
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        BlastHelpers.loopInRadius(this.getBlastRadius(), (x, y, z) -> {
            if (isInsideMap(y + yi()) && shouldEditPos(x, y, z))
            {
                edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z));
            }
        });
        return false;
    }

    @Override
    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        if (world instanceof WorldServer)
        {
            //Sort distance
            edits.sort(buildSorter());

            //Pull out fluids and falling blocks to prevent lag issues
            List<BlockPos> removeFirst = edits.stream().filter(blockPos -> {
                IBlockState state = world.getBlockState(blockPos);
                return state.getMaterial() == Material.WATER || state.getBlock() instanceof BlockFalling;
            }).collect(Collectors.toList());

            //Schedule edits to run in the world
            ((WorldServer) world).addScheduledTask(() -> {

                //Remove any blocks that could cause issues when queued
                removeFirst.forEach(blockPos -> world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 2));

                //Queue edits, even the ones from the previous
                BlockEditHandler.queue(world, edits, blockPos -> destroyBlock(blockPos));

                //Notify blast we have entered world again
                onPostThreadJoinWorld();
            });
        }
    }

    protected boolean isInsideMap(int y)
    {
        return y >= 0 && y < 256;
    }

    protected boolean shouldEditPos(int x, int y, int z)
    {
        final double distSQ = x * x + y * y + z * z;
        final double blastSQ = this.getBlastRadius() * this.getBlastRadius();

        final int featherEdge = (int)Math.floor(blastSQ * 0.05f);
        final int delta = (int)Math.floor(blastSQ - distSQ);

        if(delta < featherEdge)
        {
            final double p2 = 1 - (delta / (double)featherEdge);

            System.out.println(featherEdge + " " + delta + " " + p2);
            return world().rand.nextFloat() < p2;
        }
        return true;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    protected boolean onDamageEntity(Entity entity)
    {
        if (entity instanceof EntityExplosion)
        {
            if (((EntityExplosion) entity).getBlast() instanceof BlastRedmatter)
            {
                if (!MinecraftForge.EVENT_BUS.post(new BlastCancelEvent(this, ((EntityExplosion) entity).getBlast())))
                {
                    entity.setDead();
                }
                return true;
            }
        }

        return !ConfigBlast.ANTIMATTER_BLOCK_AND_ENT_DAMAGE_ON_REDMATTER; //if entity damage is enabled, return false so the entity damage logic can continue and vice versa
    }
}
