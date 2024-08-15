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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class EliteMasksAddon extends JavaPlugin {

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

//        getServer().getPluginManager().registerEvents(new Listener() {
//
//            @EventHandler
//            public void onRightClick(PlayerInteractEvent event) {
//                Player player = event.getPlayer();
//                ItemStack item = player.getInventory().getItemInMainHand();
//
//                if (event.getAction() == Action.RIGHT_CLICK_AIR && item != null) {
//                    item.setType(Material.END_ROD);
//                }
//            }
//
//        }, this);
    }

    @Override
    public void onDisable() {
    }

}
