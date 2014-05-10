package icbm.sentry.items.weapons;

import icbm.core.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.vector.RayTraceHelper;

/**
 * Prefab for all weapon to be based on in ICBM. Does the same basic logic as sentry guns to fire the weapon.
 * 
 * @author Darkguardsman, Archtikz
 */
public abstract class ItemWeaponElectrical extends ItemICBMElectrical {
	protected int blockRange = 150;
	protected String soundEffect;
	protected int bps;
	protected int inaccuracy;
	
	// TODO: Fix inaccuracy/bps
	
	public ItemWeaponElectrical(int id, String name, String soundEffect) {
		super(id, name);
		this.soundEffect = soundEffect;
	}

	public ItemStack searchInventoryForAmmo(EntityPlayer player, boolean reality) {
		for(int i = 0; i < player.inventory.mainInventory.length; i++) {
			if(player.inventory.mainInventory[i] != null) {
				if(player.inventory.mainInventory[i].getItem() instanceof IItemAmmunition) {
					ItemStack stack = player.inventory.mainInventory[i];
					if(reality) {
						player.inventory.mainInventory[i] = null;
					}
					return stack;
				}
			}
		}
		return null;
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
		if(player.isSneaking()) {
			onSneakClick(itemstack, world, player);
			return itemstack;
		}
		
		onPreWeaponFired(itemstack, world, player);
		if(!isEmpty(itemstack)) {
			onWeaponFired(itemstack, world, player);
			onPostWeaponFired(itemstack, world, player);
		}
		return itemstack;
	}

	/**
	 * Basic check if the weapon can be fired.
	 * 
	 * @param itemstack - weapon
	 * @param world - world the weapon is going to be firing in
	 * @param player - player firing the weapon
	 * @param rounds - rounds the will be consumed when fired
	 * @return true if the weapon can be fired
	 */
	public boolean canFireWeapon(ItemStack itemstack, World world, EntityPlayer player, int rounds) {
		return true;
	}
	
	public abstract void onRender(World world, EntityPlayer player, Vector3 hit);
	public abstract boolean isEmpty(ItemStack stack);
	public abstract void onSneakClick(ItemStack stack, World world, EntityPlayer shooter);
	public abstract void onPreWeaponFired(ItemStack stack, World world, EntityPlayer shooter);
	public abstract void onPostWeaponFired(ItemStack stack, World world, EntityPlayer shooter);
	/**
	 * Called when the player fires the weapon, should handle all weapon firing actions, audio, and effects. Shouldn't handle ammo.
	 * 
	 * @param itemstack
	 * @param world
	 * @param player
	 */
	public void onWeaponFired(ItemStack weaponStack, World world, EntityPlayer player) {
		Vec3 playerPosition = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 playerLook = RayTraceHelper.getLook(player, 1.0f);
		Vec3 p = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord, playerPosition.yCoord + playerLook.yCoord, playerPosition.zCoord + playerLook.zCoord);
		Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * blockRange, playerPosition.yCoord + playerLook.yCoord * blockRange, playerPosition.zCoord + playerLook.zCoord * blockRange);
	
		MovingObjectPosition hit = RayTraceHelper.do_rayTraceFromEntity(player, new Vector3().toVec3(), blockRange, true);

		if (hit != null) {
			if (hit.typeOfHit == EnumMovingObjectType.ENTITY && hit.entityHit != null) {
				onHitEntity(world, player, hit.entityHit);
			} else if (hit.typeOfHit == EnumMovingObjectType.TILE) {
				onHitBlock(world, player, new Vector3(hit.hitVec));
			}
			playSoundEffect(player);
			playerViewOffset = hit.hitVec;
			onRender(world, player, new Vector3(hit));

			// TODO make beam brighter the longer it has been used
			// TODO adjust the laser for the end of the gun
			float x = (float) (MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.4) - MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
			float z = (float) (MathHelper.sin((float) (player.rotationYawHead * 0.0174532925)) * (-.4) + MathHelper.cos((float) (player.rotationYawHead * 0.0174532925)) * (-.1));
		}
	}

	public abstract void onHitEntity(World world, EntityPlayer shooter, Entity entityHit);
	public abstract void onHitBlock(World world, EntityPlayer shooter, Vector3 hitVec);
	
	public void playSoundEffect(EntityPlayer player) {
		if (this.soundEffect != null && !this.soundEffect.isEmpty()) player.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, this.soundEffect, 5F, 1F);
	}

	public float getGunDamage(ItemStack stack) {
		return 5f;
	}

}
