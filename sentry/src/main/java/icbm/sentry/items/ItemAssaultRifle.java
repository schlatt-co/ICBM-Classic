package icbm.sentry.items;

import java.util.List;

import icbm.core.prefab.item.ItemICBMBase;
import icbm.sentry.weapon.types.WeaponConventional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

public class ItemAssaultRifle extends ItemICBMBase {

	private WeaponConventional weaponSystem;
	
	public ItemAssaultRifle(int id) {
		super(id, "assaultRifle");
	}
	

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
		if(weaponSystem == null) {
			weaponSystem = new WeaponConventional(player, 10F);
			weaponSystem.aimOffset = new Vector3(1);
		}
		
		// TODO: Fix only hitting on client mode
		MovingObjectPosition mop = player.rayTrace(100, 1f);
		if(mop != null) {
			if(world.isRemote) System.out.println(mop.blockX + ", " + mop.blockY + ", " + mop.blockZ);
			if(mop.typeOfHit == EnumMovingObjectType.ENTITY) {
				weaponSystem.fire(Vector3.fromCenter(mop.entityHit));
			} else {
				weaponSystem.fire(new Vector3(mop.hitVec));
			}
		}
		return itemstack;
	}

	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
		//Don't bother with localization
		list.add("Work in progress - Archtikz");
		
		super.addInformation(itemStack, entityPlayer, list, par4);
	}
	
}