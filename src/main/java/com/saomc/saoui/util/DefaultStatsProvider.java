package com.saomc.saoui.util;

import com.saomc.saoui.api.info.IPlayerStatsProvider;
import com.saomc.saoui.config.OptionCore;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;

import java.text.DecimalFormat;
import java.util.Collection;

/**
 * Part of saoui
 *
 * @author Bluexin
 */
public class DefaultStatsProvider implements IPlayerStatsProvider {

    private static float attr(double attributeValue) {
        return (float) ((int) (attributeValue * 1000)) / 1000;
    }

    @Override
    public String getStatsString(EntityPlayer player) {
        final StringBuilder builder = new StringBuilder();
        EntityLivingBase mount = (EntityLivingBase) player.ridingEntity;

        if (player.isRiding() && OptionCore.MOUNT_STAT_VIEW.isEnabled()) {
            final String name = mount.getCommandSenderName();
            final double maxHealth = attr(mount.getMaxHealth());
            double health = attr(mount.getHealth());
            final double resistance = attr(mount.getTotalArmorValue());
            final double speed = attr(mount.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
            final double jump;
            DecimalFormat df3 = new DecimalFormat("0.000");
            DecimalFormat df1 = new DecimalFormat("0.0");
            String speedFormated = df3.format(speed);
            health *= 10;
            health += 0.5F;
            health /= 10.0F;
            String healthFormated = df1.format(health);

            builder.append(I18n.format("displayName")).append(": ").append(name).append('\n');
            builder.append(I18n.format("displayHpLong")).append(": ").append(healthFormated).append("/").append(maxHealth).append('\n');
            builder.append(I18n.format("displayResLong")).append(": ").append(resistance).append('\n');
            builder.append(I18n.format("displaySpdLong")).append(": ").append(speedFormated).append('\n');
            if (mount instanceof EntityHorse) {
                jump = ((EntityHorse) mount).getHorseJumpStrength();
                String jumpFormated = df3.format(jump);
                builder.append(I18n.format("displayJmpLong")).append(": ").append(jumpFormated).append('\n');
            }
        } else {
            final int level = PlayerStats.instance().getStats().getLevel(player);
            final int experience = (int) (PlayerStats.instance().getStats().getExpPct(player) * 100);

            final float health = attr(player.getHealth());

            final float maxHealth = attr(player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue());
            final float attackDamage = attr(player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
            // final float movementSpeed = attr(player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
            // final float knocbackResistance = attr(player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue());

            float itemDamage = 0.0F;

            if (player.getHeldItem() != null) {
                final Collection<?> itemAttackMain = player.getHeldItem().getAttributeModifiers().get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());

                itemDamage += itemAttackMain.stream().filter(value -> value instanceof AttributeModifier).map(value -> (AttributeModifier) value)
                        .filter(mod -> mod.getName().equals("Weapon modifier")).mapToDouble(AttributeModifier::getAmount).sum();
            }

            final float strength = attr(attackDamage + itemDamage);
            final float agility = attr(player.getAIMoveSpeed()) * 10;
            final float resistance = attr(player.getTotalArmorValue());

            builder.append(I18n.format("displayLvLong")).append(": ").append(level).append('\n');
            builder.append(I18n.format("displayXpLong")).append(": ").append(experience).append("%\n");

            builder.append(I18n.format("displayHpLong")).append(": ").append(health).append("/").append(maxHealth).append('\n');
            builder.append(I18n.format("displayStrLong")).append(": ").append(strength).append('\n');
            builder.append(I18n.format("displayDexLong")).append(": ").append(agility).append('\n');
            builder.append(I18n.format("displayResLong")).append(": ").append(resistance).append("\n");
        }

        return builder.toString();
    }
}
