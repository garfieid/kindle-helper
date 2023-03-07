package cn.zpj;

/**
 * @author zhangpengji
 */
public class App {

    public static void main(String[] args) throws Exception {
        String fileName = null;
        if (null != args && args.length > 0) {
            fileName = args[0];
        }
        StringToHtml stringToHtml = new StringToHtml();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stringToHtml.end();
            System.out.println("exit!");
        }));
        System.out.println("Please press Enter to start");
        System.in.read();
        System.out.println("start...");
        stringToHtml.start(fileName);
    }
}
