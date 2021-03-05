package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.ArenaApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

/**
 * Managed player. Instances of these are created when someone joins the server and are stored in ArenaApi.
 */
@RequiredArgsConstructor
public class ArenaPlayer {
    @Getter
    private final Player player;

    private final Map<String, Deque<ConditionStage>> conditionMap = new HashMap<>();

    /**
     * Applies a new condition to the internal player.
     * @param applyCondition The consumer which will apply the condition
     * @param removeCondition The consumer that will undo the condition
     */
    public void applyCondition(String context, Consumer<Player> applyCondition, Consumer<Player> removeCondition) {
        conditionMap.getOrDefault(context, new ArrayDeque<>()).push(new ConditionStage(applyCondition, removeCondition));
        applyCondition.accept(player);
    }

    /**
     * Removes one condition from the player. If there are no active conditions, this will perform no operation.
     */
    public void retractConditionFor(String context) {
        Deque<ConditionStage> conditions = conditionMap.get(context);
        if(conditions != null) {
            ConditionStage condition  = conditions.poll();
            if(condition != null) {
                condition.apply(player);
            }
        }
        else {
            ArenaApi.warning("Tried to fetch condition stack for unknown context " + context);
        }
    }

    public void removeAllConditions(String context) {
        Deque<ConditionStage> conditions = conditionMap.get(context);
        if(conditions != null) {
            while(conditions.size() > 0) {
                ConditionStage condition = conditions.poll();
                condition.remove(player);
            }

            conditionMap.remove(context);
        }
        else {
            ArenaApi.warning("Tried to fetch condition stack for unknown context " + context);
        }
    }

    public List<String> getConditionContexts() {
        return new ArrayList<>(conditionMap.keySet());
    }
}
