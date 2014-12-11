public class HelloTest {
    /**
     * This main method shows a use of our CustomClassLoader for
     * loading some class and running it. All the objects referenced
     * from the IntegerPrinter class will be loaded with
     * our CustomClassLoader.
     */
    public static void main(String[] args) throws Exception {
        CustomClassLoader loader = new CustomClassLoader(
            StaticAccessorTest.class.getClassLoader());
        Class<?> clazz =
            loader.loadClass("Hello");
        Object instance = clazz.newInstance();
        clazz.getMethod("runMe").invoke(instance);
    }
}