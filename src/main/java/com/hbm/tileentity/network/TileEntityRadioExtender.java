package com.hbm.tileentity.network;

import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRadioExtender extends TileEntityLoadedBase implements IRORValueProvider, IRORInteractive {

	public boolean valid_p = false;
	public boolean valid_i = false;
	public int[] coords = new int[3];

	public TileEntityRadioExtender() {
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();

			coords[0] = xCoord + dir.offsetX;
			coords[1] = yCoord + dir.offsetY;
			coords[2] = zCoord + dir.offsetZ;
			TileEntity tile = Compat.getTileStandard(worldObj, coords[0], coords[1], coords[2]);

			valid_p = tile instanceof IRORValueProvider;
			valid_i = tile instanceof IRORInteractive;

			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(valid_p);
		buf.writeBoolean(valid_i);
		BufferUtil.writeIntArray(buf, coords);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.valid_p = buf.readBoolean();
		this.valid_i = buf.readBoolean();
		this.coords = BufferUtil.readIntArray(buf);
	}

	// honestly this isn't very optimised and i don't like it, the TE check should happen only once but oh well serialization is fucking hard
	// i tried to but i suppose making a function to reconstruct it from raw bytes is way harder than just calling 3 times (if both provider and interactive work)
	@Override
	public String[] getFunctionInfo() {
		if (valid_p) {
			IRORValueProvider prov = (IRORValueProvider) Compat.getTileStandard(worldObj, coords[0], coords[1], coords[2]);
			if (prov != null)
				return prov.getFunctionInfo();
		}
		return new String[0];
	}

	@Override
	public String provideRORValue(String name) {
		if (valid_p) {
			IRORValueProvider prov = (IRORValueProvider) Compat.getTileStandard(worldObj, coords[0], coords[1], coords[2]);
			if (prov != null)
				return prov.provideRORValue(name);
		}
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {
		if (valid_i) {
			IRORInteractive inter = (IRORInteractive) Compat.getTileStandard(worldObj, coords[0], coords[1], coords[2]);
			if (inter != null)
				return inter.runRORFunction(name, params);
		}
		return null;
	}
}
