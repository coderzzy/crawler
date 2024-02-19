package utils;

import java.util.Random;

public class CrawlUtils {

    /**
     * 生成随机用户名，数字和字母组成
     *
     * @param length
     * @return
     */
    public static String getStringRandom(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                // 只有小写字母
                val.append((char) (random.nextInt(26) + 97));
            } else {
                val.append(random.nextInt(10));
            }
        }
        return val.toString();
    }

    /**
     * 随机休眠
     *
     * @param min
     * @param max
     */
    public static void randomSleep(int min, int max) throws InterruptedException {
        Random random = new Random();
        Thread.sleep(min + random.nextInt(max - min));
    }
}
