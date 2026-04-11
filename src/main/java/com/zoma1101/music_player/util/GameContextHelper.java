package com.zoma1101.music_player.util; // パッケージは適切に設定

import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.Boat; // Boatをインポート
import net.minecraft.world.entity.vehicle.Minecart; // Minecartをインポート
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ゲーム内の特定のコンテキスト（戦闘状態、村など）を判断するためのヘルパークラス。
 */
public class GameContextHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    // 戦闘状態追跡用のフィールド
    private static final Set<Integer> activeCombatEntityIds = new HashSet<>();
    private static final int COMBAT_EXIT_GRACE_TICKS = 80;
    private static final int CLIENT_COMBAT_PULSE_TICKS = 80;
    private static final int COMBAT_PULSE_SENTINEL_ID = Integer.MIN_VALUE;
    private static final CombatStateTracker combatStateTracker = new CombatStateTracker(COMBAT_EXIT_GRACE_TICKS);
    private static final CombatPulseTracker combatPulseTracker = new CombatPulseTracker(CLIENT_COMBAT_PULSE_TICKS);
    private static boolean lastComputedCombatState = false;

    // 戦闘・村判定で使用する定数
    private static final double COMBAT_CHECK_RADIUS = 24.0; // 戦闘判定の半径
    private static final double VILLAGE_CHECK_RADIUS = 48.0; // 村判定の半径
    private static final int VILLAGE_CHECK_HEIGHT = 10; // 村判定の高さ方向範囲
    private static final int VILLAGER_THRESHOLD = 2; // 村と判断するのに必要な村人の数

    private GameContextHelper() {}

    /**
     * プレイヤーの周囲の敵対Mobをチェックし、戦闘状態を更新して現在の戦闘状態を返します。
     * 名札付きモブ、またはボートやトロッコに乗っているモブは戦闘状態のトリガーとしないように変更。
     * @param player 判定対象のプレイヤー
     * @param level プレイヤーがいるレベル
     * @return プレイヤーが戦闘状態にあるかどうか
     */
    public static boolean updateCombatStateAndCheck(LocalPlayer player, Level level) {
        if (player == null || level == null) {
            resetCombatTracking(); // プレイヤーやレベルが無効なら戦闘状態クリア
            return false;
        }

        Set<Integer> currentlyEngagedIds = new HashSet<>();
        List<Mob> nearbyMobs = level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(COMBAT_CHECK_RADIUS),
                LivingEntity::isAlive // エンティティが生存しているかをチェックするフィルタ
        );

        for (Mob mob : nearbyMobs) {
            if (isMobEngagedWithPlayer(mob, player)) {
                currentlyEngagedIds.add(mob.getId());
            }
        }

        boolean hasEventPulse = combatPulseTracker.isActive();
        if (hasEventPulse) {
            currentlyEngagedIds.add(COMBAT_PULSE_SENTINEL_ID);
        }

        activeCombatEntityIds.clear();
        activeCombatEntityIds.addAll(currentlyEngagedIds);
        activeCombatEntityIds.remove(COMBAT_PULSE_SENTINEL_ID);

        boolean inCombat = combatStateTracker.update(currentlyEngagedIds);
        if (inCombat != lastComputedCombatState) {
            LOGGER.info(
                    "Combat state changed: {} -> {} (engagedMobs={}, eventPulseActive={}, pulseTicksLeft={})",
                    lastComputedCombatState,
                    inCombat,
                    activeCombatEntityIds.size(),
                    hasEventPulse,
                    combatPulseTracker.getRemainingTicks()
            );
            lastComputedCombatState = inCombat;
        }
        return inCombat;
    }

    public static void onClientTick() {
        combatPulseTracker.onClientTick();
    }

    public static void resetCombatTracking() {
        activeCombatEntityIds.clear();
        combatPulseTracker.reset();
        combatStateTracker.reset();
        lastComputedCombatState = false;
    }

    public static void registerClientCombatPulse(String reason) {
        combatPulseTracker.pulse();
        LOGGER.debug("Registered client combat pulse (reason={}), pulseTicksLeft={}", reason, combatPulseTracker.getRemainingTicks());
    }

    private static boolean isMobEngagedWithPlayer(Mob mob, LocalPlayer player) {
        if (mob.hasCustomName() || isIgnoredPassenger(mob)) {
            return false;
        }

        LivingEntity target = mob.getTarget();
        LivingEntity mobLastHurtBy = mob.getLastHurtByMob();
        LivingEntity playerLastHurtMob = player.getLastHurtMob();
        LivingEntity playerLastHurtBy = player.getLastHurtByMob();

        return shouldTreatAsCombatTarget(
                mob instanceof Enemy,
                mob.isAggressive(),
                mob.canAttack(player),
                target != null && target.getId() == player.getId(),
                mobLastHurtBy != null && mobLastHurtBy.getId() == player.getId(),
                playerLastHurtMob != null && playerLastHurtMob.getId() == mob.getId(),
                playerLastHurtBy != null && playerLastHurtBy.getId() == mob.getId()
        );
    }

    static boolean shouldTreatAsCombatTarget(
            boolean hostileByClass,
            boolean aggressive,
            boolean canAttackPlayer,
            boolean targetingPlayer,
            boolean mobRecentlyHurtByPlayer,
            boolean playerRecentlyHurtMob,
            boolean playerRecentlyHurtByMob
    ) {
        boolean hasCombatRelation = targetingPlayer
                || mobRecentlyHurtByPlayer
                || playerRecentlyHurtMob
                || playerRecentlyHurtByMob;
        if (!hasCombatRelation) {
            return false;
        }

        if (targetingPlayer || playerRecentlyHurtByMob) {
            return true;
        }

        return hostileByClass || aggressive || canAttackPlayer;
    }

    private static boolean isIgnoredPassenger(Mob mob) {
        if (!mob.isPassenger()) {
            return false;
        }
        Entity vehicle = mob.getVehicle();
        return vehicle instanceof Boat || vehicle instanceof Minecart;
    }

    /**
     * プレイヤーが村にいる可能性をヒューリスティックに判定します。
     * 周囲に鐘があるか、一定数以上の村人がいるかをチェックします。
     * @param player 判定対象のプレイヤー
     * @param level プレイヤーがいるレベル
     * @return プレイヤーが村にいる可能性が高いかどうか
     */
    public static boolean isInVillageHeuristic(LocalPlayer player, Level level) {
        if (player == null || level == null) {
            return false; // プレイヤーやレベルが無効なら村ではない
        }

        BlockPos playerPos = player.blockPosition();

        // 一定範囲内のブロックをチェックし、鐘（BELLS）があるか探す
        boolean bellFound = BlockPos.betweenClosedStream(
                playerPos.offset(-(int) VILLAGE_CHECK_RADIUS, -VILLAGE_CHECK_HEIGHT, -(int) VILLAGE_CHECK_RADIUS),
                playerPos.offset((int) VILLAGE_CHECK_RADIUS, VILLAGE_CHECK_HEIGHT, (int) VILLAGE_CHECK_RADIUS)
        ).anyMatch(pos -> level.getBlockState(pos.immutable()).is(Blocks.BELL));

        if (bellFound) {
            return true; // 鐘があれば村の一部と見なす
        }

        // 一定範囲内に、設定されたしきい値以上の村人がいるかチェック
        List<Villager> nearbyVillagers = level.getEntitiesOfClass(
                Villager.class,
                player.getBoundingBox().inflate(VILLAGE_CHECK_RADIUS),
                LivingEntity::isAlive
        );

        return nearbyVillagers.size() >= VILLAGER_THRESHOLD;
    }
}
