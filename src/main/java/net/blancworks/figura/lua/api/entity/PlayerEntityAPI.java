package net.blancworks.figura.lua.api.entity;

import net.blancworks.figura.avatar.AvatarData;
import net.blancworks.figura.avatar.AvatarDataManager;
import net.blancworks.figura.lua.CustomScript;
import net.blancworks.figura.lua.api.item.ItemStackAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.function.Supplier;

public class PlayerEntityAPI {

    public static Identifier getID() {
        return new Identifier("default", "player");
    }

    //Global table will get the local player
    public static LuaTable getForScript(CustomScript script) {
        return new PlayerEntityLuaAPITable(() -> (PlayerEntity) script.avatarData.lastEntity);
    }

    public static class PlayerEntityLuaAPITable extends LivingEntityAPI.LivingEntityAPITable<PlayerEntity> {

        public PlayerEntityLuaAPITable(Supplier<PlayerEntity> entitySupplier) {
            super(entitySupplier);
        }

        @Override
        public void setTable() {
            super.setTable();

            set("getHeldItem", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    int hand = arg.checkint();

                    ItemStack targetStack;

                    if (hand == 1)
                        targetStack = targetEntity.get().getMainHandStack();
                    else if (hand == 2)
                        targetStack = targetEntity.get().getOffHandStack();
                    else
                        return NIL;

                    if (targetStack.equals(ItemStack.EMPTY))
                        return NIL;

                    return ItemStackAPI.getTable(targetStack);
                }
            });

            set("getFood", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaNumber.valueOf(targetEntity.get().getHungerManager().getFoodLevel());
                }
            });

            set("getSaturation", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaNumber.valueOf(targetEntity.get().getHungerManager().getSaturationLevel());
                }
            });

            set("getExperienceProgress", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaNumber.valueOf(targetEntity.get().experienceProgress);
                }
            });

            set("getExperienceLevel", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaNumber.valueOf(targetEntity.get().experienceLevel);
                }
            });

            set("getTargetedEntity", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    if (targetEntity.get() instanceof ClientPlayerEntity) {
                        Entity lookingAt = MinecraftClient.getInstance().targetedEntity;

                        if (lookingAt != null && !lookingAt.isInvisibleTo(targetEntity.get())) {
                            return EntityAPI.getTableForEntity(lookingAt);
                        }
                    }

                    return NIL;
                }
            });

            set("lastDamageSource", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    AvatarData data = AvatarDataManager.getDataForPlayer(targetEntity.get().getUuid());
                    if (data == null || data.script == null) return NIL;

                    DamageSource ds = data.script.lastDamageSource;
                    return ds == null ? NIL : LuaValue.valueOf(ds.name);
                }
            });

            set("getStoredValue", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    String key = arg1.checkjstring();

                    AvatarData data = AvatarDataManager.getDataForPlayer(targetEntity.get().getUuid());
                    if (data == null || data.script == null) return NIL;

                    return data.script.sharedValues.get(key);
                }
            });

            set("getModelType", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg1) {
                    AvatarData data = AvatarDataManager.getDataForPlayer(targetEntity.get().getUuid());
                    if (data == null || data.playerListEntry == null) return NIL;

                    return LuaValue.valueOf(data.playerListEntry.getModel());
                }
            });

            set("hasAvatar", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    AvatarData data = AvatarDataManager.getDataForPlayer(targetEntity.get().getUuid());
                    return LuaValue.valueOf(data != null && data.hasAvatar());
                }
            });

            set("getGamemode", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
                    PlayerListEntry playerListEntry = null;

                    if (networkHandler != null)
                        playerListEntry = networkHandler.getPlayerListEntry(targetEntity.get().getGameProfile().getId());

                    return playerListEntry == null || playerListEntry.getGameMode() == null ? NIL : LuaValue.valueOf(playerListEntry.getGameMode().name());
                }
            });

            set("isFlying", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(targetEntity.get().getAbilities().flying);
                }
            });
        }

        @Override
        public LuaValue rawget(LuaValue key) {
            if (targetEntity.get() == null)
                throw new LuaError("Player Entity does not exist yet! Do NOT try to access the player in init! Do it in player_init instead!");
            return super.rawget(key);
        }
    }
}
