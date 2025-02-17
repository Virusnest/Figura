package net.blancworks.figura.lua.api;

import net.blancworks.figura.lua.CustomScript;
import net.blancworks.figura.models.CustomModel;
import net.blancworks.figura.models.animations.Animation;
import net.blancworks.figura.models.animations.Animation.LoopMode;
import net.blancworks.figura.models.animations.Animation.PlayState;
import net.minecraft.util.Identifier;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class AnimationAPI {
    public static Identifier getID() {
        return new Identifier("default", "animation");
    }

    public static LuaTable getForScript(CustomScript script) {
        return new LuaTable() {{
            CustomModel model = script.avatarData.model;
            if (model != null)
                model.animations.forEach((name, anim) -> set(name, getTableForAnimation(anim)));

            set("listAnimations", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    CustomModel model = script.avatarData.model;
                    if (model == null) return NIL;

                    int i = 1;
                    LuaTable tbl = new LuaTable();
                    for (Animation animation : model.animations.values()) {
                        tbl.set(i, LuaValue.valueOf(animation.name));
                        i++;
                    }

                    return tbl;
                }
            });

            set("stopAll", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    CustomModel model = script.avatarData.model;
                    if (model == null) return NIL;

                    model.animations.values().forEach(Animation::stop);
                    return NIL;
                }
            });

            set("ceaseAll", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    CustomModel model = script.avatarData.model;
                    if (model == null) return NIL;

                    model.animations.values().forEach(Animation::cease);
                    return NIL;
                }
            });
        }};
    }

    public static LuaTable getTableForAnimation(Animation animation) {
        return new LuaTable() {{
            set("getName", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.name);
                }
            });

            set("start", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    if (!animation.isPlaying())
                        animation.play();
                    return NIL;
                }
            });

            set("play", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    animation.play();
                    return NIL;
                }
            });

            set("pause", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    animation.pause();
                    return NIL;
                }
            });

            set("stop", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    animation.stop();
                    return NIL;
                }
            });

            set("cease", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    animation.cease();
                    return NIL;
                }
            });

            set("isPlaying", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.isPlaying());
                }
            });

            set("setPlayState", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    try {
                        animation.playState = PlayState.valueOf(arg.checkjstring());
                    } catch (Exception ignored) {
                        throw new LuaError("Invalid playstate type");
                    }

                    return NIL;
                }
            });

            set("getPlayState", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.playState.name());
                }
            });

            set("setLength", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.length = arg.checknumber().tofloat();
                    return NIL;
                }
            });

            set("getLength", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.length);
                }
            });

            set("setSpeed", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    float speed = arg.checknumber().tofloat();
                    animation.speed = Math.abs(speed);
                    animation.inverted = speed < 0f;
                    return NIL;
                }
            });

            set("getSpeed", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.inverted ? -animation.speed : animation.speed);
                }
            });

            set("setLoopMode", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    LoopMode mode;
                    try {
                        mode = LoopMode.valueOf(arg.checkjstring());
                    } catch (Exception ignored) {
                        mode = LoopMode.ONCE;
                    }

                    animation.loopMode = mode;
                    return NIL;
                }
            });

            set("getLoopMode", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.loopMode.name());
                }
            });

            set("setStartOffset", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.startOffset = arg.checknumber().tofloat();
                    return NIL;
                }
            });

            set("getStartOffset", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.startOffset);
                }
            });

            set("setBlendWeight", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.blendWeight = arg.checknumber().tofloat();
                    return NIL;
                }
            });

            set("getBlendWeight", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.blendWeight);
                }
            });

            set("setStartDelay", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.startDelay = arg.checknumber().tofloat();
                    return NIL;
                }
            });

            set("getStartDelay", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.startDelay);
                }
            });

            set("setLoopDelay", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.loopDelay = arg.checknumber().tofloat();
                    return NIL;
                }
            });

            set("getLoopDelay", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.loopDelay);
                }
            });

            set("setBlendTime", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.blendTime = Math.max(arg.checknumber().tofloat(), 0f);
                    return NIL;
                }
            });

            set("getBlendTime", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.blendTime);
                }
            });

            set("setOverride", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.override = arg.checkboolean();
                    return NIL;
                }
            });

            set("getOverride", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.override);
                }
            });

            set("setReplace", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.replace = arg.checkboolean();
                    return NIL;
                }
            });

            set("getReplace", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.replace);
                }
            });

            set("setPriority", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    animation.priority = arg.checkint();
                    return NIL;
                }
            });

            set("getPriority", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(animation.priority);
                }
            });
        }};
    }
}
