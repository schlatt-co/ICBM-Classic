package icbm.gui;

import icbm.ZhuYao;
import icbm.api.ICBM;
import icbm.jiqi.TFaSheShiMuo;
import net.minecraft.src.GuiTextField;

import org.lwjgl.opengl.GL11;

import universalelectricity.electricity.ElectricInfo;
import universalelectricity.electricity.ElectricInfo.ElectricUnit;
import universalelectricity.network.PacketManager;
import universalelectricity.prefab.Vector3;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GFaSheShiMuo extends ICBMGui
{
    private TFaSheShiMuo tileEntity;
    private GuiTextField textFieldX;
    private GuiTextField textFieldZ;
    private GuiTextField textFieldY;
    private GuiTextField textFieldFreq;

    private int containerWidth;
    private int containerHeight;
    
    public GFaSheShiMuo(TFaSheShiMuo par2ICBMTileEntityMissileLauncher)
    {
        this.tileEntity = par2ICBMTileEntityMissileLauncher;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
    	super.initGui();
        this.textFieldX = new GuiTextField(fontRenderer, 110, 37, 45, 12);
        this.textFieldZ = new GuiTextField(fontRenderer, 110, 52, 45, 12);
        this.textFieldY = new GuiTextField(fontRenderer, 110, 82, 45, 12);
        this.textFieldFreq = new GuiTextField(fontRenderer, 110, 97, 45, 12);
        this.textFieldFreq.setMaxStringLength(4);
        this.textFieldX.setMaxStringLength(6);
        this.textFieldZ.setMaxStringLength(6);
        this.textFieldY.setMaxStringLength(2);
                
        this.textFieldFreq.setText(this.tileEntity.frequency+"");
        
        if (this.tileEntity.getTarget() == null)
        {
        	this.textFieldX.setText(Math.round(this.tileEntity.xCoord) + "");
            this.textFieldZ.setText(Math.round(this.tileEntity.zCoord) + "");
            this.textFieldY.setText("0");
        }
        else
        {
            this.textFieldX.setText(Math.round(this.tileEntity.getTarget().x) + "");
            this.textFieldZ.setText(Math.round(this.tileEntity.getTarget().z) + "");
            this.textFieldY.setText(Math.round(this.tileEntity.getTarget().y) + "");
        }
        
    	PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ZhuYao.CHANNEL, this.tileEntity, (int)-1, true));
    }
    
    @Override
    public void onGuiClosed()
    {
    	super.onGuiClosed();
    	PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ZhuYao.CHANNEL, this.tileEntity, (int)-1, false));
    }

    /**
     * Call this method from you GuiScreen to process the keys into textbox.
     */
    @Override
	public void keyTyped(char par1, int par2)
    {
        super.keyTyped(par1, par2);
        this.textFieldX.textboxKeyTyped(par1, par2);
        this.textFieldZ.textboxKeyTyped(par1, par2);

        if(tileEntity.getTier() >= 1)
        {
            this.textFieldY.textboxKeyTyped(par1, par2);
            
            if(tileEntity.getTier() > 1)
            {
            	this.textFieldFreq.textboxKeyTyped(par1, par2);
            }
        }

        try
        {
        	Vector3 newTarget = new Vector3(Integer.parseInt(this.textFieldX.getText()), Math.max(Integer.parseInt(this.textFieldY.getText()), 0), Integer.parseInt(this.textFieldZ.getText()));
        	
        	this.tileEntity.setTarget(newTarget);
            PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ZhuYao.CHANNEL, this.tileEntity, (int)2, this.tileEntity.getTarget().x, this.tileEntity.getTarget().y, this.tileEntity.getTarget().z));
        }
        catch (NumberFormatException e)
        {
            this.tileEntity.setTarget(new Vector3(this.tileEntity.xCoord, 0, this.tileEntity.zCoord));
        }
        
        try
        {
        	short newFrequency = (short)Math.max(Short.parseShort(this.textFieldFreq.getText()), 0);
        	
        	this.tileEntity.frequency = newFrequency;
            PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ZhuYao.CHANNEL, this.tileEntity, (int)1, this.tileEntity.frequency));
        }
        catch (NumberFormatException e)
        {
            this.tileEntity.frequency = 0;
        }
    }

    /**
     * Args: x, y, buttonClicked
     */
    @Override
	public void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);
        this.textFieldX.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
        this.textFieldZ.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);

        if(tileEntity.getTier() >= 1)
        {
        	 this.textFieldY.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
        	 
        	 if(tileEntity.getTier() > 1)
             {
        		 this.textFieldFreq.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
             }
        }
       
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
	public void drawGuiContainerForegroundLayer()
    {    	
        this.textFieldX.drawTextBox();
        this.textFieldZ.drawTextBox();

        //Draw the air detonation GUI
        if(tileEntity.getTier() >= 1)
        {
        	this.textFieldY.drawTextBox();
        	this.fontRenderer.drawString("Air Detonation", 12, 70, 4210752);
        	this.fontRenderer.drawString("Height:", 12, 85, 4210752);
        	
        	if(tileEntity.getTier() > 1)
        	{
        		this.textFieldFreq.drawTextBox();
        		this.fontRenderer.drawString("Frequency:", 12, 100, 4210752);
        	}
        }
        
        this.fontRenderer.drawString("", 45, 6, 4210752);
        this.fontRenderer.drawString("Launcher Control Panel", 30, 6, 4210752);
        
        this.fontRenderer.drawString("Missile Target", 12, 25, 4210752);
        this.fontRenderer.drawString("X-Coord:", 25, 40, 4210752);
        this.fontRenderer.drawString("Z-Coord:", 25, 55, 4210752);
        
        int inaccuracy = 30;
        
        if(this.tileEntity.connectedBase != null)
        {
        	if(this.tileEntity.connectedBase.jiaZi != null)
        	{
        		inaccuracy = this.tileEntity.connectedBase.jiaZi.getInaccuracy();
        	}
        }
        
        this.fontRenderer.drawString("Inaccuracy: "+inaccuracy+" blocks", 12, 113, 4210752);
        
        //Shows the status of the missile launcher
        this.fontRenderer.drawString("Status: "+this.tileEntity.getStatus(), 12, 125, 4210752);
    	this.fontRenderer.drawString("Voltage: "+this.tileEntity.getVoltage()+"v", 12, 137, 4210752);
        this.fontRenderer.drawString(ElectricInfo.getDisplayShort(this.tileEntity.getJoules(), ElectricUnit.JOULES)+ "/" +ElectricInfo.getDisplayShort(this.tileEntity.getMaxJoules(), ElectricUnit.WATT_HOUR), 12, 150, 4210752);
    }
    
    @Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
    {
    	int var4 = this.mc.renderEngine.getTexture(ICBM.TEXTURE_FILE_PATH+"EmptyGUI.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(var4);
        containerWidth = (this.width - this.xSize) / 2;
        containerHeight = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);
	}

    @Override
	public void updateScreen()
    {
        super.updateScreen();
        
    	this.textFieldX.setText(Math.round(this.tileEntity.getTarget().x) + "");
        this.textFieldZ.setText(Math.round(this.tileEntity.getTarget().z) + "");
        this.textFieldY.setText(Math.round(this.tileEntity.getTarget().y) + "");
        
    	this.textFieldFreq.setText(this.tileEntity.frequency + "");
    }
}
