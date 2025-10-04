package org.confluence.terraentity.init.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.function.TriFunction;
import org.confluence.terraentity.TerraEntity;
import org.confluence.terraentity.data.component.SingleBooleanComponent;
import org.confluence.terraentity.entity.util.trail.BoomerangTrail;
import org.confluence.terraentity.init.TEDataComponentTypes;
import org.confluence.terraentity.init.TEEffectStrategies;
import org.confluence.terraentity.item.Boomerang;


public class TEBoomerangItems {

    private static final TriFunction<Integer,Float,Float,Boomerang.BoomerangModifier> NORMAL_BOOMERANG_MODIFIER =
            (forwardTick, flySpeedFactor, backSpeedFactor) ->   new Boomerang.BoomerangModifier().setForwardTick(forwardTick).setFlySpeedFactor(flySpeedFactor).setBackSpeedFactor(backSpeedFactor);
    private static final TriFunction<Integer,Integer,Boomerang.BoomerangModifier,Boomerang.BoomerangModifier> MULTI_BOOMERANG_MODIFIER =
            (cd, count, modifier) ->    modifier.setNotWaitForBack().setCd(cd).setMaxCount(count);


    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TerraEntity.MODID);


    public static final DeferredItem<Boomerang> WOOD_BOOMERANG = register("wood_boomerang",3.8F,
            new Boomerang.BoomerangModifier().setDurability(100)
    );
    public static final DeferredItem<Boomerang> ENCHANTED_BOOMERANG = register("enchanted_boomerang",6.6F,
            NORMAL_BOOMERANG_MODIFIER.apply(15,1.55f,1.55f).setDurability(300)
    );
    public static final DeferredItem<Boomerang> SHROOMERANG = register("shroomerang",8.6F,
            NORMAL_BOOMERANG_MODIFIER.apply(15,1.55f,1.55f).setDurability(500)
    );
    public static final DeferredItem<Boomerang> ICE_BOOMERANG = register("ice_boomerang",8.3F,
            NORMAL_BOOMERANG_MODIFIER.apply(16,1.6f,1.6f)
                    .setOnHitEffect(TEEffectStrategies.Components.FROST_BURN_BOOMERANG_EFFECT.get())
                    .setDurability(500)
                    .setTrail(()->new BoomerangTrail(20, 0x2553be, 0x255385))
                    .setParticle(()->ParticleTypes.SNOWFLAKE)
    );   //50% 的几率造成 3 秒的霜冻。

    public static final DeferredItem<Boomerang> TRIMARANG = register("trimarang",8.3F,
            MULTI_BOOMERANG_MODIFIER.apply(10,3,
                    NORMAL_BOOMERANG_MODIFIER.apply(17,1.85f,1.85f)).setDurability(1000)
    );   // 使用三次

    public static final DeferredItem<Boomerang> COMBAT_WRENCH = register("combat_wrench",9.0F,
            NORMAL_BOOMERANG_MODIFIER.apply(10,3f,1.85f).setDurability(1500)
    );

    public static final DeferredItem<Boomerang> FLAMARANG = register("flamarang",20.3F,
            NORMAL_BOOMERANG_MODIFIER.apply(18,1.85f,1.85f)
                    .setOnHitEffect(TEEffectStrategies.Components.HELL_FIRE_EFFECT.get())
                    .setDurability(1500)
                    .setTrail(()->new BoomerangTrail(200, 0xFB6464, 0xbc2525))
                    .setParticle(()->ParticleTypes.LAVA)
    );   //狱炎效果。


    public static final DeferredItem<Boomerang> DEVELOPER_BOOMERANG = register("developer_boomerang",9999,
            new Boomerang.BoomerangModifier().setNotWaitForBack().setCd(0) // 不需要等待返回，设置cd
                    .setForwardTick(50)
                    .setFlySpeedFactor(2) // 设置向前飞行速度
                    .setBackSpeedFactor(2f) // 设置后退速度
                    .setCanPenetrate()
                    .setMaxCount(10)
    );


    public static final DeferredItem<Boomerang> BeiDou_BOOMERANG = register("bei_dou_boomerang",5,
            MULTI_BOOMERANG_MODIFIER.apply(5, 4,
                    NORMAL_BOOMERANG_MODIFIER.apply(40, 3.0f, 3.0f)
                            .setMaxPenetration(7)
                            .setOnHitEffect(TEEffectStrategies.Components.BEI_DOU_EFFECT.get())
            )
    );

    private static DeferredItem<Boomerang> register(String name, float damage, Boomerang.BoomerangModifier boomerangModifier) {
        return ITEMS.register(name, () -> new Boomerang(damage,boomerangModifier,new Item.Properties().stacksTo(1)
                .component(TEDataComponentTypes.BOOMERANG_READY, SingleBooleanComponent.TRUE)
//                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .component(DataComponents.ATTRIBUTE_MODIFIERS, boomerangModifier.attributeModifiersBuilder.build())));
    }
}
