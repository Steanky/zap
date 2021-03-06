package io.github.zap.arenaapi.game.arena;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Instances of these are created when someone joins the server and are stored in ArenaApi.
 */
@RequiredArgsConstructor
public class ArenaPlayer {
    @Getter
    private final Player player;

    private final Map<String, Map<String, ConditionStage>> conditionMap = new HashMap<>();

    /**
     * Applies a new condition to the internal player.
     * @param stage The stage we will apply under the given context
     */
    public void registerCondition(String context, String effectName, ConditionStage stage) {
        conditionMap.computeIfAbsent(context, key -> new HashMap<>()).put(effectName, stage);
    }

    /**
     * Removes one condition from the player. If there are no active conditions, this will perform no operation. Does
     * not remove anything from the internal map.
     */
    public void removeConditionFor(String context, String effectName) {
        Map<String, ConditionStage> conditions = conditionMap.get(context);
        if(conditions != null) {
            ConditionStage condition  = conditions.get(effectName);
            if(condition != null) {
                condition.remove(player);
            }
        }
    }

    /**
     * Removes all conditions for a given context.
     * @param context The condition context
     */
    public void removeAllConditionsFor(String context) {
        Map<String, ConditionStage> conditions = conditionMap.get(context);
        if(conditions != null) {
            for(ConditionStage condition : conditions.values()) {
                condition.remove(player);
            }
        }
    }

    public void removeConditionContext(String context) {
        removeAllConditionsFor(context);
        conditionMap.remove(context);
    }

    public void applyConditionFor(String context, String conditionName) {
        Map<String, ConditionStage> conditions = conditionMap.get(context);
        if(conditions != null && conditions.containsKey(conditionName)) {
            for(ConditionStage conditionStage : conditions.values()) {
                if(!conditionStage.isAdditive()) {
                    conditionStage.remove(player);
                }
            }

            conditions.get(conditionName).apply(player);
        }
    }

    public List<String> getConditionContexts() {
        return new ArrayList<>(conditionMap.keySet());
    }
}
