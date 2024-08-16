package me.uyuyuy99.elitemasksaddon;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class EliteMasksAddon extends JavaPlugin {

    private Map<UUID, Integer> hashedInvs = new HashMap<>();

    @Override
    public void onEnable() {
        ProtocolManager protocol = ProtocolLibrary.getProtocolManager();
        protocol.addPacketListener(new PacketAdapter(this,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                if (packet.getItemSlots().read(0) == EnumWrappers.ItemSlot.HEAD) {
                    ItemStack item = packet.getItemModifier().read(0);

                    if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof SkullMeta) {
                        SkullMeta meta = (SkullMeta) item.getItemMeta();

                        Field profileField;
                        GameProfile profile;
                        try {
                            profileField = meta.getClass().getDeclaredField("profile");
                            profileField.setAccessible(true);
                            profile = (GameProfile) profileField.get(meta);

                            Optional<Property> textureProp = profile.getProperties().get("textures")
                                    .stream()
                                    .filter(p -> p.getName().equals("textures"))
                                    .findFirst();

                            if (textureProp.isPresent()) {
                                String texture = textureProp.get().getValue();

                                if (ItemMask.isEndRod(texture)) {
                                    packet.getItemModifier().write(0, new ItemStack(Material.END_ROD, 1));
                                } else if (ItemMask.isBone(texture)) {
                                    packet.getItemModifier().write(0, new ItemStack(Material.BONE, 1));
                                } else if (ItemMask.isLead(texture)) {
                                    packet.getItemModifier().write(0, new ItemStack(Material.LEAD, 1));
                                }

                                event.setPacket(packet);
                            }
                        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {}
                    }
                }
            }
        });

        BukkitRunnable checkInvTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    UUID uuid = p.getUniqueId();
                    int hashCode = Arrays.hashCode(p.getInventory().getContents());

                    if (!hashedInvs.containsKey(uuid) || hashedInvs.get(uuid) != hashCode) {
                        hashedInvs.put(uuid, hashCode);

                        ItemStack[] contents = p.getInventory().getContents();
                        boolean changed = false;

                        for (ItemStack item : contents) {
                            if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof SkullMeta) {
                                SkullMeta meta = (SkullMeta) item.getItemMeta();

                                Field profileField;
                                GameProfile profile;
                                try {
                                    profileField = meta.getClass().getDeclaredField("profile");
                                    profileField.setAccessible(true);
                                    profile = (GameProfile) profileField.get(meta);

                                    Optional<Property> textureProp = profile.getProperties().get("textures")
                                            .stream()
                                            .filter(pp -> pp.getName().equals("textures"))
                                            .findFirst();

                                    if (textureProp.isPresent()) {
                                        String texture = textureProp.get().getValue();

                                        if (ItemMask.isEndRod(texture)) {
                                            item.setType(Material.END_ROD);
                                        } else if (ItemMask.isBone(texture)) {
                                            item.setType(Material.BONE);
                                        } else if (ItemMask.isLead(texture)) {
                                            item.setType(Material.LEAD);
                                        }

                                        changed = true;
                                    }
                                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {}
                            }
                        }

                        if (changed) {
                            p.getInventory().setContents(contents);
                        }
                    }
                }
            }
        };
        checkInvTask.runTaskTimer(this, 1L, 1L);
    }

    @Override
    public void onDisable() {
    }

}
