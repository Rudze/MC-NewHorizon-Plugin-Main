package fr.rudy.newhorizon.loot;

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
import org.bukkit.World; // Ajout de l'import pour World

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
    private static final double SACRED_PIECE_OF_JUNGLE_CHANCE = 0.10;
    private static final double WITHER_SKULL_CHANCE = 0.03;
    private static final double VAULT_KEY_CHANCE = 0.07;

    // --- CHANGEMENT ICI ---
    // Changez ce préfixe pour qu'il corresponde au début des noms de vos mondes de donjon
    private static final String DUNGEON_WORLD_PREFIX = "dungeon_normal_temple_";// Nouveau préfixe
    // Si vous avez plusieurs types de donjons, vous devrez étendre cette logique (voir la note ci-dessous)
    // --- FIN DU CHANGEMENT ---

    @EventHandler
    public void onVaultLoot(BlockDispenseLootEvent event) {
        Block block = event.getBlock();
        // Player player = event.getPlayer(); // Le joueur qui ouvre la vault, utile pour le contexte, mais non utilisé ici pour la logique de monde
        if (!(block.getState() instanceof Vault)) return;

        World world = block.getWorld();
        String worldName = world.getName();

        // Vérifier si le nom du monde commence par le préfixe désiré
        if (worldName.startsWith(DUNGEON_WORLD_PREFIX)) { // Utilisation du nouveau préfixe
            List<ItemStack> possibleLootItems = new ArrayList<>();

            // Tenter d'ajouter les items spéciaux avec leur probabilité
            if (random.nextDouble() < SACRED_PIECE_OF_JUNGLE_CHANCE) {
                CustomStack sacredpieceofjungle = CustomStack.getInstance("newhorizon:sacred_piece_of_jungle");
                if (sacredpieceofjungle != null) {
                    possibleLootItems.add(sacredpieceofjungle.getItemStack());
                }
            }

            if (random.nextDouble() < WITHER_SKULL_CHANCE) {
                possibleLootItems.add(new ItemStack(Material.WITHER_SKELETON_SKULL));
            }

            // Ajout d'items de remplissage classiques avec probabilités
            Map<ItemStack, Double> fillerItemsChances = new HashMap<>();
            fillerItemsChances.put(new ItemStack(Material.BONE, 8 + random.nextInt(8)), 0.60);
            fillerItemsChances.put(new ItemStack(Material.COBBLESTONE, 32 + random.nextInt(32)), 0.50);
            fillerItemsChances.put(new ItemStack(Material.EXPERIENCE_BOTTLE, 4 + random.nextInt(8)), 0.40);
            fillerItemsChances.put(new ItemStack(Material.DIRT, 16 + random.nextInt(16)), 0.30);
            fillerItemsChances.put(new ItemStack(Material.APPLE, 2 + random.nextInt(4)), 0.25);
            fillerItemsChances.put(new ItemStack(Material.TORCH, 8 + random.nextInt(16)), 0.20);
            fillerItemsChances.put(new ItemStack(Material.BAKED_POTATO, 4 + random.nextInt(8)), 0.15);
            fillerItemsChances.put(new ItemStack(Material.IRON_INGOT, 1 + random.nextInt(3)), 0.10);
            fillerItemsChances.put(new ItemStack(Material.GOLDEN_APPLE), 0.05);
            fillerItemsChances.put(new ItemStack(Material.DIAMOND), 0.02);

            for (Map.Entry<ItemStack, Double> entry : fillerItemsChances.entrySet()) {
                if (random.nextDouble() < entry.getValue()) {
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
            int numberOfDrops = 2 + random.nextInt(4);
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

        armorChances.put(Material.IRON_HELMET, 0.25);
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
                int level = 1 + random.nextInt(chosenEnchantment.getMaxLevel());
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
}