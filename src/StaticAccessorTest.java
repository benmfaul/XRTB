public class StaticAccessorTest {
    /**
     * This main method shows what happens when we load two
     * classes with two different ClassLoader's and access
     * some other class' static field from them.
     */
    public static void main(String... strings) throws Exception {
        // Using the first ClassLoader
        CustomClassLoader loader1 =
            new CustomClassLoader(StaticAccessorTest.class.getClassLoader());
        Class<?> clazz1 = loader1.loadClass("javablogging.StaticAccessor");
        Object instance1 = clazz1.newInstance();
        clazz1.getMethod("runMe").invoke(instance1);

        // Using the second ClassLoader
        CustomClassLoader loader2 =
            new CustomClassLoader(StaticAccessorTest.class.getClassLoader());
        Class<?> clazz2 = loader2.loadClass("javablogging.StaticAccessor");
        Object instance2 = clazz2.newInstance();
        clazz2.getMethod("runMe").invoke(instance2);
    }
}