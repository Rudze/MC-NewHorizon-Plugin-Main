package fr.rudy.newhorizon.loot;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class VaultLootListener implements Listener {

    private final Random random = new Random();

    // Probabilités des items rares/spéciaux (0.0 à 1.0)
    private static final double CURSED_BONES_CHANCE = 0.10; // 10% de chance (légèrement réduit pour plus de diversité)
    private static final double WITHER_SKULL_CHANCE = 0.03; // 3% de chance
    private static final double VAULT_KEY_CHANCE = 0.07; // 7% de chance de "rembourser" la clé (si la clé est un CustomStack "newhorizon:vault_key")

    @EventHandler
    public void onVaultLoot(BlockDispenseLootEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!(block.getState() instanceof Vault)) return;

        if (isInRegion(player, "dungeon_normal_skeleton")) {
            List<ItemStack> possibleLootItems = new ArrayList<>();

            // Tenter d'ajouter les items spéciaux avec leur probabilité
            if (random.nextDouble() < CURSED_BONES_CHANCE) {
                CustomStack cursedBones = CustomStack.getInstance("newhorizon:cursed_bones");
                if (cursedBones != null) {
                    possibleLootItems.add(cursedBones.getItemStack());
                }
            }

            if (random.nextDouble() < WITHER_SKULL_CHANCE) {
                possibleLootItems.add(new ItemStack(Material.WITHER_SKELETON_SKULL));
            }

            if (random.nextDouble() < VAULT_KEY_CHANCE) {
                CustomStack vaultKey = CustomStack.getInstance("newhorizon:vault_key"); // Assurez-vous que c'est le bon ID pour votre clé
                if (vaultKey != null) {
                    possibleLootItems.add(vaultKey.getItemStack());
                }
            }

            // Ajout d'items de remplissage classiques avec probabilités
            // (Ces probabilités sont relatives entre elles dans cette section)
            Map<ItemStack, Double> fillerItemsChances = new HashMap<>();
            fillerItemsChances.put(new ItemStack(Material.BONE, 8 + random.nextInt(8)), 0.60); // Très commun
            fillerItemsChances.put(new ItemStack(Material.COBBLESTONE, 32 + random.nextInt(32)), 0.50); // Commun
            fillerItemsChances.put(new ItemStack(Material.EXPERIENCE_BOTTLE, 4 + random.nextInt(8)), 0.40); // Assez commun
            fillerItemsChances.put(new ItemStack(Material.DIRT, 16 + random.nextInt(16)), 0.30); // Simple
            fillerItemsChances.put(new ItemStack(Material.APPLE, 2 + random.nextInt(4)), 0.25); // Fruit
            fillerItemsChances.put(new ItemStack(Material.TORCH, 8 + random.nextInt(16)), 0.20); // Lumière
            fillerItemsChances.put(new ItemStack(Material.BAKED_POTATO, 4 + random.nextInt(8)), 0.15); // Nourriture
            fillerItemsChances.put(new ItemStack(Material.IRON_INGOT, 1 + random.nextInt(3)), 0.10); // Ressource basique
            fillerItemsChances.put(new ItemStack(Material.GOLDEN_APPLE), 0.05); // Pomme d'or (rare)
            fillerItemsChances.put(new ItemStack(Material.DIAMOND), 0.02); // Diamant (très rare)

            // Ajouter les items de remplissage en fonction de leurs probabilités
            for (Map.Entry<ItemStack, Double> entry : fillerItemsChances.entrySet()) {
                if (random.nextDouble() < entry.getValue()) { // Chaque item a sa chance indépendante
                    possibleLootItems.add(entry.getKey());
                }
            }


            // Génération d'une armure et d'un outil aléatoires
            ItemStack randomArmor = generateRandomArmor();
            if (randomArmor != null) {
                possibleLootItems.add(randomArmor);
            }

            ItemStack randomTool = generateRandomTool();
            if (randomTool != null) {
                possibleLootItems.add(randomTool);
            }

            // Déterminer le nombre de drops réel (entre 2 et 5)
            int numberOfDrops = 2 + random.nextInt(4); // Génère 0, 1, 2, 3 -> 2, 3, 4, 5 drops
            List<ItemStack> finalLoot = new ArrayList<>();

            // Mélanger la liste de tous les items possibles et prendre les X premiers
            Collections.shuffle(possibleLootItems, random);
            for (int i = 0; i < Math.min(numberOfDrops, possibleLootItems.size()); i++) {
                finalLoot.add(possibleLootItems.get(i));
            }

            event.setDispensedLoot(finalLoot);
        }
    }

    private ItemStack generateRandomArmor() {
        Map<Material, Double> armorChances = new HashMap<>();
        armorChances.put(Material.LEATHER_HELMET, 0.15);
        armorChances.put(Material.LEATHER_CHESTPLATE, 0.15);
        armorChances.put(Material.LEATHER_LEGGINGS, 0.15);
        armorChances.put(Material.LEATHER_BOOTS, 0.15);

        armorChances.put(Material.GOLDEN_HELMET, 0.10);
        armorChances.put(Material.GOLDEN_CHESTPLATE, 0.10);
        armorChances.put(Material.GOLDEN_LEGGINGS, 0.10);
        armorChances.put(Material.GOLDEN_BOOTS, 0.10);

        armorChances.put(Material.IRON_HELMET, 0.25); // L'augmentation des chances ici
        armorChances.put(Material.IRON_CHESTPLATE, 0.25);
        armorChances.put(Material.IRON_LEGGINGS, 0.25);
        armorChances.put(Material.IRON_BOOTS, 0.25);

        armorChances.put(Material.DIAMOND_HELMET, 0.05);
        armorChances.put(Material.DIAMOND_CHESTPLATE, 0.05);
        armorChances.put(Material.DIAMOND_LEGGINGS, 0.05);
        armorChances.put(Material.DIAMOND_BOOTS, 0.05);

        Material chosenMaterial = getRandomMaterialByRarity(armorChances);
        if (chosenMaterial == null) return null;

        ItemStack armorItem = new ItemStack(chosenMaterial);
        ItemMeta meta = armorItem.getItemMeta();

        if (meta != null) {
            Set<Enchantment> possibleEnchantments = new HashSet<>();
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("protection")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fire_protection")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("blast_protection")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("projectile_protection")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("feather_falling")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("respiration")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("aqua_affinity")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("thorns")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("mending")));

            addRandomEnchantments(meta, possibleEnchantments, 1 + random.nextInt(3));
            setRandomDurability(meta, chosenMaterial);

            armorItem.setItemMeta(meta);
        }
        return armorItem;
    }

    private ItemStack generateRandomTool() {
        Map<Material, Double> toolChances = new HashMap<>();
        // Bois: plus commun
        toolChances.put(Material.WOODEN_SWORD, 0.08);
        toolChances.put(Material.WOODEN_PICKAXE, 0.08);
        toolChances.put(Material.WOODEN_AXE, 0.08);
        toolChances.put(Material.WOODEN_SHOVEL, 0.08);
        toolChances.put(Material.WOODEN_HOE, 0.03);

        // Pierre: commun
        toolChances.put(Material.STONE_SWORD, 0.07);
        toolChances.put(Material.STONE_PICKAXE, 0.07);
        toolChances.put(Material.STONE_AXE, 0.07);
        toolChances.put(Material.STONE_SHOVEL, 0.07);
        toolChances.put(Material.STONE_HOE, 0.02);

        // Fer: moins commun
        toolChances.put(Material.IRON_SWORD, 0.05);
        toolChances.put(Material.IRON_PICKAXE, 0.05);
        toolChances.put(Material.IRON_AXE, 0.05);
        toolChances.put(Material.IRON_SHOVEL, 0.05);
        toolChances.put(Material.IRON_HOE, 0.01);

        // Or: rare (mais pas Netherite)
        toolChances.put(Material.GOLDEN_SWORD, 0.02);
        toolChances.put(Material.GOLDEN_PICKAXE, 0.02);
        toolChances.put(Material.GOLDEN_AXE, 0.02);
        toolChances.put(Material.GOLDEN_SHOVEL, 0.02);
        toolChances.put(Material.GOLDEN_HOE, 0.01);

        // Diamant: très rare
        toolChances.put(Material.DIAMOND_SWORD, 0.01);
        toolChances.put(Material.DIAMOND_PICKAXE, 0.01);
        toolChances.put(Material.DIAMOND_AXE, 0.01);
        toolChances.put(Material.DIAMOND_SHOVEL, 0.01);
        toolChances.put(Material.DIAMOND_HOE, 0.005);

        Material chosenMaterial = getRandomMaterialByRarity(toolChances);
        if (chosenMaterial == null) return null;

        ItemStack toolItem = new ItemStack(chosenMaterial);
        ItemMeta meta = toolItem.getItemMeta();

        if (meta != null) {
            Set<Enchantment> possibleEnchantments = new HashSet<>();

            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking")));
            possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("mending")));

            if (chosenMaterial.name().endsWith("_SWORD")) {
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("smite")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("bane_of_arthropods")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("knockback")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fire_aspect")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("looting")));
            } else if (chosenMaterial.name().endsWith("_PICKAXE")) {
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("efficiency")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("silk_touch")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fortune")));
            } else if (chosenMaterial.name().endsWith("_AXE")) {
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("efficiency")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("silk_touch")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fortune")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness")));
            } else if (chosenMaterial.name().endsWith("_SHOVEL")) {
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("efficiency")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("silk_touch")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fortune")));
            } else if (chosenMaterial.name().endsWith("_HOE")) {
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("efficiency")));
                possibleEnchantments.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fortune")));
            }

            addRandomEnchantments(meta, possibleEnchantments, 1 + random.nextInt(3));
            setRandomDurability(meta, chosenMaterial);

            toolItem.setItemMeta(meta);
        }
        return toolItem;
    }

    private Material getRandomMaterialByRarity(Map<Material, Double> chances) {
        double totalWeight = chances.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = random.nextDouble() * totalWeight;

        for (Map.Entry<Material, Double> entry : chances.entrySet()) {
            randomValue -= entry.getValue();
            if (randomValue <= 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void addRandomEnchantments(ItemMeta meta, Set<Enchantment> possibleEnchantments, int maxEnchantments) {
        List<Enchantment> enchantmentsList = new ArrayList<>(possibleEnchantments);
        enchantmentsList.removeIf(e -> e == null);

        int enchantCount = Math.min(maxEnchantments, enchantmentsList.size());

        List<Enchantment> availableEnchantments = new ArrayList<>(enchantmentsList);

        for (int i = 0; i < enchantCount; i++) {
            if (availableEnchantments.isEmpty()) break;

            Enchantment chosenEnchantment = availableEnchantments.get(random.nextInt(availableEnchantments.size()));

            boolean canAdd = true;
            for (Enchantment existingEnchant : meta.getEnchants().keySet()) {
                if (existingEnchant.conflictsWith(chosenEnchantment) || existingEnchant.equals(chosenEnchantment)) {
                    canAdd = false;
                    break;
                }
            }

            if (canAdd) {
                int level = 1 + random.nextInt(Math.min(chosenEnchantment.getMaxLevel(), 5));
                meta.addEnchant(chosenEnchantment, level, true);
            }
            availableEnchantments.remove(chosenEnchantment);
        }
    }

    private void setRandomDurability(ItemMeta meta, Material material) {
        if (meta instanceof Damageable) {
            Damageable damageableMeta = (Damageable) meta;
            int maxDurability = material.getMaxDurability();
            if (maxDurability > 0) {
                int damage = random.nextInt(maxDurability / 2) + 1;
                damageableMeta.setDamage(damage);
            }
        }
    }

    private boolean isInRegion(Player player, String regionId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (manager == null) return false;

        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(regionId)) {
                return true;
            }
        }
        return false;
    }
}