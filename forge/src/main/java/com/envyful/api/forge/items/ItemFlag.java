package com.envyful.api.forge.items;

/**
 *
 * Enum class representing the ItemFlags that can be applied to an ItemStack.
 * The {@link ItemFlag#getNbtId()} returns the byte value of the flag. To combine two flags together you use the
 * logical OR operator to combine the two byte values.
 *
 */
public enum ItemFlag {

    HIDE_ENCHANTS(1),
    HIDE_MODIFIERS(2),
    HIDE_UNBREAKABLE(4),
    HIDE_CAN_DESTROY(8),
    HIDE_CAN_PLACE(16),

    ;

    private final int nbtId;

    ItemFlag(int nbtId) {
        this.nbtId = nbtId;
    }

    public int getNbtId() {
        return this.nbtId;
    }
}
