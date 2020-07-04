public class Lesson10_2 {
    /**
     * 动态规划求最小钱币数
     * @param c 用一维数组记录每一步的总金额 * @param value 用一维数组记录三种面额的纸币
     * @return
     */
    public static int getMinMoney(int[] c, int[] value) {
        int[] t = new int[3];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < value.length; j++) {
                if (i - value[j] >= 0) {
                    t[j] = c[i - value[j]] + 1;
                }
            }
            int min = Math.min(t[0], t[1]);
            min = Math.min(min, t[2]);
            c[i] = min;
        }
        return c[c.length - 1];
    }
    public static void main(String[] args) {
        int[] c = new int[100];
        int[] value = new int[] { 2, 3, 7 };
        System.out.println(getMinMoney(c, value)+1);
    }
}