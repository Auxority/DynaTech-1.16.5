package me.profelements.dynatech.items.electric;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.profelements.dynatech.DynaTech;
import me.profelements.dynatech.items.electric.abstracts.AMachine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BarbedWire extends AMachine {
    private static final int MAX_DIRECTION_VEL = 50;
    private static final Double MAX_RANGE = 9D;
    private static final int MIN_WAIT_TIME = 8;
    private static final int PUSH_POWER = 2;

    public BarbedWire(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void tick(Block b) {
        if (getCharge(b.getLocation()) < getEnergyConsumption())  {
            return;
        }
        
        DynaTech.runSync(()->sendEntitiesFlying(b.getLocation(), b.getWorld()));
        removeCharge(b.getLocation(), getEnergyConsumption());
    }

    public void sendEntitiesFlying(@Nonnull Location loc, @Nonnull World w) {
        List<Entity> shotEntities = new ArrayList<>();
        Vector wirePosition = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).add(new Vector(0.5, 0, 0.5));
        Collection<Entity> nearbyEntites = w.getNearbyEntities(loc, MAX_RANGE, MAX_RANGE, MAX_RANGE);
        for (Entity e : nearbyEntites) {
            Vector entityVelocity = e.getVelocity();
            if (isLaunchableEntity(shotEntities, e)) {
                Vector pushVelocity = calcPushVelocity(wirePosition, e, entityVelocity);
                if (NumberConversions.isFinite(pushVelocity.getX()) && NumberConversions.isFinite(pushVelocity.getY()) && NumberConversions.isFinite(pushVelocity.getZ())) {
                    e.setVelocity(pushVelocity);
                    shotEntities.add(e);
                } else if (!NumberConversions.isFinite(entityVelocity.getX()) || !NumberConversions.isFinite(entityVelocity.getY()) || !NumberConversions.isFinite(entityVelocity.getZ())) {
                    e.setVelocity(new Vector(0, 0, 0));
                }
            }
        }
    }

    @Override
    public String getMachineIdentifier() {
        return "BARBED_WIRE";
    }


    @Override
    public boolean isGraphical() {
        return false;
    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.IRON_BARS);
    }
    
    private boolean isLaunchableEntity(List<Entity> shotEntities, Entity e) {
        return e.getType() != EntityType.PLAYER
            && e.getType() != EntityType.ARMOR_STAND
            && e.getType() != EntityType.DROPPED_ITEM
            && !shotEntities.contains(e);
    }

    private Vector limitVelocity(Vector velocity) {
        if (velocity.getX() >= MAX_DIRECTION_VEL || velocity.getY() >= MAX_DIRECTION_VEL || velocity.getZ() >= MAX_DIRECTION_VEL) {
            velocity = new Vector(0, 0, 0);
        }
        return velocity;
    }
    
    private Vector calcPushVelocity(Vector wirePosition, Entity e, Vector entityVelocity) {
        Location entityLocation = e.getLocation();
        Vector entityPosition = new Vector(entityLocation.getX(), entityLocation.getY(), entityLocation.getZ());
        Vector offset = entityPosition.subtract(wirePosition);
        Vector unit = offset.normalize();
        Double distanceSq = offset.lengthSquared();
        Vector extraVelocity = unit.multiply(PUSH_POWER / distanceSq);
        return limitVelocity(extraVelocity);
    }
}
