
import java.util.Arrays;
public class Saving {

        public static void main(String[] args) {
            int[][] distances = {{0, 12, 16, 4}, {20, 0, 4, 8}, {6, 42, 0, 10}, {4, 8, 10, 0}};
            int[] capacity = {3, 3, 3, 3};
            int[] demand = {2, 2, 2, 2};

            int[] result = savingAlgorithm(distances, capacity, demand);

            System.out.println(Arrays.toString(result));
        }

        public static int[] savingAlgorithm(int[][] distances, int[] capacity, int[] demand) {
            int n = distances.length;
            int[] saving = new int[n];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        saving[i] += Math.max(0, demand[j] - distances[i][j]);
                    }
                }
            }

            int[] order = new int[n];
            Arrays.fill(order, -1);

            int index = 0;
            while (index < n) {
                int maxSavingIndex = -1;
                int maxSaving = Integer.MIN_VALUE;

                for (int i = 0; i < n; i++) {
                    if (order[i] == -1 && saving[i] > maxSaving) {
                        maxSaving = saving[i];
                        maxSavingIndex = i;
                    }
                }

                order[maxSavingIndex] = index++;
                saving[maxSavingIndex] = -1;

                for (int i = 0; i < n; i++) {
                    if (i != maxSavingIndex) {
                        saving[i] += Math.max(0, demand[maxSavingIndex] - distances[i][maxSavingIndex]);
                    }
                }
            }

            return order;
        }
}
