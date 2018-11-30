package thaumicenergistics.container.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;

import thaumicenergistics.part.PartEssentiaStorageBus;

/**
 * @author BrockWS
 */
public class ContainerEssentiaStorageBus extends ContainerSharedEssentiaBus {

    public ContainerEssentiaStorageBus(EntityPlayer player, PartEssentiaStorageBus part) {
        super(player, part);
        this.bindContainerInventory(this.getEssentiaFilter(), new InventoryBasic("null", false, 7 * 9), 8, 29, 7, 9);
        this.bindUpgradesInventory(this.part.getInventoryByName("upgrades"), 187, 8, 5);
        this.bindPlayerInventory(player.inventory, 0, 167);
    }

    @Override
    protected int calculateSlotGroup(int index) {
        if (index < 18)
            return 0;
        if (index < 27)
            return 1;
        if (index < 36)
            return 2;
        if (index < 45)
            return 3;
        if (index < 54)
            return 4;
        if (index < 63)
            return 5;
        return 0;
    }
}