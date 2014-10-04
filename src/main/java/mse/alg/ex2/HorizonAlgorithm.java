package mse.alg.ex2;

import java.util.List;

/**
 * Created by IsNull on 04.10.14.
 */
public class HorizonAlgorithm {

    /**
     * Compute the horizon from the x sorted chains.
     *
     * @param chains
     * @return
     */
    public Horizon computeHorizon(List<MonotoneChain> chains){
        return computeHorizon(chains, 0, chains.size()-1);
    }

    /**
     * Recursive horizon computation which shares the chains list instance.
     * @param chains
     * @param start Start pointer for the current chain list
     * @param end End pointer for the current chain list
     * @return
     */
    private Horizon computeHorizon(List<MonotoneChain> chains, int start, int end){
        if(start == end){
            // Simple case - Conquer it!
            return new Horizon(chains.get(start));
        }else{
            // Too complex for us - Further divide!
            Horizon part1 = computeHorizon(chains, start, end / 2);
            Horizon part2 = computeHorizon(chains, end / 2 + 1, end);

            return part1.merge(part2);
        }
    }


}
