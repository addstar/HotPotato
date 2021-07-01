package au.com.addstar.mixin;

import au.com.addstar.HotPotato;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.time.LocalDateTime;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Inject(at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER
    ), method = "insertStack(ILnet/minecraft/item/ItemStack;)Z")
    private void invAddStack(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack != null && !stack.isEmpty()) {
            PlayerInventory inv = (PlayerInventory) (Object) this;
            PlayerEntity playerEntity = inv.player;

            // Check if the hot potato is being added to an inventory
            if (stack.isItemEqualIgnoreDamage(HotPotato.HOTPOTATO_ITEM.getDefaultStack())) {
                // Fake player names
                //int min = LocalDateTime.now().getMinute();
                String pname = playerEntity.getName().asString();
                //String msg = "YOU HAVE HOT POTATO! (slot " + slot + ")";
                //System.out.println("[HotPotato] YOU HAVE HOT POTATO! (slot " + slot + ")");

                // Log all the NBT data on the stack for debugging
                //dumpNBT(stack);

                String curOwner = null;
                String prevOwner = null;
                CompoundTag ptag = stack.getOrCreateSubTag("PotatoTag");
                try {
                    //System.out.println("[HotPotato] Checking tags");
                    if (ptag != null && !ptag.isEmpty() && ptag.contains("Owner")) {
                        // existing owner
                        //System.out.println("[HotPotato] Existing owner");
                        curOwner = ptag.getString("Owner");
                        if (ptag.contains("PrevOwner")) {
                            prevOwner = ptag.getString("PrevOwner");
                        }
                    } else {
                        // no owner
                        //System.out.println("[HotPotato] First owner");
                        ptag.putString("Owner", pname);
                        ptag.putString("Since", Instant.now().toString());
                        updateItemMeta(stack);
                    }
                } catch (Exception e) {
                    System.out.println("Error checking hot potato item tags");
                    e.printStackTrace();
                    return;
                }

                //System.out.println("[HotPotato] Checking owners...");
                if ((curOwner != null) && (!curOwner.equals(pname))) {
                    ptag.putString("Owner", pname);
                    ptag.putString("PrevOwner", curOwner);
                    ptag.putString("Since", Instant.now().toString());
                    updateItemMeta(stack);
                    if (HotPotato.config.broadcast_exchange) {
                        final Text msgtext = HotPotato.makeExchangeMsg(pname, curOwner);
                        if (msgtext != null) {
                            HotPotato.broadcastMsg(
                                    playerEntity.getServer(),
                                    msgtext,
                                    HotPotato.config.broadcast_delay);
                        }
                    }
                }
            }
        }
    }

    public void dumpNBT(ItemStack stack) {
        System.out.println("NBT Tags:");
        if (stack.getTag() != null) {
            for (String key : stack.getTag().getKeys()) {
                System.out.println("  Key: " + key);
                if (stack.getSubTag(key) != null) {
                    for (String subkey : stack.getSubTag(key).getKeys()) {
                        System.out.println("    - " + subkey);
                    }
                }
            }
        }
    }

    public void updateItemMeta(ItemStack stack) {
        //System.out.println("Updating item meta...");
        CompoundTag ptag = stack.getSubTag("PotatoTag");
        if (ptag != null && !ptag.isEmpty()) {
            if (ptag.contains("Owner")) {
                CompoundTag dtag = stack.getOrCreateSubTag("display");
                //Text name = new LiteralText("HOT POTATO!!!").formatted(Formatting.RED, Formatting.BOLD);
                dtag.put("Display", HotPotato.makeText("HOT POTATO!!!", Formatting.RED, Formatting.BOLD));

                String owner = ptag.getString("Owner");
                ListTag lore = new ListTag();
                lore.add(HotPotato.makeText("-- You have been potatoed --", Formatting.LIGHT_PURPLE));
                lore.add(HotPotato.makeText("You must keep this in your inventory", Formatting.GREEN));
                lore.add(HotPotato.makeText("at all times. Secretly try to pass", Formatting.GREEN));
                lore.add(HotPotato.makeText("this on to another player as soon as", Formatting.GREEN));
                lore.add(HotPotato.makeText("you can (no give backs).", Formatting.GREEN));
                lore.add(HotPotato.makeText(" \u2023 Owner: " + owner, Formatting.YELLOW));
                if (ptag.contains("PrevOwner")) {
                    lore.add(HotPotato.makeText(" \u2022 Given by: " + ptag.getString("PrevOwner"), Formatting.DARK_GRAY));
                }

                dtag.put("Lore", lore);
                stack.putSubTag("display", dtag);

                stack.putSubTag("HideFlags", IntTag.of(127));

                stack.getEnchantments().clear();
                stack.addEnchantment(Enchantments.INFINITY, 1);
            }
        }
    }
}
