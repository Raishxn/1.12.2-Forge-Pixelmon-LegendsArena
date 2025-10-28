package com.envyful.api.forge.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * Forge {@link ItemStack} builder class. Allows specifying {@link ItemFlag}s, unbreakability, nbt data, enchants (etc).
 *
 */
public class ItemBuilder implements Cloneable {

    private Item type = Items.AIR;
    private int amount = 1;
    private int damage = 0;
    private String name = "";
    private boolean unbreakable = false;
    private List<String> lore = Lists.newArrayList();
    private List<ItemFlag> itemFlags = Lists.newArrayList();
    private Map<String, NBTBase> nbtData = Maps.newHashMap();
    private Map<Enchantment, Integer> enchants = Maps.newHashMap();

    /**
     *
     * Basic constructor providing empty item builder with no specific values to begin with.
     *
     */
    public ItemBuilder() {}

    /**
     *
     * Converts {@link ItemStack} to builder
     *
     * @param itemStack The itemstack being converted
     */
    public ItemBuilder(ItemStack itemStack) {
        this.type = itemStack.getItem();
        this.amount = itemStack.getCount();
        this.damage = itemStack.getItemDamage();
        this.name = itemStack.getDisplayName();
        this.lore = UtilItemStack.getLore(itemStack);

        if (itemStack.getTagCompound() != null) {
            for (String s : itemStack.getTagCompound().getKeySet()) {
                this.nbtData.put(s, itemStack.getTagCompound().getTag(s));
            }
        }
    }

    /**
     *
     * Sets the type of item that the itemstack will be
     *
     * @param type The minecraft type of the item
     * @return The builder
     */
    public ItemBuilder type(Item type) {
        this.type = type;
        return this;
    }

    /**
     *
     * Sets the amount of the item in the itemstack
     *
     * @param amount The amount
     * @return The builder
     */
    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     *
     * Sets the damage on the item created. I.e. Wool with damage 3 is light blue
     *
     * @param damage The damage for the item
     * @return The builder
     */
    public ItemBuilder damage(int damage) {
        this.damage = damage;
        return this;
    }

    /**
     *
     * Sets the new name of the item
     *
     * @param name The new name
     * @return The builder
     */
    public ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     *
     * Sets the list of the strings as the stored lore (doesn't ADD to the lore)
     *
     * @param lore The new lore for the item
     * @return The builder
     */
    public ItemBuilder lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    /**
     *
     * Sets the array of the strings as the stored lore (doesn't ADD to the lore)
     *
     * @param lore The new lore for the item
     * @return The builder
     */
    public ItemBuilder lore(String... lore) {
        this.lore = Arrays.asList(lore);
        return this;
    }

    /**
     *
     * Adds the array of Strings to the stored lore (doesn't SET the lore)
     *
     * @param lore The lines to add to the lore
     * @return The builder
     */
    public ItemBuilder addLore(String... lore) {
        this.lore.addAll(Lists.newArrayList(lore));
        return this;
    }

    /**
     *
     * Sets the NBT base value at the key given
     *
     * @param key The key to set the value at
     * @param primitive The value to add under the given key
     * @return The builder
     */
    public ItemBuilder nbt(String key, NBTBase primitive) {
        this.nbtData.put(key, primitive);
        return this;
    }

    /**
     *
     * Sets if the item built should be breakable
     *
     * @param unbreakable true = unbreakable, false = breakable
     * @return The builder
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     *
     * Adds the specified {@link ItemFlag} to the builder class
     *
     * @param itemFlag The item flag to add
     * @return The builder
     */
    public ItemBuilder itemFlag(ItemFlag itemFlag) {
        this.itemFlags.add(itemFlag);
        return this;
    }

    /**
     *
     * Adds the specified {@link ItemFlag}s to the builder class
     *
     * @param itemFlags The item flags to add
     * @return The builder
     */
    public ItemBuilder itemFlags(ItemFlag... itemFlags) {
        this.itemFlags.addAll(Arrays.asList(itemFlags));
        return this;
    }

    /**
     *
     * Enchants the item with the specified enchant and the given level.
     *
     * @param enchantment The enchantment to add to the builder
     * @param level The level of the enchant
     * @return The builder
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.enchants.put(enchantment, level);
        return this;
    }

    /**
     *
     * Gets the NBT tag for the specified key.
     *
     * Will return null if the value found at the key is not a tag compound.
     * However, will also insert a new tag compound (and return it) if it wasn't already there.
     *
     * @param key The key to be checked
     * @return The NBT tag at that key
     */
    public NBTTagCompound getCompound(String key) {
        NBTBase nbtBase = this.nbtData.computeIfAbsent(key, ___ -> new NBTTagCompound());

        if (!(nbtBase instanceof NBTTagCompound)) {
            return null;
        }

        return (NBTTagCompound) nbtBase;
    }

    /**
     *
     * Method to turn the {@link ItemBuilder} to a new forge {@link ItemStack} instance
     *
     * @return The forge item
     */
    public ItemStack build() {
        ItemStack itemStack = new ItemStack(this.type, this.amount, this.damage);

        NBTTagCompound compound = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();

        for (Map.Entry<String, NBTBase> entry : nbtData.entrySet()) {
            compound.setTag(entry.getKey(), entry.getValue());
        }

        itemStack.setTagCompound(compound);

        if (this.name != null && !this.name.isEmpty()) {
            itemStack.setStackDisplayName(this.name);
        }

        if (this.lore != null && !this.lore.isEmpty()) {
            NBTTagCompound display = itemStack.getOrCreateSubCompound("display");
            NBTTagList lore = new NBTTagList();

            this.lore.forEach(s -> lore.appendTag(new NBTTagString(s)));

            display.setTag("Lore", lore);
            itemStack.setTagInfo("display", display);
        }

        if (this.unbreakable) {
            itemStack.getTagCompound().setInteger("Unbreakable", 1);
        }

        for (Enchantment enchantment : enchants.keySet()) {
            itemStack.addEnchantment(enchantment, enchants.get(enchantment));
        }

        int hideFlags = 0;

        for (ItemFlag itemFlag : this.itemFlags) {
            hideFlags += itemFlag.getNbtId();
        }

        itemStack.getTagCompound().setInteger("HideFlags", hideFlags);


        return itemStack;
    }

    /**
     *
     * Method to create a copy of the ItemBuilder in a new instance
     *
     * @return The new item builder that the parameters have been copied to
     */
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public ItemBuilder clone() {
        ItemBuilder copy = new ItemBuilder();
        copy.type(this.type);
        copy.name(this.name);
        copy.amount(this.amount);
        copy.damage(this.damage);
        copy.lore(this.lore);
        return copy;
    }
}
