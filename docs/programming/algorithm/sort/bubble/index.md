# 冒泡排序（Bubble Sort）

## 1.流程

1. 从头开始比较每一对相邻元素，如果第 1 个比第 2 个大，就交换它们；
   
   执行完一轮后，最末尾那个元素就是最大的元素

2. 忽略第 ① 步中找到的最大元素，重复执行步骤 ①，直到全部元素有序

<video src="./imgs/1.mp4" controls></video>

## 2.实现

1. 以**整数**为例，先实现找出数组中最大的元素移动到最右边

    ```java
    public static void findAndMoveMax(int[] arr) {
        // 直接从第2个元素开始比较
        for (int begin = 1; begin < arr.length; begin++) {
            // 左边比右边大，则交换
            if (arr[begin - 1] > arr[begin]) {
                int tmp = arr[begin];
                arr[begin] = arr[begin - 1];
                arr[begin - 1] = tmp;
            }
        }
    }
    ```

2. 完整实现
   
   `end`: 每轮比较应该把最大的元素放到哪个索引处，另外`end>0`，而不是`end>=0`，是因为第 1 个元素无需再比较了。
   
   `begin`: 从第 1 个元素开始，找到最大的元素并放到`end`位置

    ```java
    import sort.Sort;
    import utils.Integers;

    /**
    * 冒泡排序
    * 基本实现
    *
    * @param <E> 元素类型，要求实现 Comparable 接口
    * @author yolk
    * @since 2024/6/20 10:36
    */
    public class BubbleSort1<E extends Comparable<E>> extends Sort<E> {

        /**
        * 1.以整数为例，先实现找出数组中最大的元素移动到最右边
        */
        public static void findAndMoveMax(int[] arr) {
            // 直接从第2个元素开始比较
            for (int begin = 1; begin < arr.length; begin++) {
                // 左边比右边大，则交换
                if (arr[begin - 1] > arr[begin]) {
                    int tmp = arr[begin];
                    arr[begin] = arr[begin - 1];
                    arr[begin - 1] = tmp;
                }
            }
        }

        /**
        * 2.完整实现
        * end: 每轮比较应该把最大的元素放到哪个索引处
        * 另外 end>0，而不是 end>=0，是因为第1个元素无需再比较了
        * <p>
        * begin: 从第 1 个元素到 end
        */
        @Override
        protected void sort() {
            for (int end = array.length - 1; end > 0; end--) {
                // 直接从第2个元素开始比较
                for (int begin = 1; begin <= end; begin++) {
                    // 左边比右边大，则交换
                    if (compare(begin - 1, begin) > 0) {
                        swap(begin - 1, begin);
                    }
                }
            }
        }

        public static void main(String[] args) {
            // int[] arr = {5, 3, 8, 6, 2};
            // findAndMoveMax(arr);
            // System.out.println(Arrays.toString(arr));

            Integer[] arr = Integers.random(10, 1, 100);
            Integers.println(arr);
            Sort<Integer> sort = new BubbleSort1<>();
            sort.sort(arr);

            Integers.println(arr);
        }

    }
    ```

    > 继承的`Sort`父类来自[封装公共父类](/programming/algorithm/sort/#_3-封装公共父类)
    > 
    > `Integers`类来自[工具类](/programming/algorithm/#_5-1-integers-java)

## 3.优化

### 3.1.优化 ①：如果数组已经是有序的，可以提前终止排序

> 如何判断是否有序? 
> 
> `某一趟比较中如果没有发生交换，说明数组已经有序`

```java
import sort.Sort;
import utils.Integers;

/**
 * 冒泡排序
 * 优化①: 如果数组已经有序，则提前终止
 *
 * @param <E> 元素类型，要求实现 Comparable 接口
 * @author yolk
 * @since 2024/6/20 10:36
 */
public class BubbleSort2<E extends Comparable<E>> extends Sort<E> {

    @Override
    protected void sort() {
        for (int end = array.length - 1; end > 0; end--) {
            // 标记本轮比较是否发生交换
            boolean isExchange = false;
            for (int begin = 1; begin <= end; begin++) {
                // 左边比右边大，则交换
                if (compare(begin - 1, begin) > 0) {
                    // 发生交换
                    isExchange = true;

                    swap(begin - 1, begin);
                }
            }
            if (!isExchange) {
                // 没有发生交换，说明已经是有序的，提前退出
                break;
            }
        }
    }

    public static void main(String[] args) {
        // Integer[] arr = Integers.random(10, 1, 100);
        // Integers.println(arr);
        //
        // Sort<Integer> sort = new BubbleSort2<>();
        // sort.sort(arr);
        // Integers.println(arr);

        // 测试排序效率
        Integer[] array1 = Integers.ascOrder(1, 100_000);
        Sort<Integer> sort1 = new BubbleSort1<>();
        sort1.sort(array1);
        System.out.println(sort1);

        Integer[] array2 = Integers.copy(array1);
        Sort<Integer> sort2 = new BubbleSort2<>();
        sort2.sort(array2);
        System.out.println(sort2);
    }

}
```

### 3.2.优化 ②：利用已经排好序的部分，缩小比较范围

在**优化 ①**中，只有全部排好序时才能提前退出，这种概率是非常低的。

如数组：`[55, 64, 64, 25, 41, 2, 3, 70, 84, 98]`，经过一轮比较后，`70, 84, 98`已经是有序的了，
下一轮比较时是只需要再比较到元素`3`即可，如果是原先的实现方式，则依次比较到`70、84、98`，这部分比较是没有意义的

> 如何判断尾部是否有序? 
>
> `某一趟比较中最后一次发生交换的位置，其后面的元素都是有序的`

```java
import sort.Sort;
import utils.Integers;

/**
 * 冒泡排序
 * 在优化①中，只有全部排好序时才能提前退出，这种概率是非常低的
 * 优化②：如果数组尾部已经有序时（尾部已经是最大值），可以缩小 end 最大边界值
 * 比如，现有数组：[55, 64, 64, 25, 41, 2, 3, 70, 84, 98]
 * 经过一轮比较后发现 `70, 84, 98` 已经是有序的了，下一轮比较时是只需要比较到元素`3`即可。
 * 如果是原本的算法，则依次比较到 `98、84、70`，这部分比较是没有意义的
 * 
 * @param <E> 元素类型，要求实现 Comparable 接口
 * @author yolk
 * @since 2024/6/20 10:36
 */
public class BubbleSort3<E extends Comparable<E>> extends Sort<E> {

    @Override
    protected void sort() {
        for (int end = array.length - 1; end > 0; end--) {
            /*
            lastExchangeIndex 用于记录最后一次交换的位置，那么其与之后的元素都是有序的，
            所以下一轮比较时，begin 最大值则为 lastExchangeIndex - 1（即 end 值）

            lastExchange 初始值设置为 1，即默认最后一次交换是索引为 1 的元素，
            如果数组已经是有序的，则可以提前退出，其实 lastExchange 初始值 <= 1 均可
             */
            int lastExchangeIndex = 1;
            for (int begin = 1; begin <= end; begin++) {
                if (compare(begin - 1, begin) > 0) {
                    // 记录最后一次交换的位置
                    lastExchangeIndex = begin;
                    swap(begin - 1, begin);
                }
            }
            end = lastExchangeIndex;
        }
    }

    public static void main(String[] args) {
        Integer[] arr = Integers.random(10, 1, 100);
        Integers.println(arr);

        Sort<Integer> sort = new BubbleSort3<>();
        sort.sort(arr);

        Integers.println(arr);
    }

}
```

## 4.分析

按照优化 ② 的版本，最坏、平均时间复杂度均为`O(n^2)`，最好时间复杂度为`O(n)`，空间复杂度为`O(1)`，是**稳定**、`In-place（原地）`的排序算法。



