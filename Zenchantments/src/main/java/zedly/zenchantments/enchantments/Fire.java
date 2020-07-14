package zedly.zenchantments.enchantments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;

import zedly.zenchantments.CustomEnchantment;
import zedly.zenchantments.Storage;
import zedly.zenchantments.enums.Hand;
import zedly.zenchantments.enums.Tool;
import zedly.zenchantments.util.RecipeUtil;
import zedly.zenchantments.util.Utilities;

import java.util.List;
import java.util.Set;

import static org.bukkit.Material.*;
import static zedly.zenchantments.enums.Tool.*;

public class Fire extends CustomEnchantment {

    private static final int MAX_BLOCKS = 256;

    public static int[][] SEARCH_FACES_CACTUS = new int[][]{new int[]{0, 1, 0}};
    public static int[][] SEARCH_FACES_CHORUS = new int[][]{new int[]{-1, 0, 0}, new int[]{1, 0, 0}, new int[]{0, 1, 0}, new int[]{0, 0, -1}, new int[]{0, 0, 1}};

    public static final int ID = 13;
    
    public static boolean useSoftcoded = true;
    
    // Locations where Fire has been used on a block and the drop was changed. 
    // BlockBreakEvent is not cancelled but the original item drop is not desired.
    public static final Set<Block> cancelledItemDrops = new HashSet<>();

    @Override
    public Builder<Fire> defaults() {
        return new Builder<>(Fire::new, ID)
                .maxLevel(1)
                .loreName("Fire")
                .probability(0)
                .enchantable(new Tool[]{PICKAXE, AXE, SHOVEL})
                .conflicting(Switch.class, Variety.class)
                .description("Drops the smelted version of the block broken")
                .cooldown(0)
                .power(-1.0)
                .handUse(Hand.LEFT);
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent evt, int level, boolean usedHand) {
        if (evt.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return false;
        }

        if (!useSoftcoded) {
            Bukkit.getLogger().info("Hardcoded fire enchantment drops have been removed.");
            return false;
        } else {
            if (!evt.isDropItems()) {
                return false;
            }
            if (evt.getBlock().getType() == Material.CACTUS ||
                evt.getBlock().getType() == Material.CHORUS_PLANT) {
                return cactusDrop(evt, level, usedHand);
            }
            
            ItemStack hand = Utilities.usedStack(evt.getPlayer(), usedHand);
            Collection<ItemStack> original = evt.getBlock().getDrops(hand, evt.getPlayer());
            List<ItemStack> newDrops = new ArrayList<ItemStack>();
            for (ItemStack is: original) {
                ItemStack ns = RecipeUtil.getSmeltedVariant(is);
                int oldAmount = ns.getAmount();
                int amount = ns.getAmount();
                while (amount > ns.getMaxStackSize()) {
                    ns.setAmount(ns.getMaxStackSize());
                    newDrops.add(ns);
                    amount -= ns.getMaxStackSize();
                }
                ns.setAmount(oldAmount);
                newDrops.add(ns);
            }
            if (newDrops.size() != 0) {
                Utilities.display(Utilities.getCenter(evt.getBlock()), Particle.FLAME, 10, .1f, .5f, .5f, .5f);
                for (ItemStack is: newDrops) {
                    evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), is);
                }
                Block affectedBlock = evt.getBlock();
                cancelledItemDrops.add(affectedBlock);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Storage.zenchantments, () -> {
                    cancelledItemDrops.remove(affectedBlock);
                }, 5);

                return true;
            } else {
                return false;
            }
        }
        
    }
    
    private boolean cactusDrop(BlockBreakEvent evt, int level, boolean usedHand) {
        Material original = evt.getBlock().getType();
        
        if (original == CACTUS) {
            List<Block> bks = Utilities.BFS(evt.getBlock(), MAX_BLOCKS, false, 256,
                    SEARCH_FACES_CACTUS, Sets.immutableEnumSet(CACTUS), new HashSet<Material>(),
                    true);

            for (int i = bks.size() - 1; i >= 0; i--) {
                Block block = bks.get(i);

                Utilities.display(Utilities.getCenter(block), Particle.FLAME, 10, .1f, .5f, .5f, .5f);

                evt.getBlock().getWorld().dropItemNaturally(Utilities.getCenter(block.getLocation()),
                        new ItemStack(Material.GREEN_DYE, 1));
                block.setType(AIR);

                cancelledItemDrops.add(block);

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Storage.zenchantments, () -> {
                    cancelledItemDrops.remove(block);
                }, 5);

            }
            return true;
        } else if (original == CHORUS_PLANT) {
            List<Block> bks = Utilities.BFS(evt.getBlock(), MAX_BLOCKS, false, 256,
                    SEARCH_FACES_CHORUS, Sets.immutableEnumSet(CHORUS_PLANT, CHORUS_FLOWER), new HashSet<Material>(),
                    true);

            for (int i = bks.size() - 1; i >= 0; i--) {
                Block block = bks.get(i);

                Utilities.display(Utilities.getCenter(block), Particle.FLAME, 10, .1f, .5f, .5f, .5f);

                if (block.getType().equals(CHORUS_PLANT)) {
                    evt.getBlock().getWorld().dropItemNaturally(Utilities.getCenter(block.getLocation()),
                            new ItemStack(CHORUS_FRUIT, 1));
                    block.setType(AIR);
                } else {
                    if (!Storage.COMPATIBILITY_ADAPTER.breakBlockNMS(block, evt.getPlayer())) {
                        return false;
                    }
                }

                cancelledItemDrops.add(block);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Storage.zenchantments, () -> {
                    cancelledItemDrops.remove(block);
                }, 5);

            }
            return true;
        }
        return false;
    }
}
