package zedly.zenchantments.enchantments;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import zedly.zenchantments.CustomEnchantment;
import zedly.zenchantments.Storage;
import zedly.zenchantments.enums.Hand;
import zedly.zenchantments.enums.Tool;
import zedly.zenchantments.util.Utilities;

import static org.bukkit.Material.*;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
import static zedly.zenchantments.enums.Tool.HOE;

public class Persephone extends CustomEnchantment {

    public static final int ID = 41;

    @Override
    public Builder<Persephone> defaults() {
        return new Builder<>(Persephone::new, ID)
                .maxLevel(3)
                .loreName("Persephone")
                .probability(0)
                .enchantable(new Tool[]{HOE})
                .conflicting()
                .description("Plants seeds from the player's inventory around them")
                .cooldown(0)
                .power(1.0)
                .handUse(Hand.RIGHT);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent evt, int level, boolean usedHand) {
        if (evt.getAction() == RIGHT_CLICK_BLOCK) {
            Player player = evt.getPlayer();
            Location loc = evt.getClickedBlock().getLocation();
            int radiusXZ = (int) Math.round(power * level + 2);

            if (Storage.COMPATIBILITY_ADAPTER.PersephoneCrops().contains(evt.getClickedBlock().getType())) {
                Block block = loc.getBlock();
                for (int x = -radiusXZ; x <= radiusXZ; x++) {
                    for (int y = -2; y <= 0; y++) {
                        for (int z = -radiusXZ; z <= radiusXZ; z++) {

                            if (block.getRelative(x, y, z).getLocation().distanceSquared(loc)
                                    < radiusXZ * radiusXZ) {
                                if (block.getRelative(x, y, z).getType() == FARMLAND
                                        && Storage.COMPATIBILITY_ADAPTER.Airs().contains(block.getRelative(x, y + 1, z).getType())) {
                                    if (evt.getPlayer().getInventory().contains(CARROT)) {
                                        if (ADAPTER.placeBlock(block.getRelative(x, y + 1, z), player, CARROTS,
                                                null)) {
                                            Utilities.removeItem(player, CARROT, 1);
                                        }
                                    } else if (evt.getPlayer().getInventory().contains(POTATO)) {
                                        if (ADAPTER.placeBlock(block.getRelative(x, y + 1, z), player, POTATOES,
                                                null)) {
                                            Utilities.removeItem(player, POTATO, 1);
                                        }
                                    } else if (evt.getPlayer().getInventory().contains(WHEAT_SEEDS)) {
                                        if (ADAPTER.placeBlock(block.getRelative(x, y + 1, z), player, WHEAT, null)) {
                                            Utilities.removeItem(player, WHEAT_SEEDS, 1);
                                        }
                                    } else if (evt.getPlayer().getInventory().contains(BEETROOT_SEEDS)) {
                                        if (ADAPTER.placeBlock(block.getRelative(x, y + 1, z), player, BEETROOTS,
                                                null)) {
                                            Utilities.removeItem(player, BEETROOT_SEEDS, 1);
                                        }
                                    }
                                } else if (block.getRelative(x, y, z).getType() == SOUL_SAND
                                        && Storage.COMPATIBILITY_ADAPTER.Airs().contains(block.getRelative(x, y + 1, z).getType())) {
                                    if (evt.getPlayer().getInventory().contains(NETHER_WART)) {
                                        if (ADAPTER.placeBlock(block.getRelative(x, y + 1, z), player, NETHER_WART,
                                                null)) {
                                            Utilities.removeItem(player, NETHER_WART, 1);
                                        }
                                    }
                                } else {
                                    continue;
                                }
                                if (Storage.rnd.nextBoolean()) {
                                    Utilities.damageTool(evt.getPlayer(), 1, usedHand);
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}
