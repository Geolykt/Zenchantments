package zedly.zenchantments.enchantments;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import zedly.zenchantments.CustomEnchantment;
import zedly.zenchantments.Storage;
import zedly.zenchantments.enums.Hand;
import zedly.zenchantments.enums.Tool;
import zedly.zenchantments.util.Utilities;

import static org.bukkit.Material.AIR;
import static zedly.zenchantments.enums.Tool.SWORD;

public class Transformation extends CustomEnchantment {

    public static final int ID = 64;

    @Override
    public Builder<Transformation> defaults() {
        return new Builder<>(Transformation::new, ID)
                .maxLevel(3)
                .loreName("Transformation")
                .probability(0)
                .enchantable(new Tool[]{SWORD})
                .conflicting()
                .description("Occasionally causes the attacked mob to be transformed into its similar cousin")
                .cooldown(0)
                .power(1.0)
                .handUse(Hand.LEFT);
    }

    @Override
    public boolean onEntityHit(EntityDamageByEntityEvent evt, int level, boolean usedHand) {
        if (evt.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return false;
        }
        if (evt.getEntity() instanceof Tameable) {
            if (((Tameable) evt.getEntity()).isTamed()) {
                return false;
            }
        }
        if (evt.getEntity() instanceof LivingEntity
                && ADAPTER.attackEntity((LivingEntity) evt.getEntity(), (Player) evt.getDamager(), 0)) {
            if (Storage.rnd.nextInt(100) > (100 - (level * power * 8))) {
                LivingEntity newEnt = Storage.COMPATIBILITY_ADAPTER.TransformationCycle((LivingEntity) evt.getEntity(),
                        Storage.rnd);

                if (newEnt != null) {
                    if (evt.getDamage() > ((LivingEntity) evt.getEntity()).getHealth()) {
                        evt.setCancelled(true);
                    }
                    Utilities.display(Utilities.getCenter(evt.getEntity().getLocation()), Particle.HEART, 70, .1f,
                            .5f, 2, .5f);

                    double originalHealth = ((LivingEntity) evt.getEntity()).getHealth();
                    for (ItemStack stk : ((LivingEntity) evt.getEntity()).getEquipment().getArmorContents()) {
                        if (stk.getType() != AIR) {
                            newEnt.getWorld().dropItemNaturally(newEnt.getLocation(), stk);
                        }
                    }
                    if (((LivingEntity) evt.getEntity()).getEquipment().getItemInMainHand().getType() != AIR) {
                        newEnt.getWorld().dropItemNaturally(newEnt.getLocation(),
                                ((LivingEntity) evt.getEntity()).getEquipment().getItemInMainHand());
                    }
                    if (((LivingEntity) evt.getEntity()).getEquipment().getItemInOffHand().getType() != AIR) {
                        newEnt.getWorld().dropItemNaturally(newEnt.getLocation(),
                                ((LivingEntity) evt.getEntity()).getEquipment().getItemInOffHand());
                    }

                    evt.getEntity().remove();

                    newEnt.setHealth(Math.max(1,
                            Math.min(originalHealth, newEnt.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())));

                }
            }
        }
        return true;
    }
}
