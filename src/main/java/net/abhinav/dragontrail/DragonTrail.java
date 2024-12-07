package net.abhinav.dragontrail;

import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.Vector;
import java.util.HashSet;
import java.util.Set;

public class DragonTrail extends JavaPlugin implements Listener {

    // Store dragons currently being tracked
    private Set<EnderDragon> activeDragons = new HashSet<>();
    private boolean trailEnabled = true;  // Default to enabled

    @Override
    public void onEnable() {
        // Register the event listener and command executor
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("toggledragontrail").setExecutor(new ToggleTrailCommand());

        // Always check for dragons in the world and create a trail for them
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                    if (entity instanceof EnderDragon) {
                        EnderDragon dragon = (EnderDragon) entity;
                        // Ensure we're not already tracking this dragon
                        if (!activeDragons.contains(dragon)) {
                            activeDragons.add(dragon);
                            createDragonTrail(dragon);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);  // Check every second (20 ticks)
    }

    @Override
    public void onDisable() {
        // Cleanup task (remove all dragons from tracking)
        activeDragons.clear();
    }

    // Method to create the dragon trail particles
    private void createDragonTrail(EnderDragon dragon) {
        if (!trailEnabled) return; // If the trail is disabled, don't create the trail

        new BukkitRunnable() {
            @Override
            public void run() {
                if (dragon.isDead()) {
                    activeDragons.remove(dragon);
                    cancel();
                    return;
                }

                // Dragon's location (or any other entity)
                Vector location = dragon.getLocation().toVector();

                // Create a bunch of particles to follow the dragon
                for (int i = 0; i < 100; i++) {
                    // Creating "dragon breath" particles in a spiral-like effect
                    dragon.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH,
                            location.getX() + Math.sin(i * 0.1) * i,
                            location.getY() + Math.sin(i * 0.1) * i * 0.5,
                            location.getZ() + Math.cos(i * 0.1) * i,
                            0, 0, 0, 0.1);  // The last values control particle spread and speed.
                }
            }
        }.runTaskTimer(this, 0, 1); // Spawns every tick (adjust the delay for different speeds)
    }

    // Handle dragon death event to stop the trail when a dragon dies
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            activeDragons.remove(event.getEntity());
        }
    }

    // Command to toggle the dragon trail on or off
    public class ToggleTrailCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                trailEnabled = !trailEnabled; // Toggle the state of the trail
                String status = trailEnabled ? "enabled" : "disabled";
                sender.sendMessage("Dragon trail has been " + status + ".");
                return true;
            }
            return false;
        }
    }
}
