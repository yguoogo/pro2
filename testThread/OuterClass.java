package testThread;

/**
 * Created by yuguanxu on 4/12/17.
 */
public class OuterClass {
    private String name ;
    private int age;

    /**省略getter和setter方法**/

    public String getName() {
        return name;
    }
    public int getAge(){
        return age;
    }

    public class InnerClass{
        public InnerClass(){
            name = "chenssy";
            age = 23;
        }

        public void display(){
            System.out.println("name：" + getName() +"   ;age：" + getAge());
        }
    }

    public static void main(String[] args) {
        OuterClass outerClass = new OuterClass();
        OuterClass.InnerClass innerClass = outerClass.new InnerClass();
        innerClass.display();
    }
}
