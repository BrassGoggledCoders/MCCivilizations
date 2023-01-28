package xyz.brassgoggledcoders.mccivilizations.util;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DamerauLevenshtein {
    private static final Logger LOGGER = LogManager.getLogger(DamerauLevenshtein.class);

    /**
     * Calculates the string distance between source and target strings using
     * the Damerau-Levenshtein algorithm. The distance is case-sensitive.
     *
     * @param source The source String.
     * @param target The target String.
     * @return The distance between source and target strings.
     * @throws IllegalArgumentException If either source or target is null.
     */
    public static int calculateDistance(CharSequence source, CharSequence target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
        int sourceLength = source.length();
        int targetLength = target.length();
        if (sourceLength == 0) return targetLength;
        if (targetLength == 0) return sourceLength;
        int[][] dist = new int[sourceLength + 1][targetLength + 1];
        for (int i = 0; i < sourceLength + 1; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j < targetLength + 1; j++) {
            dist[0][j] = j;
        }
        for (int i = 1; i < sourceLength + 1; i++) {
            for (int j = 1; j < targetLength + 1; j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dist[i][j] = Math.min(Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);
                if (i > 1 &&
                        j > 1 &&
                        source.charAt(i - 1) == target.charAt(j - 2) &&
                        source.charAt(i - 2) == target.charAt(j - 1)) {
                    dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + cost);
                }
            }
        }
        return dist[sourceLength][targetLength];
    }

    public static List<String> getClosest(String name, Collection<String> values, int maxDistance) {
        List<String> potentialPoolNames = Lists.newArrayList();
        int currentDistance = Integer.MAX_VALUE;
        for (String existingPool : values) {
            int distance = DamerauLevenshtein.calculateDistance(name, existingPool);
            if (distance < currentDistance) {
                potentialPoolNames.clear();
                potentialPoolNames.add(existingPool);
                currentDistance = distance;
            } else if (distance == currentDistance) {
                potentialPoolNames.add(existingPool);
            }
        }

        if (!potentialPoolNames.isEmpty() && currentDistance <= maxDistance) {
            return potentialPoolNames;
        } else {
            return Collections.emptyList();
        }
    }
}
